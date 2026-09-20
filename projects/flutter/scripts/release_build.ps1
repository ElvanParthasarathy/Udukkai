#!/usr/bin/env pwsh
# ──────────────────────────────────────────────
# Elvan Niril — Release Build Script
# ──────────────────────────────────────────────
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot

Push-Location $projectRoot
try {
    Write-Host "`n🧹 Cleaning previous build..." -ForegroundColor Cyan
    flutter clean

    Write-Host "`n📦 Installing dependencies..." -ForegroundColor Cyan
    flutter pub get

    Write-Host "`n🧪 Running tests..." -ForegroundColor Cyan
    flutter test
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`n❌ Tests failed. Aborting release build." -ForegroundColor Red
        exit 1
    }

    Write-Host "`n🏗️  Building release app bundle..." -ForegroundColor Cyan
    flutter build appbundle --release --obfuscate --split-debug-info=build/debug-info
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`n❌ Build failed." -ForegroundColor Red
        exit 1
    }

    $aabPath = "build\app\outputs\bundle\release\app-release.aab"
    Write-Host "`n✅ Release build succeeded!" -ForegroundColor Green
    Write-Host "   AAB: $projectRoot\$aabPath" -ForegroundColor Yellow
    Write-Host "   Debug symbols: $projectRoot\build\debug-info\" -ForegroundColor Yellow
}
finally {
    Pop-Location
}
