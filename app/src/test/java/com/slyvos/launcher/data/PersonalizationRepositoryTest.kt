package com.slyvos.launcher.data

import com.slyvos.launcher.data.model.AppearanceSettings
import com.slyvos.launcher.data.model.CornerGeometry
import com.slyvos.launcher.data.model.DockSettings
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalizationRepositoryTest {

    @Test
    fun testDefaultValues() {
        val initial = SlyvosPersonalization()
        assertEquals(IconSize.MEDIUM, initial.homeLayout.iconSize)
        assertTrue(initial.homeLayout.isDockVisible)
        assertEquals(LayoutDensity.BALANCED, initial.homeLayout.density)

        assertEquals(ThemeMode.DARK, initial.appearance.themeMode)
        assertEquals(SurfaceAppearance.LIQUID_TRANSLUCENT, initial.appearance.surfaceAppearance)
        assertEquals(0.30f, initial.appearance.transparencyLevel, 0.01f)
        assertEquals(CornerGeometry.ORGANIC, initial.appearance.cornerGeometry)
        assertEquals(IconPresentation.FULL_COLOR, initial.appearance.iconPresentation)

        assertEquals(SwipeUpAction.APP_DRAWER, initial.gestures.swipeUpAction)
        assertEquals(LongPressAction.PERSONALIZATION_SHEET, initial.gestures.longPressAction)

        assertTrue(initial.dock.customDockPackages.isEmpty())
    }

    @Test
    fun testUpdateHomeLayout() {
        val initial = SlyvosPersonalization()
        val updated = initial.copy(
            homeLayout = initial.homeLayout.copy(iconSize = IconSize.LARGE, isDockVisible = false, density = LayoutDensity.COMPACT)
        )

        assertEquals(IconSize.LARGE, updated.homeLayout.iconSize)
        assertFalse(updated.homeLayout.isDockVisible)
        assertEquals(LayoutDensity.COMPACT, updated.homeLayout.density)
    }

    @Test
    fun testUpdateAppearance() {
        val initial = SlyvosPersonalization()
        val updated = initial.copy(
            appearance = initial.appearance.copy(
                themeMode = ThemeMode.LIGHT,
                surfaceAppearance = SurfaceAppearance.SOLID_MINIMAL,
                transparencyLevel = 0.45f,
                cornerGeometry = CornerGeometry.PILL,
                iconPresentation = IconPresentation.MINIMAL_MONO
            )
        )

        assertEquals(ThemeMode.LIGHT, updated.appearance.themeMode)
        assertEquals(SurfaceAppearance.SOLID_MINIMAL, updated.appearance.surfaceAppearance)
        assertEquals(0.45f, updated.appearance.transparencyLevel, 0.01f)
        assertEquals(CornerGeometry.PILL, updated.appearance.cornerGeometry)
        assertEquals(IconPresentation.MINIMAL_MONO, updated.appearance.iconPresentation)
    }

    @Test
    fun testUpdateDockPackages() {
        val packages = listOf("com.android.chrome", "com.google.android.dialer")
        val initial = SlyvosPersonalization()
        val updated = initial.copy(dock = DockSettings(customDockPackages = packages))

        assertEquals(2, updated.dock.customDockPackages.size)
        assertEquals("com.android.chrome", updated.dock.customDockPackages[0])
        assertEquals("com.google.android.dialer", updated.dock.customDockPackages[1])
    }
}
