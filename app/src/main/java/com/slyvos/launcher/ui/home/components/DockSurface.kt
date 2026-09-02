package com.slyvos.launcher.ui.home.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.slyvos.launcher.data.model.AppInfo

@Composable
fun DockSurface(
    dockApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onSwipeUpTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Swipe handle indicator
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.35f))
                .clickable { onSwipeUpTrigger() }
        )

        // Translucent floating surface
        Surface(
            color = Color.White.copy(alpha = 0.07f),
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                if (dockApps.isEmpty()) {
                    Spacer(modifier = Modifier.height(52.dp))
                } else {
                    dockApps.forEach { app ->
                        AppIconItem(
                            app = app,
                            onClick = { onAppClick(app) },
                            iconSize = 48.dp,
                            showLabel = false
                        )
                    }
                }
            }
        }
    }
}
