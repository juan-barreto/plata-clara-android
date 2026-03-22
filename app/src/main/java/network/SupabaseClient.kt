package com.candlelabs.gestionpersonal.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient


object SupabaseClient {

    // Estas son las credenciales de tu proyecto Supabase
    // Equivalente en Python: SUPABASE_URL = "https://..."
    private const val SUPABASE_URL = "https://klqqopafpeiazgdgjkbv.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtscXFvcGFmcGVpYXpnZGdqa2J2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQxOTQ4MzUsImV4cCI6MjA4OTc3MDgzNX0.DCE6VeQ69v0F5WlvPAa22ry2bXiilEXplUiBc5Zw3iQ"

    val instance = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
    }
}
