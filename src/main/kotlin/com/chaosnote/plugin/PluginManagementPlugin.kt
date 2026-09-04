package com.chaosnote.plugin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.chaosnote.api.block.BlockHandle
import com.chaosnote.api.block.BlockPlugin
import com.chaosnote.plugin.ui.PluginsListTab
import com.chaosnote.plugin.ui.RepositoriesTab
import com.chaosnote.plugin.viewmodel.PluginManagementViewModel
import org.pf4j.Extension
import org.jetbrains.compose.ui.tooling.preview.Preview

@Extension
class PluginManagementPlugin : BlockPlugin {

    override val type = "com.chaosnote.plugin.PluginManagementPlugin"
    override val shortName = "Plugin Manager"

    @Composable
    override fun Render(blockHandle: BlockHandle) {
        PluginManagementUI()
    }
}

@Composable
fun PluginManagementUI() {
    val viewModel = remember { PluginManagementViewModel() }
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tab navigation
        TabRow(
            selectedTabIndex = state.selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = state.selectedTabIndex == 0,
                onClick = { viewModel.switchToRepositoriesTab() },
                text = { Text("Repositories") }
            )
            Tab(
                selected = state.selectedTabIndex == 1,
                onClick = { viewModel.switchToPluginsTab() },
                text = { Text("Plugins") }
            )
        }

        // Tab content
        when (state.selectedTabIndex) {
            0 -> RepositoriesTab(
                viewModel = viewModel,
                repositories = state.repositories,
                onFetchClick = { viewModel.fetchPlugins() }
            )
            1 -> PluginsListTab(
                viewModel = viewModel,
                plugins = state.plugins,
                progress = state.installProgress,
                isLoading = state.isLoading,
                error = state.error
            )
        }
    }
}

