package com.slyvos.launcher.data.model

object AppDrawerFilter {
    fun filterAndSort(apps: List<AppInfo>, query: String): List<AppInfo> {
        val trimmed = query.trim().lowercase()
        val list = if (trimmed.isEmpty()) {
            apps
        } else {
            apps.filter { app ->
                app.label.lowercase().contains(trimmed) ||
                app.packageName.lowercase().contains(trimmed)
            }
        }
        return list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }
}
