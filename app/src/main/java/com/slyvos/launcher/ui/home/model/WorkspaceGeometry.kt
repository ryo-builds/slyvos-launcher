package com.slyvos.launcher.ui.home.model

import android.content.res.Configuration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slyvos.launcher.data.model.IconSize
import com.slyvos.launcher.data.model.LayoutDensity

enum class ScreenOrientation {
    PORTRAIT,
    LANDSCAPE
}

data class WorkspaceGeometry(
    val orientation: ScreenOrientation,
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
    val columns: Int,
    val rows: Int,
    val cellWidthDp: Dp,
    val cellHeightDp: Dp,
    val iconSizeDp: Dp,
    val gridSpacingDp: Dp,
    val sidePaddingDp: Dp,
    val topPaddingDp: Dp,      // Dynamic Bar safe area
    val bottomPaddingDp: Dp,   // Floating Dock safe area
    val pageIndex: Int = 0
)

object WorkspaceGeometryCalculator {

    fun calculate(
        maxWidthDp: Dp,
        maxHeightDp: Dp,
        orientationConfig: Int,
        density: LayoutDensity,
        iconSize: IconSize,
        isDockVisible: Boolean,
        isDynamicBarVisible: Boolean
    ): WorkspaceGeometry {
        val isLandscape = orientationConfig == Configuration.ORIENTATION_LANDSCAPE
        val orientation = if (isLandscape) ScreenOrientation.LANDSCAPE else ScreenOrientation.PORTRAIT

        // Column and row counts based on orientation
        val columns = if (isLandscape) 6 else 4
        val rows = if (isLandscape) 3 else 5

        // Icon size DP calculation
        val iconSizeDp = when (iconSize) {
            IconSize.SMALL -> 40.dp
            IconSize.MEDIUM -> 48.dp
            IconSize.LARGE -> 56.dp
        }

        // Density spacing DP calculation
        val gridSpacingDp = when (density) {
            LayoutDensity.COMPACT -> 8.dp
            LayoutDensity.BALANCED -> 14.dp
            LayoutDensity.SPACIOUS -> 20.dp
        }

        val sidePaddingDp = when (density) {
            LayoutDensity.COMPACT -> 12.dp
            LayoutDensity.BALANCED -> 16.dp
            LayoutDensity.SPACIOUS -> 24.dp
        }

        // Safe areas for Dynamic Bar and Dock
        val topPaddingDp = if (isDynamicBarVisible) 64.dp else 24.dp
        val bottomPaddingDp = if (isDockVisible) (if (isLandscape) 72.dp else 96.dp) else 24.dp

        val usableWidthDp = (maxWidthDp - (sidePaddingDp * 2) - (gridSpacingDp * (columns - 1))).coerceAtLeast(100.dp)
        val usableHeightDp = (maxHeightDp - topPaddingDp - bottomPaddingDp - (gridSpacingDp * (rows - 1))).coerceAtLeast(100.dp)

        val cellWidthDp = (usableWidthDp / columns).coerceAtLeast(40.dp)
        val cellHeightDp = (usableHeightDp / rows).coerceAtLeast(40.dp)

        return WorkspaceGeometry(
            orientation = orientation,
            screenWidthDp = maxWidthDp,
            screenHeightDp = maxHeightDp,
            columns = columns,
            rows = rows,
            cellWidthDp = cellWidthDp,
            cellHeightDp = cellHeightDp,
            iconSizeDp = iconSizeDp,
            gridSpacingDp = gridSpacingDp,
            sidePaddingDp = sidePaddingDp,
            topPaddingDp = topPaddingDp,
            bottomPaddingDp = bottomPaddingDp,
            pageIndex = 0
        )
    }
}
