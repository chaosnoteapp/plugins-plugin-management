package com.chaosnote.plugin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaosnote.api.block.BlockHandle
import com.chaosnote.api.block.BlockPlugin
import org.jetbrains.compose.ui.tooling.preview.Preview

class PluginManagementPlugin : BlockPlugin {

    override val type = "com.chaosnote.plugin.PluginManagementPlugin"
    override val shortName = "Plugin Management"

    @Preview
    @Composable
    override fun Render(blockHandle: BlockHandle) {

        var entries by remember { mutableStateOf<List<MarketplaceEntry>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            try {
                loading = true
                entries = PluginMarketplace.scan()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }

        Column(Modifier.fillMaxSize().padding(16.dp)) {

            Text("Chaosnote Plugin Marketplace", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            if (loading) {
                CircularProgressIndicator()
                return@Column
            }

            error?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
                return@Column
            }

            LazyColumn {
                items(entries) { entry ->

                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(Modifier.weight(1f)) {
                                Text(entry.id, style = MaterialTheme.typography.titleMedium)
                                Text("Latest: ${entry.latest.version}")
                                entry.installed?.let { Text("Installed: ${it.version}") }
                            }

                            Button(onClick = {
                                when (entry.action) {
                                    Action.INSTALL -> PluginMarketplace.install(entry)
                                    Action.UPGRADE -> PluginMarketplace.upgrade(entry)
                                    Action.REMOVE -> PluginMarketplace.remove(entry)
                                }
                                entries = PluginMarketplace.scan()
                            }) {
                                Text(entry.action.name)
                            }
                        }
                    }
                }
            }
        }
    }
}
