package com.sukuna.animestudio.data.repository

import com.sukuna.animestudio.domain.model.Anime
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
}