# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================
# RETROFIT & OKHTTP
# ============================================
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# Retrofit
-keep class retrofit2.** { *; }
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ============================================
# GSON
# ============================================
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ============================================
# APPLICATION DATA CLASSES (keep untuk serialization)
# ============================================
-keep class com.example.mykasir.core_data.remote.** { *; }
-keep class com.example.mykasir.feature_manajemen_produk.model.** { *; }
-keep class com.example.mykasir.feature_transaksi.model.** { *; }
-keep class com.example.mykasir.feature_auth.model.** { *; }
-keep class com.example.mykasir.feature_collaborator.model.** { *; }
-keep class com.example.mykasir.core_data.local.entity.** { *; }

# ============================================
# ROOM DATABASE
# ============================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============================================
# COROUTINES
# ============================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============================================
# COMPOSE
# ============================================
-dontwarn androidx.compose.**

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile