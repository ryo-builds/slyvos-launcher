package com.slyvos.launcher.dynamicbar.manager

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import com.slyvos.launcher.dynamicbar.model.DynamicActivityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MediaSessionManagerObserver(
    private val context: Context
) {
    private val _mediaState = MutableStateFlow<DynamicActivityState.Music?>(null)
    val mediaState: StateFlow<DynamicActivityState.Music?> = _mediaState.asStateFlow()

    private var activeController: MediaController? = null

    private val callback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateFromController(activeController)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromController(activeController)
        }

        override fun onSessionDestroyed() {
            _mediaState.value = null
            activeController = null
        }
    }

    fun startObserving() {
        try {
            val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val controllers = sessionManager?.getActiveSessions(null)
            if (!controllers.isNullOrEmpty()) {
                val controller = controllers.firstOrNull { c ->
                    c.playbackState?.state == PlaybackState.STATE_PLAYING ||
                    c.playbackState?.state == PlaybackState.STATE_PAUSED
                } ?: controllers.first()

                bindController(controller)
            }
        } catch (e: SecurityException) {
            // NotificationListener permission not granted yet - fallback to null or manual state
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun bindController(controller: MediaController) {
        activeController?.unregisterCallback(callback)
        activeController = controller
        controller.registerCallback(callback)
        updateFromController(controller)
    }

    private fun updateFromController(controller: MediaController?) {
        if (controller == null) {
            if (_mediaState.value?.packageName != "demo") {
                _mediaState.value = null
            }
            return
        }

        val metadata = controller.metadata
        val pbState = controller.playbackState

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: "Unknown Track"
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: "Unknown Artist"
        val albumArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val isPlaying = pbState?.state == PlaybackState.STATE_PLAYING
        val durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val currentPositionMs = pbState?.position ?: 0L

        _mediaState.value = DynamicActivityState.Music(
            title = title,
            artist = artist,
            albumArt = albumArt,
            isPlaying = isPlaying,
            durationMs = durationMs,
            currentPositionMs = currentPositionMs,
            packageName = controller.packageName
        )
    }

    fun playPause() {
        if (activeController != null) {
            val pbState = activeController?.playbackState?.state
            if (pbState == PlaybackState.STATE_PLAYING) {
                activeController?.transportControls?.pause()
            } else {
                activeController?.transportControls?.play()
            }
        } else if (_mediaState.value?.packageName == "demo") {
            val current = _mediaState.value ?: return
            _mediaState.value = current.copy(isPlaying = !current.isPlaying)
        }
    }

    fun nextTrack() {
        if (activeController != null) {
            activeController?.transportControls?.skipToNext()
        } else if (_mediaState.value?.packageName == "demo") {
            _mediaState.value = DynamicActivityState.Music(
                title = "Atmospheric Echoes",
                artist = "Slyvos Ensemble",
                albumArt = null,
                isPlaying = true,
                durationMs = 210000L,
                currentPositionMs = 0L,
                packageName = "demo"
            )
        }
    }

    fun prevTrack() {
        if (activeController != null) {
            activeController?.transportControls?.skipToPrevious()
        } else if (_mediaState.value?.packageName == "demo") {
            _mediaState.value = DynamicActivityState.Music(
                title = "Liquid Minimalism",
                artist = "Slyvos Sound",
                albumArt = null,
                isPlaying = true,
                durationMs = 180000L,
                currentPositionMs = 0L,
                packageName = "demo"
            )
        }
    }

    // Demo/Simulation driver for testing when no active media player is playing on device
    fun simulateMusic(enable: Boolean) {
        if (enable) {
            _mediaState.value = DynamicActivityState.Music(
                title = "Liquid Ambient",
                artist = "Slyvos Soundscapes",
                albumArt = null,
                isPlaying = true,
                durationMs = 224000L,
                currentPositionMs = 45000L,
                packageName = "demo"
            )
        } else {
            _mediaState.value = null
        }
    }

    fun stopObserving() {
        activeController?.unregisterCallback(callback)
        activeController = null
    }
}
