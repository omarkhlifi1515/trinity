# Render Build Fix - Missing Files in Git

## Problem

Render build is failing because these files are not committed to git:
- `web/lib/auth/local-auth.ts`
- `web/components/attendance/MarkAttendancePage.tsx`
- `web/lib/storage/jsonbin.ts` (likely)

## Solution

You need to **commit these files to git** so Render can access them during build.

### Step 1: Check Git Status

```bash
git status
```

Look for files in `web/lib/` and `web/components/` that are untracked.

### Step 2: Add Missing Files

```bash
# Add auth module
git add web/lib/auth/local-auth.ts

# Add storage module  
git add web/lib/storage/jsonbin.ts

# Add attendance component
git add web/components/attendance/MarkAttendancePage.tsx

# Or add entire directories
git add web/lib/
git add web/components/
```

### Step 3: Verify Files Are Tracked

```bash
# Check if files are now tracked
git ls-files web/lib/auth/local-auth.ts
git ls-files web/components/attendance/MarkAttendancePage.tsx
git ls-files web/lib/storage/jsonbin.ts
```

Should show the file paths if they're tracked.

### Step 4: Commit and Push

```bash
git commit -m "Add missing auth, storage, and attendance components for Render build"
git push origin main
```

### Step 5: Redeploy on Render

- Render will auto-deploy on push
- Or manually trigger: Render Dashboard → Manual Deploy → Deploy latest commit

## Quick Fix Command

Run this to add all missing files:

```bash
git add web/lib/ web/components/
git status  # Review what will be committed
git commit -m "Add missing modules and components for Render build"
git push origin main
```

## Verify Files Are Not Ignored

Check `.gitignore` files:

```bash
# Check root .gitignore
cat .gitignore | grep -E "(lib|components)"

# Check web/.gitignore  
cat web/.gitignore | grep -E "(lib|components)"
```

If `lib/` or `components/` are in `.gitignore`, remove them.

## Files That Must Be Committed

These files **MUST** be in git for Render to build:

- ✅ `web/lib/auth/local-auth.ts`
- ✅ `web/lib/storage/jsonbin.ts`
- ✅ `web/components/attendance/MarkAttendancePage.tsx`
- ✅ `web/tsconfig.json` (for path aliases)
- ✅ `web/package.json` (dependencies)
- ✅ `web/next.config.js` (Next.js config)

## After Pushing

Wait for Render to rebuild. The build should succeed once files are committed.

---

**The issue is files not being in git, not a code problem!**

