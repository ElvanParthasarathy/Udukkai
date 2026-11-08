import java.util.Properties

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            // The fingerprint prompt: Google's own library (the same on every phone maker's Android)
            implementation("androidx.biometric:biometric:1.1.0")
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.database)
            implementation(libs.firebase.auth)
            // App Check: only genuine installs of this app may talk to Firebase once enforcement is on in the console
            implementation(libs.firebase.appcheck.playintegrity)
            implementation(libs.firebase.appcheck.debug)
            // The real Material 3 expressive loaders (as in Neram)
            implementation("androidx.compose.material3:material3:1.5.0-alpha12")
            // Sign in with Google (Credential Manager)
            implementation("androidx.credentials:credentials:1.3.0")
            implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
            implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
            implementation(libs.firebase.analytics)
            // SVG logos (data:image/svg+xml) in ElvanImageViewer
            implementation("com.caverock:androidsvg-aar:1.4")
            // PDFium based PDF viewer (document preview)
            implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
            // Google Play in-app updates (mandatory immediate update for Play Store builds)
            implementation("com.google.android.play:app-update:2.1.0")
            implementation("com.google.android.play:app-update-ktx:2.1.0")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation("com.composables:icons-material-symbols-cmp:2.2.1")
            implementation("com.composables:icons-material-symbols-rounded-cmp:2.2.1")
            implementation("com.composables:icons-material-symbols-rounded-filled-cmp:2.2.1")
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            // Frosted glass behind the floating bars
            implementation(libs.haze)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("org.xerial:sqlite-jdbc:3.45.1.0")
        }
    }
}

android {
    namespace = "com.elvan.udukkai"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // applicationId is set per-flavor
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 8
        versionName = "1.0.6"
        val isPlayStore = project.hasProperty("playStore") || (project.findProperty("playStore")?.toString() == "true")
        buildConfigField("Boolean", "IS_PLAY_STORE_BUILD", isPlayStore.toString())
    }

    // ── Flavor dimensions ─────────────────────────────────────────────────────
    flavorDimensions += "app"

    productFlavors {
        // ── Factory: fixed Kooli mode ─────────────────────────────────────────
        create("factory") {
            dimension = "app"
            applicationId = "com.elvan.udukkai.factory"
            buildConfigField("String", "APP_MODE", "\"factory\"")
            buildConfigField("String", "APP_DISPLAY_NAME", "\"Udukkai Factory\"")
        }
        // ── Textiles: fixed Textiles mode ──────────────────────────────────────
        create("textiles") {
            dimension = "app"
            applicationId = "com.elvan.udukkai.textiles"
            buildConfigField("String", "APP_MODE", "\"textiles\"")
            buildConfigField("String", "APP_DISPLAY_NAME", "\"Udukkai Textiles\"")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Release signing comes from kmp/keystore.properties (never committed):
    //   storeFile=../udukkai-release.jks / storePassword=... / keyAlias=... / keyPassword=...
    val keystoreProps = Properties().apply {
        val f = rootProject.file("keystore.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    signingConfigs {
        if (keystoreProps.containsKey("storeFile")) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Use the official release keystore if present, fallback to debug key
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

// ── Desktop: default entry point (./gradlew :composeApp:run) defaults to Factory ──
compose.desktop {
    application {
        mainClass = "Main_factoryKt"
        javaHome = "C:/Program Files/Android/Android Studio/jbr"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "Udukkai Factory"
            packageVersion = "1.0.0"
            windows {
                iconFile.set(project.file("src/desktopMain/resources/udukkai_icon.ico"))
            }
        }
    }
}

// ── Desktop: Factory (./gradlew :composeApp:runFactory) ──────────────────────
tasks.register<JavaExec>("runFactory") {
    group = "compose desktop"
    description = "Run Udukkai Factory desktop app (fixed Kooli mode)"
    dependsOn("compileKotlinDesktop")
    val runtimeCp = configurations.named("desktopRuntimeClasspath")
    classpath = files(
        kotlin.targets.named("desktop").map { t ->
            t.compilations.named("main").map { it.output.classesDirs }
        },
        runtimeCp,
        project.file("src/desktopMain/resources")
    )
    mainClass.set("Main_factoryKt")
    jvmArgs("-Xms64m", "-Xmx512m")
}

// ── Desktop: Textiles (./gradlew :composeApp:runTextiles) ────────────────────
tasks.register<JavaExec>("runTextiles") {
    group = "compose desktop"
    description = "Run Udukkai Textiles desktop app (fixed Pattu mode)"
    dependsOn("compileKotlinDesktop")
    val runtimeCp = configurations.named("desktopRuntimeClasspath")
    classpath = files(
        kotlin.targets.named("desktop").map { t ->
            t.compilations.named("main").map { it.output.classesDirs }
        },
        runtimeCp,
        project.file("src/desktopMain/resources")
    )
    mainClass.set("Main_textilesKt")
    jvmArgs("-Xms64m", "-Xmx512m")
}
