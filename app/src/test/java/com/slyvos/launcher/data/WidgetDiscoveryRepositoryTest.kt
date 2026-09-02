package com.slyvos.launcher.data

import android.appwidget.AppWidgetProviderInfo
import com.slyvos.launcher.data.model.SlyvosWidgetProviderInfo
import com.slyvos.launcher.data.repository.SystemWidgetDiscoveryRepository
import com.slyvos.launcher.data.repository.WidgetDiscoveryRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetDiscoveryRepositoryTest {

    @Test
    fun calculateSpan_calculatesCorrectCellSpans() {
        assertEquals(2, SystemWidgetDiscoveryRepository.calculateSpan(0))
        assertEquals(1, SystemWidgetDiscoveryRepository.calculateSpan(40))
        assertEquals(2, SystemWidgetDiscoveryRepository.calculateSpan(110))
        assertEquals(3, SystemWidgetDiscoveryRepository.calculateSpan(180))
        assertEquals(4, SystemWidgetDiscoveryRepository.calculateSpan(250))
        assertEquals(4, SystemWidgetDiscoveryRepository.calculateSpan(500))
    }

    @Test
    fun filterWidgetProviders_filtersByLabelAppNameOrPackage() {
        val repo = FakeWidgetDiscoveryRepository()
        val dummyList = listOf(
            SlyvosWidgetProviderInfo(
                providerInfo = AppWidgetProviderInfo(),
                label = "Analog Clock",
                appName = "Clock App",
                packageName = "com.android.deskclock"
            ),
            SlyvosWidgetProviderInfo(
                providerInfo = AppWidgetProviderInfo(),
                label = "Weather Forecast",
                appName = "Weather",
                packageName = "com.sec.android.weather"
            )
        )

        val clockResults = repo.filterWidgetProviders(dummyList, "clock")
        assertEquals(1, clockResults.size)
        assertEquals("Analog Clock", clockResults[0].label)

        val weatherResults = repo.filterWidgetProviders(dummyList, "weather")
        assertEquals(1, weatherResults.size)
        assertEquals("Weather Forecast", weatherResults[0].label)

        val emptyResults = repo.filterWidgetProviders(dummyList, "nonexistent")
        assertEquals(0, emptyResults.size)
    }

    private class FakeWidgetDiscoveryRepository : WidgetDiscoveryRepository {
        override suspend fun getAvailableWidgetProviders(): List<SlyvosWidgetProviderInfo> = emptyList()

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
    }
}
