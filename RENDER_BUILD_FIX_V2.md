# Render Build Fix - Module Resolution Issue

## Issues Found

1. ✅ **autoprefixer** - Moved from devDependencies to dependencies
2. ⚠️ **Module resolution** - `@/lib/auth/local-auth` not found during build

## Solution

The `rootDir: web` in render.yaml should work, but if it doesn't, you may need to:

### Option 1: Verify File Structure in Git

Make sure `web/lib/auth/local-auth.ts` is committed to git:

```bash
git add web/lib/auth/local-auth.ts
git commit -m "Add local-auth module"
git push
```

### Option 2: Update Build Command

If `rootDir` isn't working, update the build command in Render Dashboard:

1. Go to Render Dashboard → Your Service → Settings
2. Change **Build Command** to:
   ```
   cd web && npm install && npm run build
   ```
3. Change **Start Command** to:
   ```
   cd web && npm start
   ```
4. Remove **Root Directory** setting (leave empty)

### Option 3: Check .gitignore

Make sure `web/lib/` is NOT in `.gitignore`:

```bash
# Check if lib is ignored
cat web/.gitignore | grep lib

# If lib is ignored, remove it from .gitignore
```

## Files Updated

- ✅ `web/package.json` - autoprefixer moved to dependencies

## Next Steps

1. Commit and push the package.json changes
2. Verify `web/lib/auth/local-auth.ts` is in git
3. Redeploy on Render
4. If still failing, try Option 2 above

