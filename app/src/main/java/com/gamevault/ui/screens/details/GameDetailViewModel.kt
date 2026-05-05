package com.gamevault.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.repository.IgdbRepository
import com.gamevault.data.repository.SteamRepository
import com.gamevault.domain.model.Achievement
import com.gamevault.domain.model.Game
import com.gamevault.domain.usecase.ToggleVaultUseCase
import com.gamevault.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    fun toggleHiddenAchievements() {
        _state.update { it.copy(showHiddenAchievements = !it.showHiddenAchievements) }
    }

    fun toggleVaultState() {
        val currentGame = _state.value.game.data ?: return
        viewModelScope.launch {
            try {
                toggleVaultUseCase(currentGame)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
