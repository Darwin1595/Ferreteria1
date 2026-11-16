package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.models.PerfilRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        pintarDatos()

        findViewById<ImageButton>(R.id.btnVolverPerfil).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnPerfilEditar).setOnClickListener { editarPerfil() }
        findViewById<MaterialButton>(R.id.btnPerfilPedidos).setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java).putExtra("TIPO", "PEDIDOS"))
        }
        findViewById<MaterialButton>(R.id.btnPerfilSolicitudes).setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java).putExtra("TIPO", "SOLICITUDES"))
        }
        findViewById<MaterialButton>(R.id.btnPerfilSalir).setOnClickListener {
            SessionManager.cerrarSesion()
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }
    }

    private fun pintarDatos() {
        val nombre = SessionManager.nombre
        findViewById<TextView>(R.id.tvInicialPerfil).text = nombre.take(1).uppercase()
        findViewById<TextView>(R.id.tvNombrePerfil).text = nombre
        findViewById<TextView>(R.id.tvEmailPerfil).text = SessionManager.email
        findViewById<TextView>(R.id.tvTelefonoPerfil).text = SessionManager.telefono.ifEmpty { "No registrado" }
        findViewById<TextView>(R.id.tvDireccionPerfil).text = SessionManager.direccion.ifEmpty { "No registrada" }
    }

    /** Gestión de perfil: el usuario corrige su nombre, teléfono y dirección. */
    private fun editarPerfil() {
        val (dialog, v) = crearSheet(R.layout.dialog_perfil)
        val tilNombre = v.findViewById<TextInputLayout>(R.id.tilPerfilNombre)
        val tilTel = v.findViewById<TextInputLayout>(R.id.tilPerfilTelefono)
        val tilDir = v.findViewById<TextInputLayout>(R.id.tilPerfilDireccion)
        val btn = v.findViewById<MaterialButton>(R.id.btnPerfilGuardar)

        tilNombre.editText?.setText(SessionManager.nombre)
        tilTel.editText?.setText(SessionManager.telefono)
        tilDir.editText?.setText(SessionManager.direccion)

        btn.setOnClickListener {
            listOf(tilNombre, tilTel, tilDir).forEach { til -> til.error = null }
            val nombre = tilNombre.texto()
            val tel = tilTel.texto()
            val dir = tilDir.texto()

            if (nombre.length < 3) {
                tilNombre.error = "Mínimo 3 caracteres"
                return@setOnClickListener
            }
            if (tel.length < 7 || tel.length > 15 || !tel.all { c -> c.isDigit() }) {
                tilTel.error = "Solo números (entre 7 y 15 dígitos)"
                return@setOnClickListener
            }
            if (dir.length < 5) {
                tilDir.error = "Ingresa una dirección válida"
                return@setOnClickListener
            }

            btn.isEnabled = false
            ApiClient.api.actualizarPerfil(PerfilRequest(nombre, tel, dir)).ejecutar(
                { r ->
                    if (r != null) SessionManager.guardar(r)
                    dialog.dismiss()
                    pintarDatos()
                    toast("Perfil actualizado")
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