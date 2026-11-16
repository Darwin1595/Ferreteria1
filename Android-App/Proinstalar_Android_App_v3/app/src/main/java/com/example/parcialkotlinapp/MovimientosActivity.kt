package com.example.parcialkotlinapp

/** RF-02-08: historial de movimientos de stock (entradas, salidas y ajustes). */
class MovimientosActivity : ListaAdminActivity() {

    override val titulo = "Movimientos de stock"
    override val textoVacio = "Aún no hay movimientos."
    override val conBuscador = true

    override fun observar() = mostrar(vm.movimientos) { lista ->
        lista.map { m ->
            val signo = if (m.cantidad > 0) "+" else ""
            Fila(
                m.productoNombre.guion(),
                m.tipo,
                m.fecha.fechaHora(),
                "Cantidad: $signo${m.cantidad} · Quedó en ${m.stockResultante}\n" +
                        "${m.motivo.guion()} (${m.referencia.guion()})",
                ""
            )
        }
    }

    override fun recargar() = vm.cargarMovimientos()
}