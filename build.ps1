# TTS Voice Service - Build Script
# Quick build script for Android APK

Write-Host "TTS Voice Service - Build Script" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Check if we're in the right directory
if (-not (Test-Path "android\build.gradle")) {
    Write-Host "Error: Must run from TTSVoiceApp directory" -ForegroundColor Red
    Write-Host "Current directory: $PWD" -ForegroundColor Yellow
    exit 1
}

# Menu
Write-Host "Select build type:" -ForegroundColor Yellow
Write-Host "1. Debug APK (for testing)" -ForegroundColor White
Write-Host "2. Release APK (for production)" -ForegroundColor White
Write-Host "3. Clean build" -ForegroundColor White
Write-Host "4. Install debug APK to device" -ForegroundColor White
Write-Host "5. Check connected devices" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Enter choice (1-5)"

switch ($choice) {
    "1" {
        Write-Host "Building Debug APK..." -ForegroundColor Green
        Set-Location android
        .\gradlew.bat assembleDebug
        Set-Location ..
        
        if (Test-Path "android\app\build\outputs\apk\debug\app-debug.apk") {
            Write-Host ""
            Write-Host "Build successful!" -ForegroundColor Green
            Write-Host "APK location: android\app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "To install on device:" -ForegroundColor Yellow
            Write-Host "  adb install android\app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
        } else {
            Write-Host "Build failed!" -ForegroundColor Red
        }
    }
    
    "2" {
        Write-Host "Building Release APK..." -ForegroundColor Green
        Set-Location android
        .\gradlew.bat assembleRelease
        Set-Location ..
        
        if (Test-Path "android\app\build\outputs\apk\release\app-release.apk") {
            Write-Host ""
            Write-Host "Build successful!" -ForegroundColor Green
            Write-Host "APK location: android\app\build\outputs\apk\release\app-release.apk" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "Note: Release APK is signed with debug key" -ForegroundColor Yellow
            Write-Host "For production, configure proper signing in build.gradle" -ForegroundColor Yellow
        } else {
            Write-Host "Build failed!" -ForegroundColor Red
        }
    }
    
    "3" {
        Write-Host "Cleaning build..." -ForegroundColor Green
        Set-Location android
        .\gradlew.bat clean
        Set-Location ..
        Write-Host "Clean complete!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Run this script again to build." -ForegroundColor Yellow
    }
    
    "4" {
        if (Test-Path "android\app\build\outputs\apk\debug\app-debug.apk") {
            Write-Host "Installing Debug APK to device..." -ForegroundColor Green
            adb install -r android\app\build\outputs\apk\debug\app-debug.apk
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Installation successful!" -ForegroundColor Green
            } else {
                Write-Host "Installation failed!" -ForegroundColor Red
                Write-Host "Make sure:" -ForegroundColor Yellow
                Write-Host "  - Device is connected via USB" -ForegroundColor White
                Write-Host "  - USB debugging is enabled" -ForegroundColor White
                Write-Host "  - adb is installed and in PATH" -ForegroundColor White
            }
        } else {
            Write-Host "Debug APK not found!" -ForegroundColor Red
            Write-Host "Build it first (option 1)" -ForegroundColor Yellow
        }
    }
    
    "5" {
        Write-Host "Checking connected devices..." -ForegroundColor Green
        adb devices
        Write-Host ""
        Write-Host "If no devices shown:" -ForegroundColor Yellow
        Write-Host "  1. Connect device via USB" -ForegroundColor White
        Write-Host "  2. Enable USB debugging in Developer Options" -ForegroundColor White
        Write-Host "  3. Accept USB debugging prompt on device" -ForegroundColor White
    }
    
    default {
        Write-Host "Invalid choice!" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
