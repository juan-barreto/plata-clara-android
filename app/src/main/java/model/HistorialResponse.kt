package com.candlelabs.gestionpersonal.model

// Modela cada elemento del historial que devuelve /historial
data class HistorialItem(
    val id: Int,
    val alquiler_inicial: Double,
    val alquiler_final: Double,
    val fecha_inicio: String,
    val fecha_calculo: String,
    val tipo_indice: String
)