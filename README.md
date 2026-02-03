# TTS Voice Service - Android REST API App

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Platform](https://img.shields.io/badge/platform-Android-green)
![License](https://img.shields.io/badge/license-MIT-orange)
![Build](https://img.shields.io/badge/build-passing-brightgreen)

## 📱 Overview

TTS Voice Service is a professional Android application that runs a REST API server for Text-to-Speech functionality. It converts Thai and English text to speech with full UTF-8 support and plays it through the device speaker. The service runs as a persistent foreground service with auto-start capabilities on device boot, app launch, and package updates.

## ✨ Features
 with full UTF-8 encoding
- 🌐 **REST API Server**: Embedded NanoHTTPD server on port 8765 (accessible on 0.0.0.0)
- 🔄 **Smart Auto-Start**: Automatically starts on:
  - Device boot (BOOT_COMPLETED)
  - App launch (first open)
  - Package update (MY_PACKAGE_REPLACED)
  - Service restart after kill (START_STICKY)
- 📱 **Foreground Service**: Persistent background operation with WakeLock
- 📲 **Enhanced Notification**: Shows device IP, queue size, speaking status, and stop button
- 🔊 **Queue Management**: Handles multiple speech requests with intelligent processing
- 🌍 **Network Access**: Access from any device on local network (WiFi)
- 💻 **Professional UI**: React Native interface with live status monitoring
- 🎨 **Custom Icon**: Professional minimal design with microphone and sound waves
- 🔤 **Language Detection**: Automatic Thai/English detection using Unicode ranges
- 🛡️ **Error Handling**: Comprehensive error handling and fallback mechanismsork
- 💻 **React Native UI**: Control panel and testing interface

## 🏗️ Architecture

```
┌─────────────────────────────────────┐
│     React Native UI (Optional)      │
│   - Service Controls                │
│   - Status Monitor                  │
│   - Test Interface                  │
└─────────────┬───────────────────────┘
              │
┌─────────────▼───────────────────────┐
│   Android Foreground Service        │
│   ┌─────────────────────────────┐   │
│   │  NanoHTTPD Server (8765)    │   │
│   │  - POST /speak              │   │
│   │  - GET /status              │   │
│   │  - GET /health              │   │
│   └─────────────┬───────────────┘   │
│                 │                   │
│   ┌─────────────▼───────────────┐   │
│   │  TTS Queue Manager          │   │
│   │  - Language Detection       │   │
│   │  - Queue Processing         │   │
│   └─────────────┬───────────────┘   │
│                 │                   │
│   ┌─────────────▼───────────────┐   │
│   │  Android TTS Engine         │   │
│   │  - Thai Language            │   │
│   │  - English Language         │   │
│   └─────────────────────────────┘   │
└─────────────────────────────────────┘
              │
┌─────────────▼───────────────────────┐
│     Boot Receiver                   │
│   - Auto-start on BOOT_COMPLETED    │
└─────────────────────────────────────┘
```

## 📋 Requirements

- **Node.js**: v18 or higher
- **npm**: v8 or higher
- **Java JDK**: 17 or higher
- **Android SDK**: API Level 33 (Android 13)
- **Android Studio**: Latest version (optional, for debugging)

## 🚀 Installation & Build
Generate Icons (Optional - already included)

```bash
# Requires Python with PIL/Pillow
pip install pillow
python generate_icons.py
```

### 3. Bundle JavaScript

```bash
npx react-native bundle --platform android --dev false \
  --entry-file index.js \
  --bundle-output android/app/src/main/assets/index.android.bundle \
  --assets-dest android/app/src/main/res
```

### 4. Build Debug APK

```bash
# Windows
cd android
.\gradlew.bat assembleDebug --no-daemon

# Li5. Build Release APK (Production)

```bash
# Windows
cd android
.\gradlew.bat assembleRelease

# Linux/Mac
cd android
./gradlew assembleRelease

# APK location: android/app/build/outputs/apk/release/app-release.apk
```

### 6

### 3. Build Release APK

```bash
# Windows
cd android
gradlew.bat assembleRelease

# Linux/Mac
cd android
./gradlew assembleRelease

# APK location: android/app/build/outputs/apk/release/app-release.apk
```

### 4. Install on Device
 Supports full UTF-8 encoding for Thai and English text.

**Request:**
```json
{
  "text": "สวัสดีครับ ทดสอบภาษาไทย"
}
```

**Response:**
```json
{
  "status": "queued",
  "text": "สวัสดีครับ ทดสอบภาษาไทย",
  "queueSize": 1,
  "message": "Text added to speech queue"
}
```

**Important:** Use `Content-Type: application/json; charset=utf-8` for Thai text.

**Example using curl:**
```bash
# Thai text
curl -X POST http://192.168.1.100:8765/speak \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"text":"สวัสดีครับ ยินดีต้อนรับ"}'

# English text
curl -X POST http://192.168.1.100:8765/speak \
  -H "Content-Type: application/json" \
  -d '{"text":"Hello world, welcome to TTS service

**Response:**
```json
{
  "status": "queued",
  "text": "สวัสดีครับ",
  "queueSize": 1,
  "message": "Text added to speech queue"
}
```

**Example using curl:**
```bash
curl -X POST http://192.168.1.100:8765/speak \
  -H "Content-Type: application/json" \
  -d '{"text":"สวัสดีครับ"}'
```

**Example using Python:**
```python
import requests

response = requests.post(
    'http://192.168.1.100:8765/speak',
    json={'text': 'สวัสดีครับ'}
)
print(response.json())
```

**Example using JavaScript:**
```javascript
fetch('http://192.168.1.100:8765/speak', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({text: 'สวัสดีครับ'})
})
.then(res => res.json())
.then(data => console.log(data));
```

### GET /status
Get service status and TTS engine information.

**Response:**
```json
{
  "service": "running",
  "port": 8765,
  "ttsInitialized": true,
  "isSpeaking": false,
  "queueSize": 0,
  "lastStatus": "Running on port 8765"
}
```

### GET /health
Simple health check endpoint.

**Response:**
```json
{
  "status": "healthy"
}
```

## 🔧 Configuration
    private const val CHANNEL_ID = "tts_service_channel"
    private const val NOTIFICATION_ID = 1
}
```

### Adjust TTS Settings

Edit the TTS configuration in `TTSForegroundService.kt`:

```kotlin
override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
        tts.setPitch(1.0f)       // Voice pitch (0.5 - 2.0)
        tts.setSpeechRate(1.0f)  // Speech rate (0.5 - 2.0)
        
        // Set Thai locale with modern API
        val thaiLocale = Locale.Builder()
            .setLanguage("th")
            .setRegion("TH")
            .build()
        tts.language = thaiLocale
    }
}
```

### Customize Notification

Edit notification appearance in `createNotification()`:

```kotlin
private fun createNotification(contentText: String): Notification {
    // Customize notification title, color, icon, etc.
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("TTS Voice Service")
        .setSmallIcon(android.R.drawable.ic_btn_speak_now)
        .setColor(0xFF4169E1.toInt())  // Royal Blue
        // ... more customization
}
```

### Modify Icon

Icon files are located in:
- Source SVG: `assets/icon.svg`
- PNG icons: `android/app/src/main/res/mipmap-*/ic_launcher.png`

To regenerate icons:
```bash
python generate_icons.py

### Adjust TTS Settings

Edit the TTS configuration in `TTSForegroundService.kt`:

```kotlin
tts**Install APK** - Service starts automatically on first launch
2. **Check Status** - View device IP address and service status in the app
3. **Check Permissions**: Settings → Apps → TTS Voice Service → Permissions
2. **View Logs**: `adb logcat | grep TTSForegroundService`
3. **Manual Start**: Open app and tap "▶️ Start Service"
4. **Check Notification**: Service should show persistent notification when running
5. **Battery Optimization**: Disable for app (Settings → Battery → App → Unrestricted)

### TTS Not Working

1. **Install TTS Engine**:
   - Open Google Play Store
   - Install "Google Text-to-Speech"
   - Go to Settings → Language & Input → Text-to-speech output
   - Select "Google Text-to-Speech Engine"

2. **Install Thai Language Pack**:
   - Open Google Text-to-Speech settings
   - Tap "Install voice data"
   - Download Thai (ไทย) language pack
   - Test: Settings → Accessibility → Text-to-speech output → "Listen to an example"

3. **Check Language Support**:
   - View logs in app or check logcat for "Available TTS languages"
   - Thai should show as `th_TH` or `th`

### Thai Text Shows as "????" (UTF-8 Issues)

**✅ Fixed in v1.0.0** - The app now properly handles UTF-8 encoding

If you still see issues:
1. Ensure you're using `Content-Type: application/json; charset=utf-8`
2. Check your HTTP client supports UTF-8
3. Test with included `test_client.py` script
4. Verify request body in logs: `adb logcat | grep "Request body"`

### Can't Connect to API from External Device
All permissions are declared in `AndroidManifest.xml` and automatically granted during installation:

- `INTERNET` - HTTP server communication and network access
- `FOREGROUND_SERVICE` - Background service operation
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` - Audio playback classification
- `RECEIVE_BOOT_COMPLETED` - Auto-start on device boot
- `WAKE_LOCK` - Keep device awake during speech playback
- `POST_NOTIFICATIONS` - Display persistent notification (Android 13+)

**User Action Required:**
- Disable battery optimization for reliable auto-start
- Allow background activity on some manufacturers
2. **Get Correct IP**:
   - Pull down notification - shows `http://192.168.x.x:8765`
   - Or check app status card
   - Must be same WiFi network

3. **Test Connection**:
   ```bash
   # Health check
   curl http://192.168.1.100:8765/health
   
   # Status check
   curl http://192.168.1.100:8765/status
   ```

4. **Firewall Issues**:
   - Some Android devices have built-in firewall
   - Check: Settings → Security → Firewall
   - Allow incoming connections on port 8765

5. **Network Binding**: 
   - SImplemented Features (v1.0.0)

- ✅ Full UTF-8 encoding support for Thai text
- ✅ Auto-start on boot, launch, and update
- ✅ Enhanced notification with IP display and stop button
- ✅ Professional custom icon (minimal design)
- ✅ Network-accessible HTTP server (0.0.0.0 binding)
- ✅ Persistent foreground service with WakeLock
- ✅ Automatic language detection (Thai/English)
- ✅ Queue management system
- ✅ React Native UI with live monitoring
- ✅ Comprehensive error handling

## 🚀 Future Enhancements

- [ ] Authentication/API keys for security
- [ ] Support for more languages (Chinese, Japanese, Korean, etc.)
- [ ] Dynamic voice configuration API (pitch, rate, volume control)
- [ ] Audio file export (WAV/MP3)
- [ ] HTTPS support with SSL certificates
- [ ] WebSocket support for real-time updates
- [ ] Multiple voice profiles/personalities
- [ ] Text preprocessing and SSML support
- [ ] Voice effects (echo, reverb, speed change)
- [ ] Audio streaming instead of local playback
- [ ] REST API for voice list and selection
- [ ] Batch processing of multiple texts
- [ ] Scheduled speech tasks
- [ ] Integration with other apps via Intent

## 📊 Technical Specifications

- **React Native**: 0.83.1
- **Kotlin**: Android native
- **NanoHTTPD**: 2.3.1
- **Gradle**: 9.0.0
- **Android SDK**: API 33 (Android 13)
- **JDK**: 17+
- **Node.js**: 18+
- **APK Size**: ~104 MB (debug build)
- **Min Android Version**: API 21 (Android 5.0)
- **Target Android Version**: API 33 (Android 13)

## 📁 Project Structure

```
TTSVoiceApp/
├── android/                          # Android native code
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/ttsvoiceapp/
│   │   │   │   ├── TTSForegroundService.kt    # Main service
│   │   │   │   ├── BootReceiver.kt            # Boot auto-start
│   │   │   │   ├── PackageReplacedReceiver.kt # Update auto-start
│   │   │   │   ├── TTSServiceModule.kt        # RN bridge
│   │   │   │   ├── TTSServicePackage.kt       # RN package
│   │   │   │   ├── MainActivity.kt            # Main activity
│   │   │   │   └── MainApplication.kt         # App entry
│   │   │   ├── res/
│   │   │   │   ├── mipmap-*/                  # App icons (PNG)
│   │   │   │   ├── drawable/                  # Vector drawables
│   │   │   │   └── values/                    # Colors, strings
│   │   │   └── AndroidManifest.xml            # App configuration
│   │   └── build.gradle                       # App dependencies
│   └── build.gradle                           # Project config
├── assets/
│   └── icon.svg                               # Source icon (SVG)
├── App.tsx                                    # React Native UI
├── index.js                                   # RN entry point
├── generate_icons.py                          # Icon generator
├── test_client.py                             # API test script
├── package.json                               # Node dependencies
└── README.md                                  # This file
```

## 🤝 Contributing

Contributions are welcome! Here's how:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to branch**: `git push origin feature/amazing-feature`
5. **Open Pull Request**

### Development Setup

```bash
# Clone repository
git clone https://github.com/yourusername/TTSVoiceApp.git
cd TTSVoiceApp

# Install dependencies
npm install

# Run on Android device
npx react-native run-android

# View logs
npx react-native log-android
```

## 📞 Support

For issues and questions:

1. **Check Documentation**: Read through this README and troubleshooting section
2. **Review Logs**: `adb logcat | grep TTS`
3. **Test API**: Use included `test_client.py` script
4. **GitHub Issues**: Report bugs or request features
5. **Android TTS Docs**: [Android Text-to-Speech Documentation](https://developer.android.com/reference/android/speech/tts/TextToSpeech)

## 📄 License

MIT License - Feel free to use, modify, and distribute

Copyright (c) 2026 TTS Voice Service

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software.

## 🙏 Acknowledgments

- **NanoHTTPD** - Lightweight HTTP server library
- **React Native** - Cross-platform mobile framework
- **Android TTS Engine** - Built-in text-to-speech functionality
- **Google Text-to-Speech** - High-quality voice synthesis

## 📝 Changelog

### v1.0.0 (2026-02-03)
- ✅ Initial release
- ✅ Full UTF-8 encoding support for Thai language
- ✅ Auto-start on boot, launch, and package update
- ✅ Enhanced notification with IP address display
- ✅ Professional custom icon with minimal design
- ✅ Network-accessible server (0.0.0.0 binding)
- ✅ Persistent foreground service
- ✅ Automatic Thai/English language detection
- ✅ Queue management system
- ✅ React Native UI with live status
- ✅ Comprehensive documentation

---

**Made with ❤️ for Thai & English Text-to-Speech**

*Supporting seamless multilingual voice synthesis on Android*Service → Battery → Allow background activity
   - **Oppo/Realme**: Settings → Battery → App Battery Management → TTS Voice Service → Disable

3. **Boot Receiver**:
   - Check if RECEIVE_BOOT_COMPLETED permission is granted
   - View logs after reboot: `adb logcat | grep BootReceiver`

4. **Test Auto-Start**:
   - After installation, app auto-starts service
   - Reboot device and check notification appears
   - Update app and service should restart

### Icon Not Showing

1. **Uninstall Old Version**:
   ```bash
   adb uninstall com.ttsvoiceapp
   ```

2. **Install Fresh**:
   ```bash
   adb install android/app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Clear Launcher Cache**:
   - Settings → Apps → Launcher → Storage → Clear Cache
   - Or restart device

4. **Check Icon Files**:
   - Verify PNG files exist in `android/app/src/main/res/mipmap-*/`
   - Regenerate if needed: `python generate_icons.py`
            json={"text": text},
            headers={"Content-Type": "application/json; charset=utf-8"}
        )
        if response.ok:
            result = response.json()
            print(f"✅ Success: {result}")
            print(f"   Queue Size: {result.get('queueSize', 0)}")
        else:
            print(f"❌ Error: {response.json()}")
    except Exception as e:
        print(f"❌ Connection error: {e}")
        print(f"   Make sure device is on same WiFi network")
        print(f"   Check if service is running")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python test_client.py 'text to speak'")
        print("Example: python test_client.py 'สวัสดีครับ'")
    else:
        speak(" ".join(sys.argv[1:]))
```

**Run the test client:**
```bash
# Thai text
python test_client.py "สวัสดีครับ ยินดีต้อนรับ"

# English text
python test_client.py "Hello world"same Wi-Fi network
3. Send POST request to `http://<device-ip>:8765/speak`

**Example Python Script:**
```python
#!/usr/bin/env python3
import requests
import sys

DEVICE_IP = "192.168.1.100"  # Change to your device IP
API_URL = f"http://{DEVICE_IP}:8765/speak"

def speak(text):
    try:
        response = requests.post(API_URL, json={"text": text})
        if response.ok:
            print(f"✅ Success: {response.json()}")
        else:
            print(f"❌ Error: {response.json()}")
    except Exception as e:
        print(f"❌ Connection error: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python speak.py 'text to speak'")
    else:
        speak(" ".join(sys.argv[1:]))
```

## 🐛 Troubleshooting

### Service Not Starting

1. Check if app has required permissions (Settings → Apps → TTS Voice Service → Permissions)
2. Check logs: `adb logcat | grep TTSForegroundService`
3. Try manual start from the app UI

### TTS Not Working

1. Ensure Thai language pack is installed (Settings → Language & Input → Text-to-speech)
2. Test TTS: Settings → Accessibility → Text-to-speech output → Listen to an example
3. Install Google Text-to-Speech from Play Store

### Can't Connect to API

1. Verify service is running (check app UI)
2. Confirm device IP address
3. Check if both devices are on same Wi-Fi network
4. Test with: `curl http://<device-ip>:8765/health`
5. Check firewall settings

### Auto-Start Not Working

1. Check battery optimization (Settings → Battery → App → Unrestricted)
2. Some manufacturers block auto-start (check device settings)
3. Grant "Display over other apps" permission
4. Test manually: Reboot device and check if service starts

## 📱 Permissions Required

- `INTERNET` - HTTP server communication
- `FOREGROUND_SERVICE` - Background service operation
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` - Audio playback
- `RECEIVE_BOOT_COMPLETED` - Auto-start on boot
- `WAKE_LOCK` - Keep service running
- `POST_NOTIFICATIONS` - Foreground service notification

## 🔒 Security Considerations

⚠️ **Important**: This app has no built-in authentication. Anyone on your local network can send text to be spoken.

**Recommendations:**
- Use only on trusted networks
- Don't expose to public networks
- Consider adding API key authentication for production use
- Monitor logs for suspicious activity

## 📄 License

MIT License - Feel free to use and modify

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

For issues and questions:
- Check the Troubleshooting section
- Review logs: `adb logcat | grep TTS`
- Check Android documentation for TTS issues

## 🎯 Future Enhancements

- [ ] Add authentication/API keys
- [ ] Support more languages
- [ ] Voice configuration (pitch, rate, volume)
- [ ] Audio file export
- [ ] HTTPS support
- [ ] WebSocket support for real-time updates
- [ ] Multiple voice profiles
- [ ] Text preprocessing and SSML support

---

Made with ❤️ for Thai & English Text-to-Speech
