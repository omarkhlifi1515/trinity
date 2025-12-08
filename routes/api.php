<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;
use App\Models\User;
use App\Models\Task;
use App\Models\Leave;
use App\Models\Attendance;
use OpenAI\Laravel\Facades\OpenAI;

/*
|--------------------------------------------------------------------------
| 1. AUTHENTICATION (Public)
|--------------------------------------------------------------------------
*/

// Matches Android: @POST("login")
Route::post('/login', function (Request $request) {
    $request->validate([
        'email' => 'required|email',
        'password' => 'required',
    ]);

    $user = User::where('email', $request->email)->first();

    if (! $user || ! Hash::check($request->password, $user->password)) {
        throw ValidationException::withMessages([
            'email' => ['Invalid credentials.'],
        ]);
    }

    // Return the specific JSON format the Android App expects
    return response()->json([
        'token' => $user->createToken('mobile-app')->plainTextToken,
        'user' => [
            'id' => $user->id,
            'name' => $user->name,
            'email' => $user->email,
            'role' => 'Employee', // Default role for mobile
            'created_at' => $user->created_at,
            'updated_at' => $user->updated_at,
        ]
    ]);
});

/*
|--------------------------------------------------------------------------
| 2. PROTECTED ROUTES (Requires Login)
|--------------------------------------------------------------------------
*/
Route::middleware('auth:sanctum')->group(function () {

    // --- USER PROFILE ---
    Route::get('/users', function (Request $request) {
        return $request->user();
    });

    // --- TASKS ---
    
    // Matches Android: @GET("tasks/userTasks")
    Route::get('/tasks/userTasks', function (Request $request) {
        // Fetch tasks assigned to the logged-in user
        // Note: Assuming 'assignee_id' exists based on migration 2025_10_17
        $tasks = Task::where('assignee_id', $request->user()->id)
                     ->orWhere('assignee_id', null) // Optional: Show unassigned tasks
                     ->orderBy('created_at', 'desc')
                     ->get();
                     
        return response()->json($tasks);
    });

    // Matches Android: @POST("tasks")
    Route::post('/tasks', function (Request $request) {
        $validated = $request->validate([
            'title' => 'required|string',
            'description' => 'nullable|string',
            'priority' => 'required|string',
        ]);

        $task = Task::create([
            'name' => $validated['title'], // Map 'title' to 'name'
            'description' => $validated['description'],
            'priority' => $validated['priority'],
            'status' => 'pending',
            'assignee_id' => $request->user()->id, // Self-assign for now
            'created_by' => $request->user()->id,
        ]);

        return response()->json($task);
    });

    // --- LEAVES ---

    // Matches Android: @GET("leaves")
    Route::get('/leaves', function (Request $request) {
        return Leave::where('employee_id', $request->user()->id)->get();
    });

    // Matches Android: @POST("leaves")
    Route::post('/leaves', function (Request $request) {
        $validated = $request->validate([
            'leave_type' => 'required|string',
            'start_date' => 'required|date',
            'end_date' => 'required|date',
            'reason' => 'required|string',
        ]);

        $leave = Leave::create([
            'employee_id' => $request->user()->id,
            'leave_type' => $validated['leave_type'],
            'start_date' => $validated['start_date'],
            'end_date' => $validated['end_date'],
            'reason' => $validated['reason'],
            'status' => 'pending',
        ]);

        return response()->json($leave);
    });

    // --- AI CHAT (From Previous Step) ---

    Route::post('/chat/start', function (Request $request) {
        $id = DB::table('ai_threads')->insertGetId([
            'user_id' => $request->user()->id,
            'subject' => $request->input('subject', 'New Conversation'),
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        return response()->json(['thread_id' => $id]);
    });

    Route::post('/chat/{threadId}/send', function (Request $request, $threadId) {
        $userMessage = $request->input('message');

        // 1. Save User Message
        DB::table('ai_messages')->insert([
            'thread_id' => $threadId, 'role' => 'user', 'content' => $userMessage,
            'created_at' => now(), 'updated_at' => now(),
        ]);

        // 2. Ask Google Gemini using native API
        $history = DB::table('ai_messages')->where('thread_id', $threadId)
            ->orderBy('created_at', 'asc')->select('role', 'content')->get()
            ->toArray();

        try {
            $apiKey = config('openai.api_key');
            if (empty($apiKey)) {
                return response()->json(['error' => 'Google Gemini API key is not configured.'], 500);
            }
            
            $model = 'gemini-pro';
            $url = "https://generativelanguage.googleapis.com/v1beta/models/{$model}:generateContent?key={$apiKey}";
            
            // Convert history to Gemini format
            $geminiContents = [];
            foreach ($history as $msg) {
                $role = $msg->role === 'assistant' ? 'model' : 'user';
                $geminiContents[] = [
                    'role' => $role,
                    'parts' => [
                        ['text' => $msg->content]
                    ]
                ];
            }
            
            $payload = ['contents' => $geminiContents];
            
            $client = new \GuzzleHttp\Client(['timeout' => 30, 'verify' => false]);
            $httpResponse = $client->post($url, [
                'json' => $payload,
                'headers' => ['Content-Type' => 'application/json'],
            ]);
            
            $responseBody = $httpResponse->getBody()->getContents();
            $responseData = json_decode($responseBody, true);
            
            if (isset($responseData['error'])) {
                \Log::error('Gemini API Error', ['error' => $responseData['error']]);
                return response()->json(['error' => 'Google Gemini API error: ' . ($responseData['error']['message'] ?? 'Unknown error')], 500);
            }
            
            if (!isset($responseData['candidates']) || empty($responseData['candidates'])) {
                \Log::error('Gemini API No Candidates', ['response' => $responseData]);
                return response()->json(['error' => 'Invalid response from Google Gemini API.'], 500);
            }
            
            $candidate = $responseData['candidates'][0];
            $botReply = $candidate['content']['parts'][0]['text'] ?? '';
            
            if (empty($botReply)) {
                \Log::error('Gemini API Empty Reply', ['candidate' => $candidate]);
                return response()->json(['error' => 'Empty response from Google Gemini API.'], 500);
            }
        } catch (\Exception $e) {
            \Log::error('Gemini API Error', ['error' => $e->getMessage()]);
            return response()->json(['error' => 'Failed to get AI response: ' . $e->getMessage()], 500);
        }

        // 3. Save Bot Reply
        DB::table('ai_messages')->insert([
            'thread_id' => $threadId, 'role' => 'assistant', 'content' => $botReply,
            'created_at' => now(), 'updated_at' => now(),
        ]);

        return response()->json(['reply' => $botReply]);
    });
Route::middleware('auth:sanctum')->post('/chat/send', [\App\Http\Controllers\ChatController::class, 'send']);

    Route::get('/chat/{threadId}', function ($threadId) {
        return DB::table('ai_messages')->where('thread_id', $threadId)->get();
    });
});