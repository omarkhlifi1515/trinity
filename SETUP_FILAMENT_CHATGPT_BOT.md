# Setup Filament ChatGPT Bot Package with Custom Style

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

## Step 3: Configure for Google Gemini

Update `config/filament-chatgpt-bot.php`:

```php
<?php

return [
    'enable' => true, // Enable the chatbot
    
    // The package uses orhanerday/open-ai which should work with OpenAI-compatible endpoints
    // We'll configure it to use Gemini via the OpenAI config
];
```

Make sure your `.env` has:
```env
OPENAI_API_KEY=AIzaSyC8MF40ez6A-_nHXF1Vd6aZ9tw9okjvrUg
```

## Step 4: Publish Views (to customize)

```bash
php artisan vendor:publish --tag="filament-chatgpt-bot-views"
```

This will publish views to: `resources/views/vendor/filament-chatgpt-bot/`

## Step 5: Customize the View to Match Old Style

After publishing, I'll create a custom view that matches your old chatbot style. The view file will be at:
`resources/views/vendor/filament-chatgpt-bot/components/chatgpt-bot.blade.php`

## Step 6: Clear Cache

```bash
php artisan config:clear
php artisan view:clear
php artisan cache:clear
```

## Step 7: Test

1. Go to `http://127.0.0.1:8000/admin`
2. Look for the chat icon in the bottom-right corner
3. Click it to open the chat panel

## Note

The package uses `orhanerday/open-ai` library. To make it work with Google Gemini, we may need to configure the OpenAI PHP package (which we already have) to use Gemini's endpoint, and the package should pick it up.

