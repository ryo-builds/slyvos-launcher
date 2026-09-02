package com.slyvos.launcher.dynamicbar.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.provider.Settings
import com.slyvos.launcher.dynamicbar.model.DynamicActivityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SystemQuickSettingsManager(
    private val context: Context
) {

    private val _quickSurfaceState = MutableStateFlow(fetchCurrentState())
    val quickSurfaceState: StateFlow<DynamicActivityState.QuickSurface> = _quickSurfaceState.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            refresh()
        }
    }

    fun startListening() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        try {
            context.registerReceiver(receiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        refresh()
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refresh() {
        _quickSurfaceState.value = fetchCurrentState()
    }

    private fun fetchCurrentState(): DynamicActivityState.QuickSurface {
        var batteryPercent = 85
        var isCharging = false
        try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                batteryPercent = (level * 100) / scale
            }
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Wi-Fi
        var isWifiOn = false
        var wifiName: String? = null
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (wifiManager != null) {
                isWifiOn = wifiManager.isWifiEnabled
                if (isWifiOn && cm != null) {
                    val activeNetwork = cm.activeNetwork
                    val caps = cm.getNetworkCapabilities(activeNetwork)
                    if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        @Suppress("DEPRECATION")
                        val wifiInfo = wifiManager.connectionInfo
                        val ssid = wifiInfo?.ssid?.replace("\"", "")
                        wifiName = if (ssid.isNullOrEmpty() || ssid == "<unknown ssid>") "Connected" else ssid
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Bluetooth
        var isBtOn = false
        try {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val btAdapter = btManager?.adapter
            isBtOn = btAdapter?.isEnabled ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sound Mode
        var soundMode = "Sound"
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            soundMode = when (audioManager?.ringerMode) {
                AudioManager.RINGER_MODE_SILENT -> "Silent"
                AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
                else -> "Sound"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Brightness
        var brightnessPercent = 70
        try {
            val brightnessVal = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            brightnessPercent = (brightnessVal * 100) / 255
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return DynamicActivityState.QuickSurface(
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            wifiEnabled = isWifiOn,
            wifiName = wifiName,
            bluetoothEnabled = isBtOn,
            bluetoothConnectedDevice = if (isBtOn) "Available" else null,
            soundMode = soundMode,
            brightnessPercent = brightnessPercent
        )
    }

    // Toggle Actions
    fun toggleWifi() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleBluetooth() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cycleSoundMode() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            when (audioManager.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                AudioManager.RINGER_MODE_VIBRATE -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                else -> audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
            refresh()
        } catch (e: Exception) {
            openSoundSettings()
        }
    }

    fun openSoundSettings() {
        try {
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openDisplaySettings() {
        try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
