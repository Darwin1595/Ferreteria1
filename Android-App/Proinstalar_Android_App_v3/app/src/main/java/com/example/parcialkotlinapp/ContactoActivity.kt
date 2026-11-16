package com.example.parcialkotlinapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ContactoActivity : AppCompatActivity() {

    // ⚠️ CAMBIA ESTOS DATOS por los reales de tu ferretería
    private val whatsapp = "573000000000"            // código de país + número, sin + ni espacios
    private val telefono = "+57 300 000 0000"
    private val direccion = "Calle 100 # 15-20, Bogotá"
    private val horario = "Lunes a Sábado: 8:00 a.m. – 6:00 p.m."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacto)

        findViewById<TextView>(R.id.tvHorarioContacto).text = horario
        findViewById<TextView>(R.id.tvTelefonoContacto).text = telefono
        findViewById<TextView>(R.id.tvDireccionContacto).text = direccion
        findViewById<ImageButton>(R.id.btnVolverContacto).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnWhatsapp).setOnClickListener {
            val msg = Uri.encode("Hola PROINSTALAR, necesito información.")
            abrir(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsapp?text=$msg")))
        }
        findViewById<MaterialButton>(R.id.btnLlamar).setOnClickListener {
            abrir(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono.replace(" ", ""))))
        }
        findViewById<MaterialButton>(R.id.btnMapa).setOnClickListener {
            abrir(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(direccion))))
        }
    }

    private fun abrir(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast("No hay una aplicación disponible para esta acción")
        }
    }
}