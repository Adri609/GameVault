package com.gamevault.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.SettingsDataStore
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.repository.FirestoreRepository
import com.gamevault.domain.model.Game
import com.gamevault.domain.model.GameStatus
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
 * Estado reactivo que representa los datos públicos e internos del perfil del usuario.
 * Actúa como única fuente de verdad para la UI de la pantalla de perfil.
 *
 * @property user Estado de la petición (Carga, Éxito, Error) que contiene los datos del usuario.
 * @property totalGames Número total de juegos guardados en la bóveda del usuario.
 * @property completedGames Número de juegos marcados explícitamente como completados.
 * @property playingGames Número de juegos que el usuario está jugando actualmente.
 * @property averageRating Media de las puntuaciones globales (IGDB) de los juegos de la bóveda.
 * @property personalAverage Media de las valoraciones personales otorgadas por el usuario.
 * @property lastAddedGame El juego introducido más recientemente en la colección.
 * @property topGenres Los 3 géneros más repetidos dentro de la colección del usuario.
 * @property topPlatforms Las 3 plataformas más comunes dentro de la colección del usuario.
 * @property isUpdating Indica si actualmente se está ejecutando una petición de guardado de perfil.
 * @property updateSuccess Bandera para notificar a la UI que los cambios se guardaron con éxito.
 * @property isUploadingImage Indica si se está subiendo una nueva imagen de perfil a Storage.
 */
data class ProfileState(
    val user: Resource<User> = Resource.Loading(),
    val totalGames: Int = 0,
    val completedGames: Int = 0,
    val playingGames: Int = 0,
    val averageRating: Double = 0.0,
    val personalAverage: Double = 0.0,
    val lastAddedGame: Game? = null,
    val topGenres: List<String> = emptyList(),
    val topPlatforms: List<String> = emptyList(),
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val isUploadingImage: Boolean = false
)

/**
 * ViewModel dedicado exclusivamente a la gestión de la identidad y estadísticas del usuario.
 *
 * Responsabilidades:
 * 1. Sincronizar el perfil del usuario entre la base de datos remota (Firestore) y la caché local.
 * 2. Gestionar las operaciones de edición del perfil (biografía, redes sociales, carga de avatar).
 * 3. Calcular en tiempo real las estadísticas del "Dashboard Premium" leyendo de la base de datos local.
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

    /**
     * Carga inicial de datos del perfil y suscripción a los cambios de la bóveda local.
     * Muestra la caché inmediatamente para mejorar la percepción de velocidad, mientras
     * valida y actualiza silenciosamente desde Firestore en segundo plano.
     */
    private fun loadProfileData() {
        viewModelScope.launch {
            // 1. Mostrar caché inmediatamente
            settingsDataStore.cachedUser.first()?.let { cached ->
                _state.update { it.copy(user = Resource.Success(cached)) }
            }

            // 2. Traer datos frescos de la nube
            firestoreRepository.getUserProfile().onSuccess { user ->
                settingsDataStore.cacheUserProfile(user)
                _state.update { it.copy(user = Resource.Success(user)) }
            }.onFailure { e ->
                if (_state.value.user !is Resource.Success) {
                    _state.update { it.copy(user = Resource.Error(e.message ?: "Error al cargar perfil")) }
                }
            }

            // 3. Suscripción a la bóveda local para calcular estadísticas
            val userId = auth.currentUser?.uid ?: return@launch
            gameDao.getAllFavoriteGames(userId).collectLatest { games ->
                val total = games.size

                // Cálculo de juegos por estado para el Backlog
                val completed = games.count { it.status == GameStatus.COMPLETED }
                val playing = games.count { it.status == GameStatus.PLAYING }

                // Cálculo de la media global (Ignoramos nulos y evitamos dividir por cero)
                val avg = if (total > 0) games.mapNotNull { it.rating }.average() else 0.0

                // Cálculo de la media personal (Ignoramos juegos que no han sido valorados)
                val personalAvg = games.mapNotNull { it.personalRating }
                    .takeIf { it.isNotEmpty() }
                    ?.average() ?: 0.0

                // Último juego añadido (Asumiendo que el DAO devuelve ordenado por fecha desc)
                val lastEntity = games.firstOrNull()
                val lastGame = lastEntity?.let { entity ->
                    Game(
                        id = entity.id,
                        name = entity.name,
                        coverUrl = entity.coverUrl,
                        rating = entity.rating,
                        personalRating = entity.personalRating,
                        status = entity.status,
                        isFavorite = entity.isFavorite,
                        releaseDate = entity.releaseDate,
                        genres = entity.genres,
                        platforms = entity.platforms,
                        summary = entity.summary,
                        steamId = entity.steamId
                    )
                }

                // Cálculo del ADN Gamer (Top 3 Géneros y Plataformas)
                val genres = games.flatMap { it.genres }.filter { it.isNotBlank() }
                    .groupingBy { it }.eachCount()
                    .toList().sortedByDescending { it.second }.take(3).map { it.first }

                val platforms = games.flatMap { it.platforms }.filter { it.isNotBlank() }
                    .groupingBy { it }.eachCount()
                    .toList().sortedByDescending { it.second }.take(3).map { it.first }

                _state.update {
                    it.copy(
                        totalGames = total,
                        completedGames = completed,
                        playingGames = playing,
                        averageRating = avg,
                        personalAverage = personalAvg,
                        lastAddedGame = lastGame,
                        topGenres = genres,
                        topPlatforms = platforms
                    )
                }
            }
        }
    }

    /**
     * Actualiza la información pública y los enlaces sociales del usuario en Firestore.
     */
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

    /**
     * Gestiona la subida de una nueva imagen de perfil seleccionada desde el dispositivo local.
     */
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

    /**
     * Reinicia el flag de éxito tras consumir la notificación en la UI.
     */
    fun resetUpdateSuccess() {
        _state.update { it.copy(updateSuccess = false) }
    }
}