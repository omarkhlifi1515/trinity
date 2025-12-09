# 📱 Mobile App Setup Guide

This guide will help you configure and build the Trinity HRM mobile app.

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK with API Level 26+ (Android 8.0+)
- Gradle 8.12.1+

## Configuration

### 1. Update API Endpoints

The app uses BuildConfig fields for API configuration. Update these in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    // Replace with your Render deployment URL
    buildConfigField("String", "BASE_URL", "\"https://your-app.onrender.com/api/\"")
    
    // Supabase configuration (if using)
    buildConfigField("String", "SUPABASE_URL", "\"your-supabase-url\"")
    buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-supabase-key\"")
    
    // N8N configuration (if using)
    buildConfigField("String", "N8N_BASE_URL", "\"your-n8n-url\"")
}
```

### 2. Update Application ID

The application ID has been changed from `com.example.smarthr_app` to `com.trinity.smarthr`. 

**Important**: If you have an existing installation, you'll need to uninstall the old app first, or change the application ID back if you want to keep the same package.

### 3. Google OAuth Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Update `web_client_id` in `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="web_client_id">YOUR_GOOGLE_CLIENT_ID</string>
   ```

### 4. Network Security Configuration

The app is configured to:
- Allow HTTPS connections (production)
- Allow cleartext for localhost/development IPs only

Update `network_security_config.xml` if you need to add custom domains.

## Building the App

### Debug Build

```bash
cd "mobile app"
./gradlew assembleDebug
```

Or use Android Studio:
1. Open the project in Android Studio
2. Click "Build" → "Make Project"
3. Run on device/emulator

### Release Build

1. Create a keystore file:
   ```bash
   keytool -genkey -v -keystore trinity-release.keystore -alias trinity -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Create `keystore.properties` in the project root:
   ```properties
   storePassword=your_store_password
   keyPassword=your_key_password
   keyAlias=trinity
   storeFile=../trinity-release.keystore
   ```

3. Update `app/build.gradle.kts` to add signing config:
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               val keystorePropertiesFile = rootProject.file("keystore.properties")
               val keystoreProperties = java.util.Properties()
               keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
               
               storeFile = file(keystoreProperties["storeFile"] as String)
               storePassword = keystoreProperties["storePassword"] as String
               keyAlias = keystoreProperties["keyAlias"] as String
               keyPassword = keystoreProperties["keyPassword"] as String
           }
       }
       
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
               isMinifyEnabled = true
               proguardFiles(
                   getDefaultProguardFile("proguard-android-optimize.txt"),
                   "proguard-rules.pro"
               )
           }
       }
   }
   ```

4. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

5. Build release AAB (for Play Store):
   ```bash
   ./gradlew bundleRelease
   ```

## Troubleshooting

### Build Errors

**Error: "BuildConfig not found"**
- Clean and rebuild: `./gradlew clean build`
- Ensure `buildConfig = true` is set in `buildFeatures`

**Error: "Package name mismatch"**
- The package name was changed from `com.example.smarthr_app` to `com.trinity.smarthr`
- Update all package declarations or revert the change

**Error: "Network security config not found"**
- Ensure `network_security_config.xml` exists in `app/src/main/res/xml/`

### API Connection Issues

1. **Check BASE_URL**: Verify it matches your Render deployment URL
2. **Check network security**: Ensure HTTPS is properly configured
3. **Check API endpoints**: Verify Laravel API routes are accessible
4. **Check authentication**: Ensure JWT tokens are being sent correctly

### Google Sign-In Issues

1. Verify OAuth client ID is correct
2. Check SHA-1 fingerprint is added to Google Cloud Console
3. Ensure Google Play Services is installed on device

## Development Tips

### Using Different API URLs for Debug/Release

Create build variants in `app/build.gradle.kts`:

```kotlin
android {
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"http://localhost:8000/api/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://your-app.onrender.com/api/\"")
        }
    }
}
```

Then build with:
```bash
./gradlew assembleDevDebug
./gradlew assembleProdRelease
```

## Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## Dependencies

Key dependencies used:
- **Jetpack Compose**: Modern UI framework
- **Retrofit**: HTTP client for API calls
- **DataStore**: Local data storage
- **Coil**: Image loading
- **Supabase**: Backend services (if used)
- **Google Play Services**: Location and authentication

## Support

For issues:
1. Check Android Studio's build output
2. Review Logcat for runtime errors
3. Verify API endpoints are accessible
4. Check network security configuration

---

**Note**: Remember to update the BASE_URL in `build.gradle.kts` with your actual Render deployment URL before building for production!

