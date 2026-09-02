package com.slyvos.launcher.update

import com.slyvos.launcher.update.model.BuildMetadata
import com.slyvos.launcher.update.model.UpdateStatus
import com.slyvos.launcher.update.repository.DefaultUpdateRepository
import com.slyvos.launcher.update.repository.LocalMockUpdateDataSource
import com.slyvos.launcher.update.repository.UpdateDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateRepositoryTest {

    private val currentBuild = BuildMetadata.createCurrent(
        versionCode = 2,
        buildNumber = 2,
        stageName = "PRE_ALPHA",
        versionName = "Pre-Alpha Build #002",
        timestamp = 1000L
    )

    @Test
    fun checkForUpdates_detectsNewerBuild() = runTest {
        val newerRemote = BuildMetadata.createCurrent(
            versionCode = 3,
            buildNumber = 3,
            stageName = "PRE_ALPHA",
            versionName = "Pre-Alpha Build #003",
            timestamp = 2000L
        )
        val remoteSource = LocalMockUpdateDataSource(newerRemote)
        val repo = DefaultUpdateRepository(currentBuild, remoteSource)

        val status = repo.checkForUpdates()
        assertTrue(status is UpdateStatus.UpdateAvailable)
        val avail = status as UpdateStatus.UpdateAvailable
        assertEquals(3, avail.latestBuild.buildNumber)
    }

    @Test
    fun checkForUpdates_ignoresSameBuild() = runTest {
        val sameRemote = BuildMetadata.createCurrent(
            versionCode = 2,
            buildNumber = 2,
            stageName = "PRE_ALPHA",
            versionName = "Pre-Alpha Build #002",
            timestamp = 1000L
        )
        val remoteSource = LocalMockUpdateDataSource(sameRemote)
        val repo = DefaultUpdateRepository(currentBuild, remoteSource)

        val status = repo.checkForUpdates()
        assertTrue(status is UpdateStatus.UpToDate)
    }

    @Test
    fun checkForUpdates_ignoresOlderBuild() = runTest {
        val olderRemote = BuildMetadata.createCurrent(
            versionCode = 1,
            buildNumber = 1,
            stageName = "PRE_ALPHA",
            versionName = "Pre-Alpha Build #001",
            timestamp = 500L
        )
        val remoteSource = LocalMockUpdateDataSource(olderRemote)
        val repo = DefaultUpdateRepository(currentBuild, remoteSource)

        val status = repo.checkForUpdates()
        assertTrue(status is UpdateStatus.UpToDate)
    }

    @Test
    fun checkForUpdates_handlesNetworkOrMetadataFailureGracefully() = runTest {
        val failingSource = object : UpdateDataSource {
            override suspend fun fetchLatestBuild(): Result<BuildMetadata> {
                return Result.failure(RuntimeException("Network connection refused"))
            }
        }
        val repo = DefaultUpdateRepository(currentBuild, failingSource)

        val status = repo.checkForUpdates()
        assertTrue(status is UpdateStatus.Error)
        val err = status as UpdateStatus.Error
        assertTrue(err.message.contains("Network connection refused"))
    }
}
