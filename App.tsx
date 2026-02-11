import React, {useState, useEffect} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  Alert,
  NativeModules,
  Platform,
} from 'react-native';

const {TTSServiceModule, MusicPicker, MusicControl} = NativeModules;

function App(): React.JSX.Element {
  const [serviceRunning, setServiceRunning] = useState(false);
  const [lastStatus, setLastStatus] = useState('Unknown');
  const [testText, setTestText] = useState('สวัสดีครับ');
  const [speechSpeed, setSpeechSpeed] = useState(1.0);
  const [speechPitch, setSpeechPitch] = useState(1.0);
  const [serverUrl, setServerUrl] = useState('http://localhost:8765');
  const [deviceIp, setDeviceIp] = useState('Detecting...');
  const [logs, setLogs] = useState<string[]>([]);
  const [musicLoaded, setMusicLoaded] = useState(false);
  const [musicPlaying, setMusicPlaying] = useState(false);
  const [musicVolume, setMusicVolume] = useState(0.5);
  const [currentTrack, setCurrentTrack] = useState('No music loaded');
  const [playlistSize, setPlaylistSize] = useState(0);
  const [trackNumber, setTrackNumber] = useState(0);

  useEffect(() => {
    checkServiceStatus();
    getDeviceIp();
    const interval = setInterval(() => {
      checkServiceStatus();
      getDeviceIp();
    }, 3000);
    return () => clearInterval(interval);
  }, []);

  const getDeviceIp = async () => {
    try {
      const response = await fetch('http://localhost:8765/status');
      const data = await response.json();
      // Try to extract IP from network
      const networkInfo = await fetch('https://api.ipify.org?format=json')
        .then(r => r.json())
        .catch(() => null);
      
      // Fallback to localhost
      setDeviceIp('Check WiFi Settings');
      setServerUrl('http://localhost:8765');
    } catch (error) {
      setDeviceIp('Not available');
    }
  };

  useEffect(() => {
    checkServiceStatus();
    const interval = setInterval(checkServiceStatus, 2000);
    return () => clearInterval(interval);
  }, []);

  const addLog = (message: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs(prev => [`[${timestamp}] ${message}`, ...prev.slice(0, 19)]);
  };

  const checkServiceStatus = async () => {
    try {
      const status = await TTSServiceModule.getServiceStatus();
      setServiceRunning(status.isRunning);
      setLastStatus(status.lastStatus);
    } catch (error) {
      console.error('Failed to get status:', error);
    }
  };

  const startService = async () => {
    try {
      await TTSServiceModule.startService();
      addLog('✅ Service started successfully');
      setTimeout(checkServiceStatus, 500);
    } catch (error: any) {
      addLog(`❌ Failed to start service: ${error.message}`);
      Alert.alert('Error', `Failed to start service: ${error.message}`);
    }
  };

  const stopService = async () => {
    try {
      await TTSServiceModule.stopService();
      addLog('🛑 Service stopped');
      setTimeout(checkServiceStatus, 500);
    } catch (error: any) {
      addLog(`❌ Failed to stop service: ${error.message}`);
      Alert.alert('Error', `Failed to stop service: ${error.message}`);
    }
  };

  const sendTestRequest = async () => {
    if (!testText.trim()) {
      Alert.alert('Error', 'Please enter text to speak');
      return;
    }

    try {
      const response = await fetch(`${serverUrl}/speak`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          text: testText,
          speed: speechSpeed,
          pitch: speechPitch,
        }),
      });

      const result = await response.json();

      if (response.ok) {
        addLog(`🔊 Sent: "${testText.substring(0, 30)}..." (${result.speed}x, pitch ${result.pitch}) - Queue: ${result.queueSize}`);
        Alert.alert('Success', `Text queued for speech!\nSpeed: ${result.speed}x\nPitch: ${result.pitch}\nQueue size: ${result.queueSize}`);
      } else {
        addLog(`❌ Error: ${result.error || 'Unknown error'}`);
        Alert.alert('Error', result.error || 'Failed to send request');
      }
    } catch (error: any) {
      addLog(`❌ Network error: ${error.message}`);
      Alert.alert('Error', `Network error: ${error.message}\n\nMake sure the service is running.`);
    }
  };

  const checkHealth = async () => {
    try {
      const response = await fetch(`${serverUrl}/health`);
      const result = await response.json();
      
      if (result.status === 'healthy') {
        addLog('💚 Health check: OK');
        Alert.alert('Health Check', '✅ Service is healthy and responding');
      }
    } catch (error: any) {
      addLog(`❌ Health check failed: ${error.message}`);
      Alert.alert('Health Check Failed', error.message);
    }
  };

  const getStatus = async () => {
    try {
      const response = await fetch(`${serverUrl}/status`);
      const result = await response.json();
      
      addLog(`📊 Status: ${result.service}, TTS: ${result.ttsInitialized ? 'Ready' : 'Initializing'}`);
      
      Alert.alert(
        'Service Status',
        `Service: ${result.service}\n` +
        `Port: ${result.port}\n` +
        `TTS Initialized: ${result.ttsInitialized ? 'Yes' : 'No'}\n` +
        `Currently Speaking: ${result.isSpeaking ? 'Yes' : 'No'}\n` +
        `Queue Size: ${result.queueSize}\n` +
        `Last Status: ${result.lastStatus}`
      );
    } catch (error: any) {
      addLog(`❌ Failed to get status: ${error.message}`);
      Alert.alert('Error', error.message);
    }
  };

  // Music Control Functions
  const pickMusicFile = async () => {
    try {
      const uri = await MusicPicker.pickMusicFile();
      await MusicControl.loadMusic(uri);
      await refreshMusicState();
      addLog('🎵 Music file loaded');
      Alert.alert('Success', 'Music file loaded successfully');
    } catch (error: any) {
      if (error.code !== 'CANCELLED') {
        addLog(`❌ Failed to load music: ${error.message}`);
        Alert.alert('Error', `Failed to load music: ${error.message}`);
      }
    }
  };

  const pickMusicFolder = async () => {
    try {
      const result = await MusicPicker.pickMusicFolder();
      await MusicControl.loadPlaylist(result.audioFiles);
      await refreshMusicState();
      addLog(`🎵 Playlist loaded: ${result.count} tracks`);
      Alert.alert('Success', `Playlist loaded with ${result.count} tracks`);
    } catch (error: any) {
      if (error.code !== 'CANCELLED') {
        addLog(`❌ Failed to load playlist: ${error.message}`);
        Alert.alert('Error', `Failed to load playlist: ${error.message}`);
      }
    }
  };

  const toggleMusic = async () => {
    try {
      if (musicPlaying) {
        await MusicControl.pauseMusic();
        addLog('⏸️ Music paused');
      } else {
        await MusicControl.playMusic();
        addLog('▶️ Music playing');
      }
      await refreshMusicState();
    } catch (error: any) {
      addLog(`❌ Music control error: ${error.message}`);
      Alert.alert('Error', error.message);
    }
  };

  const stopMusic = async () => {
    try {
      await MusicControl.stopMusic();
      await refreshMusicState();
      addLog('⏹️ Music stopped');
    } catch (error: any) {
      addLog(`❌ Failed to stop music: ${error.message}`);
    }
  };

  const nextTrack = async () => {
    try {
      await MusicControl.nextTrack();
      await refreshMusicState();
      addLog('⏭️ Next track');
    } catch (error: any) {
      addLog(`❌ Failed to skip: ${error.message}`);
    }
  };

  const previousTrack = async () => {
    try {
      await MusicControl.previousTrack();
      await refreshMusicState();
      addLog('⏮️ Previous track');
    } catch (error: any) {
      addLog(`❌ Failed to go back: ${error.message}`);
    }
  };

  const changeMusicVolume = async (volume: number) => {
    try {
      await MusicControl.setVolume(volume);
      setMusicVolume(volume);
    } catch (error: any) {
      addLog(`❌ Volume error: ${error.message}`);
    }
  };

  const refreshMusicState = async () => {
    try {
      const state = await MusicControl.getMusicState();
      setMusicLoaded(state.isLoaded);
      setMusicPlaying(state.isPlaying);
      setMusicVolume(state.volume);
      setCurrentTrack(state.currentTrack);
      setPlaylistSize(state.playlistSize);
      setTrackNumber(state.trackNumber);
    } catch (error) {
      // Ignore errors if service not running
    }
  };

  // Refresh music state periodically
  useEffect(() => {
    const interval = setInterval(refreshMusicState, 3000);
    return () => clearInterval(interval);
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a2e" />
      <ScrollView contentContainerStyle={styles.scrollView}>
        <View style={styles.header}>
          <Text style={styles.title}>🎙️ TTS Voice Service</Text>
          <Text style={styles.subtitle}>Thai & English Text-to-Speech REST API</Text>
          <Text style={styles.versionText}>v1.0.0 • Background Service</Text>
        </View>

        {/* Enhanced Service Status Card */}
        <View style={styles.statusCard}>
          <View style={styles.statusHeader}>
            <Text style={styles.cardTitle}>🔊 Service Status</Text>
            {serviceRunning && (
              <View style={styles.liveBadge}>
                <View style={styles.pulsingDot} />
                <Text style={styles.liveText}>LIVE</Text>
              </View>
            )}
          </View>
          
          <View style={styles.statusRow}>
            <Text style={styles.statusLabel}>Status:</Text>
            <View style={[styles.statusBadge, serviceRunning ? styles.statusRunning : styles.statusStopped]}>
              <Text style={styles.statusBadgeText}>
                {serviceRunning ? '🟢 Active' : '🔴 Stopped'}
              </Text>
            </View>
          </View>
          
          {serviceRunning && (
            <>
              <View style={styles.infoRow}>
                <Text style={styles.infoLabel}>📡 API Endpoint:</Text>
                <Text style={styles.infoValue}>{serverUrl}</Text>
              </View>
              <View style={styles.infoRow}>
                <Text style={styles.infoLabel}>🌐 Access from:</Text>
                <Text style={styles.infoValue}>Any device on WiFi</Text>
              </View>
              <View style={styles.infoRow}>
                <Text style={styles.infoLabel}>🔄 Auto-start:</Text>
                <Text style={styles.infoValue}>On boot enabled</Text>
              </View>
            </>
          )}
          
          <View style={styles.statusFooter}>
            <Text style={styles.statusText}>💬 {lastStatus}</Text>
          </View>
        </View>

        {/* Service Controls */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Service Controls</Text>
          <View style={styles.buttonRow}>
            <TouchableOpacity
              style={[styles.button, styles.startButton, serviceRunning && styles.buttonDisabled]}
              onPress={startService}
              disabled={serviceRunning}>
              <Text style={styles.buttonText}>▶️ Start Service</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.button, styles.stopButton, !serviceRunning && styles.buttonDisabled]}
              onPress={stopService}
              disabled={!serviceRunning}>
              <Text style={styles.buttonText}>⏹️ Stop Service</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Background Music Controls */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>🎵 Background Music</Text>
          
          {/* Track Info */}
          {musicLoaded && (
            <View style={styles.trackInfo}>
              <Text style={styles.trackName}>{currentTrack}</Text>
              {playlistSize > 1 && (
                <Text style={styles.trackCounter}>Track {trackNumber} of {playlistSize}</Text>
              )}
            </View>
          )}
          
          {/* File Selection Buttons */}
          <View style={styles.buttonRow}>
            <TouchableOpacity
              style={[styles.button, styles.musicButton]}
              onPress={pickMusicFile}>
              <Text style={styles.buttonText}>📁 Select File</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.button, styles.musicButton]}
              onPress={pickMusicFolder}>
              <Text style={styles.buttonText}>📂 Select Folder</Text>
            </TouchableOpacity>
          </View>
          
          {/* Playback Controls */}
          {musicLoaded && (
            <>
              <View style={styles.buttonRow}>
                <TouchableOpacity
                  style={[styles.button, styles.musicControlButton]}
                  onPress={previousTrack}
                  disabled={playlistSize <= 1}>
                  <Text style={styles.buttonText}>⏮️</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.button, styles.musicControlButton, styles.playButton]}
                  onPress={toggleMusic}>
                  <Text style={styles.buttonText}>{musicPlaying ? '⏸️ Pause' : '▶️ Play'}</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.button, styles.musicControlButton]}
                  onPress={nextTrack}
                  disabled={playlistSize <= 1}>
                  <Text style={styles.buttonText}>⏭️</Text>
                </TouchableOpacity>
              </View>
              
              {/* Volume Control */}
              <View style={styles.controlRow}>
                <Text style={styles.controlLabel}>🔊 Volume: {Math.round(musicVolume * 100)}%</Text>
                <View style={styles.sliderContainer}>
                  <TouchableOpacity onPress={() => changeMusicVolume(Math.max(0, musicVolume - 0.1))}>
                    <Text style={styles.sliderButton}>-</Text>
                  </TouchableOpacity>
                  <View style={styles.sliderTrack}>
                    <View style={[styles.sliderFill, {width: `${musicVolume * 100}%`}]} />
                    <Text style={styles.sliderValue}>{Math.round(musicVolume * 100)}%</Text>
                  </View>
                  <TouchableOpacity onPress={() => changeMusicVolume(Math.min(1, musicVolume + 0.1))}>
                    <Text style={styles.sliderButton}>+</Text>
                  </TouchableOpacity>
                </View>
              </View>
              
              <TouchableOpacity
                style={[styles.button, styles.stopButton]}
                onPress={stopMusic}>
                <Text style={styles.buttonText}>⏹️ Stop Music</Text>
              </TouchableOpacity>
              
              <Text style={styles.musicHint}>
                💡 Music will pause automatically when TTS speaks
              </Text>
            </>
          )}
          
          {!musicLoaded && (
            <Text style={styles.musicHint}>
              Select a music file or folder to play background music
            </Text>
          )}
        </View>

        {/* Test TTS */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Test Text-to-Speech</Text>
          <TextInput
            style={styles.input}
            placeholder="Enter text in Thai or English..."
            placeholderTextColor="#888"
            value={testText}
            onChangeText={setTestText}
            multiline
          />
          
          {/* Speed Control */}
          <View style={styles.controlRow}>
            <Text style={styles.controlLabel}>🚀 Speed: {speechSpeed.toFixed(1)}x</Text>
            <View style={styles.sliderContainer}>
              <TouchableOpacity onPress={() => setSpeechSpeed(Math.max(0.5, speechSpeed - 0.1))}>
                <Text style={styles.sliderButton}>-</Text>
              </TouchableOpacity>
              <View style={styles.sliderTrack}>
                <View style={[styles.sliderFill, {width: `${((speechSpeed - 0.5) / 1.5) * 100}%`}]} />
                <Text style={styles.sliderValue}>{speechSpeed.toFixed(1)}x</Text>
              </View>
              <TouchableOpacity onPress={() => setSpeechSpeed(Math.min(2.0, speechSpeed + 0.1))}>
                <Text style={styles.sliderButton}>+</Text>
              </TouchableOpacity>
            </View>
            <View style={styles.presetButtons}>
              <TouchableOpacity style={styles.presetButton} onPress={() => setSpeechSpeed(0.7)}>
                <Text style={styles.presetButtonText}>Slow</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.presetButton} onPress={() => setSpeechSpeed(1.0)}>
                <Text style={styles.presetButtonText}>Normal</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.presetButton} onPress={() => setSpeechSpeed(1.5)}>
                <Text style={styles.presetButtonText}>Fast</Text>
              </TouchableOpacity>
            </View>
          </View>
          
          {/* Pitch Control */}
          <View style={styles.controlRow}>
            <Text style={styles.controlLabel}>🎵 Pitch: {speechPitch.toFixed(1)}</Text>
            <View style={styles.sliderContainer}>
              <TouchableOpacity onPress={() => setSpeechPitch(Math.max(0.5, speechPitch - 0.1))}>
                <Text style={styles.sliderButton}>-</Text>
              </TouchableOpacity>
              <View style={styles.sliderTrack}>
                <View style={[styles.sliderFill, {width: `${((speechPitch - 0.5) / 1.5) * 100}%`}]} />
                <Text style={styles.sliderValue}>{speechPitch.toFixed(1)}</Text>
              </View>
              <TouchableOpacity onPress={() => setSpeechPitch(Math.min(2.0, speechPitch + 0.1))}>
                <Text style={styles.sliderButton}>+</Text>
              </TouchableOpacity>
            </View>
            <View style={styles.presetButtons}>
              <TouchableOpacity style={styles.presetButton} onPress={() => setSpeechPitch(0.8)}>
                <Text style={styles.presetButtonText}>Low</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.presetButton} onPress={() => setSpeechPitch(1.0)}>
                <Text style={styles.presetButtonText}>Normal</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.presetButton} onPress={() => setSpeechPitch(1.2)}>
                <Text style={styles.presetButtonText}>High</Text>
              </TouchableOpacity>
            </View>
          </View>
          
          <TouchableOpacity
            style={[styles.button, styles.speakButton]}
            onPress={sendTestRequest}>
            <Text style={styles.buttonText}>🔊 Speak Text</Text>
          </TouchableOpacity>
          
          <View style={styles.quickButtons}>
            <TouchableOpacity
              style={styles.quickButton}
              onPress={() => setTestText('สวัสดีครับ')}>
              <Text style={styles.quickButtonText}>สวัสดีครับ</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.quickButton}
              onPress={() => setTestText('Hello World')}>
              <Text style={styles.quickButtonText}>Hello</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.quickButton}
              onPress={() => setTestText('ทดสอบระบบเสียง')}>
              <Text style={styles.quickButtonText}>ทดสอบ</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* API Info */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>API Information</Text>
          <View style={styles.buttonRow}>
            <TouchableOpacity style={[styles.button, styles.infoButton]} onPress={getStatus}>
              <Text style={styles.buttonText}>📊 Status</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.button, styles.infoButton]} onPress={checkHealth}>
              <Text style={styles.buttonText}>💚 Health</Text>
            </TouchableOpacity>
          </View>
          <View style={styles.apiInfo}>
            <Text style={styles.apiText}>POST {serverUrl}/speak</Text>
            <Text style={styles.apiCodeText}>{'{"text": "สวัสดี"}'}</Text>
            <Text style={styles.apiText}>GET {serverUrl}/status</Text>
            <Text style={styles.apiText}>GET {serverUrl}/health</Text>
          </View>
        </View>

        {/* Logs */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Activity Logs</Text>
          <ScrollView style={styles.logsContainer} nestedScrollEnabled>
            {logs.length === 0 ? (
              <Text style={styles.logEmpty}>No activity yet</Text>
            ) : (
              logs.map((log, index) => (
                <Text key={index} style={styles.logText}>{log}</Text>
              ))
            )}
          </ScrollView>
        </View>

        {/* Footer Info */}
        <View style={styles.footer}>
          <Text style={styles.footerText}>
            💡 This service will auto-start on device boot
          </Text>
          <Text style={styles.footerText}>
            🌐 Access API from any device on the same network
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0f0f1e',
  },
  scrollView: {
    padding: 16,
  },
  header: {
    alignItems: 'center',
    marginBottom: 20,
    paddingVertical: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#aaa',
  },
  statusCard: {
    backgroundColor: '#1a1a2e',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    borderWidth: 2,
    borderColor: '#2a2a4e',
  },
  statusHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  liveBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#00aa5522',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#00aa55',
  },
  pulsingDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#00ff88',
    marginRight: 6,
  },
  liveText: {
    color: '#00ff88',
    fontSize: 12,
    fontWeight: 'bold',
  },
  statusFooter: {
    marginTop: 12,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#2a2a4e',
  },
  infoRow: {
    marginTop: 8,
    paddingVertical: 6,
  },
  infoLabel: {
    fontSize: 12,
    color: '#888',
    marginBottom: 2,
  },
  infoValue: {
    fontSize: 14,
    color: '#4169e1',
    fontWeight: '500',
  },
  card: {
    backgroundColor: '#1a1a2e',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 12,
  },
  statusRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  statusLabel: {
    fontSize: 16,
    color: '#ccc',
    marginRight: 8,
  },
  statusBadge: {
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusRunning: {
    backgroundColor: '#00aa5522',
  },
  statusStopped: {
    backgroundColor: '#aa000022',
  },
  statusBadgeText: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  statusText: {
    fontSize: 14,
    color: '#aaa',
  },
  infoText: {
    fontSize: 12,
    color: '#888',
    marginTop: 4,
  },
  versionText: {
    fontSize: 12,
    color: '#666',
    marginTop: 4,
  },
  buttonRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 10,
  },
  button: {
    flex: 1,
    padding: 14,
    borderRadius: 8,
    alignItems: 'center',
  },
  startButton: {
    backgroundColor: '#00aa55',
  },
  stopButton: {
    backgroundColor: '#ff4444',
  },
  speakButton: {
    backgroundColor: '#4169e1',
  },
  infoButton: {
    backgroundColor: '#6c757d',
  },
  buttonDisabled: {
    opacity: 0.5,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  input: {
    backgroundColor: '#0f0f1e',
    borderRadius: 8,
    padding: 12,
    color: '#fff',
    fontSize: 16,
    marginBottom: 12,
    minHeight: 80,
    textAlignVertical: 'top',
    borderWidth: 1,
    borderColor: '#2a2a4e',
  },
  controlRow: {
    marginBottom: 16,
  },
  controlLabel: {
    fontSize: 16,
    color: '#fff',
    fontWeight: 'bold',
    marginBottom: 8,
  },
  sliderContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    marginBottom: 8,
  },
  sliderButton: {
    fontSize: 24,
    color: '#4169e1',
    fontWeight: 'bold',
    paddingHorizontal: 12,
    paddingVertical: 4,
  },
  sliderTrack: {
    flex: 1,
    height: 40,
    backgroundColor: '#1a1a2e',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#2a2a4e',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  sliderFill: {
    position: 'absolute',
    left: 0,
    top: 0,
    bottom: 0,
    backgroundColor: '#4169e1',
    opacity: 0.3,
  },
  sliderValue: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
    textAlign: 'center',
    zIndex: 1,
  },
  presetButtons: {
    flexDirection: 'row',
    gap: 8,
  },
  presetButton: {
    backgroundColor: '#2a2a4e',
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#4169e1',
  },
  presetButtonText: {
    color: '#4169e1',
    fontSize: 12,
    fontWeight: 'bold',
  },
  quickButtons: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 8,
  },
  quickButton: {
    backgroundColor: '#2a2a4e',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 6,
  },
  quickButtonText: {
    color: '#fff',
    fontSize: 12,
  },
  apiInfo: {
    marginTop: 12,
    padding: 12,
    backgroundColor: '#0f0f1e',
    borderRadius: 8,
  },
  apiText: {
    color: '#aaa',
    fontSize: 12,
    marginBottom: 4,
    fontFamily: 'monospace',
  },
  apiCodeText: {
    color: '#4169e1',
    fontSize: 12,
    marginBottom: 8,
    fontFamily: 'monospace',
    marginLeft: 16,
  },
  logsContainer: {
    backgroundColor: '#0f0f1e',
    borderRadius: 8,
    padding: 12,
    maxHeight: 200,
  },
  logEmpty: {
    color: '#666',
    textAlign: 'center',
    padding: 20,
  },
  logText: {
    color: '#ccc',
    fontSize: 11,
    marginBottom: 4,
    fontFamily: 'monospace',
  },
  footer: {
    marginTop: 20,
    marginBottom: 40,
    alignItems: 'center',
  },
  footerText: {
    color: '#666',
    fontSize: 12,
    marginBottom: 8,
    textAlign: 'center',
  },
  // Music Control Styles
  trackInfo: {
    backgroundColor: '#0f0f1e',
    borderRadius: 8,
    padding: 12,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#2a2a4e',
  },
  trackName: {
    color: '#fff',
    fontSize: 14,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  trackCounter: {
    color: '#888',
    fontSize: 12,
  },
  musicButton: {
    backgroundColor: '#9c27b0',
  },
  musicControlButton: {
    backgroundColor: '#2a2a4e',
    flex: 1,
  },
  playButton: {
    backgroundColor: '#00aa55',
    flex: 2,
  },
  musicHint: {
    color: '#888',
    fontSize: 12,
    textAlign: 'center',
    marginTop: 8,
    fontStyle: 'italic',
  },
});

export default App;
