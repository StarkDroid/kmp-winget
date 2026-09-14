# ----------------------------------
# Kotlin Rules
# ----------------------------------
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ----------------------------------
# JNA & Win32 Native Interop Rules
# ----------------------------------
-keep class com.sun.jna.** { *; }
-keepclassmembers class * extends com.sun.jna.** { *; }
-keep class com.velocity.kmpwinget.data.datasource.** { *; }
-dontwarn com.sun.jna.**

# ----------------------------------
# Java Reflection Rules
# ----------------------------------
-keepclassmembers class * {
    @org.jetbrains.annotations.NotNull *;
    @org.jetbrains.annotations.Nullable *;
}