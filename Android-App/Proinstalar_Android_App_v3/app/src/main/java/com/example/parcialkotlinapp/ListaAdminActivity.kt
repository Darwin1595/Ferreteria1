package com.example.parcialkotlinapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout

/**
 * Base de las pantallas de lista del administrador.
 * Maneja los estados cargando / vacío / error, el buscador y el botón flotante.
 */
abstract class ListaAdminActivity : AppCompatActivity() {

    protected lateinit var vm: AdminViewModel
    private lateinit var rv: RecyclerView
    private lateinit var tvEstado: TextView

    private var todas: List<Fila> = emptyList()
    private var busqueda = ""
    private var hayError = false

    protected abstract val titulo: String
    protected open val textoVacio: String = "No hay registros todavía."
    protected open val textoFab: String? = null
    protected open val conBuscador: Boolean = false

    /** Conecta los LiveData del ViewModel con la lista (usa mostrar). */
    protected abstract fun observar()

    /** Pide los datos de nuevo al servidor. */
    protected abstract fun recargar()

    protected open fun alPulsarFab() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SessionManager.isAdmin) {
            finish()
            return
        }
        setContentView(R.layout.activity_lista_admin)
        vm = ViewModelProvider(this)[AdminViewModel::class.java]

        findViewById<TextView>(R.id.tvTituloLista).text = titulo
        findViewById<ImageButton>(R.id.btnVolverLista).setOnClickListener { finish() }

        rv = findViewById(R.id.rvLista)
        rv.layoutManager = LinearLayoutManager(this)

        tvEstado = findViewById(R.id.tvEstadoLista)
        tvEstado.setOnClickListener { if (hayError) recargar() }

        val fab = findViewById<ExtendedFloatingActionButton>(R.id.fabLista)
        val textoBoton = textoFab
        if (textoBoton != null) {
            fab.text = textoBoton
            fab.visibility = View.VISIBLE
            fab.setOnClickListener { alPulsarFab() }
        }

        if (conBuscador) {
            val til = findViewById<TextInputLayout>(R.id.tilBuscarLista)
            til.visibility = View.VISIBLE
            til.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    busqueda = s?.toString() ?: ""
                    aplicarFiltro()
                }
            })
        }

        observar()
        recargar()
    }

    /** Muestra en la lista lo que llegue del ViewModel, convertido a filas. */
    protected fun <T> mostrar(origen: LiveData<UiState<T>>, aFilas: (T) -> List<Fila>) {
        origen.observe(this) { estado ->
            when (estado) {
                is UiState.Cargando -> {
                    if (todas.isEmpty()) {
                        hayError = false
                        tvEstado.visibility = View.VISIBLE
                        tvEstado.text = "Cargando..."
                    }
                }
                is UiState.Exito -> {
                    hayError = false
                    mostrarFilas(aFilas(estado.datos))
                }
                is UiState.Fallo -> {
                    if (estado.sesionVencida) {
                        irAlLoginPorSesionVencida()
                        return@observe
                    }
                    hayError = true
                    todas = emptyList()
                    rv.adapter = null
                    tvEstado.visibility = View.VISIBLE
                    tvEstado.text = estado.mensaje + "\nToca para reintentar"
                }
            }
        }
    }

    protected fun mostrarFilas(filas: List<Fila>) {
        todas = filas
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        val q = busqueda.trim().lowercase()
        val lista = if (q.isEmpty()) todas
        else todas.filter { (it.titulo + " " + it.detalle).lowercase().contains(q) }

        rv.adapter = HistorialAdapter(lista)
        if (lista.isEmpty()) {
            tvEstado.visibility = View.VISIBLE
            tvEstado.text = if (todas.isEmpty()) textoVacio else "Sin resultados para tu búsqueda."
        } else {
            tvEstado.visibility = View.GONE
        }
    }

    protected fun confirmar(titulo: String, mensaje: String, textoOk: String, accion: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton(textoOk) { _, _ -> accion() }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}