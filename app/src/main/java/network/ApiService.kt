package com.candlelabs.gestionpersonal.network

import com.candlelabs.gestionpersonal.model.AjusteRequest
import com.candlelabs.gestionpersonal.model.AjusteResponse
import com.candlelabs.gestionpersonal.model.DolarResponse
import com.candlelabs.gestionpersonal.model.HistorialItem
import com.candlelabs.gestionpersonal.model.IpcItem
import com.candlelabs.gestionpersonal.model.VariacionDolarResponse
import com.candlelabs.gestionpersonal.model.HistorialCotizacionItem
import com.candlelabs.gestionpersonal.model.AsistenteRequest
import com.candlelabs.gestionpersonal.model.AsistenteResponse
import com.candlelabs.gestionpersonal.model.MovimientoItem
import com.candlelabs.gestionpersonal.model.MovimientoRequest
import com.candlelabs.gestionpersonal.model.MovimientoEditRequest
import com.candlelabs.gestionpersonal.model.MensajeResponse
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path

interface ApiService {

    // Endpoint existente — no se toca
    @GET("dolar")
    suspend fun getDolar(): List<DolarResponse>
    // Trae la variación de una casa específica — "blue", "oficial", etc.
    @GET("dolar/variacion/{casa}")
    suspend fun getVariacionDolar(@Path("casa") casa: String): VariacionDolarResponse

    // Endpoint nuevo — POST a /calcular-ajuste
    // @Body le dice a Retrofit que convierta AjusteRequest a JSON automáticamente
    @POST("calcular-ajuste")
    suspend fun calcularAjuste(@Body request: AjusteRequest): AjusteResponse

    // Endpoint historial — devuelve la lista completa de cálculos guardados
    @GET("historial")
    suspend fun getHistorial(): List<HistorialItem>

    // Borra un registro específico por id
    @DELETE("historial/{id}")
    suspend fun borrarCalculo(@Path("id") id: Int)

    // Borra todo el historial
    @DELETE("historial")
    suspend fun borrarHistorial()
    //Asistente temporal
    @POST("asistente")
    suspend fun consultarAsistente(@Body request: AsistenteRequest): AsistenteResponse
    @GET("ipc")
    suspend fun getIpc(): List<IpcItem>
    // Trae el historial para los graficos de cotizaciones
    @GET("dolar/historial/{casa}")
    suspend fun getHistorialCotizacion(@Path("casa") casa: String): List<HistorialCotizacionItem>
    // Endpoints- para presupuesto:
    // Trae todos los movimientos según el filtro
// @Query es como ?filtro=mensual en la URL
// Equivalente en Python: requests.get("/presupuesto", params={"filtro": "mensual"})
    @GET("presupuesto")
    suspend fun getPresupuesto(@Query("filtro") filtro: String = "mensual"): List<MovimientoItem>

    // Agrega un movimiento nuevo
    @POST("presupuesto")
    suspend fun agregarMovimiento(@Body request: MovimientoRequest): MensajeResponse

    // Edita un movimiento existente
    @PUT("presupuesto/{id}")
    suspend fun editarMovimiento(@Path("id") id: Int, @Body request: MovimientoEditRequest): MensajeResponse

    // Borra un movimiento
    @DELETE("presupuesto/{id}")
    suspend fun borrarMovimiento(@Path("id") id: Int): MensajeResponse
}


