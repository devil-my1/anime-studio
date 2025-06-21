package com.sukuna.animestudio.data.repository

import android.net.Uri
import com.sukuna.animestudio.domain.model.Episode

interface StorageRepository {
    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): Result<String>
    suspend fun deleteProfilePicture(userId: String): Result<Unit>
    
    /**
     * Fetches all available episodes for a specific anime from Firebase Storage.
     * @param animeId The ID or title of the anime
     * @return List of episodes with their Firebase Storage URLs
     */
    suspend fun getAnimeEpisodes(animeId: String): Result<List<Episode>>
    
    /**
     * Fetches a specific episode's video URL from Firebase Storage.
     * @param animeId The ID or title of the anime
     * @param episodeNumber The episode number to fetch
     * @return The video URL for the episode
     */
    suspend fun getEpisodeVideoUrl(animeId: String, episodeNumber: Int): Result<String>
    
    /**
     * Lists all available anime folders in Firebase Storage.
     * @return List of anime IDs/titles available in storage
     */
    suspend fun getAvailableAnimeList(): Result<List<String>>
    
    /**
     * Checks if a specific episode exists in Firebase Storage.
     * @param animeId The ID or title of the anime
     * @param episodeNumber The episode number to check
     * @return True if the episode exists, false otherwise
     */
    suspend fun episodeExists(animeId: String, episodeNumber: Int): Result<Boolean>
} 