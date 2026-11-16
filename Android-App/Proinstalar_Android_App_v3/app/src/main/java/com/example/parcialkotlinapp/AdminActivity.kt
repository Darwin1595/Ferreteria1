package com.example.parcialkotlinapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

/** Panel del administrador (cubre los roles de administrador, vendedor y bodeguero). */
class AdminActivity : AppCompatActivity() {

    private lateinit var vm: AdminViewModel
    private var subAlertas: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solo entra el administrador
        if (!SessionManager.isAdmin) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_admin)
        vm = ViewModelProvider(this)[AdminViewModel::class.java]

        findViewById<TextView>(R.id.tvAdminSub).text = "Hola, ${SessionManager.nombre}"
        findViewById<View>(R.id.btnAdminSalir).setOnClickListener { confirmarSalida() }

        val grid = findViewById<GridLayout>(R.id.gridAdmin)
        agregar(grid, R.drawable.ic_build, "Solicitudes", "Servicios: estado y valor") {
            startActivity(Intent(this, AdminListaActivity::class.java).putExtra("TIPO", "SOLICITUDES"))
        }
        agregar(grid, R.drawable.ic_receipt, "Ventas", "Pedidos y venta mostrador") { ir(VentasActivity::class.java) }
        agregar(grid, R.drawable.ic_list, "Inventario", "Productos y existencias") { ir(InventarioActivity::class.java) }
        subAlertas = agregar(grid, R.drawable.ic_error, "Alertas de stock", "Revisando...") { ir(AlertasStockActivity::class.java) }
        agregar(grid, R.drawable.ic_person, "Proveedores", "Registro y contacto") { ir(ProveedoresActivity::class.java) }
        agregar(grid, R.drawable.ic_cart, "Compras", "Reponer existencias") { ir(ComprasActivity::class.java) }
        agregar(grid, R.drawable.ic_assignment, "Movimientos", "Entradas y salidas") { ir(MovimientosActivity::class.java) }
        agregar(grid, R.drawable.ic_info, "Reportes", "Stock, ventas y compras") { ir(ReportesActivity::class.java) }

        vm.alertas.observe(this) { estado ->
            if (estado is UiState.Exito) {
                val n = estado.datos.size
                subAlertas?.text = if (n == 0) "Todo en orden" else "⚠ $n producto(s) bajo el mínimo"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (SessionManager.isAdmin) vm.cargarAlertas()
    }

    private fun ir(destino: Class<*>) = startActivity(Intent(this, destino))

    private fun agregar(grid: GridLayout, icono: Int, titulo: String, sub: String, accion: () -> Unit): TextView {
        val v = layoutInflater.inflate(R.layout.view_card_opcion, grid, false)
        v.findViewById<ImageView>(R.id.imgOpcion).setImageResource(icono)
        v.findViewById<TextView>(R.id.tvOpcionTitulo).text = titulo
        val tvSub = v.findViewById<TextView>(R.id.tvOpcionSub)
        tvSub.text = sub
        val lp = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        )
        lp.width = 0
        lp.setMargins(6, 6, 6, 6)
        v.layoutParams = lp
        v.setOnClickListener { accion() }
        grid.addView(v)
        return tvSub
    }

    private fun confirmarSalida() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres salir?")
            .setPositiveButton("Salir") { _, _ ->
                SessionManager.cerrarSesion()
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}