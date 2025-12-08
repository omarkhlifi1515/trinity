<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Message;
use App\Models\Topic;
use OpenAI;

class ChatController extends Controller
{
    public function send(Request $request)
    {
        // Works with both web (session) and API (sanctum) authentication
        $user = auth()->user() ?? $request->user();
        
        if (!$user) {
            return response()->json(['error' => 'Unauthorized'], 401);
        }


        $messageText = $request->input('message');

        if (empty($messageText)) {
            return response()->json(['error' => 'Message is required'], 400);
        }

        // Get or create a default dashboard chat topic for this user (without eager loading relationships)
        $topic = Topic::without(['creator', 'receiver'])
            ->where('subject', 'Dashboard Chat')
            ->where('creator_id', $user->id)
            ->where('creator_type', get_class($user))
            ->first();
            
        if (!$topic) {
            $topic = Topic::create([
                'subject' => 'Dashboard Chat',
                'creator_id' => $user->id,
                'creator_type' => get_class($user),
                'receiver_id' => 0,
                'receiver_type' => 'App\\Bots\\AI',
            ]);
        }
        
        // Prevent relationship loading
        $topic->setRelation('receiver', null);
        $topic->setRelation('creator', null);

        // 1️⃣ Store user message (without eager loading sender to avoid issues)
        $userMessage = Message::without('sender')->create([
            'topic_id' => $topic->id,
            'sender_id' => $user->id,
            'sender_type' => get_class($user),
            'content' => $messageText,
        ]);

        try {
            // 2️⃣ Send message to Google Gemini using native API
            // Check if API key is set
            $apiKey = config('openai.api_key');
            if (empty($apiKey)) {
                throw new \Exception('Google Gemini API key is not configured. Please set GEMINI_API_KEY in your .env file.');
            }
            
            // Use Google Gemini native API instead of OpenAI-compatible endpoint
            // Try different models in order of preference
            // Common available models: gemini-pro, gemini-1.5-pro, gemini-1.5-flash-latest
            $models = ['gemini-pro', 'gemini-1.5-pro-latest', 'gemini-1.5-flash-latest', 'gemini-1.5-pro', 'gemini-1.5-flash'];
            
            // Prepare messages for Gemini format
            // Gemini uses a different format - convert from OpenAI format
            $geminiContents = [];
            
            // Add system instruction if present
            $systemInstruction = 'You are a helpful assistant. Answer concisely.';
            
            // Add user message
            $geminiContents[] = [
                'parts' => [
                    ['text' => $messageText]
                ]
            ];
            
            $payload = [
                'contents' => $geminiContents,
            ];
            
            // Add system instruction if supported
            if (!empty($systemInstruction)) {
                $payload['systemInstruction'] = [
                    'parts' => [
                        ['text' => $systemInstruction]
                    ]
                ];
            }
            
            // Make HTTP request to Gemini API
            $client = new \GuzzleHttp\Client([
                'timeout' => config('openai.request_timeout', 30),
                'verify' => false, // Disable SSL verification for development
            ]);
            
            $lastError = null;
            $responseData = null;
            
            // Try each model until one works
            foreach ($models as $model) {
                try {
                    $url = "https://generativelanguage.googleapis.com/v1beta/models/{$model}:generateContent?key={$apiKey}";
                    
                    $httpResponse = $client->post($url, [
                        'json' => $payload,
                        'headers' => [
                            'Content-Type' => 'application/json',
                        ],
                    ]);
                    
                    $responseBody = $httpResponse->getBody()->getContents();
                    $responseData = json_decode($responseBody, true);
                    
                    \Log::debug('Gemini API Response', ['model' => $model, 'response' => $responseData]);
                    
                    // Check for errors
                    if (isset($responseData['error'])) {
                        $errorMsg = $responseData['error']['message'] ?? json_encode($responseData['error']);
                        $errorCode = $responseData['error']['code'] ?? null;
                        \Log::warning('Gemini API Error with model ' . $model, [
                            'error' => $responseData['error'],
                            'code' => $errorCode,
                            'full_response' => $responseData
                        ]);
                        
                        // If model not found (404) or not supported, try next model
                        if ($errorCode == 404 || str_contains(strtolower($errorMsg), 'not found') || 
                            str_contains(strtolower($errorMsg), 'not supported') ||
                            str_contains(strtolower($errorMsg), '404')) {
                            $lastError = "Model {$model}: " . $errorMsg;
                            continue; // Try next model
                        }
                        
                        // Other errors, throw immediately
                        throw new \Exception('Google Gemini API error: ' . $errorMsg);
                    }
                    
                    // Success! Break out of loop
                    break;
                    
                } catch (\GuzzleHttp\Exception\RequestException $e) {
                    $response = $e->getResponse();
                    if ($response) {
                        $statusCode = $response->getStatusCode();
                        $errorBody = $response->getBody()->getContents();
                        $errorData = json_decode($errorBody, true);
                        
                        \Log::warning('Gemini API HTTP Exception with model ' . $model, [
                            'status' => $statusCode,
                            'error' => $errorData,
                            'message' => $e->getMessage()
                        ]);
                        
                        if (isset($errorData['error']['message'])) {
                            $errorMsg = $errorData['error']['message'];
                            
                            // If model not found (404) or not supported, try next model
                            if ($statusCode == 404 || str_contains(strtolower($errorMsg), 'not found') || 
                                str_contains(strtolower($errorMsg), 'not supported')) {
                                $lastError = "Model {$model} (HTTP {$statusCode}): " . $errorMsg;
                                continue; // Try next model
                            }
                        } else if ($statusCode == 404) {
                            // 404 without error message - likely model not found
                            $lastError = "Model {$model} not found (HTTP 404)";
                            continue; // Try next model
                        }
                    }
                    
                    // If it's the last model or not a "model not found" error, throw
                    if ($model === end($models)) {
                        throw $e;
                    }
                    $lastError = $e->getMessage();
                }
            }
            
            // If we tried all models and none worked
            if ($responseData === null || isset($responseData['error'])) {
                // Try to get list of available models for better error message
                try {
                    $listUrl = "https://generativelanguage.googleapis.com/v1beta/models?key={$apiKey}";
                    $listResponse = $client->get($listUrl);
                    $listData = json_decode($listResponse->getBody()->getContents(), true);
                    $availableModels = isset($listData['models']) ? array_column($listData['models'], 'name') : [];
                    $availableModels = array_map(function($name) {
                        return str_replace('models/', '', $name);
                    }, $availableModels);
                    
                    \Log::info('Available Gemini Models', ['models' => $availableModels]);
                    
                    throw new \Exception('Google Gemini API error: All models failed. Last error: ' . ($lastError ?? 'Unknown error') . '. Available models: ' . implode(', ', array_slice($availableModels, 0, 5)));
                } catch (\Exception $listError) {
                    throw new \Exception('Google Gemini API error: All models failed. Last error: ' . ($lastError ?? 'Unknown error') . '. Tried models: ' . implode(', ', $models));
                }
            }
            
            // Extract content from Gemini response format
            if (!isset($responseData['candidates']) || empty($responseData['candidates'])) {
                \Log::error('Gemini API No Candidates', ['response' => $responseData]);
                throw new \Exception('Invalid response from Google Gemini API: no candidates found.');
            }
            
            $candidate = $responseData['candidates'][0];
            if (!isset($candidate['content']['parts']) || empty($candidate['content']['parts'])) {
                \Log::error('Gemini API No Content Parts', ['candidate' => $candidate]);
                throw new \Exception('Invalid response from Google Gemini API: no content parts found.');
            }
            
            $replyText = $candidate['content']['parts'][0]['text'] ?? '';
            
            if (empty($replyText)) {
                \Log::error('Gemini API Empty Text', ['candidate' => $candidate]);
                throw new \Exception('Invalid response from Google Gemini API: empty text content.');
            }

            // 3️⃣ Store bot message (without eager loading sender)
            $botMessage = Message::without('sender')->create([
                'topic_id' => $topic->id,
                'sender_id' => 0,
                'sender_type' => 'App\\Bots\\AI',
                'content' => $replyText,
            ]);

            // Return response without relationships to avoid errors
            return response()->json([
                'bot_message' => [
                    'id' => $botMessage->id,
                    'content' => $botMessage->content,
                    'created_at' => $botMessage->created_at?->toDateTimeString(),
                ],
            ]);
        } catch (\Exception $e) {
            // Handle all exceptions and provide user-friendly error messages
            $errorMessage = $e->getMessage();
            $errorClass = get_class($e);
            
            \Log::error('Chat Error [' . $errorClass . ']: ' . $errorMessage . ' | Stack: ' . $e->getTraceAsString());
            
            // Check for SSL/certificate errors
            if (str_contains($errorMessage, 'SSL certificate') || 
                str_contains($errorMessage, 'cURL error 60') || 
                str_contains($errorMessage, 'unable to get local issuer certificate')) {
                return response()->json([
                    'error' => 'SSL certificate error. Please ensure your system has the latest CA certificates installed.'
                ], 500);
            }
            
            // Check for rate limit errors from Google Gemini
            if (str_contains($errorMessage, 'rate limit') || 
                str_contains($errorMessage, 'Rate limit') ||
                str_contains($errorMessage, 'Request rate limit') ||
                str_contains($errorMessage, '429')) {
                // This is from Google Gemini API, not our server
                \Log::warning('Google Gemini Rate Limit Hit', [
                    'user_id' => $user->id,
                    'error' => $errorMessage
                ]);
                
                return response()->json([
                    'error' => 'Google Gemini API rate limit exceeded. Please wait 1-2 minutes and try again, or check your API quota.',
                    'source' => 'gemini',
                    'retry_after' => 120 // Suggest waiting 2 minutes for Gemini limits
                ], 429)->withHeaders([
                    'Retry-After' => '120', // Retry after 120 seconds for Gemini
                    'X-RateLimit-Source' => 'gemini'
                ]);
            }
            
            // Check for authentication errors
            if (str_contains($errorMessage, 'unauthorized') || 
                str_contains($errorMessage, 'Invalid API key') ||
                str_contains($errorMessage, '401')) {
                return response()->json([
                    'error' => 'Invalid API key. Please check your Google Gemini API configuration.'
                ], 401);
            }
            
            // Check for connection/timeout errors
            if (str_contains($errorMessage, 'Connection') || 
                str_contains($errorMessage, 'timeout') ||
                str_contains($errorMessage, 'Failed to connect')) {
                return response()->json([
                    'error' => 'Failed to connect to AI service. Please check your internet connection and try again.'
                ], 500);
            }
            
            // Generic error message
            return response()->json([
                'error' => 'Failed to get AI response: ' . $errorMessage
            ], 500);
        }
    }
}
