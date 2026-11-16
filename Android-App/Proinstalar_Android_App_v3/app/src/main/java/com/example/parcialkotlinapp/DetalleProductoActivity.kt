package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.parcialkotlinapp.models.Producto
import com.google.android.material.button.MaterialButton

/** Detalle de un producto: foto, descripción, existencias y cantidad a comprar. */
class DetalleProductoActivity : AppCompatActivity() {

    private lateinit var tvEstado: TextView
    private lateinit var contenido: View
    private lateinit var tvCantidad: TextView
    private var producto: Producto? = null
    private var cantidad = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_producto)

        tvEstado = findViewById(R.id.tvEstadoDetalle)
        contenido = findViewById(R.id.scrollDetalle)
        tvCantidad = findViewById(R.id.tvDetalleCantidad)
        findViewById<ImageButton>(R.id.btnVolverDetalle).setOnClickListener { finish() }

        val id = intent.getIntExtra("PRODUCTO_ID", 0)
        val vm = ViewModelProvider(this)[CatalogoViewModel::class.java]
        vm.producto.observe(this) { estado ->
            when (estado) {
                is UiState.Cargando -> {
                    contenido.visibility = View.GONE
                    tvEstado.visibility = View.VISIBLE
                    tvEstado.text = "Cargando producto..."
                }
                is UiState.Exito -> pintar(estado.datos)
                is UiState.Fallo -> {
                    if (estado.sesionVencida) {
                        irAlLoginPorSesionVencida()
                        return@observe
                    }
                    contenido.visibility = View.GONE
                    tvEstado.visibility = View.VISIBLE
                    tvEstado.text = estado.mensaje + "\nToca para reintentar"
                }
            }
        }
        tvEstado.setOnClickListener { vm.cargarProducto(id) }

        findViewById<View>(R.id.btnDetalleMenos).setOnClickListener {
            if (cantidad > 1) {
                cantidad--
                tvCantidad.text = cantidad.toString()
            }
        }
        findViewById<View>(R.id.btnDetalleMas).setOnClickListener {
            if (cantidad < disponibles()) {
                cantidad++
                tvCantidad.text = cantidad.toString()
            } else {
                toast("No hay más unidades disponibles")
            }
        }
        findViewById<MaterialButton>(R.id.btnDetalleAgregar).setOnClickListener { agregarAlCarrito() }

        if (id == 0) {
            tvEstado.text = "Producto no encontrado."
        } else {
            vm.cargarProducto(id)
        }
    }

    /** Unidades que aún se pueden agregar (stock menos lo que ya está en el carrito). */
    private fun disponibles(): Int {
        val p = producto ?: return 0
        val enCarrito = CartManager.lista().firstOrNull { it.producto.id == p.id }?.cantidad ?: 0
        return p.stock - enCarrito
    }

    private fun pintar(p: Producto) {
        producto = p
        cantidad = 1
        tvCantidad.text = "1"
        tvEstado.visibility = View.GONE
        contenido.visibility = View.VISIBLE

        findViewById<ImageView>(R.id.imgDetalle).cargarImagenProducto(p.imagenUrl)
        findViewById<TextView>(R.id.tvDetalleCategoria).text = (p.categoriaNombre ?: "Sin categoría").uppercase()
        findViewById<TextView>(R.id.tvDetalleNombre).text = p.nombre
        findViewById<TextView>(R.id.tvDetallePrecio).text = p.precio.cop()
        findViewById<TextView>(R.id.tvDetalleDescripcion).text = p.descripcion.guion()
        findViewById<TextView>(R.id.tvDetalleStock).text = when {
            p.stock <= 0 -> "Agotado"
            p.stock <= 5 -> "¡Últimas ${p.stock} unidades!"
            else -> "Disponibles: ${p.stock}"
        }
        findViewById<MaterialButton>(R.id.btnDetalleAgregar).isEnabled = p.stock > 0
    }

    private fun agregarAlCarrito() {
        val p = producto ?: return
        var agregadas = 0
        repeat(cantidad) { if (CartManager.agregar(p)) agregadas++ }
        if (agregadas == 0) {
            toast("No hay más unidades disponibles de ${p.nombre}")
            return
        }
        cantidad = 1
        tvCantidad.text = "1"
        mostrarSheet(
            "Agregado al carrito",
            "$agregadas × ${p.nombre}\nEn tu carrito: ${CartManager.totalItems()} producto(s) · ${CartManager.total().cop()}",
            false,
            "SEGUIR VIENDO",
            "VER CARRITO"
        ) { startActivity(Intent(this, CartActivity::class.java)) }
    }
}