package com.candlelabs.gestionpersonal.network

import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth

object RetrofitClient {
    private const val BASE_URL = "https://web-production-f82cf.up.railway.app/"

    fun create(supabase: SupabaseClient): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val token = supabase.auth.currentSessionOrNull()?.accessToken ?: ""
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}