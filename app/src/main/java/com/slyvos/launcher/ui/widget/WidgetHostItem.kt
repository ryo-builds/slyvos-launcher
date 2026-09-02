package com.slyvos.launcher.ui.widget

import android.appwidget.AppWidgetHostView
import android.content.ComponentName
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.slyvos.launcher.data.model.SlyvosWidget
import com.slyvos.launcher.widget.SlyvosAppWidgetHost

@Composable
fun WidgetHostItem(
    widget: SlyvosWidget,
    appWidgetHost: SlyvosAppWidgetHost,
    onRemoveWidget: (Int) -> Unit,
    onResizeWidget: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val hostView = remember(widget.appWidgetId) {
        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        val providerInfo = appWidgetManager.getAppWidgetInfo(widget.appWidgetId)
            ?: appWidgetManager.installedProviders.firstOrNull {
                it.provider == ComponentName(widget.providerPackage, widget.providerClass)
            }

        if (providerInfo != null) {
            appWidgetHost.createView(context, widget.appWidgetId, providerInfo).apply {
                setAppWidget(widget.appWidgetId, providerInfo)
            }
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = if (showMenu) Color(0xFF64B5F6) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = {},
                onLongClick = { showMenu = !showMenu }
            )
            .padding(8.dp)
    ) {
        if (hostView != null) {
            AndroidView(
                factory = { hostView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Widget Unavailable",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        }

        // Slyvos Context Overlay Menu on Long Press
        if (showMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Widget Controls",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Resize Options
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SizeOptionChip(text = "2 × 2", isSelected = widget.spanX == 2 && widget.spanY == 2) {
                            onResizeWidget(widget.appWidgetId, 2, 2)
                            showMenu = false
                        }
                        SizeOptionChip(text = "4 × 2", isSelected = widget.spanX == 4 && widget.spanY == 2) {
                            onResizeWidget(widget.appWidgetId, 4, 2)
                            showMenu = false
                        }
                        SizeOptionChip(text = "4 × 3", isSelected = widget.spanX == 4 && widget.spanY == 3) {
                            onResizeWidget(widget.appWidgetId, 4, 3)
                            showMenu = false
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Done / Dismiss button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .combinedClickable(
                                    onClick = { showMenu = false }
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Done",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Remove Widget button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE57373).copy(alpha = 0.25f))
                                .border(1.dp, Color(0xFFE57373).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .combinedClickable(
                                    onClick = {
                                        onRemoveWidget(widget.appWidgetId)
                                        showMenu = false
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Remove",
                                color = Color(0xFFFFCDD2),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SizeOptionChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF64B5F6).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF64B5F6) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF90CAF9) else Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
