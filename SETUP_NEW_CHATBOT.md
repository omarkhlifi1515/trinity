# Setup New Filament ChatGPT Bot (with Old Format)

## Step 1: Install the Package

Run this command:

```bash
composer update
```

This will install the `icetalker/filament-chatgpt-bot` package that's already in your `composer.json`.

## Step 2: Publish Configuration

```bash
php artisan vendor:publish --tag="filament-chatgpt-bot-config"
```

## Step 3: Publish Views (to customize)

```bash
php artisan vendor:publish --tag="filament-chatgpt-bot-views"
```

## Step 4: Configure for Google Gemini

The package uses OpenAI PHP package which we've already configured for Gemini. Just make sure your `.env` has:

```env
OPENAI_API_KEY=AIzaSyC8MF40ez6A-_nHXF1Vd6aZ9tw9okjvrUg
```

## Step 5: Customize the View

After publishing views, the view file will be at:
`resources/views/vendor/filament-chatgpt-bot/components/chatgpt-bot.blade.php`

I've created a customized version that matches your old chatbot format. Replace the published view with the custom one I'll provide.

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

