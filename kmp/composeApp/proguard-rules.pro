# Web print bundle talks to Kotlin through @JavascriptInterface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface,*Annotation*,Signature,InnerClasses,EnclosingMethod

# PDFium based viewer (AndroidPdfViewer) uses JNI
-keep class com.shockwave.** { *; }
-keep class io.github.barteksc.** { *; }

# Desktop-only JDBC and Java classes are not part of the Android build
-dontwarn org.sqlite.**
-dontwarn java.awt.**
-dontwarn javax.swing.**
