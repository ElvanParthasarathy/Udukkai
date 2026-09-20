$ErrorActionPreference = 'Stop'

# 1. Update capacitor.config.ts
$capConfig = "capacitor.config.ts"
(Get-Content $capConfig) -replace "appId: 'com.elvan.niril'", "appId: 'com.elvan.niril.lite'" | Set-Content $capConfig

# 2. Update android/app/build.gradle
$buildGradle = "android/app/build.gradle"
(Get-Content $buildGradle) -replace 'applicationId "com.elvan.niril"', 'applicationId "com.elvan.niril.lite"' -replace 'namespace "com.elvan.niril"', 'namespace "com.elvan.niril.lite"' | Set-Content $buildGradle

# 3. Update AndroidManifest.xml (if applicable)
$manifest = "android/app/src/main/AndroidManifest.xml"
(Get-Content $manifest) -replace 'package="com.elvan.niril"', 'package="com.elvan.niril.lite"' | Set-Content $manifest

# 4. Move and update MainActivity.java
$oldDir = "android/app/src/main/java/com/elvan/niril"
$newDir = "android/app/src/main/java/com/elvan/niril/lite"
New-Item -ItemType Directory -Force -Path $newDir | Out-Null

$mainActivity = Join-Path $oldDir "MainActivity.java"
if (Test-Path $mainActivity) {
    (Get-Content $mainActivity) -replace "package com.elvan.niril;", "package com.elvan.niril.lite;" | Set-Content (Join-Path $newDir "MainActivity.java")
    Remove-Item $mainActivity
}

Write-Host "Package name successfully changed to com.elvan.niril.lite"
