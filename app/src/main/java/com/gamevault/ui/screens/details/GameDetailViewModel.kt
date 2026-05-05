package com.gamevault.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.mapper.toDomainModel
import com.gamevault.data.repository.IgdbRepository
import com.gamevault.data.repository.SteamRepository
import com.gamevault.domain.model.Achievement
import com.gamevault.domain.model.Game
import com.gamevault.domain.model.GameStatus
import com.gamevault.domain.usecase.SaveVaultEntryUseCase
import com.gamevault.domain.usecase.ToggleVaultUseCase
import com.gamevault.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado que representa los datos expuestos en la pantalla de detalles del juego.
 *
 * @param game Recurso que encapsula el estado de carga y los datos base del [Game] desde la API.
 * @param achievements Lista de logros de Steam asociados al juego.
 * @param showHiddenAchievements Define si se deben mostrar los logros marcados como spoiler.
 */
data class GameDetailState(
    val game: Resource<Game> = Resource.Loading(),
    val achievements: List<Achievement> = emptyList(),
    val showHiddenAchievements: Boolean = false
)

/**
 * ViewModel que gestiona la obtención de detalles de un juego y la interacción
 * con la bóveda del usuario (guardar, actualizar, eliminar).
 */
@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val igdbRepository: IgdbRepository,
    private val steamRepository: SteamRepository,
    private val gameDao: GameDao,
    private val toggleVaultUseCase: ToggleVaultUseCase,
    private val saveVaultEntryUseCase: SaveVaultEntryUseCase,
    auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gameId: Long = checkNotNull(savedStateHandle["gameId"])

    private val _state = MutableStateFlow(GameDetailState())
    val state: StateFlow<GameDetailState> = _state.asStateFlow()

    /**
     * Observa si el juego está guardado en la bóveda local del usuario.
     */
    val isSaved: StateFlow<Boolean> = auth.currentUser?.uid?.let { userId ->
        gameDao.isGameSaved(gameId, userId)
    }?.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    ) ?: MutableStateFlow(false)

    /**
     * Observa los datos del juego directamente desde la base de datos local si existe.
     * Útil para recuperar el estado, nota personal y favorito guardados previamente.
     */
    val localVaultGame: StateFlow<Game?> = auth.currentUser?.uid?.let { userId ->
        gameDao.getGameById(gameId, userId).map { it?.toDomainModel() }
    }?.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    ) ?: MutableStateFlow(null)

    init {
        fetchGameDetails()
    }

    private fun fetchGameDetails() {
        viewModelScope.launch {
            _state.update { it.copy(game = Resource.Loading()) }

            try {
                val fetchedGame = igdbRepository.getGameDetails(gameId)
                if (fetchedGame != null) {
                    _state.update { it.copy(game = Resource.Success(fetchedGame)) }

                    fetchedGame.steamId?.let { appId ->
                        val achievements = steamRepository.getGameAchievements(appId)
                        _state.update { it.copy(achievements = achievements) }
                    }
                } else {
                    _state.update { it.copy(game = Resource.Error("No se encontró el juego")) }
                }
            } catch (e: Exception) {
                // CORRECCIÓN: Usamos la variable 'e' para que no dé warning y nos sirva en el logcat
                e.printStackTrace()
                _state.update { it.copy(game = Resource.Error("Error al cargar los detalles")) }
            }
        }
    }

    /**
     * Alterna la visibilidad de los logros que contienen spoilers.
     */
    fun toggleHiddenAchievements() {
        _state.update { it.copy(showHiddenAchievements = !it.showHiddenAchievements) }
    }

    /**
     * Acción rápida utilizada EXCLUSIVAMENTE para juegos no lanzados.
     * Añade o elimina el juego de la bóveda actuando como un interruptor.
     */
    fun toggleUnreleasedVaultState() {
        val currentGame = _state.value.game.data ?: return
        viewModelScope.launch {
            try {
                val gameToSave = currentGame.copy(status = GameStatus.WISHLIST)
                toggleVaultUseCase(gameToSave)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Acción de actualización lanzada desde el BottomSheet para juegos ya lanzados.
     * Guarda el juego con las nuevas estadísticas definidas por el usuario.
     *
     * @param status El nuevo estado del juego en la bóveda.
     * @param rating La nota personal otorgada por el usuario.
     * @param isFavorite Bandera que marca si es favorito.
     */
    fun saveVaultEntry(status: GameStatus, rating: Float?, isFavorite: Boolean) {
        // Tomamos como base los datos de la API
        val currentGame = _state.value.game.data ?: return
        viewModelScope.launch {
            try {
                val updatedGame = currentGame.copy(
                    status = status,
                    personalRating = rating,
                    isFavorite = isFavorite
                )
                saveVaultEntryUseCase(updatedGame)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Elimina explícitamente el juego de la bóveda.
     */
    fun removeFromVault() {
        val currentGame = _state.value.game.data ?: return
        viewModelScope.launch {
            try {
                // Al estar ya guardado, ToggleVaultUseCase hará la eliminación en Room y Firebase
                toggleVaultUseCase(currentGame)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}