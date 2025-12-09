# Install Filament ChatGPT Bot Package with Old Chatbot Style

## Step 1: Install the Package

The package is already in your `composer.json`. Run:

```bash
composer update
```

## Step 2: Publish Configuration

```bash
php artisan vendor:publish --tag="filament-chatgpt-bot-config"
```

This creates `config/filament-chatgpt-bot.php`.

## Step 3: Configure the Package

Edit `config/filament-chatgpt-bot.php`:

```php
<?php

return [
    'enable' => true, // Enable the chatbot
    
    'botname' => env('ICETALKER_BOTNAME', 'Gemini AI Assistant'),
    
    'openai' => [
        'api_key' => env('OPENAI_API_KEY', env('GEMINI_API_KEY')),
        'organization' => env('OPENAI_ORGANIZATION'),
    ],
    
    'proxy' => env('OPENAI_PROXY'),
];
```

## Step 4: Update .env

Make sure your `.env` has:

```env
OPENAI_API_KEY=AIzaSyC8MF40ez6A-_nHXF1Vd6aZ9tw9okjvrUg
```

## Step 5: Publish Views

```bash
php artisan vendor:publish --tag="filament-chatgpt-bot-views"
```

This publishes views to: `resources/views/vendor/filament-chatgpt-bot/`

## Step 6: Replace with Custom View (Old Style)

After publishing, replace the file:
`resources/views/vendor/filament-chatgpt-bot/livewire/chat-bot.blade.php`

With the custom version I've created that matches your old chatbot style.

## Step 7: Clear Cache

```bash
php artisan config:clear
php artisan view:clear
php artisan cache:clear
```

## Step 8: Test

1. Go to `http://127.0.0.1:8000/admin`
2. Look for the blue chat button in the bottom-right corner
3. Click it to open the chat panel

## Note About Google Gemini

The package uses `orhanerday/open-ai` library which is designed for OpenAI. To use Google Gemini, you may need to:

1. Check if the library supports custom base URLs
2. Or modify the package's OpenAI.php class to use Gemini's endpoint
3. Or use a proxy/wrapper

The custom view I created matches your old chatbot's style (blue colors, same layout).

