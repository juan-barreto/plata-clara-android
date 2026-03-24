package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.candlelabs.gestionpersonal.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Cargando : AuthUiState()
    object Exito : AuthUiState()
    data class Error(val mensaje: String) : AuthUiState()
}

class AuthViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val supabase = SupabaseClient.instance

    // ═══════════════════════════════════════════════════════
    // REGISTRO CON EMAIL + CONTRASEÑA + NOMBRE
    // Guarda el nombre en user_metadata de Supabase
    // ═══════════════════════════════════════════════════════
    fun registrarConEmail(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    this.data = buildJsonObject {
                        put("full_name", nombre)
                    }
                }
                _uiState.value = AuthUiState.Exito
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    when {
                        e.message?.contains("already registered") == true -> "Este email ya está registrado"
                        e.message?.contains("invalid") == true -> "Email o contraseña inválidos"
                        e.message?.contains("least 6") == true -> "La contraseña debe tener al menos 6 caracteres"
                        else -> e.message ?: "Error al registrar"
                    }
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // LOGIN CON EMAIL + CONTRASEÑA
    // ═══════════════════════════════════════════════════════
    fun loginConEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _uiState.value = AuthUiState.Exito
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    when {
                        e.message?.contains("Invalid login") == true -> "Email o contraseña incorrectos"
                        e.message?.contains("Email not confirmed") == true -> "Confirmá tu email antes de iniciar sesión"
                        else -> e.message ?: "Error al iniciar sesión"
                    }
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // LOGIN CON GOOGLE
    // ═══════════════════════════════════════════════════════
    fun loginConGoogle() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId("195862119946-a3qlfmg7igilcmq6193ggo3ajpqeb1op.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                val googleIdToken = GoogleIdTokenCredential.createFrom(result.credential.data)

                supabase.auth.signInWith(IDToken) {
                    idToken = googleIdToken.idToken
                    provider = Google
                }

                _uiState.value = AuthUiState.Exito

            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    when {
                        e.message?.contains("canceled") == true -> "Inicio de sesión cancelado"
                        e.message?.contains("No credentials") == true -> "No se encontraron cuentas de Google"
                        else -> e.message ?: "Error con Google Sign-In"
                    }
                )
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                _uiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error al cerrar sesión")
            }
        }
    }

    fun verificarSesion() {
        viewModelScope.launch {
            try {
                // Esperamos a que Supabase restaure la sesión guardada
                supabase.auth.awaitInitialization()
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    _uiState.value = AuthUiState.Exito
                }
            } catch (_: Exception) {
                // No hay sesión guardada
            }
        }
    }

    fun resetEstado() {
        _uiState.value = AuthUiState.Idle
    }

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(context) as T
            }
        }
    }
}