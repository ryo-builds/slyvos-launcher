package com.slyvos.launcher.data.repository

import android.content.Context
import android.content.Intent
import com.slyvos.launcher.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AppRepository {
    suspend fun getInstalledLauncherApps(): List<AppInfo>
    suspend fun getDockApps(): List<AppInfo>
}

class PackageManagerAppRepository(
    private val context: Context,
    private val personalizationRepository: PersonalizationRepository? = null
) : AppRepository {

    override suspend fun getInstalledLauncherApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                AppInfo(
                    packageName = resolveInfo.activityInfo.packageName,
                    className = resolveInfo.activityInfo.name,
                    label = resolveInfo.loadLabel(pm).toString(),
                    icon = try { resolveInfo.loadIcon(pm) } catch (e: Exception) { null }
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    override suspend fun getDockApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val allApps = getInstalledLauncherApps()
        val customPackages = personalizationRepository?.getPersonalization()?.dock?.customDockPackages.orEmpty()

        val dockList = mutableListOf<AppInfo>()

        if (customPackages.isNotEmpty()) {
            // Add custom packages if currently installed on device
            for (pkg in customPackages) {
                val found = allApps.firstOrNull { it.packageName == pkg }
                if (found != null && !dockList.any { it.packageName == found.packageName }) {
                    dockList.add(found)
                }
                if (dockList.size >= 5) break
            }
        }

        // Default fallback packages if custom packages are fewer than 5 or not set
        if (dockList.size < 5) {
            val preferredPackages = listOf(
                "com.google.android.dialer", "com.samsung.android.dialer", "com.android.dialer",
                "com.google.android.apps.messaging", "com.samsung.android.messaging", "com.android.mms",
                "com.android.chrome", "com.sec.android.app.sbrowser",
                "com.google.android.GoogleCamera", "com.sec.android.app.camera", "com.android.camera",
                "com.android.settings"
            )

            for (pkg in preferredPackages) {
                val found = allApps.firstOrNull { it.packageName == pkg }
                if (found != null && !dockList.any { it.packageName == found.packageName }) {
                    dockList.add(found)
                }
                if (dockList.size >= 5) break
            }
        }

        // General fallback if still < 5
        if (dockList.size < 5) {
            for (app in allApps) {
                if (!dockList.any { it.packageName == app.packageName }) {
                    dockList.add(app)
                }
                if (dockList.size >= 5) break
            }
        }

        dockList
    }
}
