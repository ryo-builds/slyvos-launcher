package com.slyvos.launcher.data.repository

import android.content.Context
import com.slyvos.launcher.data.model.AppearanceSettings
import com.slyvos.launcher.data.model.BlurIntensity
import com.slyvos.launcher.data.model.CornerGeometry
import com.slyvos.launcher.data.model.DockSettings
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface PersonalizationRepository {
    val personalization: StateFlow<SlyvosPersonalization>
    fun getPersonalization(): SlyvosPersonalization
    fun updateHomeLayout(transform: (HomeLayoutSettings) -> HomeLayoutSettings)
    fun updateAppearance(transform: (AppearanceSettings) -> AppearanceSettings)
    fun updateGestures(transform: (GestureSettings) -> GestureSettings)
    fun updateDockPackages(packages: List<String>)
}

class PreferencesPersonalizationRepository(
    private val context: Context
) : PersonalizationRepository {

    private val prefs = context.getSharedPreferences("slyvos_personalization_prefs", Context.MODE_PRIVATE)

    private val _personalization = MutableStateFlow(loadPersonalization())
    override val personalization: StateFlow<SlyvosPersonalization> = _personalization.asStateFlow()

    override fun getPersonalization(): SlyvosPersonalization = _personalization.value

    private fun loadPersonalization(): SlyvosPersonalization {
        val iconSize = try {
            IconSize.valueOf(prefs.getString("layout_icon_size", IconSize.MEDIUM.name) ?: IconSize.MEDIUM.name)
        } catch (e: Exception) { IconSize.MEDIUM }

        val isDockVisible = prefs.getBoolean("layout_dock_visible", true)

        val density = try {
            LayoutDensity.valueOf(prefs.getString("layout_density", LayoutDensity.BALANCED.name) ?: LayoutDensity.BALANCED.name)
        } catch (e: Exception) { LayoutDensity.BALANCED }

        val themeMode = try {
            ThemeMode.valueOf(prefs.getString("app_theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
        } catch (e: Exception) { ThemeMode.DARK }

        val surfaceAppearance = try {
            SurfaceAppearance.valueOf(prefs.getString("app_surface_appearance", SurfaceAppearance.LIQUID_TRANSLUCENT.name) ?: SurfaceAppearance.LIQUID_TRANSLUCENT.name)
        } catch (e: Exception) { SurfaceAppearance.LIQUID_TRANSLUCENT }

        val transparencyLevel = prefs.getFloat("app_transparency_level", 0.30f)

        val cornerGeometry = try {
            CornerGeometry.valueOf(prefs.getString("app_corner_geometry", CornerGeometry.ORGANIC.name) ?: CornerGeometry.ORGANIC.name)
        } catch (e: Exception) { CornerGeometry.ORGANIC }

        val blurIntensity = try {
            BlurIntensity.valueOf(prefs.getString("app_blur_intensity", BlurIntensity.SUBTLE.name) ?: BlurIntensity.SUBTLE.name)
        } catch (e: Exception) { BlurIntensity.SUBTLE }

        val iconPresentation = try {
            IconPresentation.valueOf(prefs.getString("app_icon_presentation", IconPresentation.FULL_COLOR.name) ?: IconPresentation.FULL_COLOR.name)
        } catch (e: Exception) { IconPresentation.FULL_COLOR }

        val swipeUpAction = try {
            SwipeUpAction.valueOf(prefs.getString("gesture_swipe_up", SwipeUpAction.APP_DRAWER.name) ?: SwipeUpAction.APP_DRAWER.name)
        } catch (e: Exception) { SwipeUpAction.APP_DRAWER }

        val doubleTapAction = try {
            DoubleTapAction.valueOf(prefs.getString("gesture_double_tap", DoubleTapAction.NONE.name) ?: DoubleTapAction.NONE.name)
        } catch (e: Exception) { DoubleTapAction.NONE }

        val longPressAction = try {
            LongPressAction.valueOf(prefs.getString("gesture_long_press", LongPressAction.PERSONALIZATION_SHEET.name) ?: LongPressAction.PERSONALIZATION_SHEET.name)
        } catch (e: Exception) { LongPressAction.PERSONALIZATION_SHEET }

        val dockRaw = prefs.getString("dock_packages", "") ?: ""
        val dockPackages = if (dockRaw.isBlank()) emptyList() else dockRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        return SlyvosPersonalization(
            homeLayout = HomeLayoutSettings(
                iconSize = iconSize,
                isDockVisible = isDockVisible,
                density = density
            ),
            appearance = AppearanceSettings(
                themeMode = themeMode,
                surfaceAppearance = surfaceAppearance,
                transparencyLevel = transparencyLevel,
                cornerGeometry = cornerGeometry,
                blurIntensity = blurIntensity,
                iconPresentation = iconPresentation
            ),
            gestures = GestureSettings(
                swipeUpAction = swipeUpAction,
                doubleTapAction = doubleTapAction,
                longPressAction = longPressAction
            ),
            dock = DockSettings(
                customDockPackages = dockPackages
            )
        )
    }

    private fun savePersonalization(p: SlyvosPersonalization) {
        prefs.edit()
            .putString("layout_icon_size", p.homeLayout.iconSize.name)
            .putBoolean("layout_dock_visible", p.homeLayout.isDockVisible)
            .putString("layout_density", p.homeLayout.density.name)
            .putString("app_theme_mode", p.appearance.themeMode.name)
            .putString("app_surface_appearance", p.appearance.surfaceAppearance.name)
            .putFloat("app_transparency_level", p.appearance.transparencyLevel)
            .putString("app_corner_geometry", p.appearance.cornerGeometry.name)
            .putString("app_blur_intensity", p.appearance.blurIntensity.name)
            .putString("app_icon_presentation", p.appearance.iconPresentation.name)
            .putString("gesture_swipe_up", p.gestures.swipeUpAction.name)
            .putString("gesture_double_tap", p.gestures.doubleTapAction.name)
            .putString("gesture_long_press", p.gestures.longPressAction.name)
            .putString("dock_packages", p.dock.customDockPackages.joinToString(","))
            .apply()
    }

    override fun updateHomeLayout(transform: (HomeLayoutSettings) -> HomeLayoutSettings) {
        _personalization.update { current ->
            val updated = current.copy(homeLayout = transform(current.homeLayout))
            savePersonalization(updated)
            updated
        }
    }

    override fun updateAppearance(transform: (AppearanceSettings) -> AppearanceSettings) {
        _personalization.update { current ->
            val updated = current.copy(appearance = transform(current.appearance))
            savePersonalization(updated)
            updated
        }
    }

    override fun updateGestures(transform: (GestureSettings) -> GestureSettings) {
        _personalization.update { current ->
            val updated = current.copy(gestures = transform(current.gestures))
            savePersonalization(updated)
            updated
        }
    }

    override fun updateDockPackages(packages: List<String>) {
        _personalization.update { current ->
            val updated = current.copy(dock = current.dock.copy(customDockPackages = packages))
            savePersonalization(updated)
            updated
        }
    }
}
