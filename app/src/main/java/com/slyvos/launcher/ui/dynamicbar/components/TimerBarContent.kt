package com.slyvos.launcher.ui.dynamicbar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slyvos.launcher.dynamicbar.model.DynamicActivityState
import com.slyvos.launcher.dynamicbar.model.TimerItem

@Composable
fun TimerBarCollapsedContent(
    state: DynamicActivityState.Timer,
    modifier: Modifier = Modifier
) {
    val t = state.activeTimer
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text("⏱", fontSize = 12.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = formatTimerTime(t.remainingMs),
            color = if (t.isFinished) Color(0xFFFF9800) else Color.White.copy(alpha = 0.95f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (t.isPaused) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "⏸",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun TimerBarExpandedContent(
    state: DynamicActivityState.Timer,
    onPauseResume: (TimerItem) -> Unit,
    onStop: (TimerItem) -> Unit,
    onAcknowledge: (TimerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val active = state.activeTimer
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = active.label,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (active.isFinished) {
                Text(
                    text = "Finished",
                    color = Color(0xFFFF9800),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            } else if (active.isPaused) {
                Text(
                    text = "Paused",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = formatTimerTime(active.remainingMs),
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 42.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (active.isFinished) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onAcknowledge(active) }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Dismiss", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                // Pause / Resume button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { onPauseResume(active) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (active.isPaused) "▶" else "⏸",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                // Stop button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935).copy(alpha = 0.35f))
                        .clickable { onStop(active) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏹",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

private fun formatTimerTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
