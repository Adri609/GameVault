package com.gamevault.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity
import com.gamevault.data.repository.IgdbRepository
import com.gamevault.data.repository.SteamRepository
import com.gamevault.domain.model.Achievement
import com.gamevault.domain.model.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gestiona la obtención de detalles de un juego y su estado de persistencia local.
 */
@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val gameDao: GameDao,
    savedStateHandle: SavedStateHandle // Hilt inyecta los argumentos de navegación
) : ViewModel() {
    private val repository = IgdbRepository()
    private val steamRepository = SteamRepository()

    // Extraer el id directamente de los argumentos de navegación
    private val gameId: Long = checkNotNull(savedStateHandle["gameId"])

    // Estado del juego
    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Lista de logros
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    // Estado para controlar si se quiere mostrar los logros ocultos
    private val _showHiddenAchievements = MutableStateFlow(false)
    val showHiddenAchievements: StateFlow<Boolean> = _showHiddenAchievements.asStateFlow()

    // Función para alternar la visibilidad
    fun toggleHiddenAchievements() {
        _showHiddenAchievements.value = !_showHiddenAchievements.value
    }

    // Con room se puede observar directamente si el juego está en la bóveda
    val isSaved: StateFlow<Boolean> = gameDao.isGameSaved(gameId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false // Si en algún momento se guarda o se borra, cambiará esta variable automáticamente
        )

    init {
        // Empezar a descargar los datos en cuanto se crea el ViewModel
        fetchGameDetails()
    }

    private fun fetchGameDetails() {
        viewModelScope.launch {
            _isLoading.value = true

            // Hacer la llamada
            val fetchedGame = repository.getGameDetails(gameId)

            // Asignar el resultado al estado
            _game.value = fetchedGame

            // Si tiene ID de Steam, carga los logros
            fetchedGame?.steamId?.let { appId ->
                _achievements.value = steamRepository.getGameAchievements(appId)
            }

            _isLoading.value = false
        }
    }

    /**
     * Alterna el estado del juego en la bóveda:
     * Si ya estaba guardado, lo elimina. Si no lo estaba, lo añade.
     */

    fun toggleVaultState() {
        val currentGame = _game.value ?: return

        viewModelScope.launch {
            if (isSaved.value) {
                // Borrarlo si estaba en la bóveda
                gameDao.deleteGameById(currentGame.id)
            } else {
                // Si no estaba agregarlo
                val entity = GameEntity(
                    id = currentGame.id,
                    name = currentGame.name,
                    coverUrl = currentGame.coverUrl,
                    rating = currentGame.rating,
                    releaseDate = currentGame.releaseDate,
                    genres = currentGame.genres,
                    platforms = currentGame.platforms,
                    summary = currentGame.summary,
                    steamId = currentGame.steamId
                )
                gameDao.insertGame(entity)
            }
        }
    }
}