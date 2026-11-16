package com.example.parcialkotlinapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.models.AuthResponse
import com.example.parcialkotlinapp.models.CrearUsuarioRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistroActivity : AppCompatActivity() {

    private lateinit var btnRegistrar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnRegistrar.setOnClickListener { registrar() }
        findViewById<TextView>(R.id.tvIrALogin).setOnClickListener { finish() }
    }

    private fun registrar() {
        val etNombre = findViewById<TextInputEditText>(R.id.etNombreRegistro)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmailRegistro)
        val etTelefono = findViewById<TextInputEditText>(R.id.etTelefonoRegistro)
        val etDireccion = findViewById<TextInputEditText>(R.id.etDireccionRegistro)
        val etPassword = findViewById<TextInputEditText>(R.id.etPasswordRegistro)

        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val direccion = etDireccion.text.toString().trim()
        val password = etPassword.text.toString()

        when {
            nombre.length < 3 -> { etNombre.error = "Ingresa tu nombre completo"; return }
            !email.esEmailValido() -> { etEmail.error = "Correo no válido"; return }
            telefono.length < 7 -> { etTelefono.error = "Teléfono no válido"; return }
            direccion.length < 5 -> { etDireccion.error = "Ingresa tu dirección"; return }
            password.length < 6 -> { etPassword.error = "Mínimo 6 caracteres"; return }
        }

        btnRegistrar.isEnabled = false
        val req = CrearUsuarioRequest(nombre, email, telefono, direccion, password)
        ApiClient.api.crearUsuario(req).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                btnRegistrar.isEnabled = true
                if (response.isSuccessful) {
                    mostrarAlertaRegistro()
                } else {
                    toast(ApiErrors.mensaje(response)) // p. ej. "El correo ya está registrado"
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                btnRegistrar.isEnabled = true
                toast(ApiErrors.SIN_CONEXION)
            }
        })
    }

    private fun mostrarAlertaRegistro() {
        AlertDialog.Builder(this)
            .setTitle("Registro Exitoso")
            .setMessage("Tu cuenta ha sido creada correctamente en PROINSTALAR. Ahora inicia sesión con tu correo y contraseña.")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }
}
