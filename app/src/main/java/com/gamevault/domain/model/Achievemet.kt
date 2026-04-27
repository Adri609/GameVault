package com.gamevault.domain.model

/**
 * Modelo limpio que representa un logro en la UI.
 */

data class Achievement(
    val name: String,         // El nombre interno del logro
    val title: String,        // El nombre bonito para mostrar al usuario
    val description: String,  // Lo que hay que hacer para desbloquearlo
    val iconUrl: String,      // La imagen del logro
    val isHidden: Boolean     // Si es un logro secreto (spoiler)
)
