package com.slyvos.launcher.ui.dynamicbar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slyvos.launcher.dynamicbar.manager.DynamicBarManager
import com.slyvos.launcher.dynamicbar.model.AnimationPreference
import com.slyvos.launcher.dynamicbar.model.DynamicBarSettings
import com.slyvos.launcher.dynamicbar.model.GamingVisibilityMode
import com.slyvos.launcher.update.model.DownloadStatus
import com.slyvos.launcher.update.model.UpdateStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DynamicBarSettingsSheet(
    isVisible: Boolean,
    settings: DynamicBarSettings,
    manager: DynamicBarManager,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val updateStatus by manager.updateRepository.updateStatus.collectAsState()
    val downloadStatus by manager.apkDownloader.downloadStatus.collectAsState()
    val buildMeta = manager.currentBuildMetadata

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() }
        ) {
            Surface(
                color = Color(0xFF141824).copy(alpha = 0.95f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .align(Alignment.BottomCenter)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Dynamic Bar & System Settings",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 0: BUILD & UPDATE IDENTITY
                    SectionHeader("SLYVOS BUILD & REMOTE UPDATES")
                    Surface(
                        color = Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = buildMeta.buildNumberFormatted,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Package: com.slyvos.launcher • Stage: ${buildMeta.stage.displayName}",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(buildMeta.releaseTimestamp))
                            Text(
                                text = "Build Date: $dateStr",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Update status row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val statusText = when (val s = updateStatus) {
                                    is UpdateStatus.Idle -> "Status: Ready"
                                    is UpdateStatus.Checking -> "Status: Checking server..."
                                    is UpdateStatus.UpToDate -> "Status: Up to date ✓"
                                    is UpdateStatus.UpdateAvailable -> "New Build Available: ${s.latestBuild.buildNumberFormatted}"
                                    is UpdateStatus.Error -> "Status: Offline / ${s.message}"
                                }
                                Text(
                                    text = statusText,
                                    color = if (updateStatus is UpdateStatus.UpdateAvailable) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.75f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .clickable { manager.checkForUpdates() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Check Updates", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            // Download status / action card
                            if (updateStatus is UpdateStatus.UpdateAvailable) {
                                val avail = (updateStatus as UpdateStatus.UpdateAvailable).latestBuild
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Release Notes for ${avail.buildNumberFormatted}:",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                avail.releaseNotes.forEach { note ->
                                    Text(
                                        text = "• $note",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                when (val d = downloadStatus) {
                                    is DownloadStatus.Idle -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF4CAF50))
                                                .clickable { manager.downloadAndInstallUpdate(avail) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Download & Install Build", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    is DownloadStatus.Downloading -> {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "Downloading APK (${d.progressPercent}%)...",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color.White.copy(alpha = 0.15f))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(d.progressPercent / 100f)
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(Color(0xFF2196F3))
                                                )
                                            }
                                        }
                                    }
                                    is DownloadStatus.VerifyingChecksum -> {
                                        Text(
                                            text = "Verifying SHA-256 Checksum...",
                                            color = Color(0xFFFFC107),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    is DownloadStatus.ReadyToInstall -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF4CAF50))
                                                .clickable { manager.apkInstaller.installApk(d.apkFile) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Verified ✓ — Tap to Install APK", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    is DownloadStatus.Error -> {
                                        Text(
                                            text = "Error: ${d.message}",
                                            color = Color(0xFFE53935),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 1: Active Feature Toggles
                    SectionHeader("ACTIVE FEATURES")
                    SettingSwitchRow("Music Playback", "Show active media sessions", settings.enableMusic) {
                        manager.setMusicEnabled(it)
                    }
                    SettingSwitchRow("Timers & Countdowns", "Show live running timers", settings.enableTimer) {
                        manager.setTimerEnabled(it)
                    }
                    SettingSwitchRow("Phone Calls", "Show incoming and active calls", settings.enableCall) {
                        manager.setCallEnabled(it)
                    }
                    SettingSwitchRow("Screen Recording", "Show recording state & duration", settings.enableScreenRecording) {
                        manager.setScreenRecordingEnabled(it)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 2: Gaming Behavior
                    SectionHeader("GAMING BEHAVIOR")
                    SegmentedOptionRow(
                        title = "Always Show",
                        subtitle = "Behaves normally during games",
                        isSelected = settings.gamingMode == GamingVisibilityMode.ALWAYS_SHOW,
                        onClick = { manager.setGamingMode(GamingVisibilityMode.ALWAYS_SHOW) }
                    )
                    SegmentedOptionRow(
                        title = "Hide While Gaming",
                        subtitle = "Completely hide Dynamic Bar during games",
                        isSelected = settings.gamingMode == GamingVisibilityMode.HIDE_WHILE_GAMING,
                        onClick = { manager.setGamingMode(GamingVisibilityMode.HIDE_WHILE_GAMING) }
                    )
                    SegmentedOptionRow(
                        title = "Show Only When Active",
                        subtitle = "Hide idle pill, reveal only active states",
                        isSelected = settings.gamingMode == GamingVisibilityMode.SHOW_ONLY_WHEN_ACTIVE,
                        onClick = { manager.setGamingMode(GamingVisibilityMode.SHOW_ONLY_WHEN_ACTIVE) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 3: Animation Preference
                    SectionHeader("ANIMATION PREFERENCE")
                    SegmentedOptionRow(
                        title = "Standard Motion",
                        subtitle = "Normal Slyvos liquid transitions",
                        isSelected = settings.animationPreference == AnimationPreference.STANDARD,
                        onClick = { manager.setAnimationPreference(AnimationPreference.STANDARD) }
                    )
                    SegmentedOptionRow(
                        title = "Reduced Motion",
                        subtitle = "Shorter, restrained transitions",
                        isSelected = settings.animationPreference == AnimationPreference.REDUCED,
                        onClick = { manager.setAnimationPreference(AnimationPreference.REDUCED) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // SECTION 4: Real-Device Test Drivers
                    SectionHeader("REAL-DEVICE TEST DRIVERS")
                    TestActionButton("Simulate Incoming Call") {
                        manager.callManager.simulateIncomingCall("Elena Vance")
                        onDismiss()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TestActionButton("Start 30s Test Timer") {
                        manager.timerManager.startTimer(30, "Cooking Timer")
                        onDismiss()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TestActionButton("Play Demo Music Track") {
                        manager.mediaObserver.simulateMusic(true)
                        onDismiss()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TestActionButton("Toggle Screen Recording State") {
                        manager.recordingManager.toggleRecording()
                        onDismiss()
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color.White.copy(alpha = 0.45f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White.copy(alpha = 0.95f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Normal)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF3F51B5),
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun SegmentedOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.04f))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White.copy(alpha = 0.95f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Normal)
            }
            if (isSelected) {
                Text("✓", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TestActionButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
