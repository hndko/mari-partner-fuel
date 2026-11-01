# Proguard & R8 Rules for Mari Partner Fuel
# Maintain compatibility with Kotlin 2.0.21, Jetpack Compose, ML Kit, Supabase, and Play Billing

# Keep Reflection & Serialization attributes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep class * implements kotlinx.serialization.KSerializer {
    <init>(...);
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.SerialInfo <methods>;
}

# Supabase DTO models
-keep class id.maripartner.fuel.data.remote.dto.** { *; }
-keep class id.maripartner.fuel.domain.model.** { *; }

# Google ML Kit Text Recognition
-keep class com.google.mlkit.vision.** { *; }
-keep class com.google.android.gms.vision.** { *; }

# Google Play Billing Library
-keep class com.android.billingclient.api.** { *; }

# Ktor & OkHttp Client
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn io.ktor.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
