package com.chaosnote.plugin.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

/**
 * GitHub repository configuration for plugin source
 */
@Stable
@Serializable
data class PluginRepository(
    val id: String = "",
    val url: String = "",
    val prefix: String = "*"
)

/**
 * Plugin release information from GitHub
 */
@Stable
@Serializable
data class PluginRelease(
    val name: String = "",
    val version: String = "",
    val description: String = "",
    val downloadUrl: String = "",
    val publishedAt: String = ""
)

/**
 * Plugin information with installation status
 */
@Stable
@Serializable
data class PluginInfo(
    val name: String = "",
    val description: String = "",
    val repository: String = "",
    val installedVersion: String? = null,
    val latestVersion: String = "",
    val downloadUrl: String = "",
    val releases: List<PluginRelease> = emptyList()
) {
    val isInstalled: Boolean get() = installedVersion != null
    val hasUpdate: Boolean get() = isInstalled && installedVersion != latestVersion
}

/**
 * Installation/update progress
 */
@Stable
data class PluginInstallProgress(
    val pluginName: String = "",
    val status: InstallStatus = InstallStatus.IDLE,
    val progress: Float = 0f,
    val message: String = ""
)

enum class InstallStatus {
    IDLE, DOWNLOADING, EXTRACTING, INSTALLING, DELETING, ERROR, SUCCESS
}

