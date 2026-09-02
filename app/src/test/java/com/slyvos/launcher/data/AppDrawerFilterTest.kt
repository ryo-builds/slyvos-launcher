package com.slyvos.launcher.data

import com.slyvos.launcher.data.model.AppDrawerFilter
import com.slyvos.launcher.data.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDrawerFilterTest {

    private val sampleApps = listOf(
        AppInfo("com.whatsapp", "WhatsAppActivity", "WhatsApp", null),
        AppInfo("org.telegram.messenger", "MainActivity", "Telegram", null),
        AppInfo("com.google.android.youtube", "YouTubeActivity", "YouTube", null),
        AppInfo("com.camera", "CameraActivity", "camera app", null),
        AppInfo("com.android.calculator2", "Calculator", "Calculator", null)
    )

    @Test
    fun filterAndSort_sortsAlphabeticallyCaseInsensitive_whenQueryIsEmpty() {
        val result = AppDrawerFilter.filterAndSort(sampleApps, "")

        assertEquals(5, result.size)
        assertEquals("Calculator", result[0].label)
        assertEquals("camera app", result[1].label)
        assertEquals("Telegram", result[2].label)
        assertEquals("WhatsApp", result[3].label)
        assertEquals("YouTube", result[4].label)
    }

    @Test
    fun filterAndSort_filtersByLabelCaseInsensitive() {
        val result = AppDrawerFilter.filterAndSort(sampleApps, "you")

        assertEquals(1, result.size)
        assertEquals("YouTube", result[0].label)
    }

    @Test
    fun filterAndSort_filtersByPackageNameCaseInsensitive() {
        val result = AppDrawerFilter.filterAndSort(sampleApps, "org.telegram")

        assertEquals(1, result.size)
        assertEquals("Telegram", result[0].label)
    }

    @Test
    fun filterAndSort_returnsEmptyList_whenNoMatchFound() {
        val result = AppDrawerFilter.filterAndSort(sampleApps, "nonexistentapp123")

        assertTrue(result.isEmpty())
    }

    @Test
    fun filterAndSort_trimsWhitespacesInSearchQuery() {
        val result = AppDrawerFilter.filterAndSort(sampleApps, "   whatsapp   ")

        assertEquals(1, result.size)
        assertEquals("WhatsApp", result[0].label)
    }
}
