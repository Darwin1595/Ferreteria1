package com.example.parcialkotlinapp

import android.app.Activity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.example.parcialkotlinapp.models.AjusteRequest
import com.example.parcialkotlinapp.models.CompraItemRequest
import com.example.parcialkotlinapp.models.CompraRequest
import com.example.parcialkotlinapp.models.InventarioItem
import com.example.parcialkotlinapp.models.PedidoItemRequest
import com.example.parcialkotlinapp.models.PedidoRequest
import com.example.parcialkotlinapp.models.Proveedor
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

/** Ajuste manual de existencias (RF-02-05). Nunca permite stock negativo (RF-02-07). */
fun Activity.abrirAjuste(vm: AdminViewModel, p: InventarioItem, alTerminar: () -> Unit) {
    val (dialog, v) = crearSheet(R.layout.dialog_ajuste)
    v.findViewById<TextView>(R.id.tvAjusteTitulo).text = p.nombre
    v.findViewById<TextView>(R.id.tvAjusteActual).text =
        "Existencias actuales: ${p.stock} · Mínimo: ${p.stockMinimo}"

    val tilStock = v.findViewById<TextInputLayout>(R.id.tilAjusteStock)
    val tilMotivo = v.findViewById<TextInputLayout>(R.id.tilAjusteMotivo)
    val btn = v.findViewById<MaterialButton>(R.id.btnAjusteGuardar)
    tilStock.editText?.setText(p.stock.toString())

    btn.setOnClickListener {
        tilStock.error = null
        tilMotivo.error = null
        val nuevo = tilStock.texto().toIntOrNull()
        if (nuevo == null || nuevo < 0) {
            tilStock.error = "Ingresa un número entero igual o mayor a 0"
            return@setOnClickListener
        }
        if (nuevo == p.stock) {
            tilStock.error = "Es igual a las existencias actuales"
            return@setOnClickListener
        }
        val motivo = tilMotivo.texto()
        if (motivo.length < 4) {
            tilMotivo.error = "Escribe el motivo del ajuste (mínimo 4 letras)"
            return@setOnClickListener
        }
        btn.isEnabled = false
        vm.ajustarStock(
            AjusteRequest(p.id, nuevo, motivo),
            {
                dialog.dismiss()
                toast("Existencias actualizadas")
                alTerminar()
            },
            { mensaje ->
                btn.isEnabled = true
                mostrarSheet("No se pudo ajustar", mensaje)
            }
        )
    }
    dialog.mostrarExpandido()
}

enum class ModoLineas { COMPRA, VENTA }

/**
 * Ventana para armar una lista de productos y confirmarla:
 * COMPRA = entrada de mercancía de un proveedor (RF-02-04).
 * VENTA  = venta en mostrador (RF-02-06).
 */
fun Activity.abrirLineas(
    vm: AdminViewModel,
    modo: ModoLineas,
    productos: List<InventarioItem>,
    proveedores: List<Proveedor>,
    alTerminar: () -> Unit
) {
    class Linea(val producto: InventarioItem, var cantidad: Int, val valor: Double)

    val esCompra = modo == ModoLineas.COMPRA
    val (dialog, v) = crearSheet(R.layout.dialog_lineas)

    v.findViewById<TextView>(R.id.tvLineasTitulo).text =
        if (esCompra) "Registrar compra" else "Venta en mostrador"
    val grupoProveedor = v.findViewById<View>(R.id.grupoLineasProveedor)
    val spProveedor = v.findViewById<Spinner>(R.id.spLineasProveedor)
    val spProducto = v.findViewById<Spinner>(R.id.spLineasProducto)
    val tilCantidad = v.findViewById<TextInputLayout>(R.id.tilLineasCantidad)
    val tilCosto = v.findViewById<TextInputLayout>(R.id.tilLineasCosto)
    val tilNota = v.findViewById<TextInputLayout>(R.id.tilLineasNota)
    val tvResumen = v.findViewById<TextView>(R.id.tvLineasResumen)
    val btnAgregar = v.findViewById<MaterialButton>(R.id.btnLineasAgregar)
    val btnConfirmar = v.findViewById<MaterialButton>(R.id.btnLineasConfirmar)

    val visibleSoloCompra = if (esCompra) View.VISIBLE else View.GONE
    grupoProveedor.visibility = visibleSoloCompra
    tilCosto.visibility = visibleSoloCompra
    tilNota.visibility = visibleSoloCompra

    spProveedor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, proveedores)
    spProducto.adapter = ArrayAdapter(
        this, android.R.layout.simple_spinner_dropdown_item,
        productos.map { if (esCompra) it.nombre else "${it.nombre} (disp. ${it.stock})" }
    )

    val lineas = mutableListOf<Linea>()

    fun pintarResumen() {
        if (lineas.isEmpty()) {
            tvResumen.text = "Aún no has agregado productos."
            return
        }
        val total = lineas.sumOf { it.valor * it.cantidad }
        tvResumen.text = lineas.joinToString("\n") {
            "${it.cantidad} × ${it.producto.nombre} — ${(it.valor * it.cantidad).cop()}"
        } + "\n\nTotal: ${total.cop()}"
    }
    pintarResumen()

    btnAgregar.setOnClickListener {
        tilCantidad.error = null
        tilCosto.error = null
        if (productos.isEmpty()) return@setOnClickListener

        val prod = productos[spProducto.selectedItemPosition]
        val cantidad = tilCantidad.texto().toIntOrNull()
        if (cantidad == null || cantidad <= 0) {
            tilCantidad.error = "Ingresa una cantidad mayor a 0"
            return@setOnClickListener
        }

        var valor = prod.precio
        if (esCompra) {
            val costo = tilCosto.texto().replace(',', '.').toDoubleOrNull()
            if (costo == null || costo <= 0) {
                tilCosto.error = "Ingresa el costo unitario"
                return@setOnClickListener
            }
            valor = costo
        } else {
            val yaAgregado = lineas.filter { it.producto.id == prod.id }.sumOf { it.cantidad }
            if (cantidad + yaAgregado > prod.stock) {
                tilCantidad.error = "Solo hay ${prod.stock} disponibles"
                return@setOnClickListener
            }
        }

        val igual = lineas.firstOrNull { it.producto.id == prod.id && it.valor == valor }
        if (igual != null) igual.cantidad += cantidad else lineas.add(Linea(prod, cantidad, valor))

        tilCantidad.editText?.setText("")
        tilCosto.editText?.setText("")
        pintarResumen()
    }

    btnConfirmar.setOnClickListener {
        if (lineas.isEmpty()) {
            mostrarSheet("Lista vacía", "Agrega al menos un producto antes de confirmar.")
            return@setOnClickListener
        }
        val fallo: (String) -> Unit = { mensaje ->
            btnConfirmar.isEnabled = true
            mostrarSheet("No se pudo registrar", mensaje)
        }

        if (esCompra) {
            if (proveedores.isEmpty()) {
                mostrarSheet("Falta un proveedor", "Primero registra un proveedor.")
                return@setOnClickListener
            }
            val proveedor = proveedores[spProveedor.selectedItemPosition]
            btnConfirmar.isEnabled = false
            vm.registrarCompra(
                CompraRequest(
                    proveedor.id,
                    tilNota.texto(),
                    lineas.map { CompraItemRequest(it.producto.id, it.cantidad, it.valor) }
                ),
                {
                    dialog.dismiss()
                    mostrarSheet("Compra registrada", "Se sumaron las existencias al inventario.", false)
                    alTerminar()
                },
                fallo
            )
        } else {
            btnConfirmar.isEnabled = false
            vm.registrarVenta(
                PedidoRequest(
                    lineas.map { PedidoItemRequest(it.producto.id, it.cantidad) },
                    "Venta en mostrador"
                ),
                { r ->
                    dialog.dismiss()
                    mostrarSheet(
                        "Venta registrada",
                        "Venta #${r?.id ?: ""} por ${(r?.total ?: 0.0).cop()}. Existencias actualizadas.",
                        false
                    )
                    alTerminar()
                },
                fallo
            )
        }
    }

    dialog.mostrarExpandido()
}