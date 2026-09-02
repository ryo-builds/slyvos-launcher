package com.slyvos.launcher.ui.personalization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slyvos.launcher.data.model.AppearanceSettings
import com.slyvos.launcher.data.model.BlurIntensity
import com.slyvos.launcher.data.model.CornerGeometry
import com.slyvos.launcher.data.model.DoubleTapAction
import com.slyvos.launcher.data.model.GestureSettings
import com.slyvos.launcher.data.model.HomeLayoutSettings
import com.slyvos.launcher.data.model.IconPresentation
import com.slyvos.launcher.data.model.IconSize
import com.slyvos.launcher.data.model.LayoutDensity
import com.slyvos.launcher.data.model.LongPressAction
import com.slyvos.launcher.data.model.SlyvosPersonalization
import com.slyvos.launcher.data.model.SurfaceAppearance
import com.slyvos.launcher.data.model.SwipeUpAction
import com.slyvos.launcher.data.model.ThemeMode
import com.slyvos.launcher.dynamicbar.model.AnimationPreference
import com.slyvos.launcher.dynamicbar.model.DynamicBarSettings
import com.slyvos.launcher.dynamicbar.model.GamingVisibilityMode

enum class PersonalizationTab(val label: String) {
    LAYOUT("Layout"),
    APPEARANCE("Appearance"),
    DYNAMIC_BAR("Dynamic Bar"),
    GESTURES("Gestures"),
    DOCK("Dock Apps")
}

@Composable
fun PersonalizationSheet(
    isVisible: Boolean,
    personalization: SlyvosPersonalization,
    dynamicBarSettings: DynamicBarSettings,
    onUpdateHomeLayout: ((HomeLayoutSettings) -> HomeLayoutSettings) -> Unit,
    onUpdateAppearance: ((AppearanceSettings) -> AppearanceSettings) -> Unit,
    onUpdateGestures: ((GestureSettings) -> GestureSettings) -> Unit,
    onUpdateDynamicBarSettings: ((DynamicBarSettings) -> DynamicBarSettings) -> Unit,
    onOpenDockPicker: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(PersonalizationTab.LAYOUT) }
    val isReducedMotion = dynamicBarSettings.animationPreference == AnimationPreference.REDUCED
    val animDuration = if (isReducedMotion) 0 else 300

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
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color(0xFF10141D))
                    .clickable(enabled = false) {}
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .navigationBarsPadding()
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Slyvos Personalization",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Liquid minimal adaptation",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable { onDismiss() }
                            .semantics { contentDescription = "Close personalization" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", color = Color.White, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PersonalizationTab.entries.toTypedArray()) { tab ->
                        val isSelected = tab == selectedTab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF263238) else Color.White.copy(alpha = 0.05f))
                                .clickable { selectedTab = tab }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .semantics { contentDescription = "Select ${tab.label} tab" }
                        ) {
                            Text(
                                text = tab.label,
                                color = if (isSelected) Color(0xFF64B5F6) else Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Content Panel based on selected tab
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        PersonalizationTab.LAYOUT -> LayoutTabContent(
                            homeLayout = personalization.homeLayout,
                            onUpdateHomeLayout = onUpdateHomeLayout
                        )
                        PersonalizationTab.APPEARANCE -> AppearanceTabContent(
                            appearance = personalization.appearance,
                            onUpdateAppearance = onUpdateAppearance
                        )
                        PersonalizationTab.DYNAMIC_BAR -> DynamicBarTabContent(
                            settings = dynamicBarSettings,
                            onUpdateDynamicBarSettings = onUpdateDynamicBarSettings
                        )
                        PersonalizationTab.GESTURES -> GesturesTabContent(
                            gestures = personalization.gestures,
                            onUpdateGestures = onUpdateGestures
                        )
                        PersonalizationTab.DOCK -> DockTabContent(
                            customPackages = personalization.dock.customDockPackages,
                            onOpenDockPicker = onOpenDockPicker
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LayoutTabContent(
    homeLayout: HomeLayoutSettings,
    onUpdateHomeLayout: ((HomeLayoutSettings) -> HomeLayoutSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Icon Size Option
        SectionTitle(title = "App Icon Size")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconSize.entries.forEach { size ->
                PillOptionButton(
                    label = "${size.name.lowercase().capitalize()} (${size.dpValue}dp)",
                    isSelected = homeLayout.iconSize == size,
                    onClick = { onUpdateHomeLayout { it.copy(iconSize = size) } }
                )
            }
        }

        // Floating Dock Visibility
        SectionTitle(title = "Floating Dock Visibility")
        ToggleSettingRow(
            title = "Show Floating Dock",
            subtitle = "Display quick access app surface at home bottom",
            isChecked = homeLayout.isDockVisible,
            onCheckedChange = { checked -> onUpdateHomeLayout { it.copy(isDockVisible = checked) } }
        )

        // Layout Density
        SectionTitle(title = "Layout Spacing Density")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LayoutDensity.entries.forEach { density ->
                PillOptionButton(
                    label = density.name.lowercase().capitalize(),
                    isSelected = homeLayout.density == density,
                    onClick = { onUpdateHomeLayout { it.copy(density = density) } }
                )
            }
        }
    }
}

@Composable
private fun AppearanceTabContent(
    appearance: AppearanceSettings,
    onUpdateAppearance: ((AppearanceSettings) -> AppearanceSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Theme Mode
        SectionTitle(title = "Theme Mode")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                PillOptionButton(
                    label = mode.name.lowercase().capitalize(),
                    isSelected = appearance.themeMode == mode,
                    onClick = { onUpdateAppearance { it.copy(themeMode = mode) } }
                )
            }
        }

        // Corner Geometry
        SectionTitle(title = "Organic Corner Geometry")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CornerGeometry.entries.forEach { geo ->
                PillOptionButton(
                    label = "${geo.name.lowercase().capitalize()} (${geo.cornerDp}dp)",
                    isSelected = appearance.cornerGeometry == geo,
                    onClick = { onUpdateAppearance { it.copy(cornerGeometry = geo) } }
                )
            }
        }

        // Surface Translucency Level
        SectionTitle(title = "Surface Translucency: ${(appearance.transparencyLevel * 100).toInt()}%")
        Slider(
            value = appearance.transparencyLevel,
            onValueChange = { valLevel -> onUpdateAppearance { it.copy(transparencyLevel = valLevel) } },
            valueRange = 0.15f..0.50f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF64B5F6),
                activeTrackColor = Color(0xFF64B5F6)
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Icon Presentation Mode
        SectionTitle(title = "Icon Presentation")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconPresentation.entries.forEach { pres ->
                PillOptionButton(
                    label = pres.name.replace("_", " ").lowercase().capitalize(),
                    isSelected = appearance.iconPresentation == pres,
                    onClick = { onUpdateAppearance { it.copy(iconPresentation = pres) } }
                )
            }
        }
    }
}

@Composable
private fun DynamicBarTabContent(
    settings: DynamicBarSettings,
    onUpdateDynamicBarSettings: ((DynamicBarSettings) -> DynamicBarSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(title = "Enabled Features")
        ToggleSettingRow(
            title = "Music Player Surface",
            subtitle = "Active media playback control",
            isChecked = settings.enableMusic,
            onCheckedChange = { checked -> onUpdateDynamicBarSettings { it.copy(enableMusic = checked) } }
        )
        ToggleSettingRow(
            title = "Timer & Stopwatch",
            subtitle = "Countdown and timer surface",
            isChecked = settings.enableTimer,
            onCheckedChange = { checked -> onUpdateDynamicBarSettings { it.copy(enableTimer = checked) } }
        )
        ToggleSettingRow(
            title = "Phone Calls",
            subtitle = "Active call status",
            isChecked = settings.enableCall,
            onCheckedChange = { checked -> onUpdateDynamicBarSettings { it.copy(enableCall = checked) } }
        )
        ToggleSettingRow(
            title = "Screen Recording",
            subtitle = "Capture status surface",
            isChecked = settings.enableScreenRecording,
            onCheckedChange = { checked -> onUpdateDynamicBarSettings { it.copy(enableScreenRecording = checked) } }
        )

        SectionTitle(title = "Motion & Animation")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnimationPreference.entries.forEach { pref ->
                PillOptionButton(
                    label = pref.name.lowercase().capitalize(),
                    isSelected = settings.animationPreference == pref,
                    onClick = { onUpdateDynamicBarSettings { it.copy(animationPreference = pref) } }
                )
            }
        }
    }
}

@Composable
private fun GesturesTabContent(
    gestures: GestureSettings,
    onUpdateGestures: ((GestureSettings) -> GestureSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(title = "Swipe Up Gesture")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SwipeUpAction.entries.forEach { action ->
                PillOptionButton(
                    label = action.name.replace("_", " ").lowercase().capitalize(),
                    isSelected = gestures.swipeUpAction == action,
                    onClick = { onUpdateGestures { it.copy(swipeUpAction = action) } }
                )
            }
        }

        SectionTitle(title = "Double Tap Home Canvas")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DoubleTapAction.entries.forEach { action ->
                PillOptionButton(
                    label = action.name.replace("_", " ").lowercase().capitalize(),
                    isSelected = gestures.doubleTapAction == action,
                    onClick = { onUpdateGestures { it.copy(doubleTapAction = action) } }
                )
            }
        }

        SectionTitle(title = "Long Press Home Clock/Header")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LongPressAction.entries.forEach { action ->
                PillOptionButton(
                    label = action.name.replace("_", " ").lowercase().capitalize(),
                    isSelected = gestures.longPressAction == action,
                    onClick = { onUpdateGestures { it.copy(longPressAction = action) } }
                )
            }
        }
    }
}

@Composable
private fun DockTabContent(
    customPackages: List<String>,
    onOpenDockPicker: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(title = "Floating Dock Shortcuts")
        Text(
            text = if (customPackages.isEmpty()) "Default system shortcuts currently active" else "${customPackages.size} custom apps selected for floating dock",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E88E5))
                .clickable { onOpenDockPicker() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚡ Select Dock Apps",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PillOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFF64B5F6) else Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics { contentDescription = "Option $label" }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF64B5F6)
            )
        )
    }
}

private fun String.capitalize(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
