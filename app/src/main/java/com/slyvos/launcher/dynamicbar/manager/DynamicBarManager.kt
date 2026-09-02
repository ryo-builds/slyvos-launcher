package com.slyvos.launcher.dynamicbar.manager

import android.content.Context
import com.slyvos.launcher.BuildConfig
import com.slyvos.launcher.dynamicbar.model.AnimationPreference
import com.slyvos.launcher.dynamicbar.model.DynamicActivityState
import com.slyvos.launcher.dynamicbar.model.DynamicBarExpansion
import com.slyvos.launcher.dynamicbar.model.DynamicBarSettings
import com.slyvos.launcher.dynamicbar.model.GamingVisibilityMode
import com.slyvos.launcher.update.installer.ApkDownloader
import com.slyvos.launcher.update.installer.ApkInstaller
import com.slyvos.launcher.update.model.BuildMetadata
import com.slyvos.launcher.update.model.DownloadStatus
import com.slyvos.launcher.update.repository.DefaultUpdateRepository
import com.slyvos.launcher.update.repository.HttpUpdateDataSource
import com.slyvos.launcher.update.repository.UpdateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DynamicBarManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    val quickSettingsManager = SystemQuickSettingsManager(context)
    val mediaObserver = MediaSessionManagerObserver(context)
    val timerManager = TimerManager(scope)
    val callManager = PhoneCallManager(context, scope)
    val recordingManager = ScreenRecordingManager(scope)

    // Development Update System Build Identity
    val currentBuildMetadata = BuildMetadata.createCurrent(
        versionCode = BuildConfig.VERSION_CODE,
        buildNumber = BuildConfig.BUILD_NUMBER,
        stageName = BuildConfig.RELEASE_STAGE,
        versionName = BuildConfig.VERSION_NAME,
        timestamp = BuildConfig.BUILD_TIMESTAMP,
        notes = listOf(
            "Integrated Remote Development Update System",
            "Added HTTPS APK Downloader & SHA-256 Checksum Verification",
            "Integrated Android FileProvider System Package Installer"
        )
    )

    val updateRepository: UpdateRepository = DefaultUpdateRepository(
        currentBuild = currentBuildMetadata,
        remoteDataSource = HttpUpdateDataSource(),
        context = context
    )

    val apkDownloader = ApkDownloader(context)
    val apkInstaller = ApkInstaller(context)

    private val prefs = context.getSharedPreferences("slyvos_dynamic_bar_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<DynamicBarSettings> = _settings.asStateFlow()

    private val _expansion = MutableStateFlow(DynamicBarExpansion.COLLAPSED)
    val expansion: StateFlow<DynamicBarExpansion> = _expansion.asStateFlow()

    private val _isSettingsSheetVisible = MutableStateFlow(false)
    val isSettingsSheetVisible: StateFlow<Boolean> = _isSettingsSheetVisible.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    val activeState: StateFlow<DynamicActivityState> = combine(
        _settings,
        _expansion,
        callManager.callState,
        timerManager.timerState,
        mediaObserver.mediaState,
        recordingManager.recordingState,
        quickSettingsManager.quickSurfaceState
    ) { array ->
        val set = array[0] as DynamicBarSettings
        val exp = array[1] as DynamicBarExpansion
        val call = array[2] as? DynamicActivityState.Call
        val timer = array[3] as? DynamicActivityState.Timer
        val music = array[4] as? DynamicActivityState.Music
        val recording = array[5] as? DynamicActivityState.ScreenRecording
        val quick = array[6] as DynamicActivityState.QuickSurface

        computeEffectiveState(set, exp, call, timer, music, recording, quick)
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = DynamicActivityState.Idle
    )

    fun start() {
        quickSettingsManager.startListening()
        mediaObserver.startObserving()
        callManager.startListening()

        // Asynchronous non-blocking update check on launch
        scope.launch {
            (updateRepository as? DefaultUpdateRepository)?.checkForUpdatesThrottled()
        }
    }

    fun stop() {
        quickSettingsManager.stopListening()
        mediaObserver.stopObserving()
        callManager.stopListening()
    }

    fun checkForUpdates() {
        scope.launch {
            updateRepository.checkForUpdates()
        }
    }

    fun downloadAndInstallUpdate(build: BuildMetadata) {
        val url = build.downloadUrl ?: return
        val sha256 = build.sha256 ?: ""
        scope.launch {
            val status = apkDownloader.downloadAndVerifyApk(url, sha256)
            if (status is DownloadStatus.ReadyToInstall) {
                apkInstaller.installApk(status.apkFile)
            }
        }
    }

    private fun loadSettings(): DynamicBarSettings {
        return DynamicBarSettings(
            enableMusic = prefs.getBoolean("enable_music", true),
            enableTimer = prefs.getBoolean("enable_timer", true),
            enableCall = prefs.getBoolean("enable_call", true),
            enableScreenRecording = prefs.getBoolean("enable_screen_recording", true),
            gamingMode = try {
                GamingVisibilityMode.valueOf(prefs.getString("gaming_mode", GamingVisibilityMode.ALWAYS_SHOW.name) ?: GamingVisibilityMode.ALWAYS_SHOW.name)
            } catch (e: Exception) { GamingVisibilityMode.ALWAYS_SHOW },
            animationPreference = try {
                AnimationPreference.valueOf(prefs.getString("anim_pref", AnimationPreference.STANDARD.name) ?: AnimationPreference.STANDARD.name)
            } catch (e: Exception) { AnimationPreference.STANDARD },
            isGamingActive = false
        )
    }

    private fun saveSettings(s: DynamicBarSettings) {
        prefs.edit()
            .putBoolean("enable_music", s.enableMusic)
            .putBoolean("enable_timer", s.enableTimer)
            .putBoolean("enable_call", s.enableCall)
            .putBoolean("enable_screen_recording", s.enableScreenRecording)
            .putString("gaming_mode", s.gamingMode.name)
            .putString("anim_pref", s.animationPreference.name)
            .apply()
    }

    private fun computeEffectiveState(
        set: DynamicBarSettings,
        exp: DynamicBarExpansion,
        call: DynamicActivityState.Call?,
        timer: DynamicActivityState.Timer?,
        music: DynamicActivityState.Music?,
        recording: DynamicActivityState.ScreenRecording?,
        quick: DynamicActivityState.QuickSurface
    ): DynamicActivityState {
        val candidates = mutableListOf<DynamicActivityState>()

        if (set.enableCall && call != null) candidates.add(call)
        if (set.enableTimer && timer != null) candidates.add(timer)
        if (set.enableMusic && music != null) candidates.add(music)
        if (set.enableScreenRecording && recording != null) candidates.add(recording)

        val highestActive = candidates.minByOrNull { it.priority.value }

        return when {
            highestActive != null -> highestActive
            exp == DynamicBarExpansion.EXPANDED -> quick
            else -> DynamicActivityState.Idle
        }
    }

    // Expansion Actions
    fun handleTap() {
        if (_expansion.value == DynamicBarExpansion.COLLAPSED) {
            _expansion.value = DynamicBarExpansion.EXPANDED
            quickSettingsManager.refresh()
        } else {
            _expansion.value = DynamicBarExpansion.COLLAPSED
        }
    }

    fun handleLongPress() {
        val current = activeState.value
        if (current is DynamicActivityState.Idle || current is DynamicActivityState.QuickSurface) {
            _isSettingsSheetVisible.value = true
        } else {
            _expansion.value = DynamicBarExpansion.EXPANDED
        }
    }

    fun collapse() {
        _expansion.value = DynamicBarExpansion.COLLAPSED
    }

    fun toggleSettingsSheet(visible: Boolean) {
        _isSettingsSheetVisible.value = visible
    }

    // Settings Updates
    fun updateSettings(transform: (DynamicBarSettings) -> DynamicBarSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        saveSettings(updated)
    }

    fun setMusicEnabled(enabled: Boolean) = updateSettings { it.copy(enableMusic = enabled) }
    fun setTimerEnabled(enabled: Boolean) = updateSettings { it.copy(enableTimer = enabled) }
    fun setCallEnabled(enabled: Boolean) = updateSettings { it.copy(enableCall = enabled) }
    fun setScreenRecordingEnabled(enabled: Boolean) = updateSettings { it.copy(enableScreenRecording = enabled) }
    fun setGamingMode(mode: GamingVisibilityMode) = updateSettings { it.copy(gamingMode = mode) }
    fun setAnimationPreference(pref: AnimationPreference) = updateSettings { it.copy(animationPreference = pref) }
    fun setGamingActive(active: Boolean) = updateSettings { it.copy(isGamingActive = active) }
}
