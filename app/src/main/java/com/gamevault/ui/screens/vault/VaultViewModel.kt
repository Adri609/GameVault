package com.gamevault.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.dao.GameDao
import com.gamevault.domain.model.Game
import com.gamevault.domain.usecase.SyncVaultUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gestiona la lógica de la pantalla de la Bóveda.
 */
@HiltViewModel
class VaultViewModel @Inject constructor(
    private val gameDao: GameDao,
    private val syncVaultUseCase: SyncVaultUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    /**
     * Observa los juegos de la bóveda filtrados por el usuario actual.
     */
    val savedGames: StateFlow<List<Game>> = auth.currentUser?.uid?.let { userId ->
        gameDao.getAllFavoriteGames(userId).map { entities ->
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
    }?.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    ) ?: MutableStateFlow(emptyList())

    init {
        syncWithCloud()
    }

    /**
     * Ejecuta el proceso de sincronización bidireccional.
     */
    fun syncWithCloud() {
        viewModelScope.launch {
            _isSyncing.value = true
            syncVaultUseCase()
            _isSyncing.value = false
        }
    }
}
