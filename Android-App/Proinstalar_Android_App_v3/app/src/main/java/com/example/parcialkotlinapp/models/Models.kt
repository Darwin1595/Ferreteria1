package com.example.parcialkotlinapp.models

data class LoginRequest(
    val email: String,
    val password: String
)

data class CrearUsuarioRequest(
    val nombre: String,
    val email: String,
    val telefono: String,
    val direccion: String,
    val password: String
)

/** Respuesta de login/registro del backend (incluye el token JWT). */
data class AuthResponse(
    val id: Int,
    val nombre: String?,
    val email: String?,
    val rol: String?,
    val token: String?,
    val telefono: String? = null,
    val direccion: String? = null
)

data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val categoriaId: Int = 0,
    val categoriaNombre: String? = null,
    val imagenUrl: String? = null,
    val stockMinimo: Int = 0
)


data class TipoServicio(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precioBase: Double
) {
    // Lo que muestra el Spinner
    override fun toString(): String = nombre
}

data class PedidoItemRequest(val productoId: Int, val cantidad: Int)
data class PedidoRequest(val items: List<PedidoItemRequest>, val direccion: String?)
data class PedidoResponse(val id: Int, val total: Double, val estado: String?, val mensaje: String?)

data class SolicitudRequest(
    val tipoServicioId: Int,
    val causa: String,
    val telefono: String?,
    val direccion: String?
)
data class SolicitudResponse(val id: Int, val estado: String?, val mensaje: String?)

/** Línea del carrito (solo vive en memoria). */
data class CartItem(val producto: Producto, var cantidad: Int)
// ---- Historial (pedidos y solicitudes del usuario) ----
data class PedidoItemDetalle(val nombre: String?, val cantidad: Int, val precioUnitario: Double)

data class PedidoHistorial(
    val id: Int,
    val total: Double,
    val estado: String?,
    val direccion: String?,
    val fechaCreacion: String?,
    val items: List<PedidoItemDetalle>?
)

data class ServicioRef(val nombre: String?)

data class SolicitudHistorial(
    val id: Int,
    val estado: String?,
    val notas: String?,
    val total: Double,
    val fechaCreacion: String?,
    val tipoServicio: ServicioRef?
)
// ---- Administración ----
data class SolicitudAdmin(
    val id: Int,
    val clienteNombre: String?,
    val clienteEmail: String?,
    val clienteTelefono: String?,
    val clienteDireccion: String?,
    val tipoServicio: ServicioRef?,
    val total: Double,
    val estado: String?,
    val notas: String?,
    val fechaCreacion: String?
)

data class PedidoAdmin(
    val id: Int,
    val total: Double,
    val estado: String?,
    val direccion: String?,
    val fechaCreacion: String?,
    val clienteNombre: String?,
    val clienteEmail: String?,
    val items: List<PedidoItemDetalle>?
)
// ---- Ferretería: categorías, proveedores, compras, inventario y reportes ----
data class Categoria(val id: Int, val nombre: String, val imagenUrl: String?) {
    override fun toString(): String = nombre
}

data class ProductoRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val stockMinimo: Int,
    val categoriaId: Int,
    val imagenUrl: String
)

data class Proveedor(
    val id: Int,
    val nombre: String,
    val nit: String?,
    val telefono: String?,
    val email: String?,
    val direccion: String?
) {
    override fun toString(): String = nombre
}

data class ProveedorRequest(
    val nombre: String,
    val nit: String,
    val telefono: String,
    val email: String,
    val direccion: String
)

data class CompraItemRequest(val productoId: Int, val cantidad: Int, val costoUnitario: Double)
data class CompraRequest(val proveedorId: Int, val observacion: String?, val items: List<CompraItemRequest>)
data class CompraLinea(val nombre: String?, val cantidad: Int, val costoUnitario: Double)
data class CompraResumen(
    val id: Int,
    val fecha: String?,
    val total: Double,
    val proveedorNombre: String?,
    val observacion: String?,
    val items: List<CompraLinea>?
)

data class InventarioItem(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val categoriaId: Int,
    val categoria: String?,
    val precio: Double,
    val stock: Int,
    val stockMinimo: Int,
    val imagenUrl: String?,
    val estadoStock: String?
) {
    override fun toString(): String = nombre
}

data class Movimiento(
    val id: Int,
    val productoNombre: String?,
    val tipo: String?,
    val cantidad: Int,
    val stockResultante: Int,
    val motivo: String?,
    val referencia: String?,
    val fecha: String?
)

data class AjusteRequest(val productoId: Int, val nuevoStock: Int, val motivo: String)

data class CategoriaStock(val categoria: String?, val productos: Int, val unidades: Int, val valor: Double)
data class ReporteStock(
    val totalProductos: Int,
    val unidadesTotales: Int,
    val valorInventario: Double,
    val productosBajoMinimo: Int,
    val productosAgotados: Int,
    val porCategoria: List<CategoriaStock>?
)

data class MasVendido(val nombre: String?, val unidades: Int, val total: Double)
data class ReporteResumen(
    val ventasTotal: Double,
    val ventasCantidad: Int,
    val comprasTotal: Double,
    val comprasCantidad: Int,
    val masVendidos: List<MasVendido>?
)

data class PerfilRequest(val nombre: String, val telefono: String, val direccion: String)

data class GestionSolicitudRequest(val estado: String, val total: Double?)
data class EstadoRequest(val estado: String)
data class MensajeResp(val mensaje: String?)