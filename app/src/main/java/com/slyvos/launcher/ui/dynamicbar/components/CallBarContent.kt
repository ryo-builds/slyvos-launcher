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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slyvos.launcher.dynamicbar.model.CallState
import com.slyvos.launcher.dynamicbar.model.DynamicActivityState

@Composable
fun CallBarCollapsedContent(
    state: DynamicActivityState.Call,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text("📞", fontSize = 12.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = state.callerName,
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(90.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (state.state == CallState.INCOMING) "Ringing..." else formatCallDuration(state.durationSeconds),
            color = if (state.state == CallState.INCOMING) Color(0xFFFFC107) else Color(0xFF4CAF50),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun CallBarExpandedContent(
    state: DynamicActivityState.Call,
    onAnswer: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.callerName,
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (state.phoneNumber.isNotEmpty()) {
            Text(
                text = state.phoneNumber,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (state.state == CallState.INCOMING) {
            Text(
                text = "Incoming Call",
                color = Color(0xFFFFC107),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Decline
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .weight(1f)
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFE53935))
                        .clickable { onEndCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Decline", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                // Answer
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .weight(1f)
                        .padding(start = 6.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF4CAF50))
                        .clickable { onAnswer() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Answer", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            // Active Call
            Text(
                text = formatCallDuration(state.durationSeconds),
                color = Color(0xFF4CAF50),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (state.isMuted) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f))
                        .clickable { onToggleMute() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (state.isMuted) "🔇" else "🎙", fontSize = 16.sp)
                }

                // End Call
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .clickable { onEndCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("📞", fontSize = 18.sp)
                }

                // Speaker
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (state.isSpeakerOn) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f))
                        .clickable { onToggleSpeaker() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (state.isSpeakerOn) "🔊" else "🔈", fontSize = 16.sp)
                }
            }
        }
    }
}

private fun formatCallDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
