package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MediReminderApplication
import com.example.data.database.UserEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as MediReminderApplication).authRepository

    val loggedInUser: StateFlow<UserEntity?> = repo.getLoggedInUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearMessages() {
        _authError.value = null
        _successMessage.value = null
    }

    fun login(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            _authError.value = null
            
            // Simulating network access latency for realistic material UX
            delay(500)
            
            val result = repo.login(email, password)
            if (result.isSuccess) {
                _successMessage.value = "Welcome back, ${result.getOrNull()?.fullName}!"
            } else {
                _authError.value = result.exceptionOrNull()?.message ?: "Login failed"
            }
            _loading.value = false
        }
    }

    fun signUp(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _authError.value = null
            
            delay(600)
            
            val result = repo.signUp(fullName, email, password)
            if (result.isSuccess) {
                _successMessage.value = "Account created successfully!"
            } else {
                _authError.value = result.exceptionOrNull()?.message ?: "Signup failed"
            }
            _loading.value = false
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _loading.value = true
            _authError.value = null
            
            delay(400)
            
            val result = repo.forgotPassword(email)
            if (result.isSuccess) {
                _successMessage.value = result.getOrNull()
            } else {
                _authError.value = result.exceptionOrNull()?.message ?: "Failed to process request"
            }
            _loading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
        }
    }

    fun updateProfile(fullName: String, email: String) {
        viewModelScope.launch {
            val current = loggedInUser.value ?: return@launch
            val updated = current.copy(fullName = fullName)
            repo.updateUser(updated)
            _successMessage.value = "Profile updated successfully!"
        }
    }
}
