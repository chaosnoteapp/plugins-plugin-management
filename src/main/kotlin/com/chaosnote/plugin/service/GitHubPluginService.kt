package com.chaosnote.plugin.service

import com.chaosnote.plugin.model.PluginInfo
import com.chaosnote.plugin.model.PluginRelease
import com.chaosnote.plugin.model.PluginRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service for fetching plugins from GitHub repositories
 */
class GitHubPluginService {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetch all plugins from configured repositories
     */
    suspend fun fetchPlugins(repositories: List<PluginRepository>): List<PluginInfo> {
        val allPlugins = mutableMapOf<String, PluginInfo>()  // Use map to deduplicate by plugin name

        repositories.forEach { repo ->
            try {
                val releases = if (repo.url.isSingleRepositoryUrl()) {
                    // Single repository with full URL
                    fetchReleases(repo.url)
                } else {
                    // Organization URL - fetch all repos matching prefix and their releases
                    fetchAllRepositoriesFromOrganization(repo.url, repo.prefix)
                }

                releases.forEach { release ->
                    // For single repos, apply prefix filter on release name
                    // For org repos, prefix filtering already done at repo level
                    if (repo.url.isSingleRepositoryUrl() && !matchesPrefix(release.name, repo.prefix)) {
                        return@forEach
                    }

                    val pluginName = release.name

                    // Check if we already have this plugin
                    if (allPlugins.containsKey(pluginName)) {
                        // Update if this release is newer or add to releases list
                        val existingPlugin = allPlugins[pluginName]!!
                        val updatedReleases = existingPlugin.releases + release
                        // Keep the latest version
                        val latestRelease = updatedReleases.maxByOrNull { it.version } ?: release
                        allPlugins[pluginName] = existingPlugin.copy(
                            latestVersion = latestRelease.version,
                            downloadUrl = latestRelease.downloadUrl,
                            releases = updatedReleases
                        )
                    } else {
                        // New plugin
                        allPlugins[pluginName] = PluginInfo(
                            name = pluginName,
                            description = release.description,
                            repository = repo.url,
                            latestVersion = release.version,
                            downloadUrl = release.downloadUrl,
                            releases = listOf(release)
                        )
                    }
                }
            } catch (e: Exception) {
                println("Error fetching plugins from ${repo.url}: ${e.message}")
            }
        }

        return allPlugins.values.toList()
    }

    /**
     * Check if URL is a single repository (has both org/user and repo name)
     */
    private fun String.isSingleRepositoryUrl(): Boolean {
        val parts = this.trimEnd('/').split("/")
        // https://github.com/org/repo -> parts has 5 elements (https:, , github.com, org, repo)
        return parts.size >= 5
    }

    /**
     * Fetch all repositories from an organization
     */
    private suspend fun fetchAllRepositoriesFromOrganization(
        orgUrl: String,
        prefix: String
    ): List<PluginRelease> {
        return try {
            val orgName = orgUrl.extractOrganizationName()
            if (orgName.isEmpty()) {
                println("ERROR: Could not extract organization name from: $orgUrl")
                return emptyList()
            }

            println("Fetching all repositories from organization: $orgName")

            val reposApiUrl = "https://api.github.com/orgs/$orgName/repos?per_page=100"
            val response = fetchWithUserAgent(reposApiUrl)

            if (response.isBlank()) {
                println("ERROR: No response from GitHub API for organization: $orgName")
                return emptyList()
            }

            // Parse repository list and fetch releases for each
            val repos = parseRepositoryList(response)
            println("Found ${repos.size} repositories in organization $orgName")

            val allReleases = mutableListOf<PluginRelease>()

            repos.forEach { repoName ->
                // Filter repositories by prefix BEFORE fetching releases
                if (!matchesPrefix(repoName, prefix)) {
                    println("Skipping repository (prefix mismatch): $repoName (prefix: $prefix)")
                    return@forEach
                }

                try {
                    println("Fetching releases from: $orgName/$repoName")
                    val releasesApiUrl = "https://api.github.com/repos/$orgName/$repoName/releases"
                    val releasesResponse = fetchWithUserAgent(releasesApiUrl)

                    if (releasesResponse.isNotBlank()) {
                        val releases = parseReleases(releasesResponse)
                        // Add repository name as context to each release
                        releases.forEach { release ->
                            allReleases.add(
                                release.copy(name = repoName)
                            )
                        }
                    }
                } catch (e: Exception) {
                    println("Error fetching releases from $orgName/$repoName: ${e.message}")
                }
            }

            println("Total releases found: ${allReleases.size}")
            allReleases
        } catch (e: Exception) {
            println("Error fetching repositories from organization: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Parse GitHub API response to get list of repository names
     */
    private fun parseRepositoryList(jsonString: String): List<String> {
        return try {
            val repos = mutableListOf<String>()
            val jsonArray = Json.parseToJsonElement(jsonString) as? JsonArray ?: return emptyList()

            jsonArray.forEach { element ->
                if (element is JsonObject) {
                    val repoName = element["name"]?.jsonPrimitive?.content ?: return@forEach
                    repos.add(repoName)
                }
            }

            repos
        } catch (e: Exception) {
            println("Error parsing repository list: ${e.message}")
            emptyList()
        }
    }

    /**
     * Extract organization name from URL
     */
    private fun String.extractOrganizationName(): String {
        return try {
            val cleaned = this.trimEnd('/')
            val parts = cleaned.split("/")

            // https://github.com/chaosnoteapp -> parts = [https:, , github.com, chaosnoteapp]
            if (parts.size >= 4) {
                return parts[3]
            }

            // github.com/chaosnoteapp -> parts = [github.com, chaosnoteapp]
            if (parts.size >= 2 && parts[0].contains("github")) {
                return parts[1]
            }

            ""
        } catch (e: Exception) {
            println("Error extracting organization name from: $this")
            ""
        }
    }

    /**
     * Fetch releases from GitHub repository
     */
    private suspend fun fetchReleases(repoUrl: String): List<PluginRelease> {
        return try {
            // Validate and convert URL
            val apiUrl = repoUrl.toGitHubApiUrl()

            // Check if it's a valid organization or repository URL
            val isValidOrgUrl = repoUrl.isSingleRepositoryUrl() || repoUrl.trimEnd('/').matches(Regex("""https?://github\.com/([a-zA-Z0-9\-_]+)/?$"""))

            if (!isValidOrgUrl && !repoUrl.contains("api.github.com")) {
                println("ERROR: Invalid GitHub URL: $repoUrl")
                println("Expected:")
                println("  • Organization (fetch all repos):   https://github.com/chaosnoteapp")
                println("  • Specific repository:               https://github.com/chaosnoteapp/chaosnote-plugins")
                println("")
                println("Then use prefix filter to select plugins:")
                println("  • Prefix '*' = all repositories")
                println("  • Prefix 'plugins-' = only repos starting with 'plugins-'")
                return emptyList()
            }

            println("Fetching from: $apiUrl")

            val response = fetchWithUserAgent(apiUrl)
            println("Response length: ${response.length}")

            if (response.trim().startsWith("<!DOCTYPE") || response.trim().startsWith("<html")) {
                println("ERROR: Received HTML instead of JSON")
                return emptyList()
            }

            parseReleases(response)
        } catch (e: Exception) {
            println("Error fetching releases from $repoUrl: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Fetch with User-Agent header and proper error handling
     */
    private fun fetchWithUserAgent(urlString: String): String {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            // GitHub API requires proper headers
            connection.setRequestProperty("User-Agent", "ChaosNote-PluginManager/1.0")
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            // Handle HTTP errors
            val responseCode = connection.responseCode
            println("HTTP Response Code: $responseCode")

            when (responseCode) {
                200 -> {
                    // Success
                    connection.inputStream.bufferedReader().use { it.readText() }
                }
                404 -> {
                    println("ERROR: Repository not found (404)")
                    ""
                }
                403 -> {
                    println("ERROR: Access forbidden (403) - Check repository access")
                    ""
                }
                406 -> {
                    println("ERROR: Not Acceptable (406) - Try without Accept header")
                    // Retry without Accept header
                    val retryConnection = URL(urlString).openConnection() as HttpURLConnection
                    retryConnection.setRequestProperty("User-Agent", "ChaosNote-PluginManager/1.0")
                    retryConnection.connectTimeout = 10000
                    retryConnection.readTimeout = 10000
                    if (retryConnection.responseCode == 200) {
                        retryConnection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        println("ERROR: Retry also failed with code ${retryConnection.responseCode}")
                        ""
                    }
                }
                else -> {
                    println("ERROR: HTTP $responseCode")
                    ""
                }
            }
        } catch (e: Exception) {
            println("Network error: ${e.message}")
            e.printStackTrace()
            ""
        }
    }

    /**
     * Parse GitHub API response
     */
    private fun parseReleases(jsonString: String): List<PluginRelease> {
        return try {
            if (jsonString.isBlank()) {
                println("Empty response from GitHub API")
                return emptyList()
            }

            val releases = mutableListOf<PluginRelease>()

            val jsonElement = try {
                Json.parseToJsonElement(jsonString)
            } catch (e: Exception) {
                println("Failed to parse JSON: ${e.message}")
                println("First 500 chars: ${jsonString.take(500)}")
                return emptyList()
            }

            val jsonArray = jsonElement as? JsonArray ?: run {
                println("Response is not a JSON array. Type: ${jsonElement::class.simpleName}")
                return emptyList()
            }

            jsonArray.forEach { element ->
                if (element is JsonObject) {
                    try {
                        val name = element["name"]?.jsonPrimitive?.content ?: return@forEach
                        val tagName = element["tag_name"]?.jsonPrimitive?.content ?: ""
                        val body = element["body"]?.jsonPrimitive?.content ?: ""
                        val publishedAt = element["published_at"]?.jsonPrimitive?.content ?: ""
                        val assets = element["assets"] as? JsonArray ?: return@forEach

                        // Find JAR asset
                        assets.forEach { asset ->
                            if (asset is JsonObject) {
                                val assetName = asset["name"]?.jsonPrimitive?.content ?: ""
                                if (assetName.endsWith(".jar")) {
                                    val downloadUrl = asset["browser_download_url"]?.jsonPrimitive?.content ?: ""
                                    if (downloadUrl.isNotEmpty()) {
                                        releases.add(
                                            PluginRelease(
                                                name = name,
                                                version = tagName.removePrefix("v"),
                                                description = body.take(200),
                                                downloadUrl = downloadUrl,
                                                publishedAt = publishedAt
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Error parsing release element: ${e.message}")
                    }
                }
            }

            println("Parsed ${releases.size} plugins")
            releases
        } catch (e: Exception) {
            println("Error parsing releases: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Check if plugin name matches prefix filter
     */
    private fun matchesPrefix(name: String, prefix: String): Boolean {
        if (prefix == "*") return true
        return name.startsWith(prefix)
    }

    /**
     * Convert GitHub URL to API URL
     * Accepts both:
     * - Organization URLs: https://github.com/chaosnoteapp (fetches all repos)
     * - Repository URLs: https://github.com/chaosnoteapp/chaosnote-plugins (fetches one repo)
     */
    private fun String.toGitHubApiUrl(): String {
        // Remove trailing slash
        var cleaned = this.trimEnd('/')

        // If already an API URL, return as-is
        if (cleaned.contains("api.github.com")) {
            return cleaned
        }

        // Handle GitHub URLs
        // Pattern 1: Organization only - https://github.com/username-or-organization
        val orgPattern = Regex("""https?://github\.com/([a-zA-Z0-9\-_]+)/?$""")
        val orgMatch = orgPattern.find(cleaned)
        if (orgMatch != null) {
            val (orgName) = orgMatch.destructured
            println("Recognized GitHub organization: $orgName")
            // Return as-is for organization processing
            return "https://github.com/$orgName"
        }

        // Pattern 2: Organization + Repository - https://github.com/username-or-organization/repository
        val repoPattern = Regex("""https?://github\.com/([a-zA-Z0-9\-_]+)/([a-zA-Z0-9\-_.]+)/?$""")
        val repoMatch = repoPattern.find(cleaned)
        if (repoMatch != null) {
            val (userOrOrg, repo) = repoMatch.destructured
            val apiUrl = "https://api.github.com/repos/$userOrOrg/$repo/releases"
            println("Converted GitHub repository URL: $cleaned -> $apiUrl")
            return apiUrl
        }

        // URL doesn't match expected format
        println("WARNING: URL doesn't match GitHub format")
        println("Expected: https://github.com/organization")
        println("      or: https://github.com/organization/repository")
        println("Got: $this")
        println("")
        println("Examples:")
        println("  Organization (fetch all repos):   https://github.com/chaosnoteapp")
        println("  Specific repository:               https://github.com/chaosnoteapp/chaosnote-plugins")
        println("  With prefix (e.g. 'plugins-*'):    Will fetch repos like: plugins-search, plugins-console, etc.")
        return this
    }
}

