package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.models.AuthResponse
import com.example.parcialkotlinapp.models.LoginRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvIrARegistro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay una sesión válida guardada, entra directo al menú
        if (SessionManager.isLoggedIn) {
            irAlMenu()
            return
        }

        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmailLogin)
        etPassword = findViewById(R.id.etPasswordLogin)
        btnLogin = findViewById(R.id.btnLogin)
        tvIrARegistro = findViewById(R.id.tvIrARegistro)

        btnLogin.setOnClickListener { realizarLogin() }
        tvIrARegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun realizarLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            toast("Ingrese correo y contraseña")
            return
        }
        if (!email.esEmailValido()) {
            etEmail.error = "Correo no válido"
            return
        }

        btnLogin.isEnabled = false
        btnLogin.text = "VERIFICANDO..."

        // El backend valida el usuario registrado y la contraseña (BCrypt)
        ApiClient.api.login(LoginRequest(email, password)).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                restaurarBoton()
                val body = response.body()
                if (response.isSuccessful && body != null && !body.token.isNullOrEmpty()) {
                    SessionManager.guardar(body)
                    mostrarAlertaBienvenida(body.nombre ?: "Cliente")
                } else if (response.code() == 401) {
                    // Usuario no registrado o contraseña incorrecta: NO se permite el ingreso
                    etPassword.setText("")
                    mostrarSheet(
                        titulo = "Datos incorrectos",
                        mensaje = "El correo o la contraseña no coinciden. Si aún no tienes cuenta, puedes crearla en segundos.",
                        textoPrimario = "REINTENTAR",
                        textoSecundario = "Crear una cuenta"
                    ) { startActivity(Intent(this@LoginActivity, RegistroActivity::class.java)) }
                } else {
                    toast(ApiErrors.mensaje(response))
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                restaurarBoton()
                mostrarSheet("Sin conexión", ApiErrors.SIN_CONEXION)
            }
        })
    }

    private fun restaurarBoton() {
        btnLogin.isEnabled = true
        btnLogin.text = "INGRESAR A LA FERRETERÍA"
    }

    private fun mostrarAlertaBienvenida(nombre: String) {
        AlertDialog.Builder(this)
            .setTitle("¡Bienvenido a PROINSTALAR!")
            .setMessage("Hola $nombre, es un gusto tenerte de vuelta en nuestra ferretería.")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
                irAlMenu()
            }
            .show()
    }

    private fun irAlMenu() {
        val destino = if (SessionManager.isAdmin) AdminActivity::class.java else MenuActivity::class.java
        startActivity(Intent(this, destino))
        finish()
    }
}
