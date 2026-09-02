package com.slyvos.launcher.data.model

import android.appwidget.AppWidgetProviderInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable

/**
 * Domain representation of an active widget placed on the Slyvos Home Screen.
 */
data class SlyvosWidget(
    val appWidgetId: Int,
    val providerPackage: String,
    val providerClass: String,
    val cellRow: Int = 0,
    val cellCol: Int = 0,
    val spanX: Int = 2,
    val spanY: Int = 2,
    val isConfigured: Boolean = true
)

/**
 * Domain representation of an installed widget provider available in the Slyvos Widget Picker.
 */
data class SlyvosWidgetProviderInfo(
    val providerInfo: AppWidgetProviderInfo,
    val label: String,
    val appName: String,
    val packageName: String,
    val appIcon: Drawable? = null,
    val previewBitmap: Bitmap? = null,
    val previewDrawable: Drawable? = null,
    val minWidthDp: Int = 0,
    val minHeightDp: Int = 0,
    val targetCellWidth: Int = 2,
    val targetCellHeight: Int = 2
)
