package com.sukuna.animestudio.data.repository

import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.domain.model.Episode
import com.sukuna.animestudio.domain.model.User

interface DbRepository {
    suspend fun getUserById(userId: String): User?
    suspend fun getAllUsers(): List<User>

    /**
     * Adds a [User] to Firestore and returns the generated document id or an
     * empty string if the operation fails.
     */
    suspend fun addUser(user: User): String

    /**
     * Updates a [User] in Firestore. Returns `true` if the operation succeeds.
     */
    suspend fun updateUser(user: User): Boolean

    /**
     * Updates only the favorite anime list for a user. Returns `true` if the operation succeeds.
     * This is more efficient than updating the entire user object.
     */
    suspend fun updateUserFavorites(userId: String, favoriteAnime: List<Anime>): Boolean

    /**
     * Deletes the user with [userId]. Returns `true` when the document has been
     * removed successfully.
     */
    suspend fun deleteUser(userId: String): Boolean

    suspend fun getAnimeById(animeId: String): Anime?
    suspend fun getAllAnimes(): List<Anime>

    /**
     * Adds an [Anime] document and returns its id or an empty string if failed.
     */
    suspend fun addAnime(anime: Anime): String

    suspend fun updateAnime(anime: Anime): Boolean

    suspend fun deleteAnime(animeId: String): Boolean

    /**
     * Updates an [Episode] in the database. Returns `true` if the operation succeeds.
     * Adds the episode to the specified anime if it doesn't exist, or updates it if it does.
     */
    suspend fun updateEpisode(animeId: String, episode: Episode): Boolean

    /**
     * Gets favorite statistics from all users to calculate community favorites.
     * Returns a map of anime ID to the number of users who have favorited it.
     */
    suspend fun getFavoriteStatistics(): Map<String, Int>
}