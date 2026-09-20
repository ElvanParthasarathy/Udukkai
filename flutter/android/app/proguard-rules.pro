## ──────────────────────────────────────────────
## Flutter Engine
## ──────────────────────────────────────────────
-keep class io.flutter.app.** { *; }
-keep class io.flutter.plugin.** { *; }
-keep class io.flutter.util.** { *; }
-keep class io.flutter.view.** { *; }
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }
-dontwarn io.flutter.embedding.**

## ──────────────────────────────────────────────
## Firebase Crashlytics
## ──────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

## ──────────────────────────────────────────────
## Drift / SQLite
## ──────────────────────────────────────────────
-keep class drift.** { *; }
-keep class org.sqlite.** { *; }
-keep class sqlite3.** { *; }
-dontwarn drift.**

## ──────────────────────────────────────────────
## Google Play Core (in-app updates, review, etc.)
## ──────────────────────────────────────────────
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

## ──────────────────────────────────────────────
## General best practices
## ──────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-dontwarn javax.annotation.**
