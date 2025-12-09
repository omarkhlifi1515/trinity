# Google Drive Storage Setup

This app can store user data in Google Drive instead of locally. Follow these steps to set it up:

## Step 1: Enable Google Drive API

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Google Drive API**:
   - Go to "APIs & Services" > "Library"
   - Search for "Google Drive API"
   - Click "Enable"

## Step 2: Create OAuth 2.0 Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth client ID"
3. If prompted, configure the OAuth consent screen:
   - Choose "External" (unless you have a Google Workspace)
   - Fill in required fields (App name, User support email, etc.)
   - Add your email to test users
   - Save
4. Create OAuth 2.0 Client ID:
   - Application type: **Desktop app**
   - Name: "Trinity HRM" (or any name)
   - Click "Create"
5. Download the credentials:
   - Click the download icon next to your new credential
   - Save the JSON file

## Step 3: Add Credentials to Project

1. Copy the downloaded JSON file to: `trinity-get-2/web/google-credentials.json`
2. Make sure the file is named exactly `google-credentials.json`

## Step 4: Run Setup Script

```bash
cd trinity-get-2/web
node scripts/setup-google-drive.js
```

This will:
- Open a browser window for authentication
- Ask you to authorize the app
- Save the token to `google-token.json`

## Step 5: Verify Setup

After running the setup script, you should see:
- ✅ Successfully connected to Google Drive!
- ✅ Target folder found!

## How It Works

- User data is stored in `users.json` file in your Google Drive folder
- The app automatically syncs with Google Drive
- If Google Drive is unavailable, it falls back to local storage (`data/users.json`)

## Troubleshooting

### "google-credentials.json not found"
- Make sure you downloaded the credentials JSON file
- Place it in the `web` folder
- Name it exactly `google-credentials.json`

### "Could not access target folder"
- Make sure you have access to the Google Drive folder
- The folder ID is: `1eTof5Fq-w7cBw9mXK6wXYetQy_LunpCW`
- Share the folder with the Google account you're using

### "Token expired"
- Delete `google-token.json`
- Run the setup script again

## Security Note

- Never commit `google-credentials.json` or `google-token.json` to git
- Add them to `.gitignore`
- Keep your credentials secure

