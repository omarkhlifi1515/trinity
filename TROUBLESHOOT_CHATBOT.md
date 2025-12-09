# Troubleshooting Filament ChatGPT Bot

## Issue: Chatbot doesn't open/not visible

### Step 1: Clear All Caches

Run these commands:

```bash
php artisan config:clear
php artisan view:clear
php artisan cache:clear
php artisan optimize:clear
```

### Step 2: Verify Config

Check `config/filament-chatgpt-bot.php`:

```php
'enable' => true,  // Must be true
```

### Step 3: Verify .env

Make sure your `.env` has:

```env
OPENAI_API_KEY=AIzaSyC8MF40ez6A-_nHXF1Vd6aZ9tw9okjvrUg
```

### Step 4: Check Browser Console

1. Open browser Developer Tools (F12)
2. Go to Console tab
3. Look for any JavaScript errors
4. Check if Livewire is loaded

### Step 5: Verify Component is Registered

The package should auto-register the component. Check if it's being rendered by:

1. View page source
2. Search for "filament-chatgpt-bot" or "btn-chat"
3. If not found, the component isn't rendering

### Step 6: Manual Test

Try adding the component manually to a test page to see if it works:

```blade
@livewire('filament-chatgpt-bot')
```

### Step 7: Check Package Installation

Verify the package is installed:

```bash
composer show icetalker/filament-chatgpt-bot
```

### Step 8: Restart Server

If using `php artisan serve`, restart it:

```bash
# Stop the server (Ctrl+C)
# Then restart:
php artisan serve
```

## Common Issues:

1. **Config cache**: Most common - clear config cache
2. **Not logged in**: Component only shows when authenticated
3. **Livewire not loaded**: Check if Livewire scripts are included
4. **View cache**: Clear view cache

