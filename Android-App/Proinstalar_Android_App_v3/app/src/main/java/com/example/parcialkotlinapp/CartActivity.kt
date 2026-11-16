package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.models.CartItem
import com.example.parcialkotlinapp.models.PedidoItemRequest
import com.example.parcialkotlinapp.models.PedidoRequest
import com.example.parcialkotlinapp.models.PedidoResponse
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var tvVacio: TextView
    private lateinit var tvTotal: TextView
    private lateinit var layoutPagar: View
    private lateinit var btnPagar: MaterialButton
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        rv = findViewById(R.id.rvCarrito)
        tvVacio = findViewById(R.id.tvCarritoVacio)
        tvTotal = findViewById(R.id.tvTotalCarrito)
        layoutPagar = findViewById(R.id.layoutPagar)
        btnPagar = findViewById(R.id.btnPagar)

        findViewById<ImageButton>(R.id.btnVolverCarrito).setOnClickListener { finish() }

        adapter = CartAdapter { refrescar() }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        btnPagar.setOnClickListener { confirmarCompra() }
        refrescar()
    }

    private fun refrescar() {
        adapter.actualizar(CartManager.lista())
        val vacio = CartManager.isEmpty()
        tvVacio.visibility = if (vacio) View.VISIBLE else View.GONE
        rv.visibility = if (vacio) View.GONE else View.VISIBLE
        layoutPagar.visibility = if (vacio) View.GONE else View.VISIBLE
        tvTotal.text = CartManager.total().cop()
    }

    private fun confirmarCompra() {
        val items = CartManager.lista().map { PedidoItemRequest(it.producto.id, it.cantidad) }
        if (items.isEmpty()) return

        btnPagar.isEnabled = false
        btnPagar.text = "PROCESANDO..."

        val req = PedidoRequest(items, SessionManager.direccion)
        ApiClient.api.crearPedido(req).enqueue(object : Callback<PedidoResponse> {
            override fun onResponse(call: Call<PedidoResponse>, response: Response<PedidoResponse>) {
                restaurarBoton()
                if (manejarNoAutorizado(response)) return
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    CartManager.clear()
                    val intent = Intent(this@CartActivity, CheckoutActivity::class.java)
                    intent.putExtra("TIPO", "COMPRA")
                    intent.putExtra("MENSAJE", "${body.mensaje ?: ""}\n\nPedido #${body.id} · Total ${body.total.cop()}")
                    startActivity(intent)
                    finish()
                } else {
                    toast(ApiErrors.mensaje(response)) // p. ej. stock insuficiente
                }
            }

            override fun onFailure(call: Call<PedidoResponse>, t: Throwable) {
                restaurarBoton()
                toast(ApiErrors.SIN_CONEXION)
            }
        })
    }

    private fun restaurarBoton() {
        btnPagar.isEnabled = true
        btnPagar.text = "CONFIRMAR COMPRA"
    }
}

class CartAdapter(private val onCambio: () -> Unit) : RecyclerView.Adapter<CartAdapter.VH>() {

    private var items: List<CartItem> = emptyList()

    fun actualizar(nuevos: List<CartItem>) {
        items = nuevos
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val nombre: TextView = v.findViewById(R.id.tvItemNombre)
        val precio: TextView = v.findViewById(R.id.tvItemPrecio)
        val cantidad: TextView = v.findViewById(R.id.tvItemCantidad)
        val menos: ImageButton = v.findViewById(R.id.btnMenosItem)
        val mas: ImageButton = v.findViewById(R.id.btnMasItem)
        val eliminar: ImageButton = v.findViewById(R.id.btnEliminarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_carrito, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]
        h.nombre.text = item.producto.nombre
        h.precio.text = "${item.producto.precio.cop()} c/u · Subtotal ${(item.producto.precio * item.cantidad).cop()}"
        h.cantidad.text = item.cantidad.toString()
        h.menos.setOnClickListener { CartManager.restar(item.producto.id); onCambio() }
        h.mas.setOnClickListener { v ->
            if (!CartManager.agregar(item.producto)) v.context.toast("Stock máximo alcanzado")
            onCambio()
        }
        h.eliminar.setOnClickListener { CartManager.quitar(item.producto.id); onCambio() }
    }
}
