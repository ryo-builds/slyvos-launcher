package com.slyvos.launcher.update.model

import java.io.File

sealed interface DownloadStatus {
    data object Idle : DownloadStatus
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadStatus
    data object VerifyingChecksum : DownloadStatus
    data class ReadyToInstall(val apkFile: File, val checksumHex: String) : DownloadStatus
    data class Error(val message: String) : DownloadStatus
}
