package com.slyvos.launcher.ui.personalization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.slyvos.launcher.data.model.AppInfo
import com.slyvos.launcher.dynamicbar.model.AnimationPreference

@Composable
fun DockAppPickerSheet(
    isVisible: Boolean,
    allApps: List<AppInfo>,
    selectedPackages: List<String>,
    onSaveDockApps: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    animationPreference: AnimationPreference = AnimationPreference.STANDARD,
    modifier: Modifier = Modifier
) {
    val isReducedMotion = animationPreference == AnimationPreference.REDUCED
    val animDuration = if (isReducedMotion) 0 else 300

    val currentSelections = remember(selectedPackages) {
        mutableStateListOf<String>().apply { addAll(selectedPackages) }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(animDuration)) + slideInVertically(
            animationSpec = tween(animDuration),
            initialOffsetY = { it }
        ),
        exit = fadeOut(animationSpec = tween(animDuration)) + slideOutVertically(
            animationSpec = tween(animDuration),
            targetOffsetY = { it }
        ),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color(0xFF141923))
                    .clickable(enabled = false) {}
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .navigationBarsPadding()
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dock Apps",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select up to 5 apps for floating dock (${currentSelections.size}/5)",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E7D32))
                            .clickable {
                                onSaveDockApps(currentSelections.toList())
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .semantics { contentDescription = "Save dock apps" }
                    ) {
                        Text(
                            text = "Save",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allApps, key = { "${it.packageName}/${it.className}" }) { app ->
                        val isChecked = currentSelections.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isChecked) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f))
                                .clickable {
                                    if (isChecked) {
                                        currentSelections.remove(app.packageName)
                                    } else {
                                        if (currentSelections.size < 5) {
                                            currentSelections.add(app.packageName)
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .semantics { contentDescription = "Select ${app.label} for dock" },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (app.icon != null) {
                                val bitmap = remember(app.icon) { app.icon.toBitmap().asImageBitmap() }
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = app.label,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f))
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = app.label,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (currentSelections.size < 5) {
                                            currentSelections.add(app.packageName)
                                        }
                                    } else {
                                        currentSelections.remove(app.packageName)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF64B5F6),
                                    uncheckedColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
