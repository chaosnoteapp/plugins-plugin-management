package com.chaosnote.plugin

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.net.URL
import kotlin.io.path.*
import java.nio.file.Path

object PluginMarketplace {

    private val json = Json { ignoreUnknownKeys = true }

    private val sources = listOf(
        SourceInput("https://api.github.com/orgs/chaosnoteapp", "plugins-")
    )

    private val pluginsDir = Path("plugins")
    private val cacheDir = Path("plugin-cache")

    fun scan(): List<MarketplaceEntry> {
        pluginsDir.createDirectories()
        cacheDir.createDirectories()

        val installed = pluginsDir.listDirectoryEntries("*.jar")
            .mapNotNull { parseInstalled(it) }
            .associateBy { it.id }

        return sources.flatMap { src ->
            val repos = URL("${src.orgApi}/repos?per_page=100")
                .readText()
                .let { json.decodeFromString<List<GhRepo>>(it) }

            repos.filter { it.name.startsWith(src.prefix) }
                .flatMap { repo ->
                    val releases = URL("https://api.github.com/repos/chaosnoteapp/${repo.name}/releases")
                        .readText()
                        .let { json.decodeFromString<List<GhRelease>>(it) }

                    releases.flatMap { rel ->
                        rel.assets.filter { it.name.endsWith(".jar") }.map { asset ->
                            val id = repo.name.removePrefix(src.prefix)
                            PluginDescriptor(
                                id,
                                repo.name,
                                Semver(rel.tag),
                                asset.browserDownloadUrl,
                                asset.name
                            )
                        }
                    }
                }
        }.groupBy { it.id }
            .map { (id, versions) ->
                val latest = versions.maxBy { it.version }
                val local = installed[id]
                val action = when {
                    local == null -> Action.INSTALL
                    latest.version > local.version -> Action.UPGRADE
                    else -> Action.REMOVE
                }
                MarketplaceEntry(id, latest, local, action)
            }
    }

    fun install(entry: MarketplaceEntry) = doInstall(entry.latest)

    fun upgrade(entry: MarketplaceEntry) {
        entry.installed?.file?.deleteIfExists()
        doInstall(entry.latest)
    }

    fun remove(entry: MarketplaceEntry) {
        entry.installed?.file?.deleteIfExists()
    }

    private fun doInstall(desc: PluginDescriptor) {
        val target = pluginsDir.resolve(desc.jarName)
        URL(desc.jarUrl).openStream().use { it.copyTo(target.outputStream()) }
    }

    private fun parseInstalled(path: Path): InstalledPlugin? {
        val match = Regex("(.*)-(\\d+\\.\\d+.*)\\.jar").matchEntire(path.name) ?: return null
        return InstalledPlugin(match.groupValues[1], Semver(match.groupValues[2]), path)
    }
}

data class SourceInput(val orgApi: String, val prefix: String)

enum class Action { INSTALL, UPGRADE, REMOVE }

data class MarketplaceEntry(
    val id: String,
    val latest: PluginDescriptor,
    val installed: InstalledPlugin?,
    val action: Action
)

data class PluginDescriptor(
    val id: String,
    val repo: String,
    val version: Semver,
    val jarUrl: String,
    val jarName: String
)

data class InstalledPlugin(
    val id: String,
    val version: Semver,
    val file: Path
)

@Serializable data class GhRepo(val name: String)

@Serializable data class GhRelease(
    @SerialName("tag_name") val tag: String,
    val assets: List<GhAsset>
)

@Serializable data class GhAsset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String
)

data class Semver(val raw: String) : Comparable<Semver> {
    private val parts = raw.trimStart('v').split(".", "-", "_")

    override fun compareTo(other: Semver): Int {
        for (i in 0 until maxOf(parts.size, other.parts.size)) {
            val a = parts.getOrNull(i)?.toIntOrNull() ?: 0
            val b = other.parts.getOrNull(i)?.toIntOrNull() ?: 0
            if (a != b) return a.compareTo(b)
        }
        return 0
    }

    override fun toString() = raw
}