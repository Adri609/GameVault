package com.gamevault.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.repository.IgdbRepository
import com.gamevault.domain.model.Game
import com.gamevault.domain.usecase.SyncVaultUseCase
import com.gamevault.domain.usecase.ToggleVaultUseCase
import com.gamevault.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val popularGames: Resource<List<Game>> = Resource.Loading(),
    val newReleases: Resource<List<Game>> = Resource.Loading(),
    val anticipatedGames: Resource<List<Game>> = Resource.Loading()
)

/**
 * ViewModel que gestiona los datos de la pantalla de inicio.
 * Se encarga de llamar al repositorio para obtener los juegos de IGDB.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val igdbRepository: IgdbRepository,
    private val toggleVaultUseCase: ToggleVaultUseCase,
    private val syncVaultUseCase: SyncVaultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        fetchAllCategories()
        syncVault()
    }

    private fun syncVault() {
        viewModelScope.launch {
            syncVaultUseCase()
        }
    }

    fun fetchAllCategories() {
        fetchPopularGames()
        fetchNewReleases()
        fetchAnticipatedGames()
    }

    private fun fetchPopularGames() {
        viewModelScope.launch {
            _state.update { it.copy(popularGames = Resource.Loading()) }
            try {
                val games = igdbRepository.getPopularGames()
                _state.update { it.copy(popularGames = Resource.Success(games)) }
            } catch (e: Exception) {
                _state.update { it.copy(popularGames = Resource.Error("No se pudieron cargar los juegos populares")) }
            }
        }
    }

    private fun fetchNewReleases() {
        viewModelScope.launch {
            _state.update { it.copy(newReleases = Resource.Loading()) }
            try {
                val games = igdbRepository.getNewReleases()
                _state.update { it.copy(newReleases = Resource.Success(games)) }
            } catch (e: Exception) {
                _state.update { it.copy(newReleases = Resource.Error("No se pudieron cargar los nuevos lanzamientos")) }
            }
        }
    }

    private fun fetchAnticipatedGames() {
        viewModelScope.launch {
            _state.update { it.copy(anticipatedGames = Resource.Loading()) }
            try {
                val games = igdbRepository.getAnticipatedGames()
                _state.update { it.copy(anticipatedGames = Resource.Success(games)) }
            } catch (e: Exception) {
                _state.update { it.copy(anticipatedGames = Resource.Error("No se pudieron cargar los juegos más esperados")) }
            }
        }
    }

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
