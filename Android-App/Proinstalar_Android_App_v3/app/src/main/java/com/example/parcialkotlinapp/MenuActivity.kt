package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private class Opcion(val icono: Int, val titulo: String, val sub: String, val accion: () -> Unit)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (!SessionManager.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_menu)

        findViewById<TextView>(R.id.tvSaludo).text =
            "Hola, ${SessionManager.nombre.substringBefore(' ')}"

        findViewById<View>(R.id.btnSalir).setOnClickListener { confirmarSalida() }
        findViewById<View>(R.id.cardBuscar).setOnClickListener { abrirCatalogo(null) }
        findViewById<View>(R.id.btnBannerServicio).setOnClickListener { ir(ServicioActivity::class.java) }
        findViewById<View>(R.id.cardBuscar).setOnClickListener { abrirCatalogo(null, true) }
        findViewById<View>(R.id.chipTodos).setOnClickListener { abrirCatalogo(null) }
        findViewById<View>(R.id.chipHerramientas).setOnClickListener { abrirCatalogo("Herramientas") }
        findViewById<View>(R.id.chipPinturas).setOnClickListener { abrirCatalogo("Pinturas") }
        findViewById<View>(R.id.chipGas).setOnClickListener { abrirCatalogo("Gas y Plomería") }

        val opciones = listOf(
            Opcion(R.drawable.ic_list, "Catálogo", "Herramientas y materiales") { abrirCatalogo(null) },
            Opcion(R.drawable.ic_receipt, "Mis pedidos", "Historial de compras") { abrirHistorial("PEDIDOS") },
            Opcion(R.drawable.ic_build, "Servicio técnico", "Gas, pintura, eléctrico") { ir(ServicioActivity::class.java) },
            Opcion(R.drawable.ic_assignment, "Mis solicitudes", "Estado de tus servicios") { abrirHistorial("SOLICITUDES") },
            Opcion(R.drawable.ic_paint, "Calculadora", "¿Cuánta pintura necesitas?") { ir(CalculadoraActivity::class.java) },
            Opcion(R.drawable.ic_phone, "Contacto", "WhatsApp, llamada y mapa") { ir(ContactoActivity::class.java) },
            Opcion(R.drawable.ic_person, "Mi perfil", "Tus datos y sesión") { ir(PerfilActivity::class.java) },
            Opcion(R.drawable.ic_info, "Quiénes somos", "Misión y visión") { ir(MisionActivity::class.java) }
        )
        val grid = findViewById<GridLayout>(R.id.gridOpciones)
        opciones.forEach { agregarOpcion(grid, it) }
    }

    private fun agregarOpcion(grid: GridLayout, o: Opcion) {
        val v = layoutInflater.inflate(R.layout.view_card_opcion, grid, false)
        v.findViewById<ImageView>(R.id.imgOpcion).setImageResource(o.icono)
        v.findViewById<TextView>(R.id.tvOpcionTitulo).text = o.titulo
        v.findViewById<TextView>(R.id.tvOpcionSub).text = o.sub
        val lp = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        )
        lp.width = 0
        lp.setMargins(6, 6, 6, 6)
        v.layoutParams = lp
        v.setOnClickListener { o.accion() }
        grid.addView(v)
    }

    private fun ir(destino: Class<*>) = startActivity(Intent(this, destino))

    private fun abrirCatalogo(categoria: String?, buscar: Boolean = false) {
        val i = Intent(this, MainActivity::class.java)
        if (categoria != null) i.putExtra("CATEGORIA", categoria)
        if (buscar) i.putExtra("ENFOCAR_BUSQUEDA", true)
        startActivity(i)
    }

    private fun abrirHistorial(tipo: String) {
        startActivity(Intent(this, HistorialActivity::class.java).putExtra("TIPO", tipo))
    }

    private fun confirmarSalida() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres salir?")
            .setPositiveButton("Salir") { _, _ ->
                SessionManager.cerrarSesion()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}