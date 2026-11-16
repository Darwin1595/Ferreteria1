package com.example.parcialkotlinapp

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.models.EstadoRequest
import com.example.parcialkotlinapp.models.GestionSolicitudRequest
import com.example.parcialkotlinapp.models.MensajeResp
import com.example.parcialkotlinapp.models.PedidoAdmin
import com.example.parcialkotlinapp.models.SolicitudAdmin
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** Lista para el administrador: solicitudes de servicio o pedidos de todos los clientes. */
class AdminListaActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var tvVacio: TextView
    private var esSolicitudes = true

    private val estadosSolicitud = listOf(
        "PENDIENTE" to "Pendiente",
        "EN_REVISION" to "En revisión",
        "TERMINADO" to "Terminado"
    )
    private val estadosPedido = listOf(
        "PAGADO" to "Pagado",
        "ENVIADO" to "Enviado",
        "ENTREGADO" to "Entregado"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SessionManager.isAdmin) { finish(); return }
        setContentView(R.layout.activity_historial)

        rv = findViewById(R.id.rvHistorial)
        tvVacio = findViewById(R.id.tvVacioHistorial)
        rv.layoutManager = LinearLayoutManager(this)
        findViewById<ImageButton>(R.id.btnVolverHistorial).setOnClickListener { finish() }

        esSolicitudes = intent.getStringExtra("TIPO") != "PEDIDOS"
        findViewById<TextView>(R.id.tvTituloHistorial).text =
            if (esSolicitudes) "Solicitudes de servicio" else "Pedidos de clientes"
        cargar()
    }

    private fun fecha(f: String?): String = (f ?: "").replace('T', ' ').take(16)

    private fun cargar() {
        tvVacio.visibility = View.VISIBLE
        tvVacio.text = "Cargando..."
        if (esSolicitudes) cargarSolicitudes() else cargarPedidos()
    }

    // ---------- Solicitudes ----------
    private fun cargarSolicitudes() {
        ApiClient.api.adminSolicitudes().enqueue(object : Callback<List<SolicitudAdmin>> {
            override fun onResponse(call: Call<List<SolicitudAdmin>>, response: Response<List<SolicitudAdmin>>) {
                if (manejarNoAutorizado(response)) return
                val lista = response.body()
                if (response.isSuccessful && lista != null) {
                    if (lista.isEmpty()) {
                        tvVacio.text = "Aún no hay solicitudes."
                        rv.adapter = null
                        return
                    }
                    tvVacio.visibility = View.GONE
                    rv.adapter = HistorialAdapter(lista.map { s ->
                        Fila(
                            "#${s.id} · ${s.tipoServicio?.nombre ?: "Servicio"}",
                            s.estado,
                            fecha(s.fechaCreacion),
                            "Cliente: ${s.clienteNombre}\n${s.clienteEmail} · ${s.clienteTelefono}\n" +
                                    "Dirección: ${s.clienteDireccion}\nCausa: ${s.notas ?: ""}",
                            "Valor ${s.total.cop()}"
                        ) { editarSolicitud(s) }
                    })
                } else {
                    tvVacio.text = ApiErrors.mensaje(response)
                }
            }

            override fun onFailure(call: Call<List<SolicitudAdmin>>, t: Throwable) {
                tvVacio.text = ApiErrors.SIN_CONEXION
            }
        })
    }

    private fun editarSolicitud(s: SolicitudAdmin) {
        abrirEditor(
            "Solicitud #${s.id}",
            "${s.tipoServicio?.nombre ?: "Servicio"}\n${s.clienteNombre} · ${s.clienteTelefono}\nCausa: ${s.notas ?: ""}",
            estadosSolicitud, s.estado, s.total
        ) { estado, valor, dialog, terminar ->
            ApiClient.api.adminGestionarSolicitud(s.id, GestionSolicitudRequest(estado, valor))
                .enqueue(respuesta<SolicitudAdmin>(dialog, terminar))
        }
    }

    // ---------- Pedidos ----------
    private fun cargarPedidos() {
        ApiClient.api.adminPedidos().enqueue(object : Callback<List<PedidoAdmin>> {
            override fun onResponse(call: Call<List<PedidoAdmin>>, response: Response<List<PedidoAdmin>>) {
                if (manejarNoAutorizado(response)) return
                val lista = response.body()
                if (response.isSuccessful && lista != null) {
                    if (lista.isEmpty()) {
                        tvVacio.text = "Aún no hay pedidos."
                        rv.adapter = null
                        return
                    }
                    tvVacio.visibility = View.GONE
                    rv.adapter = HistorialAdapter(lista.map { p ->
                        Fila(
                            "#${p.id} · ${p.clienteNombre}",
                            p.estado,
                            fecha(p.fechaCreacion),
                            "${p.clienteEmail}\nDirección: ${p.direccion}\n" +
                                    (p.items ?: emptyList()).joinToString("\n") { "${it.cantidad} × ${it.nombre}" },
                            "Total ${p.total.cop()}"
                        ) { editarPedido(p) }
                    })
                } else {
                    tvVacio.text = ApiErrors.mensaje(response)
                }
            }

            override fun onFailure(call: Call<List<PedidoAdmin>>, t: Throwable) {
                tvVacio.text = ApiErrors.SIN_CONEXION
            }
        })
    }

    private fun editarPedido(p: PedidoAdmin) {
        abrirEditor(
            "Pedido #${p.id}",
            "${p.clienteNombre} · ${p.clienteEmail}\nDirección: ${p.direccion}\nTotal ${p.total.cop()}",
            estadosPedido, p.estado, null
        ) { estado, _, dialog, terminar ->
            ApiClient.api.adminEstadoPedido(p.id, EstadoRequest(estado))
                .enqueue(respuesta<MensajeResp>(dialog, terminar))
        }
    }

    // ---------- Ventana de edición (la misma para ambos) ----------
    /** Si valorInicial es null, no se muestra el campo de valor (pedidos). */
    private fun abrirEditor(
        titulo: String,
        detalle: String,
        opciones: List<Pair<String, String>>,
        actual: String?,
        valorInicial: Double?,
        onGuardar: (String, Double?, BottomSheetDialog, () -> Unit) -> Unit
    ) {
        val dialog = BottomSheetDialog(this)
        val v = layoutInflater.inflate(R.layout.dialog_admin_editar, null)
        v.findViewById<TextView>(R.id.tvAdminTitulo).text = titulo
        v.findViewById<TextView>(R.id.tvAdminDetalle).text = detalle

        val grupo = v.findViewById<ChipGroup>(R.id.chipGroupEstado)
        val actualUp = (actual ?: "").uppercase()
        val hayActual = opciones.any { it.first == actualUp }
        opciones.forEachIndexed { i, (codigo, texto) ->
            val chip = layoutInflater.inflate(R.layout.chip_categoria, grupo, false) as Chip
            chip.id = View.generateViewId()
            chip.text = texto
            chip.tag = codigo
            chip.isChecked = if (hayActual) codigo == actualUp else i == 0
            grupo.addView(chip)
        }

        val til = v.findViewById<TextInputLayout>(R.id.tilValor)
        val etValor = v.findViewById<TextInputEditText>(R.id.etValorAdmin)
        if (valorInicial != null) {
            til.visibility = View.VISIBLE
            etValor.setText(
                if (valorInicial % 1.0 == 0.0) valorInicial.toLong().toString() else valorInicial.toString()
            )
        }

        val btn = v.findViewById<MaterialButton>(R.id.btnGuardarAdmin)
        btn.setOnClickListener {
            val chip = grupo.findViewById<Chip>(grupo.checkedChipId)
            val estado = chip?.tag as? String ?: return@setOnClickListener

            var valor: Double? = null
            if (valorInicial != null) {
                valor = etValor.text.toString().replace(',', '.').toDoubleOrNull()
                if (valor == null || valor < 0) {
                    etValor.error = "Ingresa un valor válido"
                    return@setOnClickListener
                }
            }
            btn.isEnabled = false
            btn.text = "GUARDANDO..."
            onGuardar(estado, valor, dialog) {
                btn.isEnabled = true
                btn.text = "GUARDAR CAMBIOS"
            }
        }

        dialog.setContentView(v)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        (v.parent as? View)?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        dialog.show()
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun <T> respuesta(dialog: BottomSheetDialog, terminar: () -> Unit): Callback<T> =
        object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                terminar()
                if (manejarNoAutorizado(response)) return
                if (response.isSuccessful) {
                    dialog.dismiss()
                    toast("Cambios guardados")
                    cargar()
                } else {
                    mostrarSheet("No se pudo guardar", ApiErrors.mensaje(response))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                terminar()
                mostrarSheet("Sin conexión", ApiErrors.SIN_CONEXION)
            }
        }
}