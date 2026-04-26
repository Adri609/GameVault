package com.gamevault.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.dao.GameDao
import com.gamevault.domain.model.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val gameDao: GameDao
) : ViewModel() {

    val savedGames: StateFlow<List<Game>> = gameDao.getAllFavoriteGames()
        .map { entities ->
            entities.map { entity ->
                Game(
                    id = entity.id,
                    name = entity.name,
                    coverUrl = entity.coverUrl,
                    rating = entity.rating,
                    releaseDate = entity.releaseDate,
                    genres = entity.genres,
                    platforms = entity.platforms
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Optimiza el uso de batería si la app se va a 2º plano
            initialValue = emptyList()
        )
}