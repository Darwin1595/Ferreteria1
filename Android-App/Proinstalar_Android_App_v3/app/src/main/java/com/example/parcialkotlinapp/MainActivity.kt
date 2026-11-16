package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.models.Producto
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import android.view.WindowManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var tvEstado: TextView
    private lateinit var tvBadge: TextView
    private lateinit var tvContador: TextView
    private lateinit var etBuscar: TextInputEditText
    private lateinit var chipGroup: ChipGroup
    private lateinit var adapter: ProductoAdapter

    private var todos: List<Producto> = emptyList()
    private var categoriaSel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        categoriaSel = intent.getStringExtra("CATEGORIA")

        rv = findViewById(R.id.rvProductos)
        tvEstado = findViewById(R.id.tvEmptyState)
        tvBadge = findViewById(R.id.tvBadgeCarrito)
        tvContador = findViewById(R.id.tvContador)
        etBuscar = findViewById(R.id.etBuscarProducto)
        chipGroup = findViewById(R.id.chipGroupCategorias)

        adapter = ProductoAdapter(emptyList(), { p ->
            startActivity(Intent(this, DetalleProductoActivity::class.java).putExtra("PRODUCTO_ID", p.id))
        }) { p ->
            if (CartManager.agregar(p)) mostrarAgregado(p)
            else toast("No hay más unidades disponibles de ${p.nombre}")
            actualizarBadge()
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<ImageButton>(R.id.btnVolverCatalogo).setOnClickListener { finish() }
        findViewById<View>(R.id.btnIrCarrito).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) { filtrar() }
        })
        tvEstado.setOnClickListener { if (todos.isEmpty()) cargarProductos() }
        cargarProductos()

        if (intent.getBooleanExtra("ENFOCAR_BUSQUEDA", false)) {
            etBuscar.requestFocus()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarBadge()
    }

    private fun actualizarBadge() {
        val n = CartManager.totalItems()
        tvBadge.visibility = if (n > 0) View.VISIBLE else View.GONE
        tvBadge.text = n.toString()
    }

    private fun armarChips() {
        chipGroup.removeAllViews()
        val categorias = todos.mapNotNull { it.categoriaNombre }.distinct()
        agregarChip("Todos", null)
        categorias.forEach { agregarChip(it, it) }
    }

    private fun agregarChip(texto: String, categoria: String?) {
        val chip = layoutInflater.inflate(R.layout.chip_categoria, chipGroup, false) as Chip
        chip.id = View.generateViewId()
        chip.text = texto
        chip.isChecked = (categoria == categoriaSel)
        chip.setOnClickListener {
            categoriaSel = categoria
            filtrar()
        }
        chipGroup.addView(chip)
    }

    private fun filtrar() {
        if (todos.isEmpty()) return
        val q = etBuscar.text.toString().trim().lowercase()
        val lista = todos.filter { p ->
            (categoriaSel == null || p.categoriaNombre == categoriaSel) &&
                    (q.isEmpty() || p.nombre.lowercase().contains(q) ||
                            (p.descripcion ?: "").lowercase().contains(q))
        }
        adapter.actualizar(lista)
        tvContador.text = "${lista.size} producto(s)"
        if (lista.isEmpty()) {
            tvEstado.visibility = View.VISIBLE
            tvEstado.text = "No encontramos productos con ese filtro."
        } else {
            tvEstado.visibility = View.GONE
        }
    }

    private fun mostrarAgregado(p: Producto) {
        val dialog = BottomSheetDialog(this)
        val v = layoutInflater.inflate(R.layout.dialog_agregado, null)
        v.findViewById<ImageView>(R.id.imgAgregado).cargarImagenProducto(p.imagenUrl)
        v.findViewById<TextView>(R.id.tvAgregadoNombre).text = p.nombre
        v.findViewById<TextView>(R.id.tvAgregadoPrecio).text = p.precio.cop()
        v.findViewById<TextView>(R.id.tvAgregadoResumen).text =
            "En tu carrito: ${CartManager.totalItems()} producto(s) · Total ${CartManager.total().cop()}"
        v.findViewById<MaterialButton>(R.id.btnSeguir).setOnClickListener { dialog.dismiss() }
        v.findViewById<MaterialButton>(R.id.btnVerCarrito).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, CartActivity::class.java))
        }
        dialog.setContentView(v)
        (v.parent as? View)?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        dialog.show()
    }

    private fun cargarProductos() {
        tvEstado.visibility = View.VISIBLE
        tvEstado.text = "Cargando catálogo..."
        ApiClient.api.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (manejarNoAutorizado(response)) return
                val lista = response.body()
                if (response.isSuccessful && lista != null) {
                    todos = lista
                    if (lista.isEmpty()) {
                        tvEstado.text = "Aún no hay productos disponibles."
                    } else {
                        armarChips()
                        filtrar()
                    }
                } else {
                    tvEstado.text = ApiErrors.mensaje(response) + "\nToca para reintentar"
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                tvEstado.text = ApiErrors.SIN_CONEXION + "\nToca para reintentar"
            }
        })
    }
}

class ProductoAdapter(
    private var productos: List<Producto>,
    private val onDetalle: (Producto) -> Unit,
    private val onAgregar: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.VH>() {

    fun actualizar(nuevos: List<Producto>) {
        productos = nuevos
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgProducto)
        val nombre: TextView = v.findViewById(R.id.tvNombreProducto)
        val desc: TextView = v.findViewById(R.id.tvDescProducto)
        val precio: TextView = v.findViewById(R.id.tvPrecioProducto)
        val stock: TextView = v.findViewById(R.id.tvStockProducto)
        val btn: MaterialButton = v.findViewById(R.id.btnAgregarCarrito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false))

    override fun getItemCount() = productos.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val p = productos[position]
        h.img.cargarImagenProducto(p.imagenUrl)
        h.nombre.text = p.nombre
        h.desc.text = p.descripcion ?: ""
        h.precio.text = p.precio.cop()
        h.stock.text = when {
            p.stock <= 0 -> "Agotado"
            p.stock <= 5 -> "¡Últimas ${p.stock} unidades!"
            else -> "Disponibles: ${p.stock}"
        }
        h.btn.isEnabled = p.stock > 0
        h.btn.setOnClickListener { onAgregar(p) }
        h.itemView.setOnClickListener { onDetalle(p) }
    }
}