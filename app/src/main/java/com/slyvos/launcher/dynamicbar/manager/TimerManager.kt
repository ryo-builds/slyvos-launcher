package com.slyvos.launcher.dynamicbar.manager

import com.slyvos.launcher.dynamicbar.model.DynamicActivityState
import com.slyvos.launcher.dynamicbar.model.TimerItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class TimerManager(
    private val scope: CoroutineScope
) {
    private val _timerState = MutableStateFlow<DynamicActivityState.Timer?>(null)
    val timerState: StateFlow<DynamicActivityState.Timer?> = _timerState.asStateFlow()

    private val timersList = mutableListOf<TimerItem>()
    private var tickerJob: Job? = null

    fun startTimer(durationSeconds: Long, label: String = "Timer") {
        val newTimer = TimerItem(
            id = UUID.randomUUID().toString(),
            label = label,
            totalDurationMs = durationSeconds * 1000L,
            remainingMs = durationSeconds * 1000L,
            isPaused = false,
            isFinished = false
        )
        timersList.add(newTimer)
        ensureTickerStarted()
        updateState()
    }

    fun pauseTimer(timerId: String) {
        val idx = timersList.indexOfFirst { it.id == timerId }
        if (idx != -1) {
            val t = timersList[idx]
            timersList[idx] = t.copy(isPaused = true)
            updateState()
        }
    }

    fun resumeTimer(timerId: String) {
        val idx = timersList.indexOfFirst { it.id == timerId }
        if (idx != -1) {
            val t = timersList[idx]
            timersList[idx] = t.copy(isPaused = false)
            updateState()
        }
    }

    fun stopTimer(timerId: String) {
        timersList.removeAll { it.id == timerId }
        updateState()
    }

    fun acknowledgeFinishedTimer(timerId: String) {
        stopTimer(timerId)
    }

    private fun ensureTickerStarted() {
        if (tickerJob == null || tickerJob?.isActive != true) {
            tickerJob = scope.launch(Dispatchers.Default) {
                while (isActive && timersList.isNotEmpty()) {
                    delay(1000)
                    var changed = false
                    val iterator = timersList.listIterator()
                    while (iterator.hasNext()) {
                        val t = iterator.next()
                        if (!t.isPaused && !t.isFinished) {
                            val newRemaining = t.remainingMs - 1000L
                            if (newRemaining <= 0) {
                                iterator.set(t.copy(remainingMs = 0L, isFinished = true))
                            } else {
                                iterator.set(t.copy(remainingMs = newRemaining))
                            }
                            changed = true
                        }
                    }
                    if (changed) {
                        updateState()
                    }
                }
            }
        }
    }

    private fun updateState() {
        if (timersList.isEmpty()) {
            _timerState.value = null
            tickerJob?.cancel()
            tickerJob = null
            return
        }

        // Collapsed state shows nearest / most relevant timer (smallest remaining, or non-finished first)
        val sortedTimers = timersList.sortedWith(
            compareBy<TimerItem> { it.isFinished }.thenBy { it.remainingMs }
        )

        val active = sortedTimers.first()
        _timerState.value = DynamicActivityState.Timer(
            activeTimer = active,
            allTimers = sortedTimers
        )
    }
}
