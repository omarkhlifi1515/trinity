<?php

return [

    /*
    |--------------------------------------------------------------------------
    | Google Gemini API Key
    |--------------------------------------------------------------------------
    |
    | Here you may specify your Google Gemini API Key. This will be
    | used to authenticate with the Google Gemini API - you can find your API key
    | on your Google AI Studio dashboard, at https://aistudio.google.com.
    |
    | Note: We're using Google's OpenAI-compatible endpoint for Gemini.
    | You can use GEMINI_API_KEY or OPENAI_API_KEY (for backward compatibility).
    */

    'api_key' => env('GEMINI_API_KEY', env('OPENAI_API_KEY', 'AIzaSyC8MF40ez6A-_nHXF1Vd6aZ9tw9okjvrUg')),
    'organization' => env('GEMINI_ORGANIZATION', env('OPENAI_ORGANIZATION')),

    /*
    |--------------------------------------------------------------------------
    | Google Gemini API Project
    |--------------------------------------------------------------------------
    |
    | Here you may specify your Google Gemini API project. This is used optionally
    | for project-based access control.
    */

    'project' => env('GEMINI_PROJECT', env('OPENAI_PROJECT')),

    /*
    |--------------------------------------------------------------------------
    | Google Gemini Base URL
    |--------------------------------------------------------------------------
    |
    | Here you may specify your Google Gemini API base URL used to make requests.
    | This uses Google's OpenAI-compatible endpoint for Gemini.
    | Defaults to: https://generativelanguage.googleapis.com/v1beta/openai
    */

    'base_uri' => env('GEMINI_BASE_URL', 'https://generativelanguage.googleapis.com/v1beta/openai'),

    /*
    |--------------------------------------------------------------------------
    | Request Timeout
    |--------------------------------------------------------------------------
    |
    | The timeout may be used to specify the maximum number of seconds to wait
    | for a response. By default, the client will time out after 30 seconds.
    */

    'request_timeout' => env('OPENAI_REQUEST_TIMEOUT', 30),
];
