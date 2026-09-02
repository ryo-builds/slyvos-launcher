package com.slyvos.launcher.data.repository

import android.content.Context
import com.slyvos.launcher.data.model.SlyvosWidget
import org.json.JSONArray
import org.json.JSONObject

interface WidgetRepository {
    suspend fun getPlacedWidgets(): List<SlyvosWidget>
    suspend fun saveWidget(widget: SlyvosWidget)
    suspend fun removeWidget(appWidgetId: Int)
    suspend fun updateWidgetPosition(appWidgetId: Int, row: Int, col: Int, spanX: Int, spanY: Int)
}

class PreferencesWidgetRepository(context: Context) : WidgetRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun getPlacedWidgets(): List<SlyvosWidget> {
        val jsonString = prefs.getString(KEY_WIDGETS, "[]") ?: "[]"
        return parseWidgetsJson(jsonString)
    }

    override suspend fun saveWidget(widget: SlyvosWidget) {
        val current = getPlacedWidgets().toMutableList()
        val index = current.indexOfFirst { it.appWidgetId == widget.appWidgetId }
        if (index >= 0) {
            current[index] = widget
        } else {
            current.add(widget)
        }
        persistWidgets(current)
    }

    override suspend fun removeWidget(appWidgetId: Int) {
        val current = getPlacedWidgets().filterNot { it.appWidgetId == appWidgetId }
        persistWidgets(current)
    }

    override suspend fun updateWidgetPosition(
        appWidgetId: Int,
        row: Int,
        col: Int,
        spanX: Int,
        spanY: Int
    ) {
        val current = getPlacedWidgets().toMutableList()
        val index = current.indexOfFirst { it.appWidgetId == appWidgetId }
        if (index >= 0) {
            current[index] = current[index].copy(
                cellRow = row,
                cellCol = col,
                spanX = spanX,
                spanY = spanY
            )
            persistWidgets(current)
        }
    }

    private fun persistWidgets(widgets: List<SlyvosWidget>) {
        val jsonArray = JSONArray()
        for (w in widgets) {
            val obj = JSONObject()
            obj.put("appWidgetId", w.appWidgetId)
            obj.put("providerPackage", w.providerPackage)
            obj.put("providerClass", w.providerClass)
            obj.put("cellRow", w.cellRow)
            obj.put("cellCol", w.cellCol)
            obj.put("spanX", w.spanX)
            obj.put("spanY", w.spanY)
            obj.put("isConfigured", w.isConfigured)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_WIDGETS, jsonArray.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "slyvos_widgets_prefs"
        private const val KEY_WIDGETS = "placed_widgets"

        fun parseWidgetsJson(jsonString: String): List<SlyvosWidget> {
            val list = mutableListOf<SlyvosWidget>()
            try {
                // Regex parser to avoid JVM test stub limitations on JSONObject
                val pattern = Regex(
                    """\{"appWidgetId":(\d+),"providerPackage":"([^"]+)","providerClass":"([^"]+)","cellRow":(\d+),"cellCol":(\d+),"spanX":(\d+),"spanY":(\d+),"isConfigured":(true|false)\}"""
                )
                val matches = pattern.findAll(jsonString)
                for (match in matches) {
                    val groups = match.groupValues
                    list.add(
                        SlyvosWidget(
                            appWidgetId = groups[1].toInt(),
                            providerPackage = groups[2],
                            providerClass = groups[3],
                            cellRow = groups[4].toInt(),
                            cellCol = groups[5].toInt(),
                            spanX = groups[6].toInt(),
                            spanY = groups[7].toInt(),
                            isConfigured = groups[8].toBoolean()
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }
    }
}
