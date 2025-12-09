<?php

use Illuminate\Support\Facades\Route;

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

// Chatbot routes removed - using icetalker/filament-chatgpt-bot package now
