# 🚀 Quick Start Guide - TTS Voice Service

## ⚡ Fast Track to APK Installation

### Prerequisites Check
- ✅ Node.js installed (v18+)
- ✅ Java JDK installed (17+)
- ✅ Android device with USB debugging enabled
- ✅ USB cable ready

### Step 1: Build the APK (5 minutes)

```powershell
# Navigate to project directory
cd C:\Programing\AI2.0\ttsVoice\TTSVoiceApp

# Build debug APK using the build script
.\build.ps1
# Choose option 1 (Debug APK)
```

**Or manually:**
```powershell
cd android
.\gradlew.bat assembleDebug
```

### Step 2: Install on Android Device

**Method A - Via USB:**
```powershell
# Connect device via USB, enable USB debugging
adb install android\app\build\outputs\apk\debug\app-debug.apk
```

**Method B - Manual Transfer:**
1. Copy `android\app\build\outputs\apk\debug\app-debug.apk` to your device
2. Open file on device and install
3. Enable "Install from Unknown Sources" if prompted

### Step 3: Configure Device (2 minutes)

1. **Install Thai TTS Engine** (if not already installed)
   - Open Google Play Store
   - Search "Google Text-to-Speech"
   - Install/Update

2. **Grant Permissions**
   - Open app settings
   - Grant all permissions (Storage, Notifications, etc.)

3. **Disable Battery Optimization**
   - Settings → Battery → App → Unrestricted
   - This ensures service keeps running

### Step 4: Start Using! 🎉

1. **Launch the App**
   - Open "TTSVoiceApp" from app drawer
   - Tap "▶️ Start Service"
   - Note your device IP address (shown in app)

2. **Test from App**
   - Enter text: "สวัสดีครับ" or "Hello World"
   - Tap "🔊 Speak Text"
   - Listen to speech output

3. **Test from Another Device**
   ```bash
   # From computer on same Wi-Fi network
   curl -X POST http://192.168.1.100:8765/speak \
     -H "Content-Type: application/json" \
     -d '{"text":"สวัสดีครับ"}'
   ```

   **Or use Python test client:**
   ```bash
   # Edit DEVICE_IP in test_client.py first
   python test_client.py speak "สวัสดีครับ"
   python test_client.py status
   ```

## 🔧 Quick Configuration

### Find Your Device IP Address
- **Settings → About → Status → IP Address**
- Or check in the app UI (shown at top)

### Change HTTP Port (Optional)
Edit `android/app/src/main/java/com/ttsvoiceapp/TTSForegroundService.kt`:
```kotlin
private const val HTTP_PORT = 8765  // Change to your desired port
```

### Enable Auto-Start on Boot
✅ Already configured! Service starts automatically when device boots.

To verify:
1. Reboot your device
2. Check if service is running (check notification)
3. Send test request to API

## 📡 API Quick Reference

### Base URL
```
http://<YOUR_DEVICE_IP>:8765
```

### Endpoints

**Speak Text:**
```bash
POST /speak
Body: {"text": "สวัสดีครับ"}
```

**Get Status:**
```bash
GET /status
```

**Health Check:**
```bash
GET /health
```

## 🐛 Quick Troubleshooting

### Problem: App crashes on start
**Solution:** 
- Check logs: `adb logcat | grep TTS`
- Reinstall: `adb uninstall com.ttsvoiceapp`
- Build again and reinstall

### Problem: Can't connect to API from computer
**Solution:**
1. Verify service is running (green status in app)
2. Both devices on same Wi-Fi?
3. Check device IP: Settings → About → Status
4. Test: `curl http://<IP>:8765/health`

### Problem: No Thai voice / speaks in wrong language
**Solution:**
1. Install Google Text-to-Speech from Play Store
2. Settings → Language & Input → Text-to-speech
3. Download Thai language pack
4. Test: Settings → Accessibility → TTS output → Listen to example

### Problem: Service stops after device sleep
**Solution:**
1. Settings → Battery → App → Unrestricted
2. Settings → Apps → Special access → Battery optimization → Not optimized
3. Some devices: Settings → Developer Options → Don't kill background processes

### Problem: Auto-start not working after reboot
**Solution:**
1. Check battery optimization (above)
2. Some brands (Xiaomi, Oppo, etc.) require additional settings:
   - Settings → Permissions → Autostart → Enable for this app
3. Grant "Display over other apps" permission

## 📱 Usage Examples

### Python Integration
```python
import requests

def speak_thai(text):
    requests.post('http://192.168.1.100:8765/speak', 
                  json={'text': text})

speak_thai('สวัสดีครับ')
speak_thai('ยินดีต้อนรับ')
```

### Node.js Integration
```javascript
const axios = require('axios');

async function speak(text) {
  await axios.post('http://192.168.1.100:8765/speak', 
                   { text });
}

speak('สวัสดีครับ');
```

### Home Assistant Integration
```yaml
# configuration.yaml
rest_command:
  tts_speak:
    url: http://192.168.1.100:8765/speak
    method: POST
    content_type: 'application/json'
    payload: '{"text":"{{ text }}"}'

# Call from automation:
# service: rest_command.tts_speak
# data:
#   text: "สวัสดีครับ"
```

## 🎯 Next Steps

1. **Set Static IP** on your Android device
   - Prevents IP from changing
   - Settings → Wi-Fi → Advanced → Static IP

2. **Create Desktop Shortcut** for quick testing
   - Create `.bat` file with curl command
   - Or use Python test_client.py

3. **Integrate with Your Apps**
   - Use the API from any programming language
   - No authentication needed (local network only)

4. **Monitor Logs** for debugging
   ```bash
   adb logcat | grep "TTSForegroundService\|TTSServiceModule"
   ```

## ⚠️ Important Notes

- **No Authentication**: Anyone on your Wi-Fi can use the service
- **Local Network Only**: Not exposed to internet
- **Battery Usage**: Foreground service uses minimal battery
- **Auto-Start**: Enabled by default, starts on device boot

## 📚 Full Documentation

See [README.md](README.md) for complete documentation including:
- Architecture details
- Advanced configuration
- Security considerations
- Troubleshooting guide
- API reference

---

**Ready to build?** Run `.\build.ps1` and follow the prompts! 🚀
