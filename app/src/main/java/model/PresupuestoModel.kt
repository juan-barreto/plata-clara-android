package com.candlelabs.gestionpersonal.model

// Un movimiento individual — ingreso o gasto
data class MovimientoItem(
    val id: String,
    val tipo: String,           // "ingreso" o "gasto"
    val categoria: String,      // "Sueldo", "Alquiler", etc
    val descripcion: String?,   // detalle opcional — puede ser null
    val monto: Double,          // siempre positivo
    val fecha: String           // formato ISO
)

// Lo que mandamos al crear un movimiento nuevo
data class MovimientoRequest(
    val tipo: String,
    val categoria: String,
    val descripcion: String,
    val monto: Double
)

// Lo que mandamos al editar un movimiento existente — igual estructura
data class MovimientoEditRequest(
    val tipo: String,
    val categoria: String,
    val descripcion: String,
    val monto: Double
)

// Respuesta genérica del backend para POST/PUT/DELETE
data class MensajeResponse(
    val mensaje: String
)