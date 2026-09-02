package com.slyvos.launcher.ui.home

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.slyvos.launcher.data.model.AppDrawerFilter
import com.slyvos.launcher.data.model.AppInfo
import com.slyvos.launcher.data.model.AppearanceSettings
import com.slyvos.launcher.data.model.GestureSettings
import com.slyvos.launcher.data.model.HomeLayoutSettings
import com.slyvos.launcher.data.model.SlyvosPersonalization
import com.slyvos.launcher.data.model.SlyvosWidget
import com.slyvos.launcher.data.model.SlyvosWidgetProviderInfo
import com.slyvos.launcher.data.repository.AppRepository
import com.slyvos.launcher.data.repository.PendingWidgetState
import com.slyvos.launcher.data.repository.PersonalizationRepository
import com.slyvos.launcher.data.repository.PreferencesPersonalizationRepository
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
    val pendingConfigWidget: SlyvosWidget? = null,
    // Phase 6 Personalization State
    val personalization: SlyvosPersonalization = SlyvosPersonalization(),
    val isPersonalizationSheetVisible: Boolean = false,
    val isDockAppPickerVisible: Boolean = false
)

class HomeScreenViewModel(
    private val appRepository: AppRepository,
    private val widgetRepository: WidgetRepository,
    private val widgetDiscoveryRepository: WidgetDiscoveryRepository,
    private val personalizationRepository: PersonalizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Observe real-time app list changes
        viewModelScope.launch {
            appRepository.appsFlow.collect { apps ->
                if (apps.isNotEmpty()) {
                    val filtered = AppDrawerFilter.filterAndSort(apps, _uiState.value.searchQuery)
                    _uiState.update {
                        it.copy(
                            allApps = apps,
                            filteredApps = filtered,
                            isLoadingApps = false
                        )
                    }
                    loadDockApps()
                }
            }
        }

        // Observe personalization changes
        viewModelScope.launch {
            personalizationRepository.personalization.collect { p ->
                _uiState.update { it.copy(personalization = p) }
                loadDockApps()
            }
        }

        // Start listening to real-time Android package changes
        appRepository.startObservingPackageChanges(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        appRepository.stopObservingPackageChanges()
    }

    fun initTimeAndApps(context: Context) {
        viewModelScope.launch {
            loadApps()
            loadWidgets()
            recoverPendingWidgetState(context)
        }
        viewModelScope.launch {
            tickClock(context)
        }
    }

    private suspend fun loadApps() {
        val all = appRepository.getInstalledLauncherApps()
        val dock = appRepository.getDockApps()
        val sortedAll = AppDrawerFilter.filterAndSort(all, _uiState.value.searchQuery)
        _uiState.update {
            it.copy(
                dockApps = dock,
                allApps = sortedAll,
                filteredApps = sortedAll,
                isLoadingApps = false
            )
        }
    }

    private suspend fun loadDockApps() {
        val dock = appRepository.getDockApps()
        _uiState.update { it.copy(dockApps = dock) }
    }

    private suspend fun loadWidgets() {
        val placed = widgetRepository.getPlacedWidgets()
        _uiState.update { it.copy(placedWidgets = placed) }
    }

    private fun recoverPendingWidgetState(context: Context) {
        val pending = widgetRepository.getPendingWidgetState() ?: return
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val info = appWidgetManager.getAppWidgetInfo(pending.appWidgetId)

        if (info != null) {
            // Check if widget is already saved
            val isAlreadyPlaced = _uiState.value.placedWidgets.any { it.appWidgetId == pending.appWidgetId }
            if (!isAlreadyPlaced) {
                val newWidget = SlyvosWidget(
                    appWidgetId = pending.appWidgetId,
                    providerPackage = pending.providerPackage,
                    providerClass = pending.providerClass,
                    spanX = 2,
                    spanY = 2
                )
                saveWidget(newWidget)
            }
        } else {
            // Provider unavailable or canceled; clear pending state
            widgetRepository.clearPendingWidgetState()
        }
    }

    private suspend fun tickClock(context: Context) {
        while (true) {
            val now = Date()
            val is24Hour = DateFormat.is24HourFormat(context)

            val timePattern = if (is24Hour) "HH:mm" else "h:mm"
            val amPmPattern = if (is24Hour) "" else "a"
            val datePattern = "EEEE, MMMM d"

            val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
            val amPmFormat = SimpleDateFormat(amPmPattern, Locale.getDefault())
            val dateFormat = SimpleDateFormat(datePattern, Locale.getDefault())

            val timeString = timeFormat.format(now)
            val amPmString = amPmFormat.format(now)
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

    // --- Phase 6 Personalization Management ---

    fun setPersonalizationSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isPersonalizationSheetVisible = visible) }
    }

    fun setDockAppPickerVisible(visible: Boolean) {
        _uiState.update { it.copy(isDockAppPickerVisible = visible) }
    }

    fun updateHomeLayout(transform: (HomeLayoutSettings) -> HomeLayoutSettings) {
        personalizationRepository.updateHomeLayout(transform)
    }

    fun updateAppearance(transform: (AppearanceSettings) -> AppearanceSettings) {
        personalizationRepository.updateAppearance(transform)
    }

    fun updateGestures(transform: (GestureSettings) -> GestureSettings) {
        personalizationRepository.updateGestures(transform)
    }

    fun saveDockPackages(packages: List<String>) {
        personalizationRepository.updateDockPackages(packages)
        viewModelScope.launch {
            loadDockApps()
        }
    }

    // --- Phase 5 & 7.1 Widget Management ---

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

        val newWidget = SlyvosWidget(
            appWidgetId = appWidgetId,
            providerPackage = providerComponent.packageName,
            providerClass = providerComponent.className,
            spanX = providerInfo.targetCellWidth,
            spanY = providerInfo.targetCellHeight
        )

        // Persist pending widget state across activity/process lifecycle
        widgetRepository.savePendingWidgetState(appWidgetId, providerComponent.packageName, providerComponent.className)
        _uiState.update { it.copy(pendingConfigWidget = newWidget) }

        if (!bound) {
            // Permission bind request dialog
            val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerComponent)
            }
            onLaunchConfigure(bindIntent, appWidgetId)
            return
        }

        val configureComponent = providerInfo.providerInfo.configure
        if (configureComponent != null) {
            // Launch widget configuration activity
            val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = configureComponent
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            onLaunchConfigure(configIntent, appWidgetId)
        } else {
            // Directly save widget and clear pending state
            saveWidget(newWidget)
            widgetRepository.clearPendingWidgetState()
            _uiState.update { it.copy(pendingConfigWidget = null) }
            closeWidgetPicker()
        }
    }

    fun onWidgetConfigureResult(
        context: Context,
        resultCode: Int,
        appWidgetHost: SlyvosAppWidgetHost,
        onLaunchConfigure: (Intent, Int) -> Unit
    ) {
        val pending = _uiState.value.pendingConfigWidget 
            ?: widgetRepository.getPendingWidgetState()?.let { state ->
                SlyvosWidget(state.appWidgetId, state.providerPackage, state.providerClass)
            } 
            ?: return

        if (resultCode == Activity.RESULT_OK) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val info = appWidgetManager.getAppWidgetInfo(pending.appWidgetId)
            val configureComponent = info?.configure

            if (configureComponent != null && info != null) {
                // If bind succeeded and widget has configure activity, launch configuration
                val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = configureComponent
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pending.appWidgetId)
                }
                onLaunchConfigure(configIntent, pending.appWidgetId)
            } else {
                // Finalize saving widget and clear pending state
                saveWidget(pending)
                widgetRepository.clearPendingWidgetState()
                _uiState.update { it.copy(pendingConfigWidget = null) }
                closeWidgetPicker()
            }
        } else {
            // Configuration or bind cancelled; delete allocated ID to prevent orphaned IDs
            appWidgetHost.deleteAppWidgetId(pending.appWidgetId)
            widgetRepository.clearPendingWidgetState()
            _uiState.update { it.copy(pendingConfigWidget = null) }
        }
    }

    private fun saveWidget(widget: SlyvosWidget) {
        viewModelScope.launch {
            widgetRepository.saveWidget(widget)
            widgetRepository.clearPendingWidgetState()
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
    private val widgetDiscoveryRepository: WidgetDiscoveryRepository,
    private val personalizationRepository: PersonalizationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeScreenViewModel(
            appRepository,
            widgetRepository,
            widgetDiscoveryRepository,
            personalizationRepository
        ) as T
    }
}
