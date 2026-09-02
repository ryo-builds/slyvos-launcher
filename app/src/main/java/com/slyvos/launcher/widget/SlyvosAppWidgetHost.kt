package com.slyvos.launcher.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

/**
 * Custom AppWidgetHost for Slyvos Launcher.
 * Manages widget host lifecycle and widget view allocation.
 */
class SlyvosAppWidgetHost(
    context: Context,
    hostId: Int = SLYVOS_HOST_ID
) : AppWidgetHost(context, hostId) {

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidgetInfo: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return SlyvosAppWidgetHostView(context)
    }

    companion object {
        const val SLYVOS_HOST_ID = 20822
    }
}

/**
 * Custom AppWidgetHostView for Slyvos Launcher.
 * Provides custom touch handling and rendering optimizations.
 */
class SlyvosAppWidgetHostView(context: Context) : AppWidgetHostView(context)
