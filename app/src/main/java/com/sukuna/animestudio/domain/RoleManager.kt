package com.sukuna.animestudio.domain

import com.sukuna.animestudio.domain.model.User
import com.sukuna.animestudio.domain.model.UserRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleManager @Inject constructor() {
    
    fun canModerateContent(user: User?): Boolean {
        return user?.role == UserRole.MODERATOR || user?.role == UserRole.ADMIN
    }

    fun canManageUsers(user: User?): Boolean {
        return user?.role == UserRole.ADMIN
    }

    fun canEditAnime(user: User?): Boolean {
        return user?.role == UserRole.MODERATOR || user?.role == UserRole.ADMIN
    }

    fun canDeleteContent(user: User?): Boolean {
        return user?.role == UserRole.ADMIN
    }

    fun isAdmin(user: User?): Boolean {
        return user?.role == UserRole.ADMIN
    }

    fun isModerator(user: User?): Boolean {
        return user?.role == UserRole.MODERATOR
    }

    fun isRegularUser(user: User?): Boolean {
        return user?.role == UserRole.USER
    }
} 