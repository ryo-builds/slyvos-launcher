package com.slyvos.launcher.ui.home

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.slyvos.launcher.data.model.AppDrawerFilter
import com.slyvos.launcher.data.model.AppInfo
import com.slyvos.launcher.data.repository.AppRepository
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
    val isLoadingApps: Boolean = true
)

class HomeScreenViewModel(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun initTimeAndApps(context: Context) {
        viewModelScope.launch {
            loadApps()
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
                // Reset search query when closing drawer
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
}

class HomeScreenViewModelFactory(
    private val appRepository: AppRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeScreenViewModel(appRepository) as T
    }
}
