package com.slyvos.launcher.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slyvos.launcher.data.model.SlyvosWidget
import com.slyvos.launcher.ui.home.model.WorkspaceGeometry
import com.slyvos.launcher.widget.SlyvosAppWidgetHost

@Composable
fun WidgetGridCanvas(
    placedWidgets: List<SlyvosWidget>,
    appWidgetHost: SlyvosAppWidgetHost,
    onRemoveWidget: (Int) -> Unit,
    onResizeWidget: (Int, Int, Int) -> Unit,
    geometry: WorkspaceGeometry? = null,
    modifier: Modifier = Modifier
) {
    if (placedWidgets.isEmpty()) return

    val sidePadding = geometry?.sidePaddingDp ?: 16.dp
    val spacing = geometry?.gridSpacingDp ?: 12.dp

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = sidePadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(placedWidgets, key = { it.appWidgetId }) { widget ->
            // Calculate responsive height based on cellHeightDp or fallback
            val unitHeight: Dp = geometry?.cellHeightDp ?: 90.dp
            val widgetHeight = (unitHeight * widget.spanY).coerceIn(80.dp, 360.dp)

            WidgetHostItem(
                widget = widget,
                appWidgetHost = appWidgetHost,
                onRemoveWidget = onRemoveWidget,
                onResizeWidget = onResizeWidget,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(widgetHeight)
            )
        }
    }
}
