# Fix: Chatbot Not Opening

## Quick Fix Steps:

### 1. Clear Config Cache (CRITICAL)
The config might be cached with old values. Run:

```bash
php artisan config:clear
```

Or manually delete: `bootstrap/cache/config.php` (if it exists)

### 2. Verify Config File
Check `config/filament-chatgpt-bot.php`:

```php
'enable' => true,  // Must be TRUE
```

### 3. Restart Laravel Server
If using `php artisan serve`, restart it:
- Press Ctrl+C to stop
- Run `php artisan serve` again

### 4. Check Browser
1. Hard refresh: Ctrl+F5 (Windows) or Cmd+Shift+R (Mac)
2. Open Developer Tools (F12)
3. Check Console for errors
4. Check if you see the button in bottom-right corner

### 5. Verify You're Logged In
The chatbot only shows when authenticated. Make sure you're logged into the admin panel.

### 6. Check View File
The custom view should be at:
`resources/views/vendor/filament-chatgpt-bot/livewire/chat-bot.blade.php`

If it doesn't exist, publish views:
```bash
php artisan vendor:publish --tag="filament-chatgpt-bot-views"
```

Then replace the published file with our custom version.

## If Still Not Working:

### Manual Test - Add Component Directly

Temporarily add this to a Filament page to test:

```blade
@livewire('filament-chatgpt-bot')
```

If this works, the issue is with the auto-registration. If it doesn't, there's a Livewire or component issue.

