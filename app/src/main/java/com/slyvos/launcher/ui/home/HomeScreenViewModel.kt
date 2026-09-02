package com.slyvos.launcher.ui.home

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.slyvos.launcher.data.model.AppDrawerFilter
import com.slyvos.launcher.data.model.AppInfo
import com.slyvos.launcher.data.model.SlyvosWidget
import com.slyvos.launcher.data.model.SlyvosWidgetProviderInfo
import com.slyvos.launcher.data.repository.AppRepository
import com.slyvos.launcher.data.repository.PreferencesWidgetRepository
import com.slyvos.launcher.data.repository.SystemWidgetDiscoveryRepository
import com.slyvos.launcher.data.repository.WidgetDiscoveryRepository
import com.slyvos.launcher.data.repository.WidgetRepository
import com.slyvos.launcher.widget.SlyvosAppWidgetHost
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val timeFormatted: String = "",
    val amPmFormatted: String = "",
    val dateFormatted: String = "",
    val dockApps: List<AppInfo> = emptyList(),
    val allApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val filteredApps: List<AppInfo> = emptyList(),
    val isDrawerVisible: Boolean = false,
    val isLoadingApps: Boolean = true,
    // Phase 5 Widget State
    val placedWidgets: List<SlyvosWidget> = emptyList(),
    val availableWidgets: List<SlyvosWidgetProviderInfo> = emptyList(),
    val filteredWidgets: List<SlyvosWidgetProviderInfo> = emptyList(),
    val widgetSearchQuery: String = "",
    val isWidgetPickerVisible: Boolean = false,
    val pendingConfigWidget: SlyvosWidget? = null
)

class HomeScreenViewModel(
    private val appRepository: AppRepository,
    private val widgetRepository: WidgetRepository,
    private val widgetDiscoveryRepository: WidgetDiscoveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun initTimeAndApps(context: Context) {
        viewModelScope.launch {
            loadApps()
            loadWidgets()
        }
        viewModelScope.launch {
            tickClock(context)
        }
    }

    private suspend fun loadApps() {
        val dock = appRepository.getDockApps()
        val all = appRepository.getInstalledLauncherApps()
        val sortedAll = AppDrawerFilter.filterAndSort(all, "")
        _uiState.update {
            it.copy(
                dockApps = dock,
                allApps = sortedAll,
                filteredApps = sortedAll,
                isLoadingApps = false
            )
        }
    }

    private suspend fun loadWidgets() {
        val widgets = widgetRepository.getPlacedWidgets()
        _uiState.update { it.copy(placedWidgets = widgets) }
    }

    private suspend fun tickClock(context: Context) {
        while (true) {
            val now = Date()
            val is24Hour = DateFormat.is24HourFormat(context)

            val timePattern = if (is24Hour) "HH:mm" else "h:mm"
            val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
            val timeString = timeFormat.format(now)

            val amPmPattern = if (is24Hour) "" else "a"
            val amPmFormat = SimpleDateFormat(amPmPattern, Locale.getDefault())
            val amPmString = amPmFormat.format(now).uppercase()

            val datePattern = "EEEE, MMMM d"
            val dateFormat = SimpleDateFormat(datePattern, Locale.getDefault())
            val dateString = dateFormat.format(now)

            _uiState.update {
                it.copy(
                    timeFormatted = timeString,
                    amPmFormatted = amPmString,
                    dateFormatted = dateString
                )
            }
            delay(1000)
        }
    }

    fun setDrawerVisible(visible: Boolean) {
        _uiState.update {
            if (!visible) {
                val filtered = AppDrawerFilter.filterAndSort(it.allApps, "")
                it.copy(isDrawerVisible = false, searchQuery = "", filteredApps = filtered)
            } else {
                it.copy(isDrawerVisible = true)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            val filtered = AppDrawerFilter.filterAndSort(it.allApps, query)
            it.copy(searchQuery = query, filteredApps = filtered)
        }
    }

    fun clearSearchQuery() {
        onSearchQueryChanged("")
    }

    fun launchApp(context: Context, app: AppInfo) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                ?: Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName(app.packageName, app.className)
                }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Phase 5 Widget Management ---

    fun openWidgetPicker(context: Context) {
        viewModelScope.launch {
            val available = widgetDiscoveryRepository.getAvailableWidgetProviders()
            val filtered = widgetDiscoveryRepository.filterWidgetProviders(available, "")
            _uiState.update {
                it.copy(
                    isWidgetPickerVisible = true,
                    availableWidgets = available,
                    filteredWidgets = filtered,
                    widgetSearchQuery = ""
                )
            }
        }
    }

    fun closeWidgetPicker() {
        _uiState.update { it.copy(isWidgetPickerVisible = false, widgetSearchQuery = "") }
    }

    fun onWidgetSearchQueryChanged(query: String) {
        _uiState.update {
            val filtered = widgetDiscoveryRepository.filterWidgetProviders(it.availableWidgets, query)
            it.copy(widgetSearchQuery = query, filteredWidgets = filtered)
        }
    }

    fun clearWidgetSearchQuery() {
        onWidgetSearchQueryChanged("")
    }

    fun selectAndAddWidget(
        context: Context,
        providerInfo: SlyvosWidgetProviderInfo,
        appWidgetHost: SlyvosAppWidgetHost,
        onLaunchConfigure: (Intent, Int) -> Unit
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetId = appWidgetHost.allocateAppWidgetId()

        val providerComponent = providerInfo.providerInfo.provider
        val bound = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, providerComponent)

        if (!bound) {
            // Launcher bound request fallback if required
            val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerComponent)
            }
            onLaunchConfigure(bindIntent, appWidgetId)
            return
        }

        val newWidget = SlyvosWidget(
            appWidgetId = appWidgetId,
            providerPackage = providerComponent.packageName,
            providerClass = providerComponent.className,
            spanX = providerInfo.targetCellWidth,
            spanY = providerInfo.targetCellHeight
        )

        val configureComponent = providerInfo.providerInfo.configure
        if (configureComponent != null) {
            // Widget requires configuration activity
            val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = configureComponent
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            _uiState.update { it.copy(pendingConfigWidget = newWidget) }
            onLaunchConfigure(configIntent, appWidgetId)
        } else {
            // Directly add widget
            saveWidget(newWidget)
            closeWidgetPicker()
        }
    }

    fun onWidgetConfigureResult(resultCode: Int, appWidgetHost: SlyvosAppWidgetHost) {
        val pending = _uiState.value.pendingConfigWidget ?: return
        if (resultCode == Activity.RESULT_OK) {
            saveWidget(pending)
            closeWidgetPicker()
        } else {
            // Configuration cancelled; delete allocated ID
            appWidgetHost.deleteAppWidgetId(pending.appWidgetId)
        }
        _uiState.update { it.copy(pendingConfigWidget = null) }
    }

    private fun saveWidget(widget: SlyvosWidget) {
        viewModelScope.launch {
            widgetRepository.saveWidget(widget)
            loadWidgets()
        }
    }

    fun removeWidget(appWidgetId: Int, appWidgetHost: SlyvosAppWidgetHost) {
        viewModelScope.launch {
            appWidgetHost.deleteAppWidgetId(appWidgetId)
            widgetRepository.removeWidget(appWidgetId)
            loadWidgets()
        }
    }

    fun resizeWidget(appWidgetId: Int, spanX: Int, spanY: Int) {
        viewModelScope.launch {
            widgetRepository.updateWidgetPosition(appWidgetId, row = 0, col = 0, spanX = spanX, spanY = spanY)
            loadWidgets()
        }
    }
}

class HomeScreenViewModelFactory(
    private val appRepository: AppRepository,
    private val widgetRepository: WidgetRepository,
    private val widgetDiscoveryRepository: WidgetDiscoveryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeScreenViewModel(appRepository, widgetRepository, widgetDiscoveryRepository) as T
    }
}
