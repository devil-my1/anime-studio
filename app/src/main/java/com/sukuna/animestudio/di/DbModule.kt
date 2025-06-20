package com.sukuna.animestudio.di

import com.google.firebase.firestore.FirebaseFirestore
import com.sukuna.animestudio.data.repository.DbRepository
import com.sukuna.animestudio.data.repository.DbRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
    @Provides
    @Singleton
    fun provideFirebaseDb(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideDbRepository(firebaseDb: FirebaseFirestore): DbRepository {
        return DbRepositoryImpl(firebaseDb)
    }
}