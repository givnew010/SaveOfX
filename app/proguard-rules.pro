# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.example.data.** { *; }
-keep class com.example.util.** { *; }
