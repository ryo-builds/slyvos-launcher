package com.slyvos.launcher.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val icon: Drawable?
)
