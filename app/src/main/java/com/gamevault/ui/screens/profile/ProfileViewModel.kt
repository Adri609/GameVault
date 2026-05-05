package com.gamevault.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.SettingsDataStore
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.repository.FirestoreRepository
import com.gamevault.domain.model.Game
import com.gamevault.domain.model.User
import com.gamevault.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado que representa los datos públicos e internos del perfil del usuario.
 * No contiene preferencias de la aplicación ni configuraciones de cuenta.
 */
data class ProfileState(
    val user: Resource<User> = Resource.Loading(),
    val totalGames: Int = 0,
    val averageRating: Double = 0.0,
    val lastAddedGame: Game? = null,
    val topGenres: List<String> = emptyList(),
    val topPlatforms: List<String> = emptyList(),
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val isUploadingImage: Boolean = false
)

/**
 * ViewModel dedicado exclusivamente a la gestión de la identidad del usuario (Perfil).
 * Maneja la lectura de datos, edición de biografía, redes sociales, carga de avatar
 * y el cálculo de estadísticas ("ADN Gamer").
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val gameDao: GameDao,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            // Mostrar caché inmediatamente mientras carga Firestore
            settingsDataStore.cachedUser.first()?.let { cached ->
                _state.update { it.copy(user = Resource.Success(cached)) }
            }

            firestoreRepository.getUserProfile().onSuccess { user ->
                settingsDataStore.cacheUserProfile(user)
                _state.update { it.copy(user = Resource.Success(user)) }
            }.onFailure { e ->
                if (_state.value.user !is Resource.Success) {
                    _state.update { it.copy(user = Resource.Error(e.message ?: "Error al cargar perfil")) }
                }
            }

            val userId = auth.currentUser?.uid ?: return@launch
            gameDao.getAllFavoriteGames(userId).collectLatest { games ->
                val total = games.size
                val avg = if (total > 0) games.mapNotNull { it.rating }.average() else 0.0

                val lastEntity = games.firstOrNull()
                val lastGame = lastEntity?.let { entity ->
                    Game(
                        id = entity.id,
                        name = entity.name,
                        coverUrl = entity.coverUrl,
                        rating = entity.rating,
                        releaseDate = entity.releaseDate,
                        genres = entity.genres,
                        platforms = entity.platforms,
                        summary = entity.summary,
                        steamId = entity.steamId
                    )
                }

                val genres = games.flatMap { it.genres }.filter { it.isNotBlank() }
                    .groupingBy { it }.eachCount()
                    .toList().sortedByDescending { it.second }.take(3).map { it.first }

                val platforms = games.flatMap { it.platforms }.filter { it.isNotBlank() }
                    .groupingBy { it }.eachCount()
                    .toList().sortedByDescending { it.second }.take(3).map { it.first }

                _state.update {
                    it.copy(
                        totalGames = total,
                        averageRating = avg,
                        lastAddedGame = lastGame,
                        topGenres = genres,
                        topPlatforms = platforms
                    )
                }
            }
        }
    }

    fun updateProfile(
        username: String,
        profilePictureUrl: String,
        bio: String,
        status: String,
        steam: String,
        twitch: String,
        discord: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, updateSuccess = false) }

            val currentUser = (_state.value.user as? Resource.Success)?.data ?: return@launch
            val updatedUser = currentUser.copy(
                username = username,
                profilePictureUrl = profilePictureUrl,
                bio = bio,
                status = status,
                steamUsername = steam,
                twitchUsername = twitch,
                discordUsername = discord
            )

            firestoreRepository.updateUserProfile(updatedUser).onSuccess {
                settingsDataStore.cacheUserProfile(updatedUser)
                _state.update {
                    it.copy(
                        user = Resource.Success(updatedUser),
                        isUpdating = false,
                        updateSuccess = true
                    )
                }
            }.onFailure {
                _state.update { it.copy(isUpdating = false) }
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isUploadingImage = true) }
            firestoreRepository.uploadProfilePicture(uri).onSuccess { url ->
                val currentUser = (_state.value.user as? Resource.Success)?.data ?: return@onSuccess
                val updatedUser = currentUser.copy(profilePictureUrl = url)

                firestoreRepository.updateUserProfile(updatedUser).onSuccess {
                    settingsDataStore.cacheUserProfile(updatedUser)
                    _state.update { it.copy(user = Resource.Success(updatedUser), isUploadingImage = false) }
                }.onFailure {
                    _state.update { it.copy(isUploadingImage = false) }
                }
            }.onFailure {
                _state.update { it.copy(isUploadingImage = false) }
            }
        }
    }

    fun resetUpdateSuccess() {
        _state.update { it.copy(updateSuccess = false) }
    }
}