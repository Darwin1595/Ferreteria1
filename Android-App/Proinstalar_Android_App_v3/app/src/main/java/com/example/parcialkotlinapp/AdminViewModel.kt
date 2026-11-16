package com.example.parcialkotlinapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.models.*
import com.example.parcialkotlinapp.network.ApiService
import retrofit2.Call

/** Capa de datos: es la única que habla con Retrofit. */
class FerreteriaRepository(private val api: ApiService = ApiClient.api) {
    fun producto(id: Int) = api.getProducto(id)
    fun categorias() = api.getCategorias()

    fun guardarProducto(id: Int?, req: ProductoRequest): Call<Producto> =
        if (id == null) api.crearProducto(req) else api.actualizarProducto(id, req)

    fun deshabilitarProducto(id: Int) = api.deshabilitarProducto(id)

    fun proveedores() = api.getProveedores()

    fun guardarProveedor(id: Int?, req: ProveedorRequest): Call<Proveedor> =
        if (id == null) api.crearProveedor(req) else api.actualizarProveedor(id, req)

    fun eliminarProveedor(id: Int) = api.eliminarProveedor(id)

    fun compras() = api.getCompras()
    fun registrarCompra(req: CompraRequest) = api.crearCompra(req)

    fun ventas() = api.adminPedidos()
    fun registrarVenta(req: PedidoRequest) = api.crearPedido(req)
    fun estadoVenta(id: Int, estado: String) = api.adminEstadoPedido(id, EstadoRequest(estado))

    fun inventario() = api.getInventario()
    fun alertas() = api.getAlertas()
    fun movimientos() = api.getMovimientos()
    fun ajustarStock(req: AjusteRequest) = api.ajustarStock(req)

    fun reporteStock() = api.getReporteStock()
    fun reporteResumen() = api.getReporteResumen()
}

/** ViewModel del cliente: detalle de un producto. */
class CatalogoViewModel : ViewModel() {
    private val repo = FerreteriaRepository()
    val producto = MutableLiveData<UiState<Producto>>()

    fun cargarProducto(id: Int) = repo.producto(id).publicarEn(producto)
}

/** ViewModel del administrador: inventario, proveedores, compras, ventas y reportes. */
class AdminViewModel : ViewModel() {
    private val repo = FerreteriaRepository()

    val inventario = MutableLiveData<UiState<List<InventarioItem>>>()
    val alertas = MutableLiveData<UiState<List<InventarioItem>>>()
    val movimientos = MutableLiveData<UiState<List<Movimiento>>>()
    val proveedores = MutableLiveData<UiState<List<Proveedor>>>()
    val compras = MutableLiveData<UiState<List<CompraResumen>>>()
    val ventas = MutableLiveData<UiState<List<PedidoAdmin>>>()
    val categorias = MutableLiveData<UiState<List<Categoria>>>()
    val reporteStock = MutableLiveData<UiState<ReporteStock>>()
    val reporteResumen = MutableLiveData<UiState<ReporteResumen>>()

    fun cargarInventario() = repo.inventario().publicarEn(inventario)
    fun cargarAlertas() = repo.alertas().publicarEn(alertas)
    fun cargarMovimientos() = repo.movimientos().publicarEn(movimientos)
    fun cargarProveedores() = repo.proveedores().publicarEn(proveedores)
    fun cargarCompras() = repo.compras().publicarEn(compras)
    fun cargarVentas() = repo.ventas().publicarEn(ventas)
    fun cargarCategorias() = repo.categorias().publicarEn(categorias)
    fun cargarReporteStock() = repo.reporteStock().publicarEn(reporteStock)
    fun cargarReporteResumen() = repo.reporteResumen().publicarEn(reporteResumen)

    // ---- Acciones ----
    fun guardarProducto(id: Int?, req: ProductoRequest, ok: () -> Unit, error: (String) -> Unit) =
        repo.guardarProducto(id, req).ejecutar({ ok() }, error)

    fun deshabilitarProducto(id: Int, ok: () -> Unit, error: (String) -> Unit) =
        repo.deshabilitarProducto(id).ejecutar({ ok() }, error)

    fun guardarProveedor(id: Int?, req: ProveedorRequest, ok: () -> Unit, error: (String) -> Unit) =
        repo.guardarProveedor(id, req).ejecutar({ ok() }, error)

    fun eliminarProveedor(id: Int, ok: () -> Unit, error: (String) -> Unit) =
        repo.eliminarProveedor(id).ejecutar({ ok() }, error)

    fun registrarCompra(req: CompraRequest, ok: () -> Unit, error: (String) -> Unit) =
        repo.registrarCompra(req).ejecutar({ ok() }, error)

    fun registrarVenta(req: PedidoRequest, ok: (PedidoResponse?) -> Unit, error: (String) -> Unit) =
        repo.registrarVenta(req).ejecutar(ok, error)

    fun cambiarEstadoVenta(id: Int, estado: String, ok: () -> Unit, error: (String) -> Unit) =
        repo.estadoVenta(id, estado).ejecutar({ ok() }, error)

    fun ajustarStock(req: AjusteRequest, ok: () -> Unit, error: (String) -> Unit) =
        repo.ajustarStock(req).ejecutar({ ok() }, error)
}