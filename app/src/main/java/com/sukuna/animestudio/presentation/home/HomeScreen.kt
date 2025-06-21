package com.sukuna.animestudio.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sukuna.animestudio.R
import com.sukuna.animestudio.domain.RoleManager
import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.presentation.components.SimpleAnimeImage

/**
 * Main Home Screen composable that displays various anime sections
 * Optimized for performance with lazy loading and proper state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    roleManager: RoleManager
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Use currentUser for real-time updates, fallback to uiState.user
    val user = currentUser ?: uiState.user

    Scaffold(
        topBar = {
            HomeTopAppBar(
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToAdminPanel = onNavigateToAdminPanel,
                user = user,
                roleManager = roleManager
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            HomeContent(
                modifier = Modifier.padding(paddingValues),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                uiState = uiState,
                onAnimeClick = viewModel::onAnimeClick,
                onAnimeFavoriteToggle = viewModel::onAnimeFavoriteToggle
            )
        }
    }
}

/**
 * Top app bar with background image and navigation actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    onNavigateToProfile: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    user: com.sukuna.animestudio.domain.model.User?,
    roleManager: RoleManager
) {
    Box {
        Image(
            painter = painterResource(id = R.drawable.home_screen_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -1.5f)
        )
        LargeTopAppBar(
            title = {
                Text(
                    text = "Anime Studio",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary,
                                )
                            )
                        ),
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                titleContentColor = MaterialTheme.colorScheme.primary
            ),
            actions = {
                if (roleManager.isAdmin(user) || roleManager.isModerator(user)) {
                    IconButton(onClick = onNavigateToAdminPanel) {
                        Icon(
                            painter = painterResource(R.drawable.admin_icon),
                            contentDescription = "Admin Panel",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                IconButton(onClick = onNavigateToProfile) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile"
                    )
                }
            }
        )
    }
}

/**
 * Main content area with search bar and all anime sections
 */
@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    uiState: HomeUiState,
    onAnimeClick: (Anime) -> Unit,
    onAnimeFavoriteToggle: (Anime) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        item {
            // Search Bar
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )
        }

        item {
            // Featured Anime Section
            FeaturedAnimeSection(
                animeList = uiState.featuredAnime,
                onAnimeClick = onAnimeClick,
                onAnimeFavoriteToggle = onAnimeFavoriteToggle
            )
        }

        item {
            // Trending Now Section
            AnimeSection(
                title = "Trending Now",
                animeList = uiState.trendingAnime,
                onAnimeClick = onAnimeClick,
                onAnimeFavoriteToggle = onAnimeFavoriteToggle
            )
        }

        item {
            // Most Favorite Section
            AnimeSection(
                title = "Most Favorite",
                animeList = uiState.mostFavoriteAnime,
                onAnimeClick = onAnimeClick,
                onAnimeFavoriteToggle = onAnimeFavoriteToggle
            )
        }

        item {
            // Top 10 Anime Section
            Top10AnimeSection(
                animeList = uiState.top10Anime,
                onAnimeClick = onAnimeClick,
                onAnimeFavoriteToggle = onAnimeFavoriteToggle
            )
        }

        item {
            // All-Time Popular Section
            AnimeSection(
                title = "All-Time Popular",
                animeList = uiState.allTimePopularAnime,
                onAnimeClick = onAnimeClick,
                onAnimeFavoriteToggle = onAnimeFavoriteToggle
            )
        }

        item {
            // Next Season Section
            NextSeasonSection(
                animeList = uiState.nextSeasonAnime,
                onAnimeClick = onAnimeClick,
                onAnimeFavoriteToggle = onAnimeFavoriteToggle
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Search bar component with modern design
 */
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search anime...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.5f
            )
        )
    )
}

/**
 * Featured Anime section with large cards
 */
@Composable
private fun FeaturedAnimeSection(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onAnimeFavoriteToggle: (Anime) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        SectionTitle(title = "Featured Anime")
        
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(animeList) { anime ->
                FeaturedAnimeCard(
                    anime = anime,
                    onAnimeClick = onAnimeClick,
                    onAnimeFavoriteToggle = onAnimeFavoriteToggle
                )
            }
        }
    }
}

/**
 * Generic anime section with horizontal scrolling
 */
@Composable
private fun AnimeSection(
    title: String,
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onAnimeFavoriteToggle: (Anime) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        SectionTitle(title = title)
        
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(animeList) { anime ->
                AnimeCard(
                    anime = anime,
                    onAnimeClick = onAnimeClick,
                    onAnimeFavoriteToggle = onAnimeFavoriteToggle
                )
            }
        }
    }
}

/**
 * Top 10 Anime section with numbered list
 */
@Composable
private fun Top10AnimeSection(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onAnimeFavoriteToggle: (Anime) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        SectionTitle(title = "Top 10 Anime")
        
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(animeList.take(10)) { anime ->
                Top10AnimeCard(
                    anime = anime,
                    rank = animeList.indexOf(anime) + 1,
                    onAnimeClick = onAnimeClick,
                    onAnimeFavoriteToggle = onAnimeFavoriteToggle
                )
            }
        }
    }
}

/**
 * Next Season section with special styling
 */
@Composable
private fun NextSeasonSection(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onAnimeFavoriteToggle: (Anime) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        SectionTitle(title = "Next Season")
        
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(animeList) { anime ->
                NextSeasonCard(
                    anime = anime,
                    onAnimeClick = onAnimeClick,
                    onAnimeFavoriteToggle = onAnimeFavoriteToggle
                )
            }
        }
    }
}

/**
 * Section title with consistent styling
 */
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.onBackground
    )
}

/**
 * Featured anime card with large size and detailed information
 */
@Composable
private fun FeaturedAnimeCard(
    anime: Anime,
    onAnimeClick: (Anime) -> Unit,
    onAnimeFavoriteToggle: (Anime) -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            .clickable { onAnimeClick(anime) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Anime image
            SimpleAnimeImage(
                imageUrl = anime.imageUrl,
                contentDescription = anime.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            // Favorite button
            IconButton(
                onClick = { onAnimeFavoriteToggle(anime) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (anime.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (anime.isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Title and rating
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (anime.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color.Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = anime.rating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standard anime card for horizontal scrolling sections
 */
@Composable
private fun AnimeCard(
    anime: Anime,
    onAnimeClick: (Anime) -> Unit,
    onAnimeFavoriteToggle: (Anime) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clickable { onAnimeClick(anime) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                SimpleAnimeImage(
                    imageUrl = anime.imageUrl,
                    contentDescription = anime.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Favorite button
                IconButton(
                    onClick = { onAnimeFavoriteToggle(anime) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (anime.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (anime.isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Content section
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (anime.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color.Yellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = anime.rating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top 10 anime card with rank indicator
 */
@Composable
private fun Top10AnimeCard(
    anime: Anime,
    rank: Int,
    onAnimeClick: (Anime) -> Unit,
    onAnimeFavoriteToggle: (Anime) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clickable { onAnimeClick(anime) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image section with rank
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                SimpleAnimeImage(
                    imageUrl = anime.imageUrl,
                    contentDescription = anime.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Rank badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Favorite button
                IconButton(
                    onClick = { onAnimeFavoriteToggle(anime) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (anime.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (anime.isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Content section
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (anime.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color.Yellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = anime.rating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Next season card with special styling for upcoming releases
 */
@Composable
private fun NextSeasonCard(
    anime: Anime,
    onAnimeClick: (Anime) -> Unit,
    onAnimeFavoriteToggle: (Anime) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clickable { onAnimeClick(anime) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image section with "Coming Soon" overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                SimpleAnimeImage(
                    imageUrl = anime.imageUrl,
                    contentDescription = anime.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Coming Soon badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Coming Soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Favorite button
                IconButton(
                    onClick = { onAnimeFavoriteToggle(anime) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (anime.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (anime.isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Content section
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (anime.releaseDate.isNotEmpty()) {
                    Text(
                        text = anime.releaseDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
