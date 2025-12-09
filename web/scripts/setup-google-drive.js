/**
 * Google Drive Setup Script
 * 
 * This script helps you set up Google Drive API access.
 * 
 * Steps:
 * 1. Go to https://console.cloud.google.com/
 * 2. Create a new project or select existing one
 * 3. Enable Google Drive API
 * 4. Create OAuth 2.0 credentials
 * 5. Download credentials as JSON
 * 6. Save it as google-credentials.json in the web folder
 * 7. Run this script: node scripts/setup-google-drive.js
 */

const { google } = require('googleapis')
const fs = require('fs')
const path = require('path')
const readline = require('readline')

const SCOPES = ['https://www.googleapis.com/auth/drive.file']
const TOKEN_PATH = path.join(process.cwd(), 'google-token.json')
const CREDENTIALS_PATH = path.join(process.cwd(), 'google-credentials.json')

async function authorize() {
  if (!fs.existsSync(CREDENTIALS_PATH)) {
    console.error('❌ Error: google-credentials.json not found!')
    console.log('\n📝 Steps to get credentials:')
    console.log('1. Go to https://console.cloud.google.com/')
    console.log('2. Create a new project or select existing one')
    console.log('3. Enable Google Drive API')
    console.log('4. Go to APIs & Services > Credentials')
    console.log('5. Create OAuth 2.0 Client ID')
    console.log('6. Application type: Desktop app')
    console.log('7. Download credentials JSON')
    console.log('8. Save it as google-credentials.json in the web folder')
    process.exit(1)
  }

  const credentials = JSON.parse(fs.readFileSync(CREDENTIALS_PATH, 'utf-8'))
  const { client_secret, client_id, redirect_uris } = credentials.installed || credentials.web || {}
  const oAuth2Client = new google.auth.OAuth2(client_id, client_secret, redirect_uris?.[0] || 'http://localhost:3000')

  // Check if we have previously stored a token
  if (fs.existsSync(TOKEN_PATH)) {
    const token = JSON.parse(fs.readFileSync(TOKEN_PATH, 'utf-8'))
    oAuth2Client.setCredentials(token)
    console.log('✅ Using existing token')
    return oAuth2Client
  }

  // Get new token
  return getNewToken(oAuth2Client)
}

function getNewToken(oAuth2Client) {
  return new Promise((resolve, reject) => {
    const authUrl = oAuth2Client.generateAuthUrl({
      access_type: 'offline',
      scope: SCOPES,
    })
    
    console.log('\n🔐 Authorize this app by visiting this url:')
    console.log(authUrl)
    console.log('\n')

    const rl = readline.createInterface({
      input: process.stdin,
      output: process.stdout,
    })

    rl.question('Enter the code from that page here: ', (code) => {
      rl.close()
      oAuth2Client.getToken(code, (err, token) => {
        if (err) {
          console.error('❌ Error retrieving access token', err)
          return reject(err)
        }
        oAuth2Client.setCredentials(token)
        
        // Store the token for future use
        fs.writeFileSync(TOKEN_PATH, JSON.stringify(token, null, 2))
        console.log('✅ Token stored to google-token.json')
        
        resolve(oAuth2Client)
      })
    })
  })
}

async function testConnection() {
  try {
    const auth = await authorize()
    const drive = google.drive({ version: 'v3', auth })
    
    // Test by listing files
    const response = await drive.files.list({
      pageSize: 10,
      fields: 'nextPageToken, files(id, name)',
    })
    
    console.log('\n✅ Successfully connected to Google Drive!')
    console.log(`📁 Found ${response.data.files?.length || 0} files`)
    
    // Check if folder exists
    const folderId = '1eTof5Fq-w7cBw9mXK6wXYetQy_LunpCW'
    try {
      await drive.files.get({ fileId: folderId })
      console.log('✅ Target folder found!')
    } catch (error) {
      console.log('⚠️  Warning: Could not access target folder. Make sure you have access to it.')
    }
    
  } catch (error) {
    console.error('❌ Error:', error.message)
    process.exit(1)
  }
}

testConnection()

