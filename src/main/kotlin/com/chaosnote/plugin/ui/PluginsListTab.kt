package com.chaosnote.plugin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chaosnote.plugin.model.InstallStatus
import com.chaosnote.plugin.model.PluginInfo
import com.chaosnote.plugin.model.PluginInstallProgress
import com.chaosnote.plugin.viewmodel.PluginManagementViewModel

@Composable
fun PluginsListTab(
    viewModel: PluginManagementViewModel,
    plugins: List<PluginInfo>,
    progress: PluginInstallProgress,
    isLoading: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            "Plugins",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Error message
        if (error != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                color = Color(0xFFFFEBEE),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        error,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFC62828)
                    )
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        // Plugins list
        if (plugins.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No plugins found. Add repositories and fetch plugins.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = plugins,
                    key = { plugin -> "${plugin.name}-${plugin.latestVersion}-${plugin.repository}" }
                ) { plugin ->
                    PluginListItem(
                        plugin = plugin,
                        progress = progress,
                        onInstall = { viewModel.installPlugin(plugin) },
                        onUpdate = { viewModel.updatePlugin(plugin) },
                        onDelete = { viewModel.deletePlugin(plugin) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PluginListItem(
    plugin: PluginInfo,
    progress: PluginInstallProgress,
    onInstall: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    val isProcessing = progress.pluginName == plugin.name &&
                       progress.status in listOf(
                           InstallStatus.DOWNLOADING,
                           InstallStatus.EXTRACTING,
                           InstallStatus.INSTALLING,
                           InstallStatus.DELETING
                       )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Plugin name and version
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            plugin.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )

                        // Show installed badge if installed
                        if (plugin.isInstalled) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    "✓ Installed",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    Text(
                        plugin.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                // Version badge
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        "v${plugin.latestVersion}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Status and progress
            if (isProcessing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        progress.message,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    LinearProgressIndicator(
                        progress = progress.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = Color(0xFF1976D2),
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            } else if (progress.pluginName == plugin.name && progress.status == InstallStatus.ERROR) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        progress.message,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFC62828)
                    )
                }
            } else if (progress.pluginName == plugin.name && progress.status == InstallStatus.SUCCESS) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        progress.message,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                when {
                    !plugin.isInstalled -> {
                        Button(
                            onClick = onInstall,
                            enabled = !isProcessing,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Install")
                        }
                    }

                    plugin.hasUpdate -> {
                        Button(
                            onClick = onUpdate,
                            enabled = !isProcessing,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update to v${plugin.latestVersion}")
                        }
                        IconButton(
                            onClick = onDelete,
                            enabled = !isProcessing,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }

                    else -> {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Installed v${plugin.installedVersion}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            enabled = !isProcessing
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

