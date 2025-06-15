package com.sukuna.animestudio.di

import com.google.firebase.storage.FirebaseStorage
import com.sukuna.animestudio.data.repository.StorageRepository
import com.sukuna.animestudio.data.repository.StorageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideStorageRepository(firebaseStorage: FirebaseStorage): StorageRepository {
        return StorageRepositoryImpl(firebaseStorage)
    }
} 