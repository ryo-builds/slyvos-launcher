package com.slyvos.launcher.update

import com.slyvos.launcher.update.model.ReleaseStage
import com.slyvos.launcher.update.model.RemoteUpdateManifest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteUpdateManifestTest {

    @Test
    fun fromJson_parsesValidManifestCorrectly() {
        val json = """
            {
              "buildNumber": 3,
              "versionCode": 3,
              "versionName": "Pre-Alpha Build #003",
              "releaseStage": "PRE_ALPHA",
              "releaseNotes": "Bug fixes and performance improvements",
              "apkUrl": "https://example.com/slyvos-build-003.apk",
              "apkSha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
              "publishedAt": "2026-09-02T12:00:00Z",
              "minimumSupportedBuildNumber": 1
            }
        """.trimIndent()

        val manifest = RemoteUpdateManifest.fromJson(json)

        assertEquals(3, manifest.buildNumber)
        assertEquals(3, manifest.versionCode)
        assertEquals("Pre-Alpha Build #003", manifest.versionName)
        assertEquals("PRE_ALPHA", manifest.releaseStage)
        assertEquals("https://example.com/slyvos-build-003.apk", manifest.apkUrl)
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", manifest.apkSha256)
        assertEquals(1, manifest.minimumSupportedBuildNumber)

        val meta = manifest.toBuildMetadata()
        assertEquals(ReleaseStage.PRE_ALPHA, meta.stage)
        assertEquals("Slyvos Pre-Alpha Build #003", meta.buildNumberFormatted)
    }

    @Test(expected = Exception::class)
    fun fromJson_throwsExceptionOnMalformedJson() {
        RemoteUpdateManifest.fromJson("INVALID_JSON_CONTENT")
    }

    @Test(expected = IllegalArgumentException::class)
    fun fromJson_throwsExceptionOnBlankApkUrl() {
        val json = """
            {
              "buildNumber": 3,
              "apkUrl": "   "
            }
        """.trimIndent()
        RemoteUpdateManifest.fromJson(json)
    }
}
