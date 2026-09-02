package com.slyvos.launcher.data.repository

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Process
import com.slyvos.launcher.data.model.SlyvosWidgetProviderInfo

interface WidgetDiscoveryRepository {
    suspend fun getAvailableWidgetProviders(): List<SlyvosWidgetProviderInfo>
    fun filterWidgetProviders(providers: List<SlyvosWidgetProviderInfo>, query: String): List<SlyvosWidgetProviderInfo>
}

open class SystemWidgetDiscoveryRepository(
    private val context: Context
) : WidgetDiscoveryRepository {

    override suspend fun getAvailableWidgetProviders(): List<SlyvosWidgetProviderInfo> {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val packageManager = context.packageManager

        val providers: List<AppWidgetProviderInfo> = try {
            appWidgetManager.getInstalledProvidersForProfile(Process.myUserHandle())
        } catch (e: Throwable) {
            try {
                appWidgetManager.installedProviders ?: emptyList()
            } catch (ex: Throwable) {
                emptyList()
            }
        }

        return providers.map { providerInfo ->
            val label = try {
                val load = providerInfo.loadLabel(packageManager)
                if (!load.isNullOrEmpty()) load.toString() else providerInfo.provider.shortClassName
            } catch (e: Exception) {
                providerInfo.provider.shortClassName
            }

            val appName = try {
                val appInfo = packageManager.getApplicationInfo(providerInfo.provider.packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                providerInfo.provider.packageName
            }

            val appIcon = try {
                packageManager.getApplicationIcon(providerInfo.provider.packageName)
            } catch (e: Exception) {
                null
            }

            val previewDrawable = try {
                providerInfo.loadPreviewImage(context, 0)
                    ?: providerInfo.loadIcon(context, 0)
            } catch (e: Exception) {
                null
            }

            val minWidth = providerInfo.minWidth
            val minHeight = providerInfo.minHeight
            val targetSpanX = (providerInfo.targetCellWidth.takeIf { it > 0 }
                ?: calculateSpan(minWidth)).coerceIn(1, 4)
            val targetSpanY = (providerInfo.targetCellHeight.takeIf { it > 0 }
                ?: calculateSpan(minHeight)).coerceIn(1, 4)

            SlyvosWidgetProviderInfo(
                providerInfo = providerInfo,
                label = label,
                appName = appName,
                packageName = providerInfo.provider.packageName,
                appIcon = appIcon,
                previewDrawable = previewDrawable,
                minWidthDp = minWidth,
                minHeightDp = minHeight,
                targetCellWidth = targetSpanX,
                targetCellHeight = targetSpanY
            )
        }.sortedWith(compareBy({ it.appName.lowercase() }, { it.label.lowercase() }))
    }

    override fun filterWidgetProviders(
        providers: List<SlyvosWidgetProviderInfo>,
        query: String
    ): List<SlyvosWidgetProviderInfo> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return providers
        return providers.filter { item ->
            item.label.contains(trimmed, ignoreCase = true) ||
                    item.appName.contains(trimmed, ignoreCase = true) ||
                    item.packageName.contains(trimmed, ignoreCase = true)
        }
    }

    companion object {
        fun calculateSpan(sizeDp: Int): Int {
            if (sizeDp <= 0) return 2
            return ((sizeDp + 30) / 70).coerceIn(1, 4)
        }
    }
}
