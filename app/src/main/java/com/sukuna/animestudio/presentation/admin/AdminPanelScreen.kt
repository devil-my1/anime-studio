package com.sukuna.animestudio.presentation.admin

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.sukuna.animestudio.R
import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.domain.model.AnimeGenre
import com.sukuna.animestudio.domain.model.AnimeStatus
import com.sukuna.animestudio.domain.model.User
import com.sukuna.animestudio.domain.model.UserRole
import com.sukuna.animestudio.utils.generateRandomId
import java.util.Date

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
            AnimeManagementDialog(
                onDismiss = { showAnimeManagement = false }
            )
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
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(34.dp)
                )

                is androidx.compose.ui.graphics.vector.ImageVector -> Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(34.dp)
                )

                else -> throw IllegalArgumentException("Icon must be either a Painter or an ImageVector")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
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
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
            .padding(12.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onDelete() }
                )
            },
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
        // Role dropdown
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.width(120.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)),
            ) {
                Text(
                    user.role.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                )
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

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimeManagementDialog(
    onDismiss: () -> Unit,
    viewModel: AdminPanelViewModel = hiltViewModel()
) {
    val animes by viewModel.animes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var search by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Anime?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Anime?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Fetch anime on open
    LaunchedEffect(Unit) {
        viewModel.fetchAnimes()
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
            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Anime Management",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Anime",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Search
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text("Search anime...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Content
                    when {
                        isLoading -> {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
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
                            val filtered = animes.filter {
                                it.title.contains(search, ignoreCase = true) ||
                                        it.description.contains(search, ignoreCase = true)
                            }
                            if (filtered.isEmpty()) {
                                Text(
                                    "No anime found.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),

                                    ) {
                                    items(filtered) { anime ->

                                        AnimeCard(
                                            anime = anime,
                                            onEdit = { showEditDialog = anime },
                                            onDelete = { showDeleteDialog = anime }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Anime Dialog
    if (showAddDialog) {
        AnimeFormDialog(
            anime = null,
            onDismiss = { showAddDialog = false },
            onSave = { anime ->
                viewModel.addAnime(anime) { success ->
                    snackbarMessage =
                        if (success) "Anime added successfully." else "Failed to add anime."
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Anime Dialog
    if (showEditDialog != null) {
        AnimeFormDialog(
            anime = showEditDialog,
            onDismiss = { showEditDialog = null },
            onSave = { anime ->
                viewModel.updateAnime(anime) { success ->
                    snackbarMessage =
                        if (success) "Anime updated successfully." else "Failed to update anime."
                    showEditDialog = null
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog != null) {
        val anime = showDeleteDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Anime") },
            text = { Text("Are you sure you want to delete ${anime.title}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteAnime(anime) { success ->
                        snackbarMessage =
                            if (success) "Anime deleted successfully." else "Failed to delete anime."
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
private fun AnimeCard(
    anime: Anime,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(vertical = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onEdit() }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            // Anime image
            AsyncImage(
                model = anime.imageUrl.ifEmpty { R.drawable.ic_launcher_foreground },
                contentDescription = "Anime Cover",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.3f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Anime details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier

                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    Color.Transparent,
                                )
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Text(
                    text = anime.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                )

            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Rating: ${anime.rating} ⭐ • Episodes: ${anime.episodesCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(8.dp)

                        .background(
                            Color.Yellow.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Anime",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

            }


        }
    }
}

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimeFormDialog(
    anime: Anime?,
    onDismiss: () -> Unit,
    onSave: (Anime) -> Unit
) {
    var title by remember { mutableStateOf(anime?.title ?: "") }
    var description by remember { mutableStateOf(anime?.description ?: "") }
    var imageUrl by remember { mutableStateOf(anime?.imageUrl ?: "") }
    var rating by remember { mutableStateOf(anime?.rating?.toString() ?: "0.0") }
    var episodeCount by remember { mutableStateOf(anime?.episodesCount?.toString() ?: "12") }
    var episodes by remember { mutableStateOf(anime?.episodes ?: emptyList()) }
    var showGenreDialog by remember { mutableStateOf(false) }
    val selectedGenres = remember { mutableStateOf(anime?.genre ?: listOf(AnimeGenre.ACTION)) }
    var selectedStatus by remember { mutableStateOf(anime?.animeStatus ?: AnimeStatus.IN_PROGRESS) }
    var releaseDate by remember {
        mutableStateOf(
            anime?.releaseDate ?: java.text.SimpleDateFormat("yyyy-MM-dd").format(Date())
        )
    }

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }


    if (showGenreDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showGenreDialog = false },
            title = { Text("Select Genres") },
            text = {
                LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                    items(AnimeGenre.entries.size) { index ->
                        val genre = AnimeGenre.entries[index]
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedGenres.value =
                                        if (selectedGenres.value.contains(genre)) {
                                            selectedGenres.value.filter { it != genre }
                                        } else {
                                            selectedGenres.value + genre
                                        }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedGenres.value.contains(genre),
                                onCheckedChange = { checked ->
                                    selectedGenres.value = if (checked) {
                                        selectedGenres.value + genre
                                    } else {
                                        selectedGenres.value.filter { it != genre }
                                    }
                                }
                            )
                            Text(genre.name.toString())
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showGenreDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showDatePicker) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val currentDate = remember {
            val df = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            try {
                df.parse(releaseDate)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }

        val datePickerDialog = android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                releaseDate = "$year-${(month + 1).toString().padStart(2, '0')}-${
                    day.toString().padStart(2, '0')
                }"
                showDatePicker = false
            },
            java.util.Calendar.getInstance().apply { timeInMillis = currentDate }
                .get(java.util.Calendar.YEAR),
            java.util.Calendar.getInstance().apply { timeInMillis = currentDate }
                .get(java.util.Calendar.MONTH),
            java.util.Calendar.getInstance().apply { timeInMillis = currentDate }
                .get(java.util.Calendar.DAY_OF_MONTH)
        )

        LaunchedEffect(Unit) {
            datePickerDialog.show()
            showDatePicker = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(if (anime == null) "Add Anime" else "Edit Anime") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.95f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Column {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                        )
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Image URL") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = rating,
                                onValueChange = { rating = it },
                                label = { Text("Rating") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = episodeCount,
                                onValueChange = { episodeCount = it },
                                label = { Text("Episodes") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(8.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Selected Genres",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    OutlinedButton(
                                        onClick = { showGenreDialog = true },
                                        modifier = Modifier.height(32.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {

                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Genre",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.secondary.copy(
                                                alpha = 0.7f
                                            )
                                        )
                                    }
                                }

                                if (selectedGenres.value.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No genres selected",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                    ) {
                                        items(selectedGenres.value.size) { index ->
                                            val genre = selectedGenres.value[index]
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onLongPress = {
                                                                selectedGenres.value =
                                                                    selectedGenres.value.filter { it != genre }
                                                            }
                                                        )
                                                    },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.secondary.copy(
                                                        alpha = 0.2f
                                                    )
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = 8.dp,
                                                            vertical = 6.dp
                                                        ),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = genre.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        overflow = TextOverflow.Ellipsis,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 1,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            OutlinedButton(onClick = { statusDropdownExpanded = true }) {
                                Row {

                                    Text(
                                        selectedStatus.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Status",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = statusDropdownExpanded,
                                onDismissRequest = { statusDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                AnimeStatus.entries.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.name) },
                                        onClick = {
                                            selectedStatus = status
                                            statusDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = releaseDate,
                            onValueChange = { },
                            label = { Text("Release Date") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calendar),
                                    contentDescription = "Select Date",
                                    modifier = Modifier.clickable { showDatePicker = true }
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    val newAnime = Anime(
                        id = anime?.id ?: generateRandomId(),
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        genre = selectedGenres.value,
                        rating = rating.toDoubleOrNull() ?: 0.0,
                        episodesCount = episodeCount.toIntOrNull() ?: 12,
                        animeStatus = selectedStatus,
                        releaseDate = releaseDate
                    )
                    onSave(newAnime)
                },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text(if (anime == null) "Add" else "Update")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

