# Proguard rules for Mari Partner Fuel
# Keep Supabase & Ktor serialization classes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
