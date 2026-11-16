package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.models.SolicitudRequest
import com.example.parcialkotlinapp.models.SolicitudResponse
import com.example.parcialkotlinapp.models.TipoServicio
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** Mini formulario para solicitar un servicio técnico. */
class ServicioActivity : AppCompatActivity() {

    private lateinit var spServicio: Spinner
    private lateinit var tvInfo: TextView
    private lateinit var etCausa: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etDireccion: TextInputEditText
    private lateinit var btnEnviar: MaterialButton
    private var servicios: List<TipoServicio> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_servicio)

        spServicio = findViewById(R.id.spServicio)
        tvInfo = findViewById(R.id.tvInfoServicio)
        etCausa = findViewById(R.id.etCausaServicio)
        etTelefono = findViewById(R.id.etTelefonoServicio)
        etDireccion = findViewById(R.id.etDireccionServicio)
        btnEnviar = findViewById(R.id.btnEnviarSolicitud)

        findViewById<ImageButton>(R.id.btnVolverServicio).setOnClickListener { finish() }

        // Datos de contacto precargados con los del usuario registrado
        etTelefono.setText(SessionManager.telefono)
        etDireccion.setText(SessionManager.direccion)

        spServicio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val s = servicios.getOrNull(pos) ?: return
                tvInfo.text = "${s.descripcion ?: ""}\nValor base: ${s.precioBase.cop()}"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnEnviar.isEnabled = false
        btnEnviar.setOnClickListener { enviar() }
        cargarServicios()
    }

    private fun cargarServicios() {
        tvInfo.text = "Cargando servicios..."
        ApiClient.api.getServicios().enqueue(object : Callback<List<TipoServicio>> {
            override fun onResponse(call: Call<List<TipoServicio>>, response: Response<List<TipoServicio>>) {
                if (manejarNoAutorizado(response)) return
                val lista = response.body()
                if (response.isSuccessful && !lista.isNullOrEmpty()) {
                    servicios = lista
                    spServicio.adapter = ArrayAdapter(
                        this@ServicioActivity, android.R.layout.simple_spinner_dropdown_item, lista
                    )
                    btnEnviar.isEnabled = true
                } else {
                    tvInfo.text = "No se pudieron cargar los servicios. Intenta de nuevo."
                }
            }

            override fun onFailure(call: Call<List<TipoServicio>>, t: Throwable) {
                tvInfo.text = ApiErrors.SIN_CONEXION
            }
        })
    }

    private fun enviar() {
        val servicio = servicios.getOrNull(spServicio.selectedItemPosition) ?: return
        val causa = etCausa.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val direccion = etDireccion.text.toString().trim()

        if (causa.length < 10) {
            etCausa.error = "Cuéntanos con más detalle la causa (mín. 10 caracteres)"
            return
        }
        if (telefono.length < 7) { etTelefono.error = "Teléfono no válido"; return }
        if (direccion.length < 5) { etDireccion.error = "Ingresa la dirección del servicio"; return }

        btnEnviar.isEnabled = false
        btnEnviar.text = "ENVIANDO..."

        val req = SolicitudRequest(servicio.id, causa, telefono, direccion)
        ApiClient.api.crearSolicitud(req).enqueue(object : Callback<SolicitudResponse> {
            override fun onResponse(call: Call<SolicitudResponse>, response: Response<SolicitudResponse>) {
                restaurarBoton()
                if (manejarNoAutorizado(response)) return
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    val intent = Intent(this@ServicioActivity, CheckoutActivity::class.java)
                    intent.putExtra("TIPO", "SERVICIO")
                    intent.putExtra(
                        "MENSAJE",
                        "Hemos recibido tu solicitud de ${servicio.nombre} (N.º ${body.id}).\n\n" +
                            "En breve un técnico se contactará contigo al $telefono."
                    )
                    startActivity(intent)
                    finish()
                } else {
                    toast(ApiErrors.mensaje(response))
                }
            }

            override fun onFailure(call: Call<SolicitudResponse>, t: Throwable) {
                restaurarBoton()
                toast(ApiErrors.SIN_CONEXION)
            }
        })
    }

    private fun restaurarBoton() {
        btnEnviar.isEnabled = true
        btnEnviar.text = "ENVIAR SOLICITUD"
    }
}
