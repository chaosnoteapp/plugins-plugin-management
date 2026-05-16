package com.chaosnote.plugin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chaosnote.plugin.model.PluginRepository
import com.chaosnote.plugin.viewmodel.PluginManagementViewModel

@Composable
fun RepositoriesTab(
    viewModel: PluginManagementViewModel,
    repositories: List<PluginRepository>,
    onFetchClick: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "GitHub Repositories",
                style = MaterialTheme.typography.headlineSmall
            )
            Button(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Repository")
            }
        }

        // Repositories list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = repositories,
                key = { it.id }
            ) { repo ->
                RepositoryItem(
                    repository = repo,
                    onUpdate = { url, prefix ->
                        viewModel.updateRepository(repo.id, url, prefix)
                    },
                    onDelete = {
                        viewModel.removeRepository(repo.id)
                    }
                )
            }
        }

        // Fetch button
        Button(
            onClick = onFetchClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(top = 16.dp)
        ) {
            Text("Fetch Plugins from Repositories")
        }
    }

    // Add repository dialog
    if (showAddDialog) {
        AddRepositoryDialog(
            onAdd = { url, prefix ->
                viewModel.addRepository(url, prefix)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun RepositoryItem(
    repository: PluginRepository,
    onUpdate: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editUrl by remember { mutableStateOf(repository.url) }
    var editPrefix by remember { mutableStateOf(repository.prefix) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (isEditing) {
                TextField(
                    value = editUrl,
                    onValueChange = { editUrl = it },
                    label = { Text("Repository URL") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                TextField(
                    value = editPrefix,
                    onValueChange = { editPrefix = it },
                    label = { Text("Project Name Prefix (e.g., 'plugins-' or '*' for all)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { isEditing = false }) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onUpdate(editUrl, editPrefix)
                            isEditing = false
                        }
                    ) {
                        Text("Save")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            repository.url,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Prefix: ${repository.prefix}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Row {
                        TextButton(onClick = { isEditing = true }) {
                            Text("Edit")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddRepositoryDialog(
    onAdd: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    var prefix by remember { mutableStateOf("*") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Repository") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("GitHub Repository URL") },
                    placeholder = { Text("https://github.com/username/repo") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = prefix,
                    onValueChange = { prefix = it },
                    label = { Text("Project Name Prefix") },
                    placeholder = { Text("* (all) or 'plugins-' for example") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isNotBlank()) {
                        onAdd(url, prefix)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

