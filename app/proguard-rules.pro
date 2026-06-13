# Creator OS Lite ProGuard / R8 Rules

# ---- Moshi (JSON serialization) ----
# Keep Moshi's generated adapters
-keep class com.example.** { *; }
-keep class **.MoshiJsonAdapter { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.JsonClass @com.squareup.moshi.FromJson @com.squareup.moshi.ToJson *;
}
-keepclassmembers @com.squareup.moshi.JsonClass class * {
    *** *;
}

# ---- Retrofit ----
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ---- OkHttp ----
-dontwarn okhttp3.**
-dontwarn okio.**

# ---- Kotlin Coroutines ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ---- Keep application entry point ----
-keep class com.example.CreatorOSApplication { *; }
-keep class com.example.MainActivity { *; }
