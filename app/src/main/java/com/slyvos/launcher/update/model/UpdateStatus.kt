package com.slyvos.launcher.update.model

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data class UpToDate(val currentBuild: BuildMetadata) : UpdateStatus
    data class UpdateAvailable(
        val currentBuild: BuildMetadata,
        val latestBuild: BuildMetadata,
        val isMandatory: Boolean = false
    ) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}
