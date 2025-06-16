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
    override suspend fun getUserById(userId: String): User {
        return firebaseDb.collection("users").document(userId).get()
            .await()
            .toObject(User::class.java) ?: User()

    }

    override suspend fun getAllUsers(): List<User> {
        return firebaseDb.collection("users").get()
            .await()
            .documents
            .mapNotNull { it.toObject(User::class.java) }
    }

    override suspend fun addUser(user: User): Long {
        return firebaseDb.collection("users").add(user)
            .await()
            .id.hashCode().toLong() // Using hashCode as a placeholder for ID
    }

    override suspend fun updateUser(user: User): Int {
        return try {
            val result = firebaseDb.collection("users").document(user.id).set(user)
                .await()
            result.let { 1 } // Assuming 1 indicates success
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error updating user", e)
            e.printStackTrace()
            -1
        }

    }

    override suspend fun deleteUser(userId: String): Int {
        return firebaseDb.collection("users").document(userId).delete()
            .await()
            .let { 1 } // Assuming 1 indicates success
    }

    override suspend fun getAnimeById(animeId: String): Anime {
        return firebaseDb.collection("animes").document(animeId).get()
            .await()
            .toObject(Anime::class.java) ?: Anime() // Return an empty Anime object if not found
    }

    override suspend fun getAllAnimes(): List<Anime> {
        return firebaseDb.collection("animes").get()
            .await()
            .documents
            .mapNotNull { it.toObject(Anime::class.java) }
    }

    override suspend fun addAnime(anime: Anime): Long {
        return firebaseDb.collection("animes").add(anime)
            .await()
            .id.toLong() // Assuming the ID is a string representation of a long
    }

    override suspend fun updateAnime(anime: Anime): Int {
        return try {
            firebaseDb.collection("animes").document(anime.id).set(anime)
                .await()
            1 // Assuming 1 indicates success
        } catch (e: Exception) {
            Log.e("DbRepositoryImpl", "Error updating anime", e)
            e.printStackTrace()
            -1 // Indicating failure
        }
    }

    override suspend fun deleteAnime(animeId: String): Int {
        return firebaseDb.collection("animes").document(animeId).delete()
            .await()
            .let { 1 } // Assuming 1 indicates success
    }

}