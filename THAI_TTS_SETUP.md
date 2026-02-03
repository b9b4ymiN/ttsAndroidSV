# 🇹🇭 Thai Language TTS Setup Guide

## ✅ Updated APK with Improved Thai Support

**New APK Location:**
```
C:\Programing\AI2.0\ttsVoice\TTSVoiceApp\android\app\build\outputs\apk\debug\app-debug.apk
```

The app has been updated with:
- ✅ Better Thai locale handling using modern Locale.Builder()
- ✅ Improved language detection and validation
- ✅ Fallback mechanisms for Thai language
- ✅ Detailed error logging
- ✅ Better error messages when Thai TTS is not available

---

## 🔧 Setting Up Thai TTS on Your Android Device

### Step 1: Install Google Text-to-Speech Engine

1. **Open Google Play Store**
2. **Search for:** "Google Text-to-Speech"
3. **Install or Update** the app
   - Publisher: Google LLC
   - Icon: Speaker/sound icon

### Step 2: Download Thai Language Pack

**Method 1: From Settings (Recommended)**
1. Open **Settings** on your Android
2. Go to **System** → **Language & Input**
3. Tap **Text-to-speech output**
4. Tap the ⚙️ (gear icon) next to "Google Text-to-Speech Engine"
5. Tap **"Install voice data"**
6. Find **"ไทย (ประเทศไทย)"** or **"Thai (Thailand)"**
7. Tap the **download icon** to install
8. Wait for download to complete (may take a few minutes)

**Method 2: From Google TTS App**
1. Open **Google Text-to-Speech** app from app drawer
2. Tap **Settings**
3. Tap **Install voice data**
4. Scroll to find **Thai (Thailand)**
5. Download the voice pack

### Step 3: Set Google TTS as Default Engine

1. Go to **Settings** → **System** → **Language & Input**
2. Tap **Text-to-speech output**
3. Select **"Google Text-to-Speech Engine"** as Preferred engine
4. Tap **"Listen to an example"** to test

### Step 4: Test Thai TTS in System

1. Still in **Text-to-speech output** settings
2. Tap **"Listen to an example"**
3. If you hear Thai voice, it's working! ✅
4. If English voice, Thai pack may not be installed correctly

---

## 📱 Reinstall Updated App

### Option 1: Via ADB
```powershell
cd C:\Programing\AI2.0\ttsVoice\TTSVoiceApp\android
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

The `-r` flag reinstalls and keeps data.

### Option 2: Manual Install
1. Uninstall old version first
2. Copy new APK to device
3. Install new APK

---

## 🧪 Testing Thai TTS

### Test 1: From the App

1. **Open TTSVoiceApp**
2. **Start the service**
3. **Test with Thai text:**
   - "สวัสดีครับ"
   - "ทดสอบระบบเสียงภาษาไทย"
   - "หนึ่ง สอง สาม สี่ ห้า"
4. **Tap "Speak Text"**
5. **You should hear Thai speech** 🎉

### Test 2: Check Logs

Connect via USB and check logs:
```powershell
adb logcat | Select-String "TTSForegroundService"
```

**Look for these messages:**

✅ **Success messages:**
```
Available TTS languages: [th_TH, en_US, ...]
Thai language set successfully with result: 0
Language set to Thai successfully (result: 0)
Speaking (Thai): สวัสดีครับ
```

❌ **Error messages (if Thai not working):**
```
Thai language not fully supported: -1
Please install Thai language pack in TTS settings
Error: Thai TTS not available
```

### Test 3: From API (After Thai works in app)

```powershell
# Replace with your device IP
curl -X POST http://192.168.1.100:8765/speak `
  -H "Content-Type: application/json" `
  -d '{\"text\":\"สวัสดีครับ ยินดีต้อนรับ\"}'
```

**Or use Python test client:**
```bash
python test_client.py speak "สวัสดีครับ"
python test_client.py speak "ทดสอบระบบเสียงภาษาไทย"
```

---

## 🔍 Troubleshooting Thai TTS Issues

### Issue 1: App speaks English instead of Thai

**Diagnosis:**
```powershell
adb logcat | Select-String "Language set to"
# Should show: "Language set to Thai successfully"
# If shows: "Language set to English" - Thai pack not installed
```

**Solutions:**
1. ✅ Make sure Google Text-to-Speech is installed
2. ✅ Download Thai language pack (see Step 2 above)
3. ✅ Restart the app after installing Thai pack
4. ✅ Test in system TTS settings first

### Issue 2: "Thai language data is missing" error

**This means:**
- Thai TTS engine is not installed
- Thai voice data is not downloaded

**Fix:**
1. Open **Settings** → **Language & Input** → **Text-to-speech**
2. Tap **Google Text-to-Speech** settings
3. **Install voice data** → Download **Thai (Thailand)**
4. Restart TTSVoiceApp service

### Issue 3: App says "Thai TTS not supported"

**Possible causes:**
- Device doesn't support Thai TTS
- Google TTS app is outdated
- Thai language pack corrupted

**Solutions:**
1. **Update Google TTS:**
   - Play Store → Google Text-to-Speech → Update
2. **Clear TTS data:**
   - Settings → Apps → Google Text-to-Speech
   - Storage → Clear Data
   - Re-download Thai voice pack
3. **Try alternative TTS engine:**
   - Install "Samsung Text-to-Speech" (Samsung devices)
   - Install "Vaja TTS" (alternative Thai TTS)

### Issue 4: Thai characters not detected

**Check if text contains Thai Unicode:**
```kotlin
// Thai Unicode range: U+0E00 to U+0E7F
// Examples: ก-ฮ, เ-์
```

**Test with pure Thai text:**
- ✅ "สวัสดี" (pure Thai)
- ❌ "Hello สวัสดี" (mixed - may use English voice)

For mixed text, send separate requests:
```python
# Send Thai separately
speak("สวัสดี")
# Send English separately  
speak("Hello")
```

### Issue 5: Voice sounds robotic or wrong accent

**Adjust TTS settings:**

In the app's `TTSForegroundService.kt`, you can modify:
```kotlin
tts.setPitch(1.0f)       // Try: 0.8 - 1.2
tts.setSpeechRate(0.9f)  // Try: 0.8 - 1.1 (slower for Thai)
```

Then rebuild:
```powershell
cd android
.\gradlew.bat assembleDebug
```

---

## 📊 Verify Thai TTS Installation

### Check 1: Available Languages
```powershell
adb logcat | Select-String "Available TTS languages"
```

Should show: `[..., th_TH, th, ...]`

### Check 2: Test Thai Voice
```powershell
adb shell "am start -a android.speech.tts.engine.INSTALL_TTS_DATA"
```

This opens TTS voice data installer.

### Check 3: List Installed Engines
```powershell
adb shell settings get secure tts_default_synth
```

Should show: `com.google.android.tts`

---

## 🎯 Expected Behavior

### ✅ When Working Correctly:

1. **English text** → English voice
   ```json
   {"text": "Hello World"}
   ```
   Response: `"Speaking (English): Hello World"`

2. **Thai text** → Thai voice
   ```json
   {"text": "สวัสดีครับ"}
   ```
   Response: `"Speaking (Thai): สวัสดีครับ"`

3. **Auto-detection** works based on Unicode characters

### 📝 Logs When Working:
```
TTSForegroundService: Available TTS languages: [en_US, th_TH, ...]
TTSForegroundService: Thai language set successfully with result: 0
TTSForegroundService: Language set to Thai successfully (result: 0)
TTSForegroundService: Speaking (Thai): สวัสดีครับ
TTSForegroundService: TTS started: abc-123-def
TTSForegroundService: TTS completed: abc-123-def
```

---

## 💡 Tips for Best Results

### 1. Use Standard Thai Text
- ✅ Use proper Thai spelling
- ✅ Include Thai vowels and tone marks
- ❌ Avoid transliteration (e.g., "sawasdee")
- ❌ Avoid English letters mixed in Thai words

### 2. Optimize Speech Settings
For clearer Thai pronunciation:
```kotlin
tts.setPitch(1.0f)          // Standard pitch
tts.setSpeechRate(0.85f)    // Slightly slower for clarity
```

### 3. Handle Long Text
Split long text into sentences:
```python
sentences = [
    "สวัสดีครับ",
    "ยินดีต้อนรับสู่ระบบเสียง",
    "ขอบคุณที่ใช้บริการ"
]

for sentence in sentences:
    speak(sentence)
    time.sleep(2)  # Pause between sentences
```

### 4. Test Different Voices
Some devices have multiple Thai voices:
- Go to TTS settings
- Select voice variant
- Test each one to find best quality

---

## 🚀 Quick Test Commands

After installation:

```powershell
# Test Thai from Windows PowerShell
$deviceIP = "192.168.1.100"  # Your device IP

# Test 1: Simple greeting
curl -X POST http://${deviceIP}:8765/speak -H "Content-Type: application/json" -d '{\"text\":\"สวัสดีครับ\"}'

# Test 2: Longer sentence  
curl -X POST http://${deviceIP}:8765/speak -H "Content-Type: application/json" -d '{\"text\":\"ยินดีต้อนรับสู่ระบบแปลงข้อความเป็นเสียง\"}'

# Test 3: Numbers
curl -X POST http://${deviceIP}:8765/speak -H "Content-Type: application/json" -d '{\"text\":\"หนึ่ง สอง สาม สี่ ห้า\"}'

# Check status
curl http://${deviceIP}:8765/status
```

---

## 📞 Still Not Working?

If Thai TTS still doesn't work after following all steps:

1. **Check device compatibility:**
   - Some old Android versions may not support Thai TTS
   - Minimum recommended: Android 6.0+

2. **Try alternative TTS engine:**
   - Install "Vaja TTS" from Play Store (Thai-specific)
   - Install "eSpeak TTS" (supports 80+ languages)

3. **Enable detailed logging:**
   ```powershell
   adb logcat -s TTSForegroundService:V TextToSpeech:V
   ```

4. **Contact for support:**
   - Share logcat output
   - Mention device model and Android version
   - Describe exact error message

---

## ✅ Success Checklist

Before considering setup complete:

- [ ] Google Text-to-Speech installed
- [ ] Thai language pack downloaded
- [ ] "Listen to example" plays Thai voice
- [ ] Updated APK installed
- [ ] Service started in app
- [ ] Thai text spoken correctly in app
- [ ] API responds to Thai text
- [ ] Logs show "Speaking (Thai)"

---

**Happy speaking in Thai! 🇹🇭🎉**
