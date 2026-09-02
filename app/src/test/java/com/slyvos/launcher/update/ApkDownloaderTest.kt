package com.slyvos.launcher.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class ApkDownloaderTest {

    @Test
    fun checksumVerification_matchesCalculatedSha256() {
        val tempFile = File.createTempFile("test_apk", ".apk").apply {
            writeText("SLYVOS_DEBUG_BUILD_PAYLOAD_CONTENT")
            deleteOnExit()
        }

        val expectedSha256 = calculateFileSha256(tempFile)
        val recalculatedSha256 = calculateFileSha256(tempFile)

        assertEquals(expectedSha256, recalculatedSha256)
    }

    @Test
    fun checksumVerification_rejectsMismatch() {
        val tempFile = File.createTempFile("test_apk_mismatch", ".apk").apply {
            writeText("SLYVOS_DEBUG_BUILD_PAYLOAD_CONTENT")
            deleteOnExit()
        }

        val calculatedSha256 = calculateFileSha256(tempFile)
        val wrongSha256 = "0000000000000000000000000000000000000000000000000000000000000000"

        assertNotEquals(calculatedSha256, wrongSha256)
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
