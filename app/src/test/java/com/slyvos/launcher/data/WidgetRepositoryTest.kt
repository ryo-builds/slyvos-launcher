package com.slyvos.launcher.data

import com.slyvos.launcher.data.model.SlyvosWidget
import com.slyvos.launcher.data.repository.PreferencesWidgetRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetRepositoryTest {

    @Test
    fun parseWidgetsJson_deserializesValidJsonArray() {
        val json = """
            [
                {"appWidgetId":101,"providerPackage":"com.example.clock","providerClass":"com.example.clock.ClockWidget","cellRow":0,"cellCol":0,"spanX":2,"spanY":2,"isConfigured":true},
                {"appWidgetId":102,"providerPackage":"com.example.weather","providerClass":"com.example.weather.WeatherWidget","cellRow":1,"cellCol":0,"spanX":4,"spanY":2,"isConfigured":true}
            ]
        """.trimIndent()

        val widgets = PreferencesWidgetRepository.parseWidgetsJson(json)

        assertEquals(2, widgets.size)
        assertEquals(101, widgets[0].appWidgetId)
        assertEquals("com.example.clock", widgets[0].providerPackage)
        assertEquals("com.example.clock.ClockWidget", widgets[0].providerClass)
        assertEquals(2, widgets[0].spanX)

        assertEquals(102, widgets[1].appWidgetId)
        assertEquals("com.example.weather", widgets[1].providerPackage)
        assertEquals(4, widgets[1].spanX)
    }

    @Test
    fun parseWidgetsJson_handlesEmptyJsonArray() {
        val widgets = PreferencesWidgetRepository.parseWidgetsJson("[]")
        assertTrue(widgets.isEmpty())
    }

    @Test
    fun parseWidgetsJson_handlesInvalidJsonGracefully() {
        val widgets = PreferencesWidgetRepository.parseWidgetsJson("invalid json input")
        assertTrue(widgets.isEmpty())
    }
}
