<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use OpenAI\Laravel\Facades\OpenAI;
use Illuminate\Support\Facades\DB;
use App\Models\User;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

// 1. LOGIN ROUTE (Mobile App calls this first)
Route::post('/login', function (Request $request) {
    $request->validate([
        'email' => 'required|email',
        'password' => 'required',
    ]);

    $user = User::where('email', $request->email)->first();

    if (! $user || ! Hash::check($request->password, $user->password)) {
        throw ValidationException::withMessages([
            'email' => ['The provided credentials are incorrect.'],
        ]);
    }

    // Create the "Access Key" for the mobile app
    $token = $user->createToken('mobile-app')->plainTextToken;

    return response()->json([
        'token' => $token,
        'user' => $user,
    ]);
});

// 2. PROTECTED ROUTES (Chat & User Info)
Route::middleware('auth:sanctum')->group(function () {

    // Get current user info
    Route::get('/user', function (Request $request) {
        return $request->user();
    });

    // Start a new AI Chat Thread
    Route::post('/chat/start', function (Request $request) {
        $id = DB::table('ai_threads')->insertGetId([
            'user_id' => $request->user()->id,
            'subject' => $request->input('subject', 'New Conversation'),
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        
        return response()->json(['thread_id' => $id]);
    });

    // Send Message & Get AI Reply
    Route::post('/chat/{threadId}/send', function (Request $request, $threadId) {
        $userMessage = $request->input('message');

        // A. Save User Message
        DB::table('ai_messages')->insert([
            'thread_id' => $threadId,
            'role' => 'user',
            'content' => $userMessage,
            'created_at' => now(),
            'updated_at' => now(),
        ]);

        // B. Get History for Context
        $history = DB::table('ai_messages')
            ->where('thread_id', $threadId)
            ->orderBy('created_at', 'asc')
            ->select('role', 'content')
            ->get()
            ->map(fn($msg) => ['role' => $msg->role, 'content' => $msg->content])
            ->toArray();

        // C. Ask OpenAI
        $result = OpenAI::chat()->create([
            'model' => 'gpt-4o-mini',
            'messages' => $history,
        ]);

        $botReply = $result->choices[0]->message->content;

        // D. Save Bot Reply
        DB::table('ai_messages')->insert([
            'thread_id' => $threadId,
            'role' => 'assistant',
            'content' => $botReply,
            'created_at' => now(),
            'updated_at' => now(),
        ]);

        return response()->json(['reply' => $botReply]);
    });
    
    // Get Chat History
    Route::get('/chat/{threadId}', function ($threadId) {
        return DB::table('ai_messages')->where('thread_id', $threadId)->get();
    });
});