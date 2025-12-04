# Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-keep interface kotlinx.coroutines.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Application classes
-keep class com.example.mobiletrinity.api.** { *; }
-keep class com.example.mobiletrinity.data.** { *; }
-keep class com.example.mobiletrinity.ui.** { *; }

# Keep data classes
-keep class com.example.mobiletrinity.api.TaskRequest { *; }
-keep class com.example.mobiletrinity.api.TaskResponse { *; }
-keep class com.example.mobiletrinity.data.Task { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
