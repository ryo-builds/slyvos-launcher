package com.slyvos.launcher.dynamicbar.manager

import com.slyvos.launcher.dynamicbar.model.DynamicActivityState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScreenRecordingManager(
    private val scope: CoroutineScope
) {
    private val _recordingState = MutableStateFlow<DynamicActivityState.ScreenRecording?>(null)
    val recordingState: StateFlow<DynamicActivityState.ScreenRecording?> = _recordingState.asStateFlow()

    private var durationJob: Job? = null

    fun startRecording() {
        durationJob?.cancel()
        val initial = DynamicActivityState.ScreenRecording(durationSeconds = 0, isRecording = true)
        _recordingState.value = initial

        durationJob = scope.launch(Dispatchers.Default) {
            var duration = 0L
            while (isActive && _recordingState.value?.isRecording == true) {
                delay(1000)
                duration++
                _recordingState.value = _recordingState.value?.copy(durationSeconds = duration)
            }
        }
    }

    fun stopRecording() {
        durationJob?.cancel()
        durationJob = null
        _recordingState.value = null
    }

    fun toggleRecording() {
        if (_recordingState.value == null) {
            startRecording()
        } else {
            stopRecording()
        }
    }
}
