package com.gamevault.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity
import com.gamevault.data.repository.IgdbRepository
import com.gamevault.domain.model.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gestiona la lógica de búsqueda de videojuegos.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val gameDao: GameDao
) : ViewModel() {

    private val repository = IgdbRepository()

    // Estado del texto de búsqueda ingresado por el usuario
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Lista de resultados obtenidos de la API
    private val _searchResults = MutableStateFlow<List<Game>>(emptyList())
    val searchResults: StateFlow<List<Game>> = _searchResults.asStateFlow()

    // Controla la visibilidad del indicador de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Indica si se ha completado al menos una búsqueda
    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    /**
     * Actualiza la consulta de búsqueda actual.
     */
    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        _hasSearched.value = false
    }

    /**
     * Ejecuta la búsqueda de juegos en el repositorio.
     */
    fun performSearch() {
        if (_searchQuery.value.trim().isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _hasSearched.value = false

            _searchResults.value = repository.searchGames(_searchQuery.value)

            _isLoading.value = false
            _hasSearched.value = true
        }
    }

    /**
     * Guarda un juego seleccionado en la base de datos local.
     */
    fun addToVault(game: Game) {
        viewModelScope.launch {
            val entity = GameEntity(
                id = game.id,
                name = game.name,
                coverUrl = game.coverUrl,
                rating = game.rating,
                releaseDate = game.releaseDate,
                genres = game.genres,
                platforms = game.platforms,
                summary = game.summary
            )
            gameDao.insertGame(entity)
        }
    }
}