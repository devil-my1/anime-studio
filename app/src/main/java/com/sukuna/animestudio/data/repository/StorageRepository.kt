package com.sukuna.animestudio.data.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): Result<String>
    suspend fun deleteProfilePicture(userId: String): Result<Unit>
} 