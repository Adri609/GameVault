package com.gamevault.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.SettingsDataStore
import com.gamevault.data.local.ThemeMode
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado que representa las configuraciones puramente técnicas de la app.
 */
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val passwordResetSent: Boolean = false
)

/**
 * ViewModel encargado de gestionar las preferencias de usuario (Notificaciones, Tema)
 * y el ciclo de vida de la cuenta (Cerrar sesión, Eliminar cuenta, Recuperar contraseña).
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val gameDao: GameDao,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsDataStore.themeMode.collectLatest { mode ->
                _state.update { it.copy(themeMode = mode) }
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
        // Aquí en un futuro podrías añadir lógica extra para (des)suscribir a temas de FCM
    }

    fun sendPasswordReset() {
        val email = auth.currentUser?.email ?: return
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                _state.update { it -> it.copy(passwordResetSent = true) }
            }
        }
    }

    fun resetPasswordState() {
        _state.update { it.copy(passwordResetSent = false) }
    }

    fun signOut() {
        auth.signOut()
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            firestoreRepository.deleteUserData().onSuccess {
                gameDao.clearVault(userId)
                auth.currentUser?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) onComplete()
                }
            }
        }
    }
}