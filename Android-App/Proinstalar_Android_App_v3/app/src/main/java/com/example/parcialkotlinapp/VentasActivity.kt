package com.example.parcialkotlinapp

import androidx.appcompat.app.AlertDialog
import com.example.parcialkotlinapp.models.PedidoAdmin

/**
 * RF-02-06: ventas. Muestra las compras de los clientes (app) y las ventas de mostrador,
 * y permite registrar una venta nueva. Al vender, el servidor descuenta el stock.
 */
class VentasActivity : ListaAdminActivity() {

    override val titulo = "Ventas"
    override val textoVacio = "Aún no hay ventas. Toca «Venta mostrador» para registrar una."
    override val textoFab: String? = "Venta mostrador"
    override val conBuscador = true

    private val estados = listOf("PAGADO" to "Pagado", "ENVIADO" to "Enviado", "ENTREGADO" to "Entregado")

    override fun observar() = mostrar(vm.ventas) { lista ->
        lista.map { p ->
            Fila(
                "#${p.id} · ${p.clienteNombre.guion()}",
                p.estado,
                p.fechaCreacion.fechaHora(),
                "${p.clienteEmail.guion()}\nDirección: ${p.direccion.guion()}\n" +
                        (p.items ?: emptyList()).joinToString("\n") { "${it.cantidad} × ${it.nombre}" },
                "Total ${p.total.cop()}"
            ) { cambiarEstado(p) }
        }
    }

    override fun recargar() {
        vm.cargarVentas()
        vm.cargarInventario()
    }

    override fun alPulsarFab() {
        val productos = vm.inventario.datos()
        if (productos.isNullOrEmpty()) {
            toast("Cargando productos… intenta de nuevo en un momento")
            return
        }
        abrirLineas(vm, ModoLineas.VENTA, productos, emptyList()) { recargar() }
    }

    private fun cambiarEstado(p: PedidoAdmin) {
        AlertDialog.Builder(this)
            .setTitle("Venta #${p.id}: cambiar estado")
            .setItems(estados.map { it.second }.toTypedArray()) { _, i ->
                vm.cambiarEstadoVenta(
                    p.id, estados[i].first,
                    {
                        toast("Estado actualizado")
                        recargar()
                    },
                    { mensaje -> mostrarSheet("No se pudo cambiar", mensaje) }
                )
            }
            .show()
    }
}