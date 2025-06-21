package com.sukuna.animestudio.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sukuna.animestudio.domain.model.Episode
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {
    private val TAG = "StorageRepositoryImpl"
    private val ANIMES_STORAGE_PATH = "animes_storage"

    override suspend fun uploadProfilePicture(userId: String, imageUri: Uri): Result<String> {
        return try {
            val storageRef = storage.reference
                .child("profile_pictures")
                .child("$userId.jpg")

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile picture: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteProfilePicture(userId: String): Result<Unit> {
        return try {
            val storageRef = storage.reference
                .child("profile_pictures")
                .child("$userId.jpg")

            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting profile picture: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetches all available episodes for a specific anime from Firebase Storage.
     * Scans the anime folder and returns episodes with their video URLs.
     */
    override suspend fun getAnimeEpisodes(animeId: String): Result<List<Episode>> {
        return try {
            Log.d(TAG, "Fetching episodes for anime: $animeId")
            
            // Get reference to the anime folder
            val animeFolderRef = storage.reference
                .child(ANIMES_STORAGE_PATH)
                .child(animeId)
            
            // List all files in the anime folder
            val result = animeFolderRef.listAll().await()
            
            val episodes = mutableListOf<Episode>()
            
            // Process each file in the folder
            result.items.forEach { fileRef ->
                val fileName = fileRef.name
                
                // Check if the file is a video file (episode)
                if (isVideoFile(fileName)) {
                    val episodeNumber = extractEpisodeNumber(fileName)
                    if (episodeNumber != null) {
                        try {
                            // Get the download URL for the video
                            val downloadUrl = fileRef.downloadUrl.await()
                            
                            val episode = Episode(
                                id = "${animeId}_episode_$episodeNumber",
                                title = "Episode $episodeNumber",
                                episodeNumber = episodeNumber,
                                animeId = animeId,
                                videoUrl = downloadUrl.toString(),
                                duration = 24 // Default duration, can be updated later
                            )
                            
                            episodes.add(episode)
                            Log.d(TAG, "Found episode $episodeNumber for anime $animeId")
                        } catch (e: Exception) {
                            Log.w(TAG, "Error getting download URL for episode $episodeNumber: ${e.message}")
                        }
                    }
                }
            }
            
            // Sort episodes by episode number
            val sortedEpisodes = episodes.sortedBy { it.episodeNumber }
            
            Log.d(TAG, "Successfully fetched ${sortedEpisodes.size} episodes for anime $animeId")
            Result.success(sortedEpisodes)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching episodes for anime $animeId: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetches a specific episode's video URL from Firebase Storage.
     */
    override suspend fun getEpisodeVideoUrl(animeId: String, episodeNumber: Int): Result<String> {
        return try {
            Log.d(TAG, "Fetching video URL for anime: $animeId, episode: $episodeNumber")
            
            // Construct the file path
            val fileName = "episode_$episodeNumber.mp4"
            val episodeRef = storage.reference
                .child(ANIMES_STORAGE_PATH)
                .child(animeId)
                .child(fileName)
            
            // Get the download URL
            val downloadUrl = episodeRef.downloadUrl.await()
            
            Log.d(TAG, "Successfully fetched video URL for episode $episodeNumber")
            Result.success(downloadUrl.toString())
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching video URL for anime $animeId, episode $episodeNumber: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Lists all available anime folders in Firebase Storage.
     */
    override suspend fun getAvailableAnimeList(): Result<List<String>> {
        return try {
            Log.d(TAG, "Fetching available anime list from Firebase Storage")
            
            val animesStorageRef = storage.reference.child(ANIMES_STORAGE_PATH)
            val result = animesStorageRef.listAll().await()
            
            val animeList = result.prefixes.map { it.name }
            
            Log.d(TAG, "Successfully fetched ${animeList.size} available anime")
            Result.success(animeList)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching available anime list: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Checks if a specific episode exists in Firebase Storage.
     */
    override suspend fun episodeExists(animeId: String, episodeNumber: Int): Result<Boolean> {
        return try {
            Log.d(TAG, "Checking if episode exists: anime $animeId, episode $episodeNumber")
            
            val fileName = "episode_$episodeNumber.mp4"
            val episodeRef = storage.reference
                .child(ANIMES_STORAGE_PATH)
                .child(animeId)
                .child(fileName)
            
            // Try to get metadata to check if file exists
            val metadata = episodeRef.metadata.await()
            val exists = metadata != null
            
            Log.d(TAG, "Episode $episodeNumber for anime $animeId exists: $exists")
            Result.success(exists)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if episode exists: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Helper function to check if a file is a video file.
     */
    private fun isVideoFile(fileName: String): Boolean {
        val videoExtensions = listOf(".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm")
        return videoExtensions.any { fileName.lowercase().endsWith(it) }
    }

    /**
     * Helper function to extract episode number from filename.
     * Supports formats like: episode_1.mp4, ep1.mp4, 01.mp4, etc.
     */
    private fun extractEpisodeNumber(fileName: String): Int? {
        return try {
            // Remove file extension
            val nameWithoutExtension = fileName.substringBeforeLast(".")
            
            // Try different patterns to extract episode number
            val patterns = listOf(
                Regex("episode_(\\d+)"),
                Regex("episode-(\\d+)"),
                Regex("ep(\\d+)"),
                Regex("(\\d+)"),
                Regex("e(\\d+)")
            )
            
            for (pattern in patterns) {
                val match = pattern.find(nameWithoutExtension.lowercase())
                if (match != null) {
                    return match.groupValues[1].toInt()
                }
            }
            
            null
        } catch (e: Exception) {
            Log.w(TAG, "Error extracting episode number from filename: $fileName")
            null
        }
    }
} 