package com.gamevault.domain.model

/**
 * Modelo que representa a un usuario registrado en la aplicación.
 */
data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val profilePictureUrl: String = "",
    val bio: String = "",
    val status: String = "",
    val registrationDate: Long = System.currentTimeMillis(),
    val steamUsername: String = "",
    val twitchUsername: String = "",
    val discordUsername: String = ""
)
