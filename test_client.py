#!/usr/bin/env python3
"""
TTS Voice Service - Example Client Script
Send text to Android device for Text-to-Speech playback
"""

import requests
import sys
import json

# Configuration - Change these values
DEVICE_IP = "192.168.1.100"  # Your Android device IP address
API_PORT = 8765
API_BASE_URL = f"http://{DEVICE_IP}:{API_PORT}"

def speak(text, speed=1.0, pitch=1.0):
    """Send text to TTS service with optional speed and pitch control"""
    url = f"{API_BASE_URL}/speak"
    payload = {
        "text": text,
        "speed": speed,
        "pitch": pitch
    }
    
    try:
        print(f"📤 Sending: {text}")
        if speed != 1.0 or pitch != 1.0:
            print(f"   Speed: {speed}x, Pitch: {pitch}")
        response = requests.post(url, json=payload, timeout=5)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Success!")
            print(f"   Status: {result.get('status')}")
            print(f"   Speed: {result.get('speed')}x")
            print(f"   Pitch: {result.get('pitch')}")
            print(f"   Queue Size: {result.get('queueSize')}")
            print(f"   Message: {result.get('message')}")
        else:
            error = response.json()
            print(f"❌ Error: {error.get('error')}")
            
    except requests.exceptions.ConnectionError:
        print(f"❌ Connection Error: Cannot reach {API_BASE_URL}")
        print(f"   Make sure:")
        print(f"   1. Service is running on Android device")
        print(f"   2. Both devices are on same Wi-Fi network")
        print(f"   3. IP address ({DEVICE_IP}) is correct")
    except requests.exceptions.Timeout:
        print(f"❌ Timeout: Service didn't respond in time")
    except Exception as e:
        print(f"❌ Error: {e}")

def get_status():
    """Get service status"""
    url = f"{API_BASE_URL}/status"
    
    try:
        response = requests.get(url, timeout=5)
        if response.status_code == 200:
            status = response.json()
            print("📊 Service Status:")
            print(f"   Service: {status.get('service')}")
            print(f"   Port: {status.get('port')}")
            print(f"   TTS Initialized: {status.get('ttsInitialized')}")
            print(f"   Currently Speaking: {status.get('isSpeaking')}")
            print(f"   Queue Size: {status.get('queueSize')}")
            print(f"   Last Status: {status.get('lastStatus')}")
        else:
            print(f"❌ Status check failed: {response.status_code}")
    except Exception as e:
        print(f"❌ Error getting status: {e}")

def health_check():
    """Check if service is healthy"""
    url = f"{API_BASE_URL}/health"
    
    try:
        response = requests.get(url, timeout=5)
        if response.status_code == 200:
            print("💚 Service is healthy and responding")
        else:
            print(f"❌ Health check failed: {response.status_code}")
    except Exception as e:
        print(f"❌ Health check error: {e}")

def print_usage():
    """Print usage instructions"""
    print("TTS Voice Service - Client Script")
    print()
    print("Usage:")
    print(f"  python {sys.argv[0]} speak <text> [speed] [pitch]  - Send text to speak")
    print(f"  python {sys.argv[0]} status                         - Get service status")
    print(f"  python {sys.argv[0]} health                         - Health check")
    print()
    print("Parameters:")
    print("  speed: Speech speed (0.5 - 2.0), default: 1.0")
    print("         0.5 = very slow, 1.0 = normal, 1.5 = fast, 2.0 = very fast")
    print("  pitch: Voice pitch (0.5 - 2.0), default: 1.0")
    print("         0.5 = very low, 1.0 = normal, 1.5 = high, 2.0 = very high")
    print()
    print("Examples:")
    print(f"  python {sys.argv[0]} speak \"สวัสดีครับ\"")
    print(f"  python {sys.argv[0]} speak \"พูดช้า\" 0.7")
    print(f"  python {sys.argv[0]} speak \"พูดเร็ว\" 1.5")
    print(f"  python {sys.argv[0]} speak \"Hello\" 1.0 1.2")
    print(f"  python {sys.argv[0]} status")
    print()
    print(f"Configuration: {API_BASE_URL}")
    print(f"Edit DEVICE_IP in this script to match your Android device IP")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print_usage()
        sys.exit(1)
    
    command = sys.argv[1].lower()
    
    if command == "speak":
        if len(sys.argv) < 3:
            print("❌ Error: Please provide text to speak")
            print(f"Usage: python {sys.argv[0]} speak \"your text here\" [speed] [pitch]")
            sys.exit(1)
        
        # Parse arguments
        text = sys.argv[2]
        speed = float(sys.argv[3]) if len(sys.argv) > 3 else 1.0
        pitch = float(sys.argv[4]) if len(sys.argv) > 4 else 1.0
        
        # Validate ranges
        if not 0.5 <= speed <= 2.0:
            print(f"⚠️  Warning: Speed {speed} out of range (0.5-2.0), clamping...")
            speed = max(0.5, min(2.0, speed))
        
        if not 0.5 <= pitch <= 2.0:
            print(f"⚠️  Warning: Pitch {pitch} out of range (0.5-2.0), clamping...")
            pitch = max(0.5, min(2.0, pitch))
        
        speak(text, speed, pitch)
        
    elif command == "status":
        get_status()
        
    elif command == "health":
        health_check()
        
    else:
        print(f"❌ Unknown command: {command}")
        print_usage()
        sys.exit(1)
