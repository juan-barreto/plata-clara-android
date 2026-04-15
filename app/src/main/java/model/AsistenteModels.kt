package com.candlelabs.gestionpersonal.model

// Mensaje individual del chat — rol puede ser "user" o "assistant"
data class MensajeChat(
    val role: String,
    val content: String
)

// Categoría serializable para mandar al backend
data class CategoriaContexto(
    val nombre: String,
    val gastado: Double,
    val presupuesto: Double
)

// Lo que mandamos al backend — incluye contexto financiero del usuario
data class AsistenteRequest(
    val mensaje: String,
    val historial: List<MensajeChat>,
    val nombre: String = "Usuario",
    val ingreso: Double = 0.0,
    val gastos: Double = 0.0,
    val balance: Double = 0.0,
    val categorias: List<CategoriaContexto> = emptyList()
)

// Lo que recibimos del backend
data class AsistenteResponse(
    val respuesta: String
)