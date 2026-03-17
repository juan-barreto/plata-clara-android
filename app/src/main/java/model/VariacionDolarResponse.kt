package com.candlelabs.gestionpersonal.model

data class VariacionDolarResponse(
    val casa: String,
    val venta_actual: Double,
    val venta_anterior: Double,
    val compra_actual: Double,    // nueva
    val compra_anterior: Double,  // nueva
    val variacion_porcentual: Double,
    val fecha_actual: String,
    val fecha_anterior: String
)