package com.example.parcialkotlinapp

import android.content.res.ColorStateList
import android.graphics.Color
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
import com.example.parcialkotlinapp.models.PedidoHistorial
import com.example.parcialkotlinapp.models.SolicitudHistorial
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistorialActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var tvVacio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        rv = findViewById(R.id.rvHistorial)
        tvVacio = findViewById(R.id.tvVacioHistorial)
        rv.layoutManager = LinearLayoutManager(this)
        findViewById<ImageButton>(R.id.btnVolverHistorial).setOnClickListener { finish() }

        if (intent.getStringExtra("TIPO") == "SOLICITUDES") {
            findViewById<TextView>(R.id.tvTituloHistorial).text = "Mis Solicitudes"
            cargarSolicitudes()
        } else {
            findViewById<TextView>(R.id.tvTituloHistorial).text = "Mis Pedidos"
            cargarPedidos()
        }
    }

    private fun cargarPedidos() {
        ApiClient.api.misPedidos().enqueue(object : Callback<List<PedidoHistorial>> {
            override fun onResponse(call: Call<List<PedidoHistorial>>, response: Response<List<PedidoHistorial>>) {
                if (manejarNoAutorizado(response)) return
                val lista = response.body()
                if (response.isSuccessful && lista != null) {
                    if (lista.isEmpty()) tvVacio.text = "Aún no has hecho compras."
                    else {
                        tvVacio.visibility = View.GONE
                        rv.adapter = HistorialAdapter(lista.map { p ->
                            Fila(
                                "Pedido #${p.id}", p.estado, p.fechaCreacion.fechaCorta(),
                                (p.items ?: emptyList()).joinToString("\n") { "${it.cantidad} × ${it.nombre}" },
                                "Total ${p.total.cop()}"
                            )
                        })
                    }
                } else tvVacio.text = ApiErrors.mensaje(response)
            }

            override fun onFailure(call: Call<List<PedidoHistorial>>, t: Throwable) {
                tvVacio.text = ApiErrors.SIN_CONEXION
            }
        })
    }

    private fun cargarSolicitudes() {
        ApiClient.api.misSolicitudes().enqueue(object : Callback<List<SolicitudHistorial>> {
            override fun onResponse(call: Call<List<SolicitudHistorial>>, response: Response<List<SolicitudHistorial>>) {
                if (manejarNoAutorizado(response)) return
                val lista = response.body()
                if (response.isSuccessful && lista != null) {
                    if (lista.isEmpty()) tvVacio.text = "Aún no has solicitado servicios."
                    else {
                        tvVacio.visibility = View.GONE
                        rv.adapter = HistorialAdapter(lista.map { s ->
                            Fila(
                                s.tipoServicio?.nombre ?: "Servicio #${s.id}", s.estado, s.fechaCreacion.fechaCorta(),
                                "Causa: ${s.notas ?: ""}",
                                "Valor base ${s.total.cop()}"
                            )
                        })
                    }
                } else tvVacio.text = ApiErrors.mensaje(response)
            }

            override fun onFailure(call: Call<List<SolicitudHistorial>>, t: Throwable) {
                tvVacio.text = ApiErrors.SIN_CONEXION
            }
        })
    }
}

private fun String?.fechaCorta(): String = (this ?: "").replace('T', ' ').take(16)

class Fila(
    val titulo: String,
    val estado: String?,
    val fecha: String,
    val detalle: String,
    val total: String,
    val onClick: (() -> Unit)? = null
)

class HistorialAdapter(private val filas: List<Fila>) : RecyclerView.Adapter<HistorialAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val titulo: TextView = v.findViewById(R.id.tvHTitulo)
        val estado: TextView = v.findViewById(R.id.tvHEstado)
        val fecha: TextView = v.findViewById(R.id.tvHFecha)
        val detalle: TextView = v.findViewById(R.id.tvHDetalle)
        val total: TextView = v.findViewById(R.id.tvHTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false))

    override fun getItemCount() = filas.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val f = filas[position]
        h.titulo.text = f.titulo
        h.fecha.text = f.fecha
        h.detalle.text = f.detalle
        h.total.text = f.total
        h.itemView.setOnClickListener { f.onClick?.invoke() }
        h.itemView.isClickable = f.onClick != null
        h.estado.text = (f.estado ?: "").replace('_', ' ')
        h.fecha.visibility = if (f.fecha.isBlank()) View.GONE else View.VISIBLE
        h.total.visibility = if (f.total.isBlank()) View.GONE else View.VISIBLE
        h.estado.visibility = if (f.estado.isNullOrBlank()) View.GONE else View.VISIBLE
        val color = when ((f.estado ?: "").uppercase()) {
            "PENDIENTE", "STOCK_BAJO" -> "#F59E0B"
            "EN_REVISION", "EN_PROCESO", "ENVIADO", "AJUSTE" -> "#1B4FA0"
            "TERMINADO", "COMPLETADO", "PAGADO", "ENTREGADO", "ENTRADA", "DISPONIBLE" -> "#16A34A"
            "CANCELADO", "AGOTADO", "SALIDA" -> "#DC2626"
            else -> "#64748B"
        }
        h.estado.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
    }
}