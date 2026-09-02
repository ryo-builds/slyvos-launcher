package com.slyvos.launcher.update.model

data class RemoteUpdateManifest(
    val buildNumber: Int,
    val versionCode: Int,
    val versionName: String,
    val releaseStage: String,
    val releaseNotes: String,
    val apkUrl: String,
    val apkSha256: String,
    val publishedAt: String,
    val minimumSupportedBuildNumber: Int
) {
    fun toBuildMetadata(): BuildMetadata {
        val stage = try {
            ReleaseStage.valueOf(releaseStage.uppercase())
        } catch (e: Exception) {
            ReleaseStage.PRE_ALPHA
        }
        val formattedNumber = String.format("#%03d", buildNumber)
        val buildName = "Slyvos ${stage.displayName} Build $formattedNumber"
        val notesList = releaseNotes.split("\n", ";").map { it.trim() }.filter { it.isNotEmpty() }

        return BuildMetadata(
            versionCode = versionCode,
            buildNumber = buildNumber,
            stage = stage,
            versionName = versionName,
            buildNumberFormatted = buildName,
            releaseTimestamp = 0L,
            releaseNotes = if (notesList.isNotEmpty()) notesList else listOf(releaseNotes),
            downloadUrl = apkUrl,
            sha256 = apkSha256
        )
    }

    companion object {
        fun fromJson(jsonString: String): RemoteUpdateManifest {
            fun parseString(key: String, default: String = ""): String {
                val pattern = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"")
                val match = pattern.find(jsonString)
                return match?.groupValues?.get(1) ?: default
            }

            fun parseInt(key: String, default: Int = 0): Int {
                val pattern = Regex("\"$key\"\\s*:\\s*(\\d+)")
                val match = pattern.find(jsonString)
                return match?.groupValues?.get(1)?.toIntOrNull() ?: default
            }

            val buildNum = parseInt("buildNumber", -1)
            require(buildNum >= 0) { "Missing or invalid buildNumber in manifest JSON" }

            val vCode = parseInt("versionCode", buildNum)
            val vName = parseString("versionName", "Build #$buildNum")
            val stage = parseString("releaseStage", "PRE_ALPHA")
            val notes = parseString("releaseNotes", "New development build available")
            val apkUrl = parseString("apkUrl", "")
            val sha256 = parseString("apkSha256", "")
            val pubAt = parseString("publishedAt", "")
            val minBuild = parseInt("minimumSupportedBuildNumber", 1)

            require(apkUrl.isNotBlank()) { "apkUrl cannot be blank" }

            return RemoteUpdateManifest(
                buildNumber = buildNum,
                versionCode = vCode,
                versionName = vName,
                releaseStage = stage,
                releaseNotes = notes,
                apkUrl = apkUrl,
                apkSha256 = sha256,
                publishedAt = pubAt,
                minimumSupportedBuildNumber = minBuild
            )
        }
    }
}
