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

@Composable
fun QuickSurfaceContent(
    state: DynamicActivityState.QuickSurface,
    onWifiClick: () -> Unit,
    onBluetoothClick: () -> Unit,
    onSoundClick: () -> Unit,
    onBrightnessClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Surface",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${state.batteryPercent}%",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
                if (state.isCharging) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "⚡",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Tile Actions Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickTileItem(
                label = "Wi-Fi",
                status = state.wifiName ?: if (state.wifiEnabled) "On" else "Off",
                isActive = state.wifiEnabled,
                onClick = onWifiClick
            )
            QuickTileItem(
                label = "Bluetooth",
                status = if (state.bluetoothEnabled) state.bluetoothConnectedDevice ?: "On" else "Off",
                isActive = state.bluetoothEnabled,
                onClick = onBluetoothClick
            )
            QuickTileItem(
                label = "Sound",
                status = state.soundMode,
                isActive = state.soundMode != "Silent",
                onClick = onSoundClick
            )
            QuickTileItem(
                label = "Brightness",
                status = "${state.brightnessPercent}%",
                isActive = true,
                onClick = onBrightnessClick
            )
        }
    }
}

@Composable
private fun QuickTileItem(
    label: String,
    status: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.06f))
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = status,
                color = if (isActive) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
