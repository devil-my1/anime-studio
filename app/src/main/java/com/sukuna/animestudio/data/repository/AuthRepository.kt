package com.sukuna.animestudio.data.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun signUp(email: String, password: String): Result<FirebaseUser>
    suspend fun signOut()
    fun isUserSignedIn(): Boolean
} 