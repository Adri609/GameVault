package com.gamevault.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.repository.IgdbRepository
import com.gamevault.domain.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona los datos de la pantalla de inicio.
 * Se encarga de llamar al repositorio para obtener los juegos de IGDB.
 */
class HomeViewModel : ViewModel() {

    private val repository = IgdbRepository()

    // Estado para Populares
    private val _popularGames = MutableStateFlow<List<Game>>(emptyList())
    val popularGames: StateFlow<List<Game>> = _popularGames.asStateFlow()

    // Estado para Nuevos Lanzamientos
    private val _newReleases = MutableStateFlow<List<Game>>(emptyList())
    val newReleases: StateFlow<List<Game>> = _newReleases.asStateFlow()

    // Estado para los juegos más esperados
    private val _anticipatedGames = MutableStateFlow<List<Game>>(emptyList())
    val anticipatedGames: StateFlow<List<Game>> = _anticipatedGames.asStateFlow()

    init {
        // Lanzar todas las peticiones a la vez al inicializar
        fetchPopularGames()
        fetchNewReleases()
        fetchAnticipatedGames()
    }

    /**
     * Solicita los juegos populares al repositorio.
     */
    private fun fetchPopularGames() {
        viewModelScope.launch {
            try {
                _popularGames.value = repository.getPopularGames()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Solicita los nuevos lanzamientos al repositorio.
     */
    private fun fetchNewReleases() {
        viewModelScope.launch {
            try {
                _newReleases.value = repository.getNewReleases()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Solicita los juegos más esperados al repositorio.
     */
    private fun fetchAnticipatedGames() {
        viewModelScope.launch {
            try {
                _anticipatedGames.value = repository.getAnticipatedGames()
            } catch (e: Exception) {
                // Captura específica de errores de red para depuración
                if (e is retrofit2.HttpException) {
                    android.util.Log.e(
                        "GameVaultError",
                        "Error Anticipated: ${e.response()?.errorBody()?.string()}"
                    )
                }
                e.printStackTrace()
            }
        }
    }
}