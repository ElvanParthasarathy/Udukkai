# PowerShell script to build and publish Udukkai releases to GitHub Releases
param(
    [switch]$NoBuild
)

$ErrorActionPreference = "Stop"
Set-Location "$PSScriptRoot\.."

# Read version
$gradleContent = Get-Content "kmp\composeApp\build.gradle.kts" -Raw
if ($gradleContent -match 'versionName\s*=\s*"([^"]+)"') {
    $version = $matches[1]
} else {
    Write-Error "Could not parse versionName from kmp\composeApp\build.gradle.kts"
}

$tag = "v$version"
Write-Host "Target version: $version ($tag)"

# Build APKs
if (-not $NoBuild) {
    Write-Host "Compiling release APKs..."
    Set-Location "kmp"
    $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
    .\gradlew assembleFactoryRelease assembleTextilesRelease --console=plain
    Set-Location "$PSScriptRoot\.."
}

$factoryApk = "kmp\composeApp\build\outputs\apk\factory\release\composeApp-factory-release.apk"
$textilesApk = "kmp\composeApp\build\outputs\apk\textiles\release\composeApp-textiles-release.apk"

$destFactory = "$env:TEMP\Udukkai-Factory.apk"
$destTextiles = "$env:TEMP\Udukkai-Textiles.apk"

Copy-Item $factoryApk -Destination $destFactory -Force
Copy-Item $textilesApk -Destination $destTextiles -Force

$notesFile = "$env:TEMP\udukkai_notes.md"
$notesContent = "Udukkai $version release.`n`nInstall Udukkai-Factory.apk or Udukkai-Textiles.apk. Installed copies update automatically from the in-app updater."
Set-Content -Path $notesFile -Value $notesContent -Encoding UTF8

Write-Host "Publishing to GitHub Releases via gh CLI..."
try {
    gh release view $tag
    gh release upload $tag $destFactory $destTextiles --clobber
    gh release edit $tag --latest --notes-file $notesFile
} catch {
    gh release create $tag $destFactory $destTextiles --title "Udukkai $version" --latest --notes-file $notesFile
}

Write-Host "Successfully published $tag to GitHub!"
