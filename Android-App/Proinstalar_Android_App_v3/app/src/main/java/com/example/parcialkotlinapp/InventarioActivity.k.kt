package com.example.parcialkotlinapp

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.parcialkotlinapp.models.Categoria
import com.example.parcialkotlinapp.models.InventarioItem
import com.example.parcialkotlinapp.models.ProductoRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

/**
 * RF-02-01 y RF-02-02: registrar herramientas y materiales, categorizar.
 * También es el inventario: existencias, mínimo y ajustes.
 */
class InventarioActivity : ListaAdminActivity() {

    override val titulo = "Inventario"
    override val textoVacio = "No hay productos. Toca «Nuevo producto» para registrar uno."
    override val textoFab: String? = "Nuevo producto"
    override val conBuscador = true

    override fun observar() = mostrar(vm.inventario) { lista ->
        lista.map { p ->
            Fila(
                p.nombre, p.estadoStock, p.categoria.guion(),
                "Existencias: ${p.stock} (mínimo ${p.stockMinimo})",
                p.precio.cop()
            ) { opciones(p) }
        }
    }

    override fun recargar() {
        vm.cargarInventario()
        vm.cargarCategorias()
    }

    override fun alPulsarFab() = abrirFormulario(null)

    private fun opciones(p: InventarioItem) {
        AlertDialog.Builder(this)
            .setTitle(p.nombre)
            .setItems(arrayOf("Editar producto", "Ajustar existencias", "Deshabilitar producto")) { _, i ->
                when (i) {
                    0 -> abrirFormulario(p)
                    1 -> abrirAjuste(vm, p) { recargar() }
                    else -> confirmar(
                        "Deshabilitar producto",
                        "«${p.nombre}» dejará de aparecer en el catálogo. Su historial se conserva.",
                        "DESHABILITAR"
                    ) {
                        vm.deshabilitarProducto(
                            p.id,
                            {
                                toast("Producto deshabilitado")
                                recargar()
                            },
                            { mensaje -> mostrarSheet("No se pudo deshabilitar", mensaje) }
                        )
                    }
                }
            }
            .show()
    }

    private fun abrirFormulario(p: InventarioItem?) {
        val categorias = vm.categorias.datos()
        if (categorias.isNullOrEmpty()) {
            toast("Cargando categorías… intenta de nuevo en un momento")
            vm.cargarCategorias()
            return
        }

        val (dialog, v) = crearSheet(R.layout.dialog_producto)
        v.findViewById<TextView>(R.id.tvProductoTitulo).text =
            if (p == null) "Nuevo producto" else "Editar producto"

        val tilNombre = v.findViewById<TextInputLayout>(R.id.tilProdNombre)
        val tilDesc = v.findViewById<TextInputLayout>(R.id.tilProdDescripcion)
        val tilPrecio = v.findViewById<TextInputLayout>(R.id.tilProdPrecio)
        val tilStock = v.findViewById<TextInputLayout>(R.id.tilProdStock)
        val tilMinimo = v.findViewById<TextInputLayout>(R.id.tilProdMinimo)
        val spCategoria = v.findViewById<Spinner>(R.id.spProductoCategoria)
        val btn = v.findViewById<MaterialButton>(R.id.btnProductoGuardar)

        spCategoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)

        if (p != null) {
            tilNombre.editText?.setText(p.nombre)
            tilDesc.editText?.setText(p.descripcion ?: "")
            tilPrecio.editText?.setText(
                if (p.precio % 1.0 == 0.0) p.precio.toLong().toString() else p.precio.toString()
            )
            tilMinimo.editText?.setText(p.stockMinimo.toString())
            // Las existencias se cambian con "Ajustar existencias" para dejar trazabilidad.
            tilStock.visibility = View.GONE
            spCategoria.setSelection(categorias.indexOfFirst { it.id == p.categoriaId }.coerceAtLeast(0))
        } else {
            tilMinimo.editText?.setText("10")
        }

        btn.setOnClickListener {
            listOf(tilNombre, tilPrecio, tilStock, tilMinimo).forEach { til -> til.error = null }

            val nombre = tilNombre.texto()
            val precio = tilPrecio.texto().replace(',', '.').toDoubleOrNull()
            val stockNuevo = tilStock.texto().toIntOrNull()
            val minimo = tilMinimo.texto().toIntOrNull()

            if (nombre.length < 3) {
                tilNombre.error = "Mínimo 3 caracteres"
                return@setOnClickListener
            }
            if (precio == null || precio <= 0) {
                tilPrecio.error = "Ingresa un precio mayor a 0"
                return@setOnClickListener
            }
            if (p == null && (stockNuevo == null || stockNuevo < 0)) {
                tilStock.error = "Ingresa las existencias (0 o más)"
                return@setOnClickListener
            }
            if (minimo == null || minimo < 0) {
                tilMinimo.error = "Ingresa el stock mínimo (0 o más)"
                return@setOnClickListener
            }

            val categoria = spCategoria.selectedItem as Categoria
            val pedido = ProductoRequest(
                nombre,
                tilDesc.texto(),
                precio,
                p?.stock ?: (stockNuevo ?: 0),
                minimo,
                categoria.id,
                p?.imagenUrl ?: categoria.imagenUrl ?: ""
            )

            btn.isEnabled = false
            vm.guardarProducto(
                p?.id,
                pedido,
                {
                    dialog.dismiss()
                    toast("Producto guardado")
                    recargar()
                },
                { mensaje ->
                    btn.isEnabled = true
                    mostrarSheet("No se pudo guardar", mensaje)
                }
            )
        }
        dialog.mostrarExpandido()
    }
}