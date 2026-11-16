package com.example.parcialkotlinapp

/** RF-02-04 y RF-02-05: registrar compras a proveedores; al confirmar suben las existencias. */
class ComprasActivity : ListaAdminActivity() {

    override val titulo = "Compras a proveedores"
    override val textoVacio = "Aún no hay compras. Toca «Nueva compra» para registrar una."
    override val textoFab: String? = "Nueva compra"

    override fun observar() = mostrar(vm.compras) { lista ->
        lista.map { c ->
            val nota = if (c.observacion.isNullOrBlank()) "" else "\nNota: ${c.observacion}"
            Fila(
                "Compra #${c.id} · ${c.proveedorNombre.guion()}",
                null,
                c.fecha.fechaHora(),
                (c.items ?: emptyList()).joinToString("\n") {
                    "${it.cantidad} × ${it.nombre} a ${it.costoUnitario.cop()}"
                } + nota,
                "Total ${c.total.cop()}"
            )
        }
    }

    override fun recargar() {
        vm.cargarCompras()
        vm.cargarProveedores()
        vm.cargarInventario()
    }

    override fun alPulsarFab() {
        val productos = vm.inventario.datos()
        val proveedores = vm.proveedores.datos()
        if (proveedores.isNullOrEmpty()) {
            mostrarSheet("Falta un proveedor", "Primero registra un proveedor en la sección Proveedores.")
            return
        }
        if (productos.isNullOrEmpty()) {
            toast("Cargando productos… intenta de nuevo en un momento")
            return
        }
        abrirLineas(vm, ModoLineas.COMPRA, productos, proveedores) { recargar() }
    }
}