# 🔧 Development Environment Setup

## Prerequisites Installation Guide

This guide will help you set up your development environment to build the Android APK.

## Step 1: Install Java JDK 17+

### Option A: Using Chocolatey (Recommended for Windows)
```powershell
# Install Chocolatey first if not installed
# See https://chocolatey.org/install

# Install OpenJDK 17
choco install openjdk17
```

### Option B: Manual Installation
1. Download Java JDK from: https://adoptium.net/temurin/releases/
2. Select Version: 17 (LTS)
3. Install and add to PATH

### Verify Installation
```powershell
java -version
# Should show: openjdk version "17.x.x"
```

## Step 2: Install Node.js 18+

### Option A: Using Official Installer
1. Download from: https://nodejs.org/
2. Install LTS version (18.x or higher)
3. Follow installation wizard

### Option B: Using Chocolatey
```powershell
choco install nodejs-lts
```

### Verify Installation
```powershell
node --version
npm --version
```

## Step 3: Install Android SDK

### Option A: Install Android Studio (Recommended)

1. **Download Android Studio**
   - Visit: https://developer.android.com/studio
   - Download latest version for Windows
   - Run installer

2. **Initial Setup**
   - Launch Android Studio
   - Select "Standard" installation
   - Wait for SDK components to download
   - Default location: `C:\Users\<YourUsername>\AppData\Local\Android\Sdk`

3. **Configure SDK Components**
   - Open Android Studio
   - Go to: Tools → SDK Manager
   - Install:
     - ✅ Android SDK Platform 33 (Android 13)
     - ✅ Android SDK Build-Tools 33.x.x
     - ✅ Android SDK Command-line Tools
     - ✅ Android Emulator (optional)

### Option B: Command-line Tools Only (Advanced)

1. **Download SDK Command-line Tools**
   - Visit: https://developer.android.com/studio#command-tools
   - Download "Command line tools only" for Windows

2. **Extract and Setup**
   ```powershell
   # Create SDK directory
   New-Item -ItemType Directory -Path "C:\Android\sdk"
   
   # Extract downloaded zip to C:\Android\sdk\cmdline-tools\latest
   
   # Set environment variable
   [Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Android\sdk", "User")
   [Environment]::SetEnvironmentVariable("ANDROID_SDK_ROOT", "C:\Android\sdk", "User")
   ```

3. **Install SDK Packages**
   ```powershell
   cd C:\Android\sdk\cmdline-tools\latest\bin
   .\sdkmanager.bat "platform-tools" "platforms;android-33" "build-tools;33.0.0"
   ```

### Step 4: Set Environment Variables

**Windows (PowerShell as Administrator):**
```powershell
# Set ANDROID_HOME
[Environment]::SetEnvironmentVariable(
    "ANDROID_HOME", 
    "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk", 
    "User"
)

# Set ANDROID_SDK_ROOT
[Environment]::SetEnvironmentVariable(
    "ANDROID_SDK_ROOT", 
    "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk", 
    "User"
)

# Add to PATH
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
$androidPlatformTools = "$env:ANDROID_HOME\platform-tools"
$androidCmdlineTools = "$env:ANDROID_HOME\cmdline-tools\latest\bin"

if ($currentPath -notlike "*$androidPlatformTools*") {
    [Environment]::SetEnvironmentVariable(
        "Path", 
        "$currentPath;$androidPlatformTools;$androidCmdlineTools", 
        "User"
    )
}

# Restart PowerShell for changes to take effect
```

**Verify:**
```powershell
echo $env:ANDROID_HOME
# Should output: C:\Users\<YourUsername>\AppData\Local\Android\Sdk

adb --version
# Should show adb version
```

### Step 5: Create local.properties (Automatic)

After setting ANDROID_HOME, create this file in the android folder:

**File: `android/local.properties`**
```properties
sdk.dir=C:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
```

Or let the build script create it automatically:
```powershell
cd C:\Programing\AI2.0\ttsVoice\TTSVoiceApp

# This will create local.properties with correct SDK path
npx react-native doctor
```

## Step 6: Install Project Dependencies

```powershell
cd C:\Programing\AI2.0\ttsVoice\TTSVoiceApp

# Install npm dependencies
npm install
```

## Step 7: Build the APK

```powershell
# Using build script
.\build.ps1

# Or manually
cd android
.\gradlew.bat assembleDebug
```

## 🎯 Quick Verification Checklist

Run these commands to verify your setup:

```powershell
# Java
java -version
# ✅ Should show version 17 or higher

# Node.js
node --version
# ✅ Should show v18.x.x or higher

# npm
npm --version
# ✅ Should show 8.x.x or higher

# Android SDK
echo $env:ANDROID_HOME
# ✅ Should show SDK path

# ADB (Android Debug Bridge)
adb --version
# ✅ Should show adb version

# Gradle (via project)
cd android
.\gradlew.bat --version
# ✅ Should show Gradle version
```

## 🐛 Common Issues

### Issue: "ANDROID_HOME is not set"

**Solution:**
```powershell
# Set manually for current session
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"

# Or create local.properties file
cd C:\Programing\AI2.0\ttsVoice\TTSVoiceApp\android
echo "sdk.dir=C:\\Users\\$env:USERNAME\\AppData\\Local\\Android\\Sdk" > local.properties
```

### Issue: "SDK location not found"

**Solution:**
1. Install Android Studio
2. Open SDK Manager and install Platform 33
3. Set ANDROID_HOME environment variable
4. Restart terminal/PowerShell

### Issue: "Could not find or load main class org.gradle.wrapper.GradleWrapperMain"

**Solution:**
```powershell
cd android
# Re-download gradle wrapper
.\gradlew.bat wrapper --gradle-version=8.3
```

### Issue: Java version incompatible

**Solution:**
```powershell
# Check Java version
java -version

# If not 17+, install correct version
choco install openjdk17

# Or set JAVA_HOME to correct version
$env:JAVA_HOME = "C:\Program Files\OpenJDK\jdk-17.x.x"
```

## 🚀 Alternative: Cloud Build (No Local Setup)

If you prefer not to install Android development tools:

### Option 1: Use GitHub Actions (CI/CD)
- Push code to GitHub
- Set up GitHub Actions workflow
- Download built APK from Actions artifacts

### Option 2: Use Online Build Services
- **EAS Build** (Expo Application Services)
- **AppCenter** (Microsoft)
- **Bitrise**

### Option 3: Use Pre-built APK
If someone else has built environment:
- Ask them to build and share the APK
- You can install directly on device

## 📱 Device Setup (No SDK Required)

If you just want to **install and use** the app (not build it):

1. Get the APK file (from someone who built it)
2. Transfer to Android device
3. Enable "Install from Unknown Sources"
4. Tap APK to install
5. Done! ✅

## Next Steps

Once environment is set up:
1. ✅ Build APK: `.\build.ps1`
2. ✅ Install on device: `adb install android\app\build\outputs\apk\debug\app-debug.apk`
3. ✅ Follow [QUICKSTART.md](QUICKSTART.md) for usage

---

**Need Help?**
- Android Studio Setup: https://developer.android.com/studio/install
- React Native Environment: https://reactnative.dev/docs/environment-setup
- Node.js: https://nodejs.org/
