package com.gamevault.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.repository.IgdbRepository
import com.gamevault.data.repository.SteamRepository
import com.gamevault.domain.model.Achievement
import com.gamevault.domain.model.Game
import com.gamevault.domain.model.GameStatus
import com.gamevault.domain.usecase.ToggleVaultUseCase
import com.gamevault.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado que representa los datos expuestos en la pantalla de detalles del juego.
 *
 * @property game Recurso que encapsula el estado de carga y los datos del [Game].
 * @property achievements Lista de logros de Steam asociados al juego.
 * @property showHiddenAchievements Define si se deben mostrar los logros marcados como spoiler.
 */
data class GameDetailState(
    val game: Resource<Game> = Resource.Loading(),
    val achievements: List<Achievement> = emptyList(),
    val showHiddenAchievements: Boolean = false
)

/**
 * ViewModel que gestiona la obtención de detalles de un juego y su estado de persistencia local.
 */
@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val igdbRepository: IgdbRepository,
    private val steamRepository: SteamRepository,
    private val gameDao: GameDao,
    private val toggleVaultUseCase: ToggleVaultUseCase,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gameId: Long = checkNotNull(savedStateHandle["gameId"])

    private val _state = MutableStateFlow(GameDetailState())
    val state: StateFlow<GameDetailState> = _state.asStateFlow()

    // Observar si el juego está guardado para el usuario actual
    val isSaved: StateFlow<Boolean> = auth.currentUser?.uid?.let { userId ->
        gameDao.isGameSaved(gameId, userId)
    }?.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    ) ?: MutableStateFlow(false)

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
     * Si no está en la bóveda, lo añade inyectándole automáticamente el estado [GameStatus.WISHLIST].
     * Si ya estaba, lo elimina (actúa como un Toggle).
     */
    fun toggleUnreleasedVaultState() {
        val currentGame = _state.value.game.data ?: return
        viewModelScope.launch {
            try {
                // Inyectamos el estado WISHLIST antes de mandarlo al UseCase
                val gameToSave = currentGame.copy(status = GameStatus.WISHLIST)
                toggleVaultUseCase(gameToSave)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Acción avanzada lanzada desde el [ManageVaultBottomSheet] para juegos ya lanzados.
     * Guarda o actualiza el juego con la configuración exacta elegida por el usuario.
     *
     * @param status El nuevo estado del juego en la bóveda (Jugando, Completado, etc.).
     * @param rating La nota personal otorgada por el usuario (o null si no la valoró).
     * @param isFavorite Si el usuario lo ha marcado como favorito.
     */
    fun saveVaultEntry(status: GameStatus, rating: Float?, isFavorite: Boolean) {
        val currentGame = _state.value.game.data ?: return
        viewModelScope.launch {
            try {
                val updatedGame = currentGame.copy(
                    status = status,
                    personalRating = rating,
                    isFavorite = isFavorite
                )
                toggleVaultUseCase(updatedGame)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Elimina el juego actual de la bóveda del usuario.
     */
    fun removeFromVault() {
        val currentGame = _state.value.game.data ?: return
        viewModelScope.launch {
            try {
                // Al estar ya guardado, tu ToggleVaultUseCase actuará como un DELETE
                toggleVaultUseCase(currentGame)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}