package com.chaosnote.plugin.service

import com.chaosnote.plugin.model.PluginInfo
import java.io.File
import java.net.URL

/**
 * Service for managing plugin files on disk
 */
class PluginFileManager {
    private val pluginsDir: File
        get() {
            val appDataPath = System.getenv("APPDATA")
            val chaosnotePluginsDir = File(appDataPath, "Chaosnote${File.separator}plugins")
            if (!chaosnotePluginsDir.exists()) {
                chaosnotePluginsDir.mkdirs()
            }
            return chaosnotePluginsDir
        }

    /**
     * Get list of installed plugins with their versions
     * Returns map where key is plugin name (e.g., "plugins-console") and value is version (e.g., "1.0.0")
     */
    fun getInstalledPlugins(): Map<String, String> {
        val installed = mutableMapOf<String, String>()

        pluginsDir.listFiles { file ->
            file.extension == "jar"
        }?.forEach { jarFile ->
            // Extract plugin name and version from filename (e.g., plugins-console-1.0.0.jar)
            val filename = jarFile.nameWithoutExtension
            val (pluginName, version) = extractPluginNameAndVersion(filename)

            if (pluginName.isNotEmpty()) {
                installed[pluginName] = version
                println("Found installed plugin: $pluginName version $version")
            }
        }

        println("Total installed plugins: ${installed.size}")
        return installed
    }

    /**
     * Extract plugin name and version from filename
     * Format: plugin-name-1.0.0 -> (plugin-name, 1.0.0)
     *
     * Strategy: The version is everything after the LAST hyphen that looks like a version number
     * Examples:
     *   plugins-console-1.0.0 -> (plugins-console, 1.0.0)
     *   plugins-code-editor-2.1.0 -> (plugins-code-editor, 2.1.0)
     *   my-plugin-v1.2.3 -> (my-plugin, v1.2.3)
     */
    private fun extractPluginNameAndVersion(filename: String): Pair<String, String> {
        // Find the last part that looks like a version (starts with v or digit)
        val parts = filename.split("-")

        // Find the index where version starts
        // Version typically starts with 'v' or a digit and contains dots
        var versionStartIndex = -1
        for (i in parts.size - 1 downTo 0) {
            val part = parts[i]
            if (part.isNotEmpty() && (part[0].isDigit() || part[0] == 'v')) {
                // Check if this looks like a version (contains dots or is just 'v' followed by number)
                if (part.contains(".") || (part[0] == 'v' && part.length > 1)) {
                    versionStartIndex = i
                    break
                }
            }
        }

        return if (versionStartIndex > 0) {
            // Found a version at versionStartIndex
            val pluginName = parts.take(versionStartIndex).joinToString("-")
            val version = parts.drop(versionStartIndex).joinToString("-")
            Pair(pluginName, version)
        } else if (versionStartIndex == 0) {
            // Version is the only part - shouldn't happen but handle it
            Pair("", parts[0])
        } else {
            // No version found - entire string is plugin name
            Pair(filename, "unknown")
        }
    }

    /**
     * Download and install plugin
     */
    suspend fun installPlugin(plugin: PluginInfo): Result<String> {
        return try {
            val jarFile = downloadJar(plugin.downloadUrl)
            val targetFile = File(pluginsDir, "${plugin.name}-${plugin.latestVersion}.jar")

            if (!jarFile.renameTo(targetFile)) {
                // If rename fails, copy instead
                jarFile.copyTo(targetFile, overwrite = true)
                jarFile.delete()
            }

            Result.success("Plugin installed: ${targetFile.absolutePath}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update installed plugin to latest version
     */
    suspend fun updatePlugin(plugin: PluginInfo): Result<String> {
        return try {
            // Delete old version
            val oldFiles = pluginsDir.listFiles { file ->
                file.extension == "jar" && file.nameWithoutExtension.startsWith(plugin.name)
            }
            oldFiles?.forEach { it.delete() }

            // Install new version
            installPlugin(plugin)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete installed plugin
     */
    fun deletePlugin(plugin: PluginInfo): Result<String> {
        return try {
            val files = pluginsDir.listFiles { file ->
                file.extension == "jar" && file.nameWithoutExtension.startsWith(plugin.name)
            }

            files?.forEach { file ->
                if (!file.delete()) {
                    throw Exception("Failed to delete ${file.absolutePath}")
                }
            }

            Result.success("Plugin deleted successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Download JAR file from URL
     */
    private suspend fun downloadJar(downloadUrl: String): File {
        val url = URL(downloadUrl)
        val tempFile = File.createTempFile("plugin", ".jar")

        url.openStream().use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }


    /**
     * Get plugins directory path
     */
    fun getPluginsDirPath(): String = pluginsDir.absolutePath
}

