<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\ChatController;

// Root route - redirect to admin panel
Route::get('/', function () {
    return redirect('/admin');
});

// Test route to verify routing is working (no auth required)
Route::get('/test-routes', function () {
    return response()->json([
        'message' => 'Routes are loading correctly',
        'timestamp' => now()->toDateTimeString(),
        'routes_loaded' => true
    ]);
});

// Chatbot route for Filament dashboard - must be outside Filament panel routes
// Rate limit: 20 requests per minute per user to prevent hitting Google Gemini rate limits
// Note: If you get rate limit errors, it's likely from Google Gemini API limits, not our server

// POST route for sending chat messages
Route::post('/dashboard/chat', [ChatController::class, 'send'])
    ->middleware(['web', 'auth', 'throttle:20,1'])
    ->name('dashboard.chat.send');

// GET route for testing (returns info message)
Route::get('/dashboard/chat', function () {
    if (!auth()->check()) {
        return response()->json(['error' => 'Unauthorized'], 401);
    }
    return response()->json([
        'message' => 'Chat API endpoint is available. Use POST method to send messages.',
        'endpoint' => '/dashboard/chat',
        'method' => 'POST',
        'authenticated' => true,
        'user' => auth()->user()->name ?? 'Unknown'
    ]);
})->middleware(['web', 'auth'])->name('dashboard.chat.info');
