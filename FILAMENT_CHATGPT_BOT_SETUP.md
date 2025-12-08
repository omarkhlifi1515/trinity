# Filament ChatGPT Bot Setup Guide

## Step 1: Install the Package

Run the following command to install the package:

```bash
composer update
```

Or if you prefer to install just this package:

```bash
composer require icetalker/filament-chatgpt-bot
```

## Step 2: Publish Configuration Files

After installation, publish the config files:

```bash
php artisan vendor:publish --tag="filament-chatgpt-bot-config"
```

This will create `config/filament-chatgpt-bot.php` in your project.

## Step 3: Configure for Google Gemini

Since you're using Google Gemini instead of OpenAI, you'll need to configure it. The package uses the OpenAI PHP package, which we've already configured for Gemini.

### Update your `.env` file:

```env
# Google Gemini API Key (the package will use OPENAI_API_KEY)
OPENAI_API_KEY=AIzaSyC8MF40ez6A-_nHXF1Vd6aZ9tw9okjvrUg

# Optional: If the package supports base URL configuration
# You may need to check the package's config file for this option
```

### Update `config/filament-chatgpt-bot.php` (after publishing):

The package should use the OpenAI config we already have. If the package has its own base URL setting, you can configure it there to use Gemini's endpoint.

## Step 4: Optional - Publish Views (if you want to customize)

If you want to customize the chat bot UI:

```bash
php artisan vendor:publish --tag="filament-chatgpt-bot-views"
```

## Step 5: Clear Cache

After configuration:

```bash
php artisan config:clear
php artisan cache:clear
```

## Step 6: Test

1. Go to your admin panel: `http://127.0.0.1:8000/admin`
2. Look for a small gray icon in the bottom-right corner
3. Click it to open the chat panel
4. Try sending a message

## Configuration Options

In `config/filament-chatgpt-bot.php`, you can:

- **Enable/Disable**: Set `'enable' => true/false` to show/hide the bot
- **Customize appearance**: Modify the views if published
- **Configure API settings**: The package should use your existing OpenAI config

## Note About Gemini Compatibility

The package is designed for OpenAI/ChatGPT, but since we've configured the OpenAI PHP package to use Google's OpenAI-compatible endpoint, it should work with Gemini. However, you may need to:

1. Check if the package allows base URL configuration
2. Ensure the model name is compatible (the package might default to GPT models)
3. Test thoroughly to ensure all features work correctly

## Troubleshooting

If the bot doesn't appear:
- Make sure you're logged in
- Check that `'enable' => true` in the config
- Clear all caches
- Check browser console for errors

If API calls fail:
- Verify your API key is correct
- Check that the OpenAI config is using Gemini's endpoint
- Review Laravel logs for detailed error messages

