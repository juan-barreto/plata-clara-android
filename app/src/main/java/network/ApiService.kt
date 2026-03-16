package com.candlelabs.gestionpersonal.network

import com.candlelabs.gestionpersonal.model.AjusteRequest
import com.candlelabs.gestionpersonal.model.AjusteResponse
import com.candlelabs.gestionpersonal.model.DolarResponse
import com.candlelabs.gestionpersonal.model.HistorialItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // Endpoint existente — no se toca
    @GET("dolar")
    suspend fun getDolar(): List<DolarResponse>

    // Endpoint nuevo — POST a /calcular-ajuste
    // @Body le dice a Retrofit que convierta AjusteRequest a JSON automáticamente
    @POST("calcular-ajuste")
    suspend fun calcularAjuste(@Body request: AjusteRequest): AjusteResponse

    // Endpoint historial — devuelve la lista completa de cálculos guardados
    @GET("historial")
    suspend fun getHistorial(): List<HistorialItem>
}