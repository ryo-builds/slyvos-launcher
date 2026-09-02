package com.slyvos.launcher.update.repository

import android.content.Context
import com.slyvos.launcher.update.config.SlyvosUpdateConfig
import com.slyvos.launcher.update.model.BuildMetadata
import com.slyvos.launcher.update.model.UpdateStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

interface UpdateRepository {
    val updateStatus: StateFlow<UpdateStatus>
    val currentBuild: BuildMetadata
    suspend fun checkForUpdates(): UpdateStatus
}

class DefaultUpdateRepository(
    override val currentBuild: BuildMetadata,
    private val remoteDataSource: UpdateDataSource,
    private val context: Context? = null
) : UpdateRepository {

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.UpToDate(currentBuild))
    override val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    private val prefs = context?.getSharedPreferences("slyvos_update_prefs", Context.MODE_PRIVATE)

    override suspend fun checkForUpdates(): UpdateStatus = checkForUpdatesInternal(force = true)

    suspend fun checkForUpdatesThrottled(): UpdateStatus = checkForUpdatesInternal(force = false)

    private suspend fun checkForUpdatesInternal(force: Boolean): UpdateStatus = withContext(Dispatchers.IO) {
        if (!force && prefs != null) {
            val lastCheck = prefs.getLong("last_check_timestamp", 0L)
            val now = System.currentTimeMillis()
            if (now - lastCheck < SlyvosUpdateConfig.CHECK_THROTTLE_MS) {
                return@withContext _updateStatus.value
            }
        }

        _updateStatus.value = UpdateStatus.Checking
        val result = remoteDataSource.fetchLatestBuild()
        val newStatus = result.fold(
            onSuccess = { latest ->
                prefs?.edit()?.putLong("last_check_timestamp", System.currentTimeMillis())?.apply()

                val isNewerBuild = latest.buildNumber > currentBuild.buildNumber || latest.versionCode > currentBuild.versionCode
                val isMandatory = currentBuild.buildNumber < (latest.sha256?.toIntOrNull() ?: 0) || (latest.downloadUrl?.contains("mandatory") == true)

                if (isNewerBuild) {
                    UpdateStatus.UpdateAvailable(
                        currentBuild = currentBuild,
                        latestBuild = latest,
                        isMandatory = isMandatory
                    )
                } else {
                    UpdateStatus.UpToDate(currentBuild)
                }
            },
            onFailure = { throwable ->
                UpdateStatus.Error(throwable.message ?: "Network error checking update server")
            }
        )
        _updateStatus.value = newStatus
        newStatus
    }
}
