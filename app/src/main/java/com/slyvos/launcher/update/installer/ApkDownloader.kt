package com.slyvos.launcher.update.installer

import android.content.Context
import com.slyvos.launcher.update.config.SlyvosUpdateConfig
import com.slyvos.launcher.update.model.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class ApkDownloader(
    private val context: Context
) {
    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus.asStateFlow()

    private var currentDownloadJob: java.util.concurrent.atomic.AtomicBoolean = java.util.concurrent.atomic.AtomicBoolean(false)

    suspend fun downloadAndVerifyApk(
        downloadUrl: String,
        expectedSha256: String,
        targetFileName: String = "slyvos-update.apk"
    ): DownloadStatus = withContext(Dispatchers.IO) {
        if (!currentDownloadJob.compareAndSet(false, true)) {
            return@withContext _downloadStatus.value
        }

        try {
            _downloadStatus.value = DownloadStatus.Downloading(0, 0, -1)

            val downloadDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val outputFile = File(downloadDir, targetFileName)
            if (outputFile.exists()) outputFile.delete()

            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = SlyvosUpdateConfig.CONNECT_TIMEOUT_MS
                readTimeout = SlyvosUpdateConfig.READ_TIMEOUT_MS
                setRequestProperty("User-Agent", "Slyvos-Launcher-Updater")
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val err = "HTTP $responseCode download failure"
                    _downloadStatus.value = DownloadStatus.Error(err)
                    return@withContext _downloadStatus.value
                }

                val totalLength = connection.contentLengthLong
                var bytesReadTotal = 0L

                connection.inputStream.use { input ->
                    outputFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            bytesReadTotal += bytes

                            val percent = if (totalLength > 0) ((bytesReadTotal * 100) / totalLength).toInt() else 0
                            _downloadStatus.value = DownloadStatus.Downloading(percent, bytesReadTotal, totalLength)

                            bytes = input.read(buffer)
                        }
                    }
                }

                // Checksum Verification Phase
                if (expectedSha256.isNotBlank()) {
                    _downloadStatus.value = DownloadStatus.VerifyingChecksum
                    val calculatedSha256 = calculateFileSha256(outputFile)

                    if (!calculatedSha256.equals(expectedSha256.trim(), ignoreCase = true)) {
                        outputFile.delete()
                        val errStr = "SHA-256 Checksum mismatch! Expected $expectedSha256, got $calculatedSha256"
                        _downloadStatus.value = DownloadStatus.Error(errStr)
                        return@withContext _downloadStatus.value
                    }
                }

                val ready = DownloadStatus.ReadyToInstall(outputFile, expectedSha256)
                _downloadStatus.value = ready
                ready
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            val errStatus = DownloadStatus.Error(e.message ?: "Download failed")
            _downloadStatus.value = errStatus
            errStatus
        } finally {
            currentDownloadJob.set(false)
        }
    }

    fun resetStatus() {
        _downloadStatus.value = DownloadStatus.Idle
    }

    private fun calculateFileSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytes = input.read(buffer)
            while (bytes >= 0) {
                digest.update(buffer, 0, bytes)
                bytes = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
