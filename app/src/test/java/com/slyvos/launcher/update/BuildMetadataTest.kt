package com.slyvos.launcher.update

import com.slyvos.launcher.update.model.BuildMetadata
import com.slyvos.launcher.update.model.ReleaseStage
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildMetadataTest {

    @Test
    fun createCurrent_formatsBuildNumberCorrectly() {
        val meta = BuildMetadata.createCurrent(
            versionCode = 200,
            buildNumber = 2,
            stageName = "PRE_ALPHA",
            versionName = "Pre-Alpha Build #002",
            timestamp = 1700000000000L
        )

        assertEquals(200, meta.versionCode)
        assertEquals(2, meta.buildNumber)
        assertEquals(ReleaseStage.PRE_ALPHA, meta.stage)
        assertEquals("Pre-Alpha Build #002", meta.versionName)
        assertEquals("Slyvos Pre-Alpha Build #002", meta.buildNumberFormatted)
    }

    @Test
    fun createCurrent_handlesAlphaStage() {
        val meta = BuildMetadata.createCurrent(
            versionCode = 1500,
            buildNumber = 15,
            stageName = "ALPHA",
            versionName = "Alpha Build #015",
            timestamp = 1700000000000L
        )

        assertEquals(1500, meta.versionCode)
        assertEquals(15, meta.buildNumber)
        assertEquals(ReleaseStage.ALPHA, meta.stage)
        assertEquals("Slyvos Alpha Build #015", meta.buildNumberFormatted)
    }

    @Test
    fun createCurrent_handlesUnknownStageFallback() {
        val meta = BuildMetadata.createCurrent(
            versionCode = 10,
            buildNumber = 1,
            stageName = "UNKNOWN_FUTURE_STAGE",
            versionName = "Build #001",
            timestamp = 1700000000000L
        )

        assertEquals(ReleaseStage.PRE_ALPHA, meta.stage)
        assertEquals("Slyvos Pre-Alpha Build #001", meta.buildNumberFormatted)
    }
}
