package com.sukuna.animestudio.di

import com.sukuna.animestudio.domain.RoleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoleModule {
    @Provides
    @Singleton
    fun provideRoleManager(): RoleManager = RoleManager()
}
