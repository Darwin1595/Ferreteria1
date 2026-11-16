package com.example.parcialkotlinapp

/** RF-02-09: productos con existencias iguales o por debajo del mínimo. */
class AlertasStockActivity : ListaAdminActivity() {

    override val titulo = "Alertas de stock"
    override val textoVacio = "✔ Todo el inventario está por encima del mínimo."

    override fun observar() = mostrar(vm.alertas) { lista ->
        lista.map { p ->
            Fila(
                p.nombre, p.estadoStock, p.categoria.guion(),
                "Existencias: ${p.stock} · Mínimo: ${p.stockMinimo}\nToca para ajustar las existencias.",
                ""
            ) { abrirAjuste(vm, p) { recargar() } }
        }
    }

    override fun recargar() = vm.cargarAlertas()
}