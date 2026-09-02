package com.slyvos.launcher.data.model

import com.slyvos.launcher.dynamicbar.model.DynamicBarSettings

enum class IconSize(val dpValue: Int) {
    SMALL(40),
    MEDIUM(48),
    LARGE(56)
}

enum class LayoutDensity(val spacingDp: Int) {
    COMPACT(8),
    BALANCED(12),
    SPACIOUS(18)
}

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class SurfaceAppearance {
    LIQUID_TRANSLUCENT,
    SOLID_MINIMAL,
    AMBIENT_GRADIENT
}

enum class CornerGeometry(val cornerDp: Int) {
    SHARP(8),
    ORGANIC(20),
    PILL(28)
}

enum class BlurIntensity(val blurRadiusDp: Float) {
    DISABLED(0f),
    SUBTLE(15f),
    RICH(30f)
}

enum class IconPresentation {
    FULL_COLOR,
    MINIMAL_MONO
}

enum class SwipeUpAction {
    APP_DRAWER,
    PERSONALIZATION_SHEET
}

enum class DoubleTapAction {
    NONE,
    EXPAND_DYNAMIC_BAR
}

enum class LongPressAction {
    PERSONALIZATION_SHEET,
    WIDGET_PICKER
}

data class HomeLayoutSettings(
    val iconSize: IconSize = IconSize.MEDIUM,
    val isDockVisible: Boolean = true,
    val density: LayoutDensity = LayoutDensity.BALANCED
)

data class AppearanceSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val surfaceAppearance: SurfaceAppearance = SurfaceAppearance.LIQUID_TRANSLUCENT,
    val transparencyLevel: Float = 0.30f, // 0.15f to 0.50f
    val cornerGeometry: CornerGeometry = CornerGeometry.ORGANIC,
    val blurIntensity: BlurIntensity = BlurIntensity.SUBTLE,
    val iconPresentation: IconPresentation = IconPresentation.FULL_COLOR
)

data class GestureSettings(
    val swipeUpAction: SwipeUpAction = SwipeUpAction.APP_DRAWER,
    val doubleTapAction: DoubleTapAction = DoubleTapAction.NONE,
    val longPressAction: LongPressAction = LongPressAction.PERSONALIZATION_SHEET
)

data class DockSettings(
    val customDockPackages: List<String> = emptyList() // User chosen package names
)

data class SlyvosPersonalization(
    val homeLayout: HomeLayoutSettings = HomeLayoutSettings(),
    val appearance: AppearanceSettings = AppearanceSettings(),
    val gestures: GestureSettings = GestureSettings(),
    val dock: DockSettings = DockSettings(),
    val dynamicBar: DynamicBarSettings = DynamicBarSettings()
)
