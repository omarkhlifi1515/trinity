<?php

// config for Icetalker/FilamentChatgptBot
return [
    'enable' => true,

    'botname' => env('ICETALKER_BOTNAME', 'Gemini AI Assistant'),

    'openai' => [
        'api_key' => env('OPENAI_API_KEY', env('GEMINI_API_KEY', 'AIzaSyC8MF40ez6A-_nHXF1Vd6aZ9tw9okjvrUg')),
        'organization' => env('OPENAI_ORGANIZATION'),
    ],
    
    'proxy' => env('OPENAI_PROXY'),

];