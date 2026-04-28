package com.gamevault.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity
import com.gamevault.data.repository.FirestoreRepository
import com.gamevault.domain.model.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gestiona la lógica de la pantalla de la Bóveda.
 * Se encarga de observar la base de datos local, mapear los datos a modelos de dominio,
 * y sincronizar con la nube (Firestore) si es necesario.
 */
@HiltViewModel
class VaultViewModel @Inject constructor(
    private val gameDao: GameDao,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    // Estado para mostrar un indicador de carga mientras se sincroniza
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    /**
     * Flujo de estado que contiene la lista de juegos guardados por el usuario.
     */
    val savedGames: StateFlow<List<Game>> = gameDao.getAllFavoriteGames()
        .map { entities ->
            entities.map { entity ->
                Game(
                    id = entity.id,
                    name = entity.name,
                    coverUrl = entity.coverUrl,
                    rating = entity.rating,
                    releaseDate = entity.releaseDate,
                    genres = entity.genres,
                    platforms = entity.platforms,
                    summary = entity.summary,
                    steamId = entity.steamId
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Nada más abrir la Bóveda, intentar sincronizar con la nube
        syncWithCloud()
    }

    /**
     * Descarga los juegos de Firestore y los guarda en Room.
     */
    private fun syncWithCloud() {
        viewModelScope.launch {
            _isSyncing.value = true

            firestoreRepository.getUserVaultFromCloud().onSuccess { cloudGames ->
                if (cloudGames.isNotEmpty()) {
                    android.util.Log.d("GameVault_Sync", "Descargados ${cloudGames.size} juegos de la nube. Actualizando Room...")

                    cloudGames.forEach { dto ->
                        // Transformar el DTO de Firebase a la Entidad de Room
                        val entity = GameEntity(
                            id = dto.id,
                            name = dto.name,
                            coverUrl = dto.coverUrl,
                            releaseDate = dto.releaseDate,
                            steamId = dto.steamId,
                            rating = dto.rating,
                            genres = emptyList(),
                            platforms = emptyList(),
                            summary = null
                        )

                        // Insertar o reemplazar en Room
                        gameDao.insertGame(entity)
                    }
                }
            }.onFailure { error ->
                android.util.Log.e("GameVault_Sync", "Error en la sincronización: ${error.message}")
            }

            _isSyncing.value = false
        }
    }
}