package com.sukuna.animestudio.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Dialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.sukuna.animestudio.domain.model.Anime

/**
 * Dialog for managing anime entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeManagementDialog(
    onDismiss: () -> Unit,
    viewModel: AdminPanelViewModel = hiltViewModel()
) {
    val animes by viewModel.animes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf<Anime?>(null) }
    var showDeleteDialog by rememberSaveable { mutableStateOf<Anime?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchAnimes() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Anime Management", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, contentDescription = null) }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text("Search anime...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    when {
                        isLoading -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator() }
                        error != null -> Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                        else -> {
                            val filtered = animes.filter { it.title.contains(search, true) || it.description.contains(search, true) }
                            if (filtered.isEmpty()) {
                                Text("No anime found.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(filtered, key = { it.id }) { anime ->
                                        AnimeCard(anime = anime, onEdit = { showEditDialog = anime }, onDelete = { showDeleteDialog = anime })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AnimeFormDialog(
            anime = null,
            onDismiss = { showAddDialog = false },
            onSave = { anime ->
                viewModel.addAnime(anime) { success ->
                    snackbarMessage = if (success) "Anime added successfully." else "Failed to add anime."
                    showAddDialog = false
                }
            }
        )
    }

    if (showEditDialog != null) {
        AnimeFormDialog(
            anime = showEditDialog,
            onDismiss = { showEditDialog = null },
            onSave = { anime ->
                viewModel.updateAnime(anime) { success ->
                    snackbarMessage = if (success) "Anime updated successfully." else "Failed to update anime."
                    showEditDialog = null
                }
            }
        )
    }

    if (showDeleteDialog != null) {
        val anime = showDeleteDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Anime") },
            text = { Text("Are you sure you want to delete ${anime.title}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteAnime(anime) { success ->
                        snackbarMessage = if (success) "Anime deleted successfully." else "Failed to delete anime."
                        showDeleteDialog = null
                    }
                }) { Text("Delete") }
            },
            dismissButton = { Button(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    if (snackbarMessage != null) {
        Snackbar(modifier = Modifier.padding(8.dp), action = { Button(onClick = { snackbarMessage = null }) { Text("OK") } }) {
            Text(snackbarMessage!!)
        }
    }
}

