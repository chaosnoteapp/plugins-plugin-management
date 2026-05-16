package com.chaosnote.plugin.viewmodel

import androidx.compose.runtime.Stable
import com.chaosnote.plugin.model.PluginInfo
import com.chaosnote.plugin.model.PluginInstallProgress
import com.chaosnote.plugin.model.PluginRepository
import com.chaosnote.plugin.service.GitHubPluginService
import com.chaosnote.plugin.service.PluginFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
data class PluginManagementState(
    val repositories: List<PluginRepository> = emptyList(),
    val plugins: List<PluginInfo> = emptyList(),
    val installProgress: PluginInstallProgress = PluginInstallProgress(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTabIndex: Int = 0
)

class PluginManagementViewModel(
    private val fileManager: PluginFileManager = PluginFileManager(),
    private val githubService: GitHubPluginService = GitHubPluginService()
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _state = MutableStateFlow(PluginManagementState())
    val state: StateFlow<PluginManagementState> = _state

    init {
        loadRepositories()
    }

    /**
     * Load repositories from persistent storage
     */
    private fun loadRepositories() {
        // TODO: Load from persistent storage (preferences/database)
        _state.update {
            it.copy(
                repositories = emptyList()
            )
        }
    }

    /**
     * Add new repository
     */
    fun addRepository(url: String, prefix: String = "*") {
        if (url.isBlank()) return

        val repo = PluginRepository(
            id = url.hashCode().toString(),
            url = url,
            prefix = prefix
        )

        _state.update { state ->
            state.copy(
                repositories = state.repositories + repo
            )
        }

        // TODO: Persist to storage
    }

    /**
     * Remove repository
     */
    fun removeRepository(repoId: String) {
        _state.update { state ->
            state.copy(
                repositories = state.repositories.filterNot { it.id == repoId }
            )
        }

        // TODO: Persist to storage
    }

    /**
     * Update repository
     */
    fun updateRepository(repoId: String, url: String, prefix: String) {
        _state.update { state ->
            state.copy(
                repositories = state.repositories.map { repo ->
                    if (repo.id == repoId) {
                        repo.copy(url = url, prefix = prefix)
                    } else {
                        repo
                    }
                }
            )
        }

        // TODO: Persist to storage
    }

    /**
     * Fetch plugins from all repositories
     */
    fun fetchPlugins() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val plugins = githubService.fetchPlugins(_state.value.repositories)
                val installedPlugins = fileManager.getInstalledPlugins()

                // Merge installed info with fetched plugins
                val enrichedPlugins = plugins.map { plugin ->
                    val installed = installedPlugins[plugin.name]
                    plugin.copy(installedVersion = installed)
                }

                _state.update {
                    it.copy(
                        plugins = enrichedPlugins,
                        isLoading = false,
                        selectedTabIndex = 1
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to fetch plugins: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Install plugin
     */
    fun installPlugin(plugin: PluginInfo) {
        scope.launch {
            _state.update { state ->
                state.copy(
                    installProgress = PluginInstallProgress(
                        pluginName = plugin.name,
                        status = com.chaosnote.plugin.model.InstallStatus.DOWNLOADING,
                        message = "Downloading ${plugin.name}..."
                    )
                )
            }

            try {
                val result = fileManager.installPlugin(plugin)

                if (result.isSuccess) {
                    // Update installed plugins
                    _state.update { state ->
                        state.copy(
                            plugins = state.plugins.map { p ->
                                if (p.name == plugin.name) {
                                    p.copy(installedVersion = plugin.latestVersion)
                                } else {
                                    p
                                }
                            },
                            installProgress = PluginInstallProgress(
                                pluginName = plugin.name,
                                status = com.chaosnote.plugin.model.InstallStatus.SUCCESS,
                                message = "Plugin installed successfully"
                            )
                        )
                    }
                } else {
                    _state.update { state ->
                        state.copy(
                            installProgress = PluginInstallProgress(
                                pluginName = plugin.name,
                                status = com.chaosnote.plugin.model.InstallStatus.ERROR,
                                message = result.exceptionOrNull()?.message ?: "Unknown error"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { state ->
                    state.copy(
                        installProgress = PluginInstallProgress(
                            pluginName = plugin.name,
                            status = com.chaosnote.plugin.model.InstallStatus.ERROR,
                            message = e.message ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

    /**
     * Update plugin to latest version
     */
    fun updatePlugin(plugin: PluginInfo) {
        scope.launch {
            _state.update { state ->
                state.copy(
                    installProgress = PluginInstallProgress(
                        pluginName = plugin.name,
                        status = com.chaosnote.plugin.model.InstallStatus.DOWNLOADING,
                        message = "Updating ${plugin.name}..."
                    )
                )
            }

            try {
                val result = fileManager.updatePlugin(plugin)

                if (result.isSuccess) {
                    _state.update { state ->
                        state.copy(
                            plugins = state.plugins.map { p ->
                                if (p.name == plugin.name) {
                                    p.copy(installedVersion = plugin.latestVersion)
                                } else {
                                    p
                                }
                            },
                            installProgress = PluginInstallProgress(
                                pluginName = plugin.name,
                                status = com.chaosnote.plugin.model.InstallStatus.SUCCESS,
                                message = "Plugin updated successfully"
                            )
                        )
                    }
                } else {
                    _state.update { state ->
                        state.copy(
                            installProgress = PluginInstallProgress(
                                pluginName = plugin.name,
                                status = com.chaosnote.plugin.model.InstallStatus.ERROR,
                                message = result.exceptionOrNull()?.message ?: "Unknown error"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { state ->
                    state.copy(
                        installProgress = PluginInstallProgress(
                            pluginName = plugin.name,
                            status = com.chaosnote.plugin.model.InstallStatus.ERROR,
                            message = e.message ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

    /**
     * Delete installed plugin
     */
    fun deletePlugin(plugin: PluginInfo) {
        scope.launch {
            _state.update { state ->
                state.copy(
                    installProgress = PluginInstallProgress(
                        pluginName = plugin.name,
                        status = com.chaosnote.plugin.model.InstallStatus.DELETING,
                        message = "Deleting ${plugin.name}..."
                    )
                )
            }

            try {
                val result = fileManager.deletePlugin(plugin)

                if (result.isSuccess) {
                    _state.update { state ->
                        state.copy(
                            plugins = state.plugins.map { p ->
                                if (p.name == plugin.name) {
                                    p.copy(installedVersion = null)
                                } else {
                                    p
                                }
                            },
                            installProgress = PluginInstallProgress(
                                pluginName = plugin.name,
                                status = com.chaosnote.plugin.model.InstallStatus.SUCCESS,
                                message = "Plugin deleted successfully"
                            )
                        )
                    }
                } else {
                    _state.update { state ->
                        state.copy(
                            installProgress = PluginInstallProgress(
                                pluginName = plugin.name,
                                status = com.chaosnote.plugin.model.InstallStatus.ERROR,
                                message = result.exceptionOrNull()?.message ?: "Unknown error"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { state ->
                    state.copy(
                        installProgress = PluginInstallProgress(
                            pluginName = plugin.name,
                            status = com.chaosnote.plugin.model.InstallStatus.ERROR,
                            message = e.message ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

    /**
     * Switch to plugins tab
     */
    fun switchToPluginsTab() {
        _state.update { it.copy(selectedTabIndex = 1) }
    }

    /**
     * Switch to repositories tab
     */
    fun switchToRepositoriesTab() {
        _state.update { it.copy(selectedTabIndex = 0) }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

