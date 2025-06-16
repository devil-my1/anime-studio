package com.sukuna.animestudio.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val profilePictureUrl: String = "",
    val bio: String = "",
    val favoriteAnime: List<Anime> = emptyList(),
    val watchlist: List<Anime> = emptyList(),
    val completedAnime: List<Anime> = emptyList(),
    val watchingAnime: List<Anime> = emptyList(),
    val droppedAnime: List<Anime> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
