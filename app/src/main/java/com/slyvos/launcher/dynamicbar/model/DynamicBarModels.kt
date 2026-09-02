package com.slyvos.launcher.dynamicbar.model

import android.graphics.Bitmap

enum class DynamicBarPriority(val value: Int) {
    CALL(1),
    TIMER(2),
    MUSIC(3),
    SCREEN_RECORDING(4),
    QUICK_SURFACE(5),
    IDLE(6)
}

enum class CallState {
    INCOMING,
    ACTIVE
}

enum class GamingVisibilityMode {
    ALWAYS_SHOW,
    HIDE_WHILE_GAMING,
    SHOW_ONLY_WHEN_ACTIVE
}

enum class AnimationPreference {
    STANDARD,
    REDUCED
}

enum class DynamicBarExpansion {
    COLLAPSED,
    EXPANDED
}

data class DynamicBarSettings(
    val enableMusic: Boolean = true,
    val enableTimer: Boolean = true,
    val enableCall: Boolean = true,
    val enableScreenRecording: Boolean = true,
    val gamingMode: GamingVisibilityMode = GamingVisibilityMode.ALWAYS_SHOW,
    val animationPreference: AnimationPreference = AnimationPreference.STANDARD,
    val isGamingActive: Boolean = false
)

data class TimerItem(
    val id: String,
    val label: String,
    val totalDurationMs: Long,
    val remainingMs: Long,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false
)

sealed interface DynamicActivityState {
    val priority: DynamicBarPriority

    data object Idle : DynamicActivityState {
        override val priority = DynamicBarPriority.IDLE
    }

    data class QuickSurface(
        val batteryPercent: Int,
        val isCharging: Boolean,
        val wifiEnabled: Boolean,
        val wifiName: String?,
        val bluetoothEnabled: Boolean,
        val bluetoothConnectedDevice: String?,
        val soundMode: String, // "Sound", "Vibrate", "Silent"
        val brightnessPercent: Int
    ) : DynamicActivityState {
        override val priority = DynamicBarPriority.QUICK_SURFACE
    }

    data class Music(
        val title: String,
        val artist: String,
        val albumArt: Bitmap?,
        val isPlaying: Boolean,
        val durationMs: Long,
        val currentPositionMs: Long,
        val packageName: String? = null
    ) : DynamicActivityState {
        override val priority = DynamicBarPriority.MUSIC
    }

    data class Timer(
        val activeTimer: TimerItem,
        val allTimers: List<TimerItem> = emptyList()
    ) : DynamicActivityState {
        override val priority = DynamicBarPriority.TIMER
    }

    data class Call(
        val callerName: String,
        val phoneNumber: String,
        val state: CallState,
        val durationSeconds: Long = 0,
        val isMuted: Boolean = false,
        val isSpeakerOn: Boolean = false
    ) : DynamicActivityState {
        override val priority = DynamicBarPriority.CALL
    }

    data class ScreenRecording(
        val durationSeconds: Long,
        val isRecording: Boolean
    ) : DynamicActivityState {
        override val priority = DynamicBarPriority.SCREEN_RECORDING
    }
}
