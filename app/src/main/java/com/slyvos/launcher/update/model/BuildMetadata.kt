package com.slyvos.launcher.update.model

enum class ReleaseStage(val displayName: String) {
    PRE_ALPHA("Pre-Alpha"),
    ALPHA("Alpha"),
    BETA("Beta"),
    RELEASE_CANDIDATE("Release Candidate"),
    STABLE("Stable")
}

data class BuildMetadata(
    val versionCode: Int,
    val buildNumber: Int,
    val stage: ReleaseStage,
    val versionName: String,
    val buildNumberFormatted: String,
    val releaseTimestamp: Long,
    val releaseNotes: List<String> = emptyList(),
    val downloadUrl: String? = null,
    val sha256: String? = null
) {
    companion object {
        fun createCurrent(
            versionCode: Int,
            buildNumber: Int,
            stageName: String,
            versionName: String,
            timestamp: Long,
            notes: List<String> = emptyList()
        ): BuildMetadata {
            val stage = try {
                ReleaseStage.valueOf(stageName.uppercase())
            } catch (e: Exception) {
                ReleaseStage.PRE_ALPHA
            }
            val formattedNumber = String.format("#%03d", buildNumber)
            val buildName = "Slyvos ${stage.displayName} Build $formattedNumber"

            return BuildMetadata(
                versionCode = versionCode,
                buildNumber = buildNumber,
                stage = stage,
                versionName = versionName,
                buildNumberFormatted = buildName,
                releaseTimestamp = timestamp,
                releaseNotes = notes
            )
        }
    }
}
