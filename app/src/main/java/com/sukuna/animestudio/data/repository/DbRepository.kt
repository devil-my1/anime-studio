package com.sukuna.animestudio.data.repository

import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.domain.model.User

interface DbRepository {
    suspend fun getUserById(userId: String): User?
    suspend fun getAllUsers(): List<User>
    suspend fun addUser(user: User): Long
    suspend fun updateUser(user: User): Int
    suspend fun deleteUser(userId: String): Int
    suspend fun getAnimeById(animeId: String): Anime?
    suspend fun getAllAnimes(): List<Anime>
    suspend fun addAnime(anime: Anime): Long
    suspend fun updateAnime(anime: Anime): Int
    suspend fun deleteAnime(animeId: String): Int
}