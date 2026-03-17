package com.candlelabs.gestionpersonal.model

// Modela cada fila del historial
data class PeriodoAjuste(
    val periodo: String,
    val alquiler: Double
)

// Modela la respuesta completa del endpoint
data class AjusteResponse(
    val historial: List<PeriodoAjuste>
)

// Modela el cuerpo del POST que mandamos
data class AjusteRequest(
    val alquiler: Double,
    val fecha_inicio: String,
    val fecha_firma: String,
    val indice: String,
    val periodo: Int
)