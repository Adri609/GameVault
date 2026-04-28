package com.gamevault.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.repository.IgdbRepository
import com.gamevault.domain.model.Game
import com.gamevault.domain.usecase.ToggleVaultUseCase
import com.gamevault.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val results: Resource<List<Game>>? = null // null significa que aún no se ha buscado nada
)

/**
 * ViewModel que gestiona la lógica de búsqueda de videojuegos.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val igdbRepository: IgdbRepository,
    private val toggleVaultUseCase: ToggleVaultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    /**
     * Actualiza la consulta de búsqueda actual.
     */
    fun onQueryChange(newQuery: String) {
        _state.update { it.copy(query = newQuery) }
    }

    /**
     * Ejecuta la búsqueda de juegos en el repositorio.
     */
    fun performSearch() {
        val currentQuery = _state.value.query
        if (currentQuery.trim().isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(results = Resource.Loading()) }

            try {
                val games = igdbRepository.searchGames(currentQuery)
                _state.update { it.copy(results = Resource.Success(games)) }
            } catch (e: Exception) {
                _state.update { it.copy(results = Resource.Error("Error al buscar juegos")) }
            }
        }
    }

    /**
     * Gestiona el estado del juego en la bóveda (añadir/eliminar).
     */
    fun toggleVault(game: Game) {
        viewModelScope.launch {
            try {
                toggleVaultUseCase(game)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
