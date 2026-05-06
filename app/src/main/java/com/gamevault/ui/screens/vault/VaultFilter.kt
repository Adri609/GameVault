package com.gamevault.ui.screens.vault

import com.gamevault.domain.model.GameStatus

/**
 * Representa los posibles filtros aplicables a la lista de la Bóveda.
 * Combina opciones especiales de la interfaz de usuario (Todos, Favoritos)
 * con los estados nativos del dominio [GameStatus].
 *
 * Al usar una clase sellada, evitamos duplicar los estados del dominio y
 * aseguramos que cualquier nuevo estado añadido a [GameStatus] pueda ser
 * soportado automáticamente.
 *
 * @property title Nombre legible que se mostrará en el menú desplegable.
 */
sealed class VaultFilter(val title: String) {
    data object All : VaultFilter("Todos")
    data object Favorites : VaultFilter("Favoritos")

    data class ByStatus(val status: GameStatus) : VaultFilter(status.displayName)
}