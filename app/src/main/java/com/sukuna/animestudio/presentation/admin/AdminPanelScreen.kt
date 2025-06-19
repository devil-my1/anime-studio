package com.sukuna.animestudio.presentation.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.sukuna.animestudio.R
import com.sukuna.animestudio.domain.model.User
import com.sukuna.animestudio.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit
) {
    var showUserManagement by remember { mutableStateOf(false) }
    var showAnimeManagement by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Anime Studio (Admin Panel)",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.admin_icon),
                        contentDescription = "Admin Panel",
                        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(34.dp)
                            .clickable { onBack() },
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),

            ) {
            item {
                AdminPanelCard(
                    title = "User Management",
                    icon = Icons.Default.Person,
                    description = "View, edit, and manage users.",
                    onClick = { showUserManagement = true }
                )
            }
            item {
                AdminPanelCard(
                    title = "Anime Management",
                    icon = painterResource(R.drawable.anime_icon),
                    description = "Add, edit, or remove anime entries.",
                    onClick = { showAnimeManagement = true }
                )
            }
            item {
                AdminPanelCard(
                    title = "Analytics",
                    icon = painterResource(R.drawable.bar_chart_ico),
                    description = "View app statistics and analytics.",
                    onClick = { showAnalytics = true }
                )
            }
        }
        if (showUserManagement) {
            UserManagementDialog(
                onDismiss = { showUserManagement = false }
            )
        }
        if (showAnimeManagement) {
            // TODO: Anime Management Dialog
        }
        if (showAnalytics) {
            // TODO: Analytics Dialog
        }
    }
}

@Composable
private fun AdminPanelCard(title: String, icon: Any, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (icon) {
                is Painter -> Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )

                is androidx.compose.ui.graphics.vector.ImageVector -> Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )

                else -> throw IllegalArgumentException("Icon must be either a Painter or an ImageVector")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,

                )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserManagementDialog(
    onDismiss: () -> Unit,
    viewModel: AdminPanelViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var search by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<Pair<User, Boolean>?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Fetch users on open
    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "User Management",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = onDismiss) { Text("Close") }
                }
                // Search
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search users...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                // Content
                when {
                    isLoading -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    error != null -> {
                        Text(
                            error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        val filtered = users.filter {
                            it.username.contains(search, ignoreCase = true) ||
                                    it.email.contains(search, ignoreCase = true)
                        }
                        if (filtered.isEmpty()) {
                            Text(
                                "No users found.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(filtered.size) { index ->
                                    val user = filtered[index]
                                    UserManagementRow(
                                        user = user,
                                        onRoleChange = { newRole ->
                                            viewModel.updateUserRole(user, newRole) { success ->
                                                snackbarMessage =
                                                    if (success) "Role updated." else "Failed to update role."
                                            }
                                        },
                                        onDelete = {
                                            showDeleteDialog = user to true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // Delete confirmation dialog
    if (showDeleteDialog?.second == true) {
        val user = showDeleteDialog!!.first
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${user.username}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteUser(user) { success ->
                        snackbarMessage = if (success) "User deleted." else "Failed to delete user."
                        showDeleteDialog = null
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
    // Snackbar for feedback
    if (snackbarMessage != null) {
        Snackbar(
            modifier = Modifier.padding(8.dp),
            action = {
                Button(onClick = { snackbarMessage = null }) { Text("OK") }
            }
        ) { Text(snackbarMessage!!) }
    }
}

@Composable
private fun UserManagementRow(
    user: User,
    onRoleChange: (UserRole) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant,shape = CircleShape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(user.profilePictureUrl.ifEmpty { R.drawable.ic_launcher_foreground }),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(user.username, style = MaterialTheme.typography.titleMedium)
            Text(
                user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Role dropdown
        Box {
            Button(onClick = { expanded = true }) {
                Text(user.role.name)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                UserRole.entries.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role.name) },
                        onClick = {
                            expanded = false
                            if (role != user.role) onRoleChange(role)
                        }
                    )
                }
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete User",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

