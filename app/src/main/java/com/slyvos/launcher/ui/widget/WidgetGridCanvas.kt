package com.slyvos.launcher.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slyvos.launcher.data.model.SlyvosWidget
import com.slyvos.launcher.widget.SlyvosAppWidgetHost

@Composable
fun WidgetGridCanvas(
    placedWidgets: List<SlyvosWidget>,
    appWidgetHost: SlyvosAppWidgetHost,
    onRemoveWidget: (Int) -> Unit,
    onResizeWidget: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (placedWidgets.isEmpty()) return

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(placedWidgets, key = { it.appWidgetId }) { widget ->
            // Calculate height based on spanY
            val widgetHeight = (widget.spanY * 90).dp

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
