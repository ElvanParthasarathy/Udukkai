@echo off
echo ==================================================
echo Building Native React Engine inside Flutter...
echo ==================================================

cd /D "%~dp0..\react_engine"

echo Installing minimal dependencies...
call npm install --no-fund --no-audit

echo Building minified assets...
call npx vite build

echo Clearing old assets from Flutter app...
del /Q /S "..\android\app\src\main\assets\react_app\*.*" >nul 2>&1

echo Copying new assets...
xcopy /s /e /y "dist\*" "..\android\app\src\main\assets\react_app\"

echo Done! The Native React Engine is fully integrated into the APK.
