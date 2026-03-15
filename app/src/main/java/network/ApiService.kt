package com.candlelabs.gestionpersonal.network

import retrofit2.http.GET
import com.candlelabs.gestionpersonal.model.DolarResponse
interface ApiService {
    @GET("dolar")
    suspend fun getDolar(): List<DolarResponse>
}