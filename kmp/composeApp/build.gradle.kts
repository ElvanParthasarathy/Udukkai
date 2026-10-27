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
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.database)
            implementation(libs.firebase.analytics)
            // SVG logos (data:image/svg+xml) in ElvanImageViewer
            implementation("com.caverock:androidsvg-aar:1.4")
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
        versionCode = 1
        versionName = "1.0"
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
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// ── Desktop: default entry point (./gradlew :composeApp:run) defaults to Factory ──
compose.desktop {
    application {
        mainClass = "MainFactoryKt"
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
        runtimeCp
    )
    mainClass.set("MainFactoryKt")
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
        runtimeCp
    )
    mainClass.set("MainTextilesKt")
    jvmArgs("-Xms64m", "-Xmx512m")
}
