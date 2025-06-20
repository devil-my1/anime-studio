package com.sukuna.animestudio.presentation.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sukuna.animestudio.R

/**
 * Main admin panel screen providing entry points to different admin tools.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(onBack: () -> Unit) {
    var showUserManagement by rememberSaveable { mutableStateOf(false) }
    var showAnimeManagement by rememberSaveable { mutableStateOf(false) }
    var showAnalytics by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anime Studio (Admin Panel)", color =
                MaterialTheme.colorScheme.tertiary.copy(0.8f)) },
                navigationIcon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.admin_icon),
                        contentDescription = "Admin Panel",
                        tint = MaterialTheme.colorScheme.tertiary.copy(
                            alpha = 0.8f
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .clickable { onBack() }
                            .size(24.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
    }

    if (showUserManagement) {
        UserManagementDialog(onDismiss = { showUserManagement = false })
    }

    if (showAnimeManagement) {
        AnimeManagementDialog(onDismiss = { showAnimeManagement = false })
    }

    if (showAnalytics) {
        // TODO: Analytics dialog
    }
}
