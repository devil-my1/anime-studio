package com.sukuna.animestudio.presentation.admin

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.sukuna.animestudio.R
import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.domain.model.AnimeGenre
import com.sukuna.animestudio.domain.model.AnimeStatus
import com.sukuna.animestudio.utils.generateRandomId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Dialog used for adding or editing anime information.
 */
@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeFormDialog(
    anime: Anime?,
    onDismiss: () -> Unit,
    onSave: (Anime) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(anime?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(anime?.description ?: "") }
    var imageUrl by rememberSaveable { mutableStateOf(anime?.imageUrl ?: "") }
    var rating by rememberSaveable { mutableStateOf(anime?.rating?.toString() ?: "0.0") }
    var episodeCount by rememberSaveable {
        mutableStateOf(
            anime?.episodesCount?.toString() ?: "12"
        )
    }
    var showGenreDialog by remember { mutableStateOf(false) }
    val selectedGenres = remember { mutableStateOf(anime?.genre ?: listOf(AnimeGenre.ACTION)) }
    var selectedStatus by rememberSaveable {
        mutableStateOf(
            anime?.animeStatus ?: AnimeStatus.IN_PROGRESS
        )
    }
    var releaseDate by rememberSaveable {
        mutableStateOf(anime?.releaseDate ?: SimpleDateFormat("yyyy-MM-dd").format(Date()))
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
                                            selectedGenres.value.filterNot { it == genre }
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
                                        selectedGenres.value.filterNot { it == genre }
                                    }
                                }
                            )
                            Text(genre.name)
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showGenreDialog = false }) { Text("Done") } }
        )
    }

    if (showDatePicker) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = remember {
            try {
                df.parse(releaseDate)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                releaseDate = "$year-${(month + 1).toString().padStart(2, '0')}-${
                    day.toString().padStart(2, '0')
                }"
                showDatePicker = false
            },
            Calendar.getInstance().apply { timeInMillis = currentDate }.get(Calendar.YEAR),
            Calendar.getInstance().apply { timeInMillis = currentDate }.get(Calendar.MONTH),
            Calendar.getInstance().apply { timeInMillis = currentDate }.get(Calendar.DAY_OF_MONTH)
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            maxLines = 5
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
                                .padding(vertical = 8.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(12.dp)

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
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Genre",
                                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
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
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        modifier = Modifier.height(140.dp)
                                    ) {
                                        items(selectedGenres.value.size) { index ->
                                            val genre = selectedGenres.value[index]
                                            OutlinedButton(onClick = {
                                                selectedGenres.value =
                                                    selectedGenres.value.filterNot { it == genre }
                                            }) {
                                                Text(
                                                    genre.name,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = MaterialTheme.colorScheme.tertiary.copy(
                                                        alpha = 0.8f
                                                    )
                                                )
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
                        ) {
                            OutlinedButton(
                                onClick = { statusDropdownExpanded = true },
                                border = _root_ide_package_.androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                )
                            ) {
                                Row {
                                    Text(
                                        selectedStatus.name,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Status",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = statusDropdownExpanded,
                                onDismissRequest = { statusDropdownExpanded = false }) {
                                AnimeStatus.entries.forEach { status ->
                                    DropdownMenuItem(text = { Text(status.name) }, onClick = {
                                        selectedStatus = status
                                        statusDropdownExpanded = false
                                    })
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
            ) { Text(if (anime == null) "Add" else "Update") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

