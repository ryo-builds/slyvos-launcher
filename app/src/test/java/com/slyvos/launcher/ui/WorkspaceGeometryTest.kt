package com.slyvos.launcher.ui

import android.content.res.Configuration
import androidx.compose.ui.unit.dp
import com.slyvos.launcher.data.model.IconSize
import com.slyvos.launcher.data.model.LayoutDensity
import com.slyvos.launcher.ui.home.model.ScreenOrientation
import com.slyvos.launcher.ui.home.model.WorkspaceGeometryCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceGeometryTest {

    @Test
    fun testPortraitWorkspaceGeometry() {
        val geometry = WorkspaceGeometryCalculator.calculate(
            maxWidthDp = 360.dp,
            maxHeightDp = 800.dp,
            orientationConfig = Configuration.ORIENTATION_PORTRAIT,
            density = LayoutDensity.COMPACT,
            iconSize = IconSize.MEDIUM,
            isDockVisible = true,
            isDynamicBarVisible = true
        )

        assertEquals(ScreenOrientation.PORTRAIT, geometry.orientation)
        assertEquals(4, geometry.columns)
        assertEquals(5, geometry.rows)
        assertEquals(48.dp, geometry.iconSizeDp)
        assertEquals(8.dp, geometry.gridSpacingDp)
        assertEquals(12.dp, geometry.sidePaddingDp)
        assertTrue(geometry.cellWidthDp >= 40.dp)
        assertTrue(geometry.cellHeightDp >= 40.dp)
    }

    @Test
    fun testLandscapeWorkspaceGeometry() {
        val geometry = WorkspaceGeometryCalculator.calculate(
            maxWidthDp = 800.dp,
            maxHeightDp = 360.dp,
            orientationConfig = Configuration.ORIENTATION_LANDSCAPE,
            density = LayoutDensity.SPACIOUS,
            iconSize = IconSize.LARGE,
            isDockVisible = true,
            isDynamicBarVisible = true
        )

        assertEquals(ScreenOrientation.LANDSCAPE, geometry.orientation)
        assertEquals(6, geometry.columns)
        assertEquals(3, geometry.rows)
        assertEquals(56.dp, geometry.iconSizeDp)
        assertEquals(20.dp, geometry.gridSpacingDp)
        assertEquals(24.dp, geometry.sidePaddingDp)
        assertTrue(geometry.cellWidthDp >= 40.dp)
        assertTrue(geometry.cellHeightDp >= 40.dp)
    }

    @Test
    fun testDensityAndIconSizeVariations() {
        val compactGeometry = WorkspaceGeometryCalculator.calculate(
            maxWidthDp = 380.dp,
            maxHeightDp = 800.dp,
            orientationConfig = Configuration.ORIENTATION_PORTRAIT,
            density = LayoutDensity.COMPACT,
            iconSize = IconSize.SMALL,
            isDockVisible = true,
            isDynamicBarVisible = true
        )

        val spaciousGeometry = WorkspaceGeometryCalculator.calculate(
            maxWidthDp = 380.dp,
            maxHeightDp = 800.dp,
            orientationConfig = Configuration.ORIENTATION_PORTRAIT,
            density = LayoutDensity.SPACIOUS,
            iconSize = IconSize.LARGE,
            isDockVisible = true,
            isDynamicBarVisible = true
        )

        assertEquals(40.dp, compactGeometry.iconSizeDp)
        assertEquals(56.dp, spaciousGeometry.iconSizeDp)
        assertTrue(compactGeometry.gridSpacingDp < spaciousGeometry.gridSpacingDp)
    }
}
