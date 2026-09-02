package com.slyvos.launcher.update.repository

import com.slyvos.launcher.update.config.SlyvosUpdateConfig
import com.slyvos.launcher.update.model.BuildMetadata
import com.slyvos.launcher.update.model.RemoteUpdateManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class HttpUpdateDataSource(
    private val manifestUrlProvider: () -> String = { SlyvosUpdateConfig.manifestUrl }
) : UpdateDataSource {

    override suspend fun fetchLatestBuild(): Result<BuildMetadata> = withContext(Dispatchers.IO) {
        runCatching {
            val urlString = manifestUrlProvider()
            require(urlString.isNotBlank()) { "Update manifest URL is blank" }

            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = SlyvosUpdateConfig.CONNECT_TIMEOUT_MS
                readTimeout = SlyvosUpdateConfig.READ_TIMEOUT_MS
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Slyvos-Launcher-Updater")
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw IllegalStateException("HTTP error $responseCode while checking updates")
                }

                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val manifest = RemoteUpdateManifest.fromJson(jsonText)
                manifest.toBuildMetadata()
            } finally {
                connection.disconnect()
            }
        }
    }
}
