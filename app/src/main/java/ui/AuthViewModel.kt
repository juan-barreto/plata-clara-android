package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.candlelabs.gestionpersonal.network.RetrofitClient
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
    object RegistroExitoso : AuthUiState()
    object ResetEnviado : AuthUiState()
    data class Error(val mensaje: String) : AuthUiState()
}

class AuthViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val supabase = SupabaseClient.instance

    // Chequea si el usuario ya configuró ingreso O ya tiene movimientos
    // Si cualquiera de las dos es true, no le pide el ingreso de nuevo
    private suspend fun usuarioYaConfiguro(): Boolean {
        // Check 1: SharedPreferences tiene ingreso
        val tieneIngreso = context.getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE)
            .getFloat("ingreso_principal", 0f) > 0f
        if (tieneIngreso) return true

        // Check 2: Ya tiene movimientos en el backend (usuario existente)
        return try {
            val movimientos = RetrofitClient.instance.getPresupuesto("mensual")
            movimientos.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun registrarConEmail(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    this.data = buildJsonObject { put("full_name", nombre) }
                }
                _uiState.value = AuthUiState.RegistroExitoso
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    when {
                        e.message?.contains("already registered") == true -> "Este email ya está registrado"
                        e.message?.contains("invalid") == true -> "Email o contraseña inválidos"
                        e.message?.contains("least 6") == true -> "La contraseña debe tener al menos 6 caracteres"
                        e.message?.contains("rate_limit") == true -> "Demasiados intentos. Esperá unos minutos."
                        else -> e.message ?: "Error al registrar"
                    }
                )
            }
        }
    }

    fun loginConEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _uiState.value = if (usuarioYaConfiguro()) AuthUiState.Exito else AuthUiState.RegistroExitoso
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

    fun loginConGoogle() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId("195862119946-a3qlfmg7igilcmq6193ggo3ajpqeb1op.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()
                val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                val googleIdToken = GoogleIdTokenCredential.createFrom(result.credential.data)

                supabase.auth.signInWith(IDToken) {
                    idToken = googleIdToken.idToken
                    provider = Google
                }

                _uiState.value = if (usuarioYaConfiguro()) AuthUiState.Exito else AuthUiState.RegistroExitoso
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

    fun recuperarPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                supabase.auth.resetPasswordForEmail(email)
                _uiState.value = AuthUiState.ResetEnviado
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error al enviar el mail de recuperación")
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            try { supabase.auth.signOut(); _uiState.value = AuthUiState.Idle }
            catch (e: Exception) { _uiState.value = AuthUiState.Error(e.message ?: "Error al cerrar sesión") }
        }
    }

    fun verificarSesion() {
        viewModelScope.launch {
            try {
                supabase.auth.awaitInitialization()
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    _uiState.value = if (usuarioYaConfiguro()) AuthUiState.Exito else AuthUiState.RegistroExitoso
                }
            } catch (_: Exception) { }
        }
    }

    fun resetEstado() { _uiState.value = AuthUiState.Idle }
    fun completarSetup() { _uiState.value = AuthUiState.Exito }

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(context) as T
            }
        }
    }
}