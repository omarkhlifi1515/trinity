import { google } from 'googleapis'
import fs from 'fs'
import path from 'path'

// Google Drive API configuration
const FOLDER_ID = '1eTof5Fq-w7cBw9mXK6wXYetQy_LunpCW'
const USERS_FILE_NAME = 'users.json'

let driveClient: any = null

// Initialize Google Drive client
export async function initDriveClient() {
  if (driveClient) {
    return driveClient
  }

  // Check for credentials
  const credentialsPath = path.join(process.cwd(), 'google-credentials.json')
  const tokenPath = path.join(process.cwd(), 'google-token.json')

  if (!fs.existsSync(credentialsPath)) {
    console.warn('⚠️ Google credentials not found. Using local storage as fallback.')
    return null
  }

  try {
    const credentials = JSON.parse(fs.readFileSync(credentialsPath, 'utf-8'))
    
    const { client_secret, client_id, redirect_uris } = credentials.installed || credentials.web || {}
    const oAuth2Client = new google.auth.OAuth2(client_id, client_secret, redirect_uris?.[0] || 'http://localhost:3000')

    // Load token if exists
    if (fs.existsSync(tokenPath)) {
      const token = JSON.parse(fs.readFileSync(tokenPath, 'utf-8'))
      oAuth2Client.setCredentials(token)
    } else {
      // Need to get new token
      console.warn('⚠️ Google token not found. Run the setup script to authenticate.')
      return null
    }

    driveClient = google.drive({ version: 'v3', auth: oAuth2Client })
    return driveClient
  } catch (error) {
    console.error('Failed to initialize Google Drive client:', error)
    return null
  }
}

// Find or create users.json file in Google Drive
async function getUsersFileId(drive: any): Promise<string | null> {
  try {
    // Search for existing file
    const response = await drive.files.list({
      q: `name='${USERS_FILE_NAME}' and '${FOLDER_ID}' in parents and trashed=false`,
      fields: 'files(id, name)',
    })

    if (response.data.files && response.data.files.length > 0) {
      return response.data.files[0].id!
    }

    // Create new file if not found
    const fileMetadata = {
      name: USERS_FILE_NAME,
      parents: [FOLDER_ID],
    }

    const media = {
      mimeType: 'application/json',
      body: JSON.stringify([], null, 2),
    }

    const file = await drive.files.create({
      requestBody: fileMetadata,
      media: media,
      fields: 'id',
    })

    return file.data.id!
  } catch (error) {
    console.error('Error getting users file:', error)
    return null
  }
}

// Read users from Google Drive
export async function readUsersFromDrive(): Promise<any[]> {
  try {
    const drive = await initDriveClient()
    if (!drive) {
      return []
    }

    const fileId = await getUsersFileId(drive)
    if (!fileId) {
      return []
    }

    const response = await drive.files.get(
      { fileId, alt: 'media' },
      { responseType: 'stream' }
    )

    let data = ''
    for await (const chunk of response.data) {
      data += chunk
    }

    return JSON.parse(data || '[]')
  } catch (error: any) {
    if (error.code === 404) {
      return []
    }
    console.error('Error reading from Google Drive:', error)
    return []
  }
}

// Write users to Google Drive
export async function writeUsersToDrive(users: any[]): Promise<boolean> {
  try {
    const drive = await initDriveClient()
    if (!drive) {
      return false
    }

    const fileId = await getUsersFileId(drive)
    if (!fileId) {
      return false
    }

    const media = {
      mimeType: 'application/json',
      body: JSON.stringify(users, null, 2),
    }

    await drive.files.update({
      fileId,
      media,
    })

    return true
  } catch (error) {
    console.error('Error writing to Google Drive:', error)
    return false
  }
}

// Fallback: Read from local file
export function readUsersFromLocal(): any[] {
  try {
    const usersFile = path.join(process.cwd(), 'data', 'users.json')
    if (fs.existsSync(usersFile)) {
      return JSON.parse(fs.readFileSync(usersFile, 'utf-8'))
    }
  } catch (error) {
    console.error('Error reading local users file:', error)
  }
  return []
}

// Fallback: Write to local file
export function writeUsersToLocal(users: any[]): void {
  try {
    const dataDir = path.join(process.cwd(), 'data')
    if (!fs.existsSync(dataDir)) {
      fs.mkdirSync(dataDir, { recursive: true })
    }
    const usersFile = path.join(dataDir, 'users.json')
    fs.writeFileSync(usersFile, JSON.stringify(users, null, 2))
  } catch (error) {
    console.error('Error writing local users file:', error)
  }
}

