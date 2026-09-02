package com.slyvos.launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.slyvos.launcher.data.model.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

interface AppRepository {
    val appsFlow: StateFlow<List<AppInfo>>
    suspend fun getInstalledLauncherApps(): List<AppInfo>
    suspend fun getDockApps(): List<AppInfo>
    fun getCachedIcon(packageName: String, className: String): ImageBitmap?
    fun cacheIcon(packageName: String, className: String, bitmap: ImageBitmap)
    fun clearIconCache()
    fun startObservingPackageChanges(scope: CoroutineScope)
    fun stopObservingPackageChanges()
}

class PackageManagerAppRepository(
    private val context: Context,
    private val personalizationRepository: PersonalizationRepository? = null
) : AppRepository {

    // Thread-safe Bounded LinkedHashMap LRU Cache (max 128 entries)
    private val iconCacheMap = object : LinkedHashMap<String, ImageBitmap>(128, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean {
            return size > 128
        }
    }
    private val iconCache = Collections.synchronizedMap(iconCacheMap)

    private val _appsFlow = MutableStateFlow<List<AppInfo>>(emptyList())
    override val appsFlow: StateFlow<List<AppInfo>> = _appsFlow.asStateFlow()

    private var launcherAppsCallback: LauncherApps.Callback? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun getCachedIcon(packageName: String, className: String): ImageBitmap? {
        val key = "$packageName/$className"
        return iconCache[key]
    }

    override fun cacheIcon(packageName: String, className: String, bitmap: ImageBitmap) {
        val key = "$packageName/$className"
        iconCache[key] = bitmap
    }

    override fun clearIconCache() {
        iconCache.clear()
    }

    override suspend fun getInstalledLauncherApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val apps = resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val cls = resolveInfo.activityInfo.name
                val key = "$pkg/$cls"

                val iconDrawable = try { resolveInfo.loadIcon(pm) } catch (e: Exception) { null }
                if (iconDrawable != null && iconCache[key] == null) {
                    val bitmap = iconDrawable.toImageBitmap()
                    iconCache[key] = bitmap
                }

                AppInfo(
                    packageName = pkg,
                    className = cls,
                    label = resolveInfo.loadLabel(pm).toString(),
                    icon = iconDrawable
                )
            }
            .sortedBy { it.label.lowercase() }

        _appsFlow.value = apps
        apps
    }

    override suspend fun getDockApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val allApps = if (_appsFlow.value.isNotEmpty()) _appsFlow.value else getInstalledLauncherApps()
        val customPackages = personalizationRepository?.getPersonalization()?.dock?.customDockPackages.orEmpty()

        val dockList = mutableListOf<AppInfo>()

        if (customPackages.isNotEmpty()) {
            val validCustomPackages = mutableListOf<String>()
            for (pkg in customPackages) {
                val found = allApps.firstOrNull { it.packageName == pkg }
                if (found != null && !dockList.any { it.packageName == found.packageName }) {
                    dockList.add(found)
                    validCustomPackages.add(pkg)
                }
                if (dockList.size >= 5) break
            }

            // Prune uninstalled packages from personalization repository if any were uninstalled
            if (validCustomPackages.size != customPackages.size) {
                personalizationRepository?.updateDockPackages(validCustomPackages)
            }
        }

        // Default fallback packages if custom packages are fewer than 5
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

    override fun startObservingPackageChanges(scope: CoroutineScope) {
        if (launcherAppsCallback != null) return

        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return

        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String, user: UserHandle) {
                scope.launch(Dispatchers.IO) {
                    getInstalledLauncherApps()
                }
            }

            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                // Evict evicted package entries from icon cache
                synchronized(iconCache) {
                    val keysToRemove = iconCache.keys.filter { it.startsWith("$packageName/") }
                    keysToRemove.forEach { iconCache.remove(it) }
                }

                scope.launch(Dispatchers.IO) {
                    getInstalledLauncherApps()
                    getDockApps() // Automatically trigger dock pruning
                }
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                onPackageRemoved(packageName, user)
            }

            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                scope.launch(Dispatchers.IO) {
                    getInstalledLauncherApps()
                }
            }

            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                scope.launch(Dispatchers.IO) {
                    getInstalledLauncherApps()
                }
            }
        }

        launcherAppsCallback = callback
        launcherApps.registerCallback(callback, mainHandler)
    }

    override fun stopObservingPackageChanges() {
        launcherAppsCallback?.let { callback ->
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            launcherApps?.unregisterCallback(callback)
            launcherAppsCallback = null
        }
    }
}

private fun Drawable.toImageBitmap(): ImageBitmap {
    if (this is BitmapDrawable && this.bitmap != null) {
        return this.bitmap.asImageBitmap()
    }
    val width = if (intrinsicWidth > 0) intrinsicWidth else 96
    val height = if (intrinsicHeight > 0) intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap.asImageBitmap()
}
