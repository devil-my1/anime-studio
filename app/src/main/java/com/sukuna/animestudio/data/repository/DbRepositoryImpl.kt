package com.sukuna.animestudio.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbRepositoryImpl @Inject constructor(
    private val firebaseDb: FirebaseFirestore
) : DbRepository {

    private val usersCollection = firebaseDb.collection("users")
    private val animeCollection = firebaseDb.collection("animes")

    override suspend fun getUserById(userId: String): User? {
        return try {
            usersCollection.document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error fetching user", e)
            null
        }

    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            usersCollection.get().await().documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error fetching users", e)
            emptyList()
        }
    }

    override suspend fun addUser(user: User): String {
        return try {
            usersCollection.document(user.id).set(user).await()
            user.id
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error adding user", e)
            ""
        }
    }

    override suspend fun updateUser(user: User): Boolean {
        return try {
            usersCollection.document(user.id).set(user).await()
            true
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error updating user", e)
            false
        }

    }

    override suspend fun deleteUser(userId: String): Boolean {
        return try {
            usersCollection.document(userId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error deleting user", e)
            false
        }
    }

    override suspend fun getAnimeById(animeId: String): Anime? {
        return try {
            animeCollection.document(animeId).get().await().toObject(Anime::class.java)
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error fetching anime", e)
            null
        }
    }

    override suspend fun getAllAnimes(): List<Anime> {
        return try {
            animeCollection.get().await().documents.mapNotNull { it.toObject(Anime::class.java) }
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error fetching animes", e)
            emptyList()
        }
    }

    override suspend fun addAnime(anime: Anime): String {
        return try {
            animeCollection.document(anime.id).set(anime).await()
            anime.id
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error adding anime", e)
            ""
        }
    }

    override suspend fun updateAnime(anime: Anime): Boolean {
        return try {
            animeCollection.document(anime.id).set(anime).await()
            true
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error updating anime", e)
            false
        }
    }

    override suspend fun deleteAnime(animeId: String): Boolean {
        return try {
            animeCollection.document(animeId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error deleting anime", e)
            false
        }
    }

}