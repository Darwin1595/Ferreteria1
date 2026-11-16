package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.ceil

class CalculadoraActivity : AppCompatActivity() {

    // Rendimiento aproximado de una pintura de interior: ~35 m² por galón y por mano
    private val rendimientoM2PorGalon = 35.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculadora)

        val etAncho = findViewById<TextInputEditText>(R.id.etAncho)
        val etAlto = findViewById<TextInputEditText>(R.id.etAlto)
        val etDescuento = findViewById<TextInputEditText>(R.id.etDescuento)
        val chips = findViewById<ChipGroup>(R.id.chipGroupManos)
        val card = findViewById<View>(R.id.cardResultado)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)

        findViewById<ImageButton>(R.id.btnVolverCalc).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnCalcular).setOnClickListener {
            val ancho = etAncho.text.toString().replace(',', '.').toDoubleOrNull()
            val alto = etAlto.text.toString().replace(',', '.').toDoubleOrNull()
            val descuento = etDescuento.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
            if (ancho == null || ancho <= 0) { etAncho.error = "Ingresa el ancho"; return@setOnClickListener }
            if (alto == null || alto <= 0) { etAlto.error = "Ingresa el alto"; return@setOnClickListener }

            val manos = when (chips.checkedChipId) {
                R.id.chipMano1 -> 1
                R.id.chipMano3 -> 3
                else -> 2
            }
            val area = (ancho * alto - descuento).coerceAtLeast(0.0)
            val galones = area * manos / rendimientoM2PorGalon
            val aComprar = ceil(galones).toInt().coerceAtLeast(1)

            tvResultado.text = "Área a pintar: %.1f m²\n".format(area) +
                    "Pintura necesaria: %.1f galones\n".format(galones) +
                    "Te recomendamos comprar: $aComprar galón(es)" +
                    (if (aComprar >= 5) " (o ${(aComprar + 4) / 5} cuñete(s) de 5 galones)" else "") +
                    "\n\nCálculo aproximado: depende de la pintura y de la superficie."
            card.visibility = View.VISIBLE
        }

        findViewById<MaterialButton>(R.id.btnVerPinturas).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).putExtra("CATEGORIA", "Pinturas"))
        }
    }
}