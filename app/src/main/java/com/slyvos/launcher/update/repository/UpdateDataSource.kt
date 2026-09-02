package com.slyvos.launcher.update.repository

import com.slyvos.launcher.update.model.BuildMetadata
import com.slyvos.launcher.update.model.ReleaseStage

interface UpdateDataSource {
    suspend fun fetchLatestBuild(): Result<BuildMetadata>
}

/**
 * Default local/mock update source for Pre-Alpha development checking.
 * Can be replaced by a RemoteApiUpdateDataSource (e.g. Firebase App Distribution, GitHub Releases, custom backend API) later.
 */
class LocalMockUpdateDataSource(
    private val nextAvailableBuild: BuildMetadata? = null
) : UpdateDataSource {
    override suspend fun fetchLatestBuild(): Result<BuildMetadata> = runCatching {
        nextAvailableBuild ?: BuildMetadata(
            versionCode = 2,
            buildNumber = 2,
            stage = ReleaseStage.PRE_ALPHA,
            versionName = "Pre-Alpha Build #002",
            buildNumberFormatted = "Slyvos Pre-Alpha Build #002",
            releaseTimestamp = System.currentTimeMillis(),
            releaseNotes = listOf(
                "Integrated Slyvos Dynamic Bar system",
                "Added real-device System Quick Surface & Media controls",
                "Added Pre-Alpha Development Update System architecture"
            )
        )
    }
}
