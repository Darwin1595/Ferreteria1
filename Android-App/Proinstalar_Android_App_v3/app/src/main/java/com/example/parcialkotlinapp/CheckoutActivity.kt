package com.example.parcialkotlinapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/** Pantalla de confirmación para compras y solicitudes de servicio. */
class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val tipo = intent.getStringExtra("TIPO")
        val mensaje = intent.getStringExtra("MENSAJE")
        val tvTitulo = findViewById<TextView>(R.id.tvCheckoutTitulo)
        val tvDesc = findViewById<TextView>(R.id.tvCheckoutDesc)

        if (tipo == "SERVICIO") {
            tvTitulo.text = "¡Solicitud Enviada!"
            tvDesc.text = mensaje ?: "Hemos recibido tu solicitud. En breve un técnico se contactará contigo."
        } else {
            tvTitulo.text = "¡Compra Exitosa!"
            tvDesc.text = mensaje ?: "Tu compra ha sido aprobada. ¡Gracias por confiar en PROINSTALAR!"
        }

        findViewById<MaterialButton>(R.id.btnVolverMenu).setOnClickListener { finish() }
    }
}
