package com.sukuna.animestudio.di

import com.sukuna.animestudio.domain.UserManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
    
    @Provides
    @Singleton
    fun provideUserManager(): UserManager = UserManager()
} 