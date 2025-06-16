package com.sukuna.animestudio.domain.model

enum class UserRole {
    /** User that isn't authenticated */
    GUEST,
    /** Regular user with basic permissions */
    USER,
    /** Can moderate content and users */
    MODERATOR,
    /** Full system access */
    ADMIN
}
