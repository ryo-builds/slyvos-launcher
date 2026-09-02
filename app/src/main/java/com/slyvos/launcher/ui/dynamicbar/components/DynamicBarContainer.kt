package com.slyvos.launcher.ui.dynamicbar.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.slyvos.launcher.dynamicbar.manager.DynamicBarManager
import com.slyvos.launcher.dynamicbar.model.AnimationPreference
import com.slyvos.launcher.dynamicbar.model.DynamicActivityState
import com.slyvos.launcher.dynamicbar.model.DynamicBarExpansion
import com.slyvos.launcher.dynamicbar.model.GamingVisibilityMode

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicBarContainer(
    manager: DynamicBarManager,
    modifier: Modifier = Modifier
) {
    val activeState by manager.activeState.collectAsState()
    val expansion by manager.expansion.collectAsState()
    val settings by manager.settings.collectAsState()
    val isSettingsVisible by manager.isSettingsSheetVisible.collectAsState()

    // Check Gaming Visibility Mode
    val isGaming = settings.isGamingActive
    val shouldHideCompletely = isGaming && settings.gamingMode == GamingVisibilityMode.HIDE_WHILE_GAMING
    val isShowOnlyActive = isGaming && settings.gamingMode == GamingVisibilityMode.SHOW_ONLY_WHEN_ACTIVE
    val isIdleState = activeState is DynamicActivityState.Idle

    if (shouldHideCompletely || (isShowOnlyActive && isIdleState)) {
        // Hidden during gaming when applicable
        return
    }

    val isExpanded = expansion == DynamicBarExpansion.EXPANDED
    val isStandardAnim = settings.animationPreference == AnimationPreference.STANDARD

    val animationSpec = remember(isStandardAnim) {
        if (isStandardAnim) {
            spring<androidx.compose.ui.unit.IntSize>(stiffness = 350f, dampingRatio = 0.7f)
        } else {
            spring<androidx.compose.ui.unit.IntSize>(stiffness = 800f, dampingRatio = 0.9f)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Expanded Backdrop Dismiss Layer
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 15f) {
                                manager.collapse()
                            }
                        }
                    }
            )
        }

        // The Dynamic Bar Pill / Surface
        Surface(
            color = if (activeState is DynamicActivityState.Idle) Color.Black else Color(0xFF0D1017).copy(alpha = 0.96f),
            shape = RoundedCornerShape(if (isExpanded) 28.dp else 20.dp),
            border = if (isExpanded) BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)) else null,
            modifier = Modifier
                .animateContentSize(animationSpec = animationSpec)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { manager.handleTap() },
                    onLongClick = { manager.handleLongPress() }
                )
        ) {
            when (val curr = activeState) {
                is DynamicActivityState.Idle -> {
                    IdlePillContent()
                }

                is DynamicActivityState.QuickSurface -> {
                    QuickSurfaceContent(
                        state = curr,
                        onWifiClick = { manager.quickSettingsManager.toggleWifi() },
                        onBluetoothClick = { manager.quickSettingsManager.toggleBluetooth() },
                        onSoundClick = { manager.quickSettingsManager.cycleSoundMode() },
                        onBrightnessClick = { manager.quickSettingsManager.openDisplaySettings() }
                    )
                }

                is DynamicActivityState.Music -> {
                    if (isExpanded) {
                        MusicBarExpandedContent(
                            state = curr,
                            onPlayPause = { manager.mediaObserver.playPause() },
                            onNext = { manager.mediaObserver.nextTrack() },
                            onPrev = { manager.mediaObserver.prevTrack() }
                        )
                    } else {
                        MusicBarCollapsedContent(state = curr)
                    }
                }

                is DynamicActivityState.Timer -> {
                    if (isExpanded) {
                        TimerBarExpandedContent(
                            state = curr,
                            onPauseResume = { t ->
                                if (t.isPaused) manager.timerManager.resumeTimer(t.id)
                                else manager.timerManager.pauseTimer(t.id)
                            },
                            onStop = { t -> manager.timerManager.stopTimer(t.id) },
                            onAcknowledge = { t -> manager.timerManager.acknowledgeFinishedTimer(t.id) }
                        )
                    } else {
                        TimerBarCollapsedContent(state = curr)
                    }
                }

                is DynamicActivityState.Call -> {
                    if (isExpanded) {
                        CallBarExpandedContent(
                            state = curr,
                            onAnswer = { manager.callManager.answerCall() },
                            onEndCall = { manager.callManager.endCall() },
                            onToggleMute = { manager.callManager.toggleMute() },
                            onToggleSpeaker = { manager.callManager.toggleSpeaker() }
                        )
                    } else {
                        CallBarCollapsedContent(state = curr)
                    }
                }

                is DynamicActivityState.ScreenRecording -> {
                    if (isExpanded) {
                        ScreenRecordingBarExpandedContent(
                            state = curr,
                            onStopRecording = { manager.recordingManager.stopRecording() }
                        )
                    } else {
                        ScreenRecordingBarCollapsedContent(state = curr)
                    }
                }
            }
        }
    }

    // Dynamic Bar Settings Sheet
    DynamicBarSettingsSheet(
        isVisible = isSettingsVisible,
        settings = settings,
        manager = manager,
        onDismiss = { manager.toggleSettingsSheet(false) }
    )
}
