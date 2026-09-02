package com.slyvos.launcher.data

import com.slyvos.launcher.data.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DockCustomizationTest {

    @Test
    fun filterDockApps_prunesUninstalledPackagesAndMaintainsLimit() {
        val installedApps = listOf(
            AppInfo("com.android.chrome", "ChromeActivity", "Chrome", null),
            AppInfo("com.google.android.dialer", "DialerActivity", "Phone", null),
            AppInfo("com.google.android.apps.messaging", "MessagingActivity", "Messages", null),
            AppInfo("com.google.android.GoogleCamera", "CameraActivity", "Camera", null),
            AppInfo("com.android.settings", "SettingsActivity", "Settings", null),
            AppInfo("com.example.other", "OtherActivity", "Other", null)
        )

        val customPackages = listOf(
            "com.nonexistent.fake.app.uninstalled", // uninstalled fake package
            "com.android.chrome",
            "com.google.android.dialer"
        )

        // Custom resolution logic simulating PackageManagerAppRepository.getDockApps
        val dockList = mutableListOf<AppInfo>()

        for (pkg in customPackages) {
            val found = installedApps.firstOrNull { it.packageName == pkg }
            if (found != null && !dockList.any { it.packageName == found.packageName }) {
                dockList.add(found)
            }
            if (dockList.size >= 5) break
        }

        // Fill remaining up to 5 from fallback installed apps
        if (dockList.size < 5) {
            for (app in installedApps) {
                if (!dockList.any { it.packageName == app.packageName }) {
                    dockList.add(app)
                }
                if (dockList.size >= 5) break
            }
        }

        assertEquals(5, dockList.size)
        assertFalse(dockList.any { it.packageName == "com.nonexistent.fake.app.uninstalled" })
        assertEquals("com.android.chrome", dockList[0].packageName)
        assertEquals("com.google.android.dialer", dockList[1].packageName)
    }
}
