package com.proinstalar.app.data.api

import com.proinstalar.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // Categorías
    @GET("api/categorias")
    suspend fun getCategorias(): Response<List<Categoria>>

    // Productos
    @GET("api/productos")
    suspend fun getProductos(
        @Query("categoriaId") categoriaId: Int? = null,
        @Query("q") query: String? = null
    ): Response<List<Producto>>

    @GET("api/productos/{id}")
    suspend fun getProductoById(@Path("id") id: Int): Response<Producto>

    // Servicios
    @GET("api/servicios")
    suspend fun getServicios(): Response<List<TipoServicio>>

    // Solicitudes / Cotizaciones / Pedidos
    @POST("api/solicitudes")
    suspend fun crearSolicitud(@Body request: SolicitudRequest): Response<SolicitudResponse>

    @GET("api/solicitudes")
    suspend fun getSolicitudes(): Response<List<SolicitudResponse>>
}
