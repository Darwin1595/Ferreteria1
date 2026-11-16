package com.example.parcialkotlinapp.api

import com.example.parcialkotlinapp.SessionManager
import com.example.parcialkotlinapp.network.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.jvm.JvmStatic

object ApiClient {
    // Como tu .NET corre en localhost:5000 y usas emulador Android, se usa 10.0.2.2
    private const val BASE_URL = "http://10.0.2.2:5000/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            // Envía el token JWT en cada petición cuando hay sesión iniciada
            .addInterceptor { chain ->
                val token = SessionManager.token
                val req = if (token.isNullOrEmpty()) chain.request()
                else chain.request().newBuilder().header("Authorization", "Bearer $token").build()
                chain.proceed(req)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val usuarioApi: UsuarioApi by lazy {
        retrofit.create(UsuarioApi::class.java)
    }

    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }

    @JvmStatic
    fun getApiService(): ApiService = api
}