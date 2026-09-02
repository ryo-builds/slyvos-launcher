package com.slyvos.launcher.update.config

object SlyvosUpdateConfig {
    // Configurable update manifest URL pointing to live public distribution repository
    var manifestUrl: String = "https://raw.githubusercontent.com/ryo-builds/slyvos-launcher-updates/main/pre-alpha.json"

    // Throttling check interval (1 hour)
    const val CHECK_THROTTLE_MS = 3600000L

    // HTTP Timeouts
    const val CONNECT_TIMEOUT_MS = 10000
    const val READ_TIMEOUT_MS = 10000
}
