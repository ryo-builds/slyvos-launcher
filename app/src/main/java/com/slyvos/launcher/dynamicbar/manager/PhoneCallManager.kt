package com.slyvos.launcher.dynamicbar.manager

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.slyvos.launcher.dynamicbar.model.CallState
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

class PhoneCallManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _callState = MutableStateFlow<DynamicActivityState.Call?>(null)
    val callState: StateFlow<DynamicActivityState.Call?> = _callState.asStateFlow()

    private var durationJob: Job? = null
    private var simulatedState: DynamicActivityState.Call? = null

    private val listener = object : PhoneStateListener() {
        @Deprecated("Deprecated in API 31")
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (simulatedState != null) return // simulation takes precedence during test
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    val name = if (phoneNumber.isNullOrEmpty()) "Incoming Call" else phoneNumber
                    setIncomingCall(name, phoneNumber ?: "")
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    if (_callState.value?.state != CallState.ACTIVE) {
                        setActiveCall(_callState.value?.callerName ?: "Active Call", _callState.value?.phoneNumber ?: "")
                    }
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    endCall()
                }
            }
        }
    }

    fun startListening() {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            @Suppress("DEPRECATION")
            tm?.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopListening() {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            @Suppress("DEPRECATION")
            tm?.listen(listener, PhoneStateListener.LISTEN_NONE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        durationJob?.cancel()
    }

    fun setIncomingCall(callerName: String, phoneNumber: String) {
        durationJob?.cancel()
        val c = DynamicActivityState.Call(
            callerName = callerName,
            phoneNumber = phoneNumber,
            state = CallState.INCOMING,
            durationSeconds = 0
        )
        _callState.value = c
    }

    fun answerCall() {
        val current = _callState.value ?: return
        setActiveCall(current.callerName, current.phoneNumber)
    }

    fun setActiveCall(callerName: String, phoneNumber: String) {
        durationJob?.cancel()
        val activeCall = DynamicActivityState.Call(
            callerName = callerName,
            phoneNumber = phoneNumber,
            state = CallState.ACTIVE,
            durationSeconds = 0
        )
        _callState.value = activeCall

        durationJob = scope.launch(Dispatchers.Default) {
            var duration = 0L
            while (isActive && _callState.value?.state == CallState.ACTIVE) {
                delay(1000)
                duration++
                _callState.value = _callState.value?.copy(durationSeconds = duration)
            }
        }
    }

    fun toggleMute() {
        val current = _callState.value ?: return
        _callState.value = current.copy(isMuted = !current.isMuted)
    }

    fun toggleSpeaker() {
        val current = _callState.value ?: return
        _callState.value = current.copy(isSpeakerOn = !current.isSpeakerOn)
    }

    fun endCall() {
        durationJob?.cancel()
        durationJob = null
        simulatedState = null
        _callState.value = null
    }

    // Simulation driver for testing
    fun simulateIncomingCall(callerName: String = "Elena Vance") {
        simulatedState = DynamicActivityState.Call(
            callerName = callerName,
            phoneNumber = "+1 (555) 234-5678",
            state = CallState.INCOMING,
            durationSeconds = 0
        )
        _callState.value = simulatedState
    }
}
