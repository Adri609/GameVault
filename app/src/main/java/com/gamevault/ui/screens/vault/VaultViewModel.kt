package com.gamevault.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.mapper.toDomainModel
import com.gamevault.domain.model.Game
import com.gamevault.domain.usecase.SyncVaultUseCase
import com.google.firebase.auth.FirebaseAuth
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
 * ViewModel encargado de gestionar la lógica de presentación y estado de la pantalla de la Bóveda.
 *
 * Responsabilidades principales:
 * 1. Exponer la colección local de juegos del usuario de forma reactiva.
 * 2. Gestionar la sincronización de la bóveda local con la nube (Firestore).
 * 3. Exponer el estado de carga durante las operaciones de sincronización.
 *
 * @param gameDao DAO para acceder y observar la base de datos local (Room).
 * @param syncVaultUseCase Caso de uso que encapsula la lógica de sincronización con la nube.
 * @param auth Instancia de Firebase Auth para identificar al usuario actual.
 */
@HiltViewModel
class VaultViewModel @Inject constructor(
    private val gameDao: GameDao,
    private val syncVaultUseCase: SyncVaultUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    /**
     * Estado que indica si actualmente se está ejecutando una sincronización con la nube.
     * Útil para mostrar indicadores de carga (spinners/progress bars) en la interfaz.
     */
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isListView = MutableStateFlow(false)
    val isListView: StateFlow<Boolean> = _isListView.asStateFlow()

    private val _currentFilter = MutableStateFlow<VaultFilter>(VaultFilter.All)
    val currentFilter: StateFlow<VaultFilter> = _currentFilter.asStateFlow()

    /** Cambia entre vista de Cuadrícula y Lista. */
    fun toggleView() {
        _isListView.value = !_isListView.value
    }

    /** Aplica un nuevo filtro a la bóveda. */
    fun setFilter(filter: VaultFilter) {
        _currentFilter.value = filter
    }

    /**
     * Flujo reactivo de la colección personal de juegos del usuario.
     *
     * Se nutre directamente de la base de datos local (Single Source of Truth).
     * Utiliza [toDomainModel] para transformar las entidades de base de datos en modelos
     * de dominio ricos, asegurando que propiedades como el estado, valoración personal
     * y favoritos estén disponibles para el filtrado en la UI.
     */
    val savedGames: StateFlow<List<Game>> = auth.currentUser?.uid?.let { userId ->
        gameDao.getAllFavoriteGames(userId).map { entities ->
            entities.map { entity -> entity.toDomainModel() }
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
     * Inicia el proceso de sincronización en segundo plano.
     *
     * Ejecuta [SyncVaultUseCase] para subir los cambios locales pendientes y
     * descargar actualizaciones desde Firestore. Actualiza el estado [isSyncing]
     * de forma automática durante el proceso.
     */
    fun syncWithCloud() {
        viewModelScope.launch {
            _isSyncing.value = true
            syncVaultUseCase()
            _isSyncing.value = false
        }
    }
}