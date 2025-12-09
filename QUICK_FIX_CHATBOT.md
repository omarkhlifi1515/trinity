# Quick Fix: Chatbot Not Opening

## Most Common Issue: Config Cache

The config is set to `enable => true`, but Laravel might be using a cached version.

### Solution 1: Clear Config Cache

**If you have PHP available:**
```bash
php artisan config:clear
php artisan view:clear
php artisan cache:clear
```

**If PHP is not in PATH:**
1. Delete this file if it exists: `bootstrap/cache/config.php`
2. Restart your Laravel server

### Solution 2: Verify Config

The config file `config/filament-chatgpt-bot.php` should have:
```php
'enable' => true,  // NOT false
```

### Solution 3: Check Browser

1. **Hard refresh the page**: Press `Ctrl+F5` (Windows) or `Cmd+Shift+R` (Mac)
2. **Check if you're logged in**: The chatbot only shows when authenticated
3. **Open Developer Tools (F12)**: Check Console tab for any errors

### Solution 4: Verify Package is Installed

Check if the package files exist:
- `vendor/icetalker/filament-chatgpt-bot/` should exist

### Solution 5: Manual Test

If the above doesn't work, try manually adding the component to test if it works:

Add this to any Filament page temporarily:
```blade
@livewire('filament-chatgpt-bot')
```

If this shows the chatbot, the auto-registration isn't working. If it doesn't, there's a deeper issue.

## Expected Behavior

After clearing cache and refreshing:
- You should see a **blue circular button** in the **bottom-right corner**
- Clicking it should open a chat window
- The chat window should match your old chatbot style (blue header, same layout)

