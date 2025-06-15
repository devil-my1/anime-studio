package com.sukuna.animestudio.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val profilePictureUrl: String = "",
    val bio: String = "",
    val favoriteAnime: List<String> = emptyList(),
    val watchlist: List<String> = emptyList(),
    val completedAnime: List<String> = emptyList(),
    val watchingAnime: List<String> = emptyList(),
    val droppedAnime: List<String> = emptyList(),
    val totalEpisodesWatched: Int = 0,
    val averageRating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
) 