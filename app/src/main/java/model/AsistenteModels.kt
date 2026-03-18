package com.candlelabs.gestionpersonal.model

// Mensaje individual del chat — rol puede ser "user" o "assistant"
data class MensajeChat(
    val role: String,
    val content: String
)

// Lo que mandamos al backend
data class AsistenteRequest(
    val mensaje: String,
    val historial: List<MensajeChat>,
    val nombre: String = "Usuario"
)

// Lo que recibimos del backend
data class AsistenteResponse(
    val respuesta: String
)