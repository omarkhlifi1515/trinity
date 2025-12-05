# Android integration notes

This file documents minimal setup steps for the Trinity Android app: Gradle dependencies, manifest, and Retrofit base URL.

## Gradle dependencies (app-level `build.gradle` or `build.gradle.kts`)

Add these (versions are examples; align with your project):

```
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-gson:2.9.0"
implementation "com.squareup.okhttp3:okhttp:4.10.0"
implementation "com.squareup.okhttp3:logging-interceptor:4.10.0"

// Hilt
implementation "com.google.dagger:hilt-android:2.45"
kapt "com.google.dagger:hilt-compiler:2.45"

// Jetpack Compose (material3)
implementation "androidx.compose.material3:material3:1.1.0"
implementation "androidx.hilt:hilt-navigation-compose:1.0.0"

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1"
```

## BuildConfig base URL

Define a build config field for the backend base URL so `NetworkModule` can reference it. Example (Groovy):

```
android {
  defaultConfig {
    buildConfigField "String", "TRINITY_BASE_URL", '"http://10.0.2.2:8000/"'
  }
}
```

For production builds replace with your deployed API URL.

## AndroidManifest

Make sure you have the `INTERNET` permission (see `src/main/AndroidManifest.xml`).

## Hilt setup

Remember to apply the Hilt plugin and add the `@HiltAndroidApp` annotated `Application` subclass if using Hilt. Example `TrinityApplication.kt`:

```kotlin
@HiltAndroidApp
class TrinityApplication : Application() {}
```
