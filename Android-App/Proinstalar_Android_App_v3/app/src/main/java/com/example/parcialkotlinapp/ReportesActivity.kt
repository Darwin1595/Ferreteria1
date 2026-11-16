package com.example.parcialkotlinapp

/** RF-02-10: reportes de inventario, ventas y compras. */
class ReportesActivity : ListaAdminActivity() {

    override val titulo = "Reportes"
    override val textoVacio = "Sin datos para mostrar."

    private var filasResumen: List<Fila> = emptyList()
    private var filasStock: List<Fila> = emptyList()

    override fun observar() {
        mostrar(vm.reporteResumen) { r ->
            val top = (r.masVendidos ?: emptyList())
                .joinToString("\n") { "• ${it.nombre} — ${it.unidades} und. · ${it.total.cop()}" }
                .ifBlank { "Aún no hay ventas." }
            filasResumen = listOf(
                Fila("Ventas", null, "", "${r.ventasCantidad} venta(s) registradas", r.ventasTotal.cop()),
                Fila("Compras a proveedores", null, "", "${r.comprasCantidad} compra(s) registradas", r.comprasTotal.cop()),
                Fila("Productos más vendidos", null, "", top, "")
            )
            filasResumen + filasStock
        }
        mostrar(vm.reporteStock) { r ->
            val hayAlertas = r.productosBajoMinimo + r.productosAgotados > 0
            val porCategoria = (r.porCategoria ?: emptyList())
                .joinToString("\n") { "• ${it.categoria.guion()}: ${it.unidades} und. · ${it.valor.cop()}" }
            filasStock = listOf(
                Fila(
                    "Inventario valorizado", null, "",
                    "${r.totalProductos} producto(s) · ${r.unidadesTotales} unidades",
                    r.valorInventario.cop()
                ),
                Fila(
                    "Estado del stock", if (hayAlertas) "STOCK_BAJO" else "DISPONIBLE", "",
                    "Bajo el mínimo: ${r.productosBajoMinimo}\nAgotados: ${r.productosAgotados}", ""
                ),
                Fila("Valor por categoría", null, "", porCategoria.ifBlank { "-" }, "")
            )
            filasResumen + filasStock
        }
    }

    override fun recargar() {
        vm.cargarReporteResumen()
        vm.cargarReporteStock()
    }
}