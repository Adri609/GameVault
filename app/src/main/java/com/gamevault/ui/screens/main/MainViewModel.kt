package com.gamevault.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.local.SettingsDataStore
import com.gamevault.data.repository.FirestoreRepository
import com.gamevault.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val settingsDataStore: SettingsDataStore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    init {
        loadUserProfile()
        observeCachedUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            firestoreRepository.getUserProfile().onSuccess { user ->
                _userProfile.value = user
            }
        }
    }

    private fun observeCachedUserProfile() {
        viewModelScope.launch {
            settingsDataStore.cachedUser.collectLatest { user ->
                _userProfile.value = user
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
