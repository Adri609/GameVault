package com.gamevault.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.SettingsDataStore
import com.gamevault.data.local.ThemeMode
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.repository.FirestoreRepository
import com.gamevault.data.repository.IgdbRepository
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

data class ProfileState(
    val user: Resource<User> = Resource.Loading(),
    val totalGames: Int = 0,
    val averageRating: Double = 0.0,
    val lastAddedGame: Game? = null,
    val topGenres: List<String> = emptyList(),
    val topPlatforms: List<String> = emptyList(),
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val passwordResetSent: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val isUploadingImage: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val igdbRepository: IgdbRepository,
    private val gameDao: GameDao,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfileData()
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsDataStore.themeMode.collectLatest { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            // Mostrar caché inmediatamente mientras carga Firestore
            settingsDataStore.cachedUser.first()?.let { cached ->
                _state.update { it.copy(user = Resource.Success(cached)) }
            }

            firestoreRepository.getUserProfile().onSuccess { user ->
                settingsDataStore.cacheUserProfile(user) // actualizar caché
                _state.update { it.copy(user = Resource.Success(user)) }
            }.onFailure { e ->
                // Solo mostrar error si no hay caché que mostrar
                if (_state.value.user !is Resource.Success) {
                    _state.update { it.copy(user = Resource.Error(e.message ?: "Error al cargar perfil")) }
                }
            }

            // Observar estadísticas locales continuamente
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

                _state.update {
                    it.copy(
                        totalGames = total,
                        averageRating = avg,
                        lastAddedGame = lastGame
                    )
                }

                // Calcular Top Géneros y Plataformas sobre TODOS los juegos de la bóveda
                val genres = games.flatMap { it.genres }.filter { it.isNotBlank() }
                    .groupingBy { it }.eachCount()
                    .toList().sortedByDescending { it.second }.take(3).map { it.first }

                val platforms = games.flatMap { it.platforms }.filter { it.isNotBlank() }
                    .groupingBy { it }.eachCount()
                    .toList().sortedByDescending { it.second }.take(3).map { it.first }

                _state.update { it.copy(topGenres = genres, topPlatforms = platforms) }
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

    fun sendPasswordReset() {
        val email = auth.currentUser?.email ?: return
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                _state.update { it.copy(passwordResetSent = true) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(mode)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _state.update { it.copy(notificationsEnabled = enabled) }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isUploadingImage = true) }
            firestoreRepository.uploadProfilePicture(uri).onSuccess { url ->
                val currentUser = (_state.value.user as? Resource.Success)?.data ?: return@onSuccess
                val updatedUser = currentUser.copy(profilePictureUrl = url)

                firestoreRepository.updateUserProfile(updatedUser).onSuccess {
                    settingsDataStore.cacheUserProfile(updatedUser)
                    _state.update {
                        it.copy(
                            user = Resource.Success(updatedUser),
                            isUploadingImage = false
                        )
                    }
                }.onFailure {
                    _state.update { it.copy(isUploadingImage = false) }
                }
            }.onFailure {
                _state.update { it.copy(isUploadingImage = false) }
            }
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch

            // 1. Borrar datos en Firestore (Bóveda y Perfil)
            firestoreRepository.deleteUserData().onSuccess {
                // 2. Borrar datos en Room (Local)
                gameDao.clearVault(userId)

                // 3. Borrar la cuenta de Firebase Auth
                auth.currentUser?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete()
                    } else {
                        android.util.Log.e(
                            "ProfileViewModel",
                            "Error al borrar cuenta: ${task.exception?.message}"
                        )
                    }
                }
            }.onFailure { e ->
                android.util.Log.e(
                    "ProfileViewModel",
                    "Error al borrar datos de Firestore: ${e.message}"
                )
            }
        }
    }

    fun resetUpdateSuccess() {
        _state.update { it.copy(updateSuccess = false) }
    }

    fun signOut() {
        auth.signOut()
    }
}

