package com.example.parcialkotlinapp.network

import com.example.parcialkotlinapp.models.AuthResponse
import com.example.parcialkotlinapp.models.CrearUsuarioRequest
import com.example.parcialkotlinapp.models.LoginRequest
import com.example.parcialkotlinapp.models.PedidoRequest
import com.example.parcialkotlinapp.models.PedidoResponse
import com.example.parcialkotlinapp.models.Producto
import com.example.parcialkotlinapp.models.SolicitudRequest
import com.example.parcialkotlinapp.models.SolicitudResponse
import com.example.parcialkotlinapp.models.TipoServicio
import com.example.parcialkotlinapp.models.PedidoHistorial
import com.example.parcialkotlinapp.models.SolicitudHistorial
import com.example.parcialkotlinapp.models.*
import retrofit2.Call
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE

interface ApiService {

    @POST("api/auth/login")
    fun login(@Body login: LoginRequest): Call<AuthResponse>

    @POST("api/auth/register")
    fun crearUsuario(@Body usuario: CrearUsuarioRequest): Call<AuthResponse>

    @GET("api/productos")
    fun getProductos(): Call<List<Producto>>

    @GET("api/servicios")
    fun getServicios(): Call<List<TipoServicio>>

    @POST("api/pedidos")
    fun crearPedido(@Body pedido: PedidoRequest): Call<PedidoResponse>

    @POST("api/solicitudes")
    fun crearSolicitud(@Body solicitud: SolicitudRequest): Call<SolicitudResponse>

    @GET("api/pedidos/mis-pedidos")
    fun misPedidos(): Call<List<PedidoHistorial>>

    @GET("api/solicitudes/mias")
    fun misSolicitudes(): Call<List<SolicitudHistorial>>

    // ---- Administrador ----
    @GET("api/solicitudes")
    fun adminSolicitudes(): Call<List<SolicitudAdmin>>

    @PUT("api/solicitudes/{id}/gestion")
    fun adminGestionarSolicitud(@Path("id") id: Int, @Body body: GestionSolicitudRequest): Call<SolicitudAdmin>

    @GET("api/pedidos")
    fun adminPedidos(): Call<List<PedidoAdmin>>

    @PUT("api/pedidos/{id}/estado")
    fun adminEstadoPedido(@Path("id") id: Int, @Body body: EstadoRequest): Call<MensajeResp>

    // ---- Ferretería: catálogo, proveedores, compras, inventario, reportes ----
    @GET("api/productos/{id}")
    fun getProducto(@Path("id") id: Int): Call<Producto>

    @GET("api/categorias")
    fun getCategorias(): Call<List<Categoria>>

    @POST("api/productos")
    fun crearProducto(@Body body: ProductoRequest): Call<Producto>

    @PUT("api/productos/{id}")
    fun actualizarProducto(@Path("id") id: Int, @Body body: ProductoRequest): Call<Producto>

    @DELETE("api/productos/{id}")
    fun deshabilitarProducto(@Path("id") id: Int): Call<MensajeResp>

    @GET("api/proveedores")
    fun getProveedores(): Call<List<Proveedor>>

    @POST("api/proveedores")
    fun crearProveedor(@Body body: ProveedorRequest): Call<Proveedor>

    @PUT("api/proveedores/{id}")
    fun actualizarProveedor(@Path("id") id: Int, @Body body: ProveedorRequest): Call<Proveedor>

    @DELETE("api/proveedores/{id}")
    fun eliminarProveedor(@Path("id") id: Int): Call<MensajeResp>

    @GET("api/compras")
    fun getCompras(): Call<List<CompraResumen>>

    @POST("api/compras")
    fun crearCompra(@Body body: CompraRequest): Call<MensajeResp>

    @GET("api/inventario")
    fun getInventario(): Call<List<InventarioItem>>

    @GET("api/inventario/alertas")
    fun getAlertas(): Call<List<InventarioItem>>

    @GET("api/inventario/movimientos")
    fun getMovimientos(): Call<List<Movimiento>>

    @POST("api/inventario/ajuste")
    fun ajustarStock(@Body body: AjusteRequest): Call<MensajeResp>

    @GET("api/reportes/stock")
    fun getReporteStock(): Call<ReporteStock>

    @GET("api/reportes/resumen")
    fun getReporteResumen(): Call<ReporteResumen>

    @PUT("api/auth/perfil")
    fun actualizarPerfil(@Body body: PerfilRequest): Call<AuthResponse>
}
