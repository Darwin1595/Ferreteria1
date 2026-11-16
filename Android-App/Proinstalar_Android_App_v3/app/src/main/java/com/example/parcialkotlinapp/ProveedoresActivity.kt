package com.example.parcialkotlinapp

import android.view.View
import android.widget.TextView
import com.example.parcialkotlinapp.models.Proveedor
import com.example.parcialkotlinapp.models.ProveedorRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

/** RF-02-03: administrar proveedores (crear, editar, eliminar, buscar). */
class ProveedoresActivity : ListaAdminActivity() {

    override val titulo = "Proveedores"
    override val textoVacio = "Aún no hay proveedores. Toca «Nuevo» para registrar uno."
    override val textoFab: String? = "Nuevo"
    override val conBuscador = true

    override fun observar() = mostrar(vm.proveedores) { lista ->
        lista.map { p ->
            Fila(
                p.nombre, null, "NIT ${p.nit.guion()}",
                "Tel: ${p.telefono.guion()}\nCorreo: ${p.email.guion()}\nDirección: ${p.direccion.guion()}",
                ""
            ) { abrirFormulario(p) }
        }
    }

    override fun recargar() = vm.cargarProveedores()

    override fun alPulsarFab() = abrirFormulario(null)

    private fun abrirFormulario(p: Proveedor?) {
        val (dialog, v) = crearSheet(R.layout.dialog_proveedor)
        v.findViewById<TextView>(R.id.tvProveedorTitulo).text =
            if (p == null) "Nuevo proveedor" else "Editar proveedor"

        val tilNombre = v.findViewById<TextInputLayout>(R.id.tilProvNombre)
        val tilNit = v.findViewById<TextInputLayout>(R.id.tilProvNit)
        val tilTel = v.findViewById<TextInputLayout>(R.id.tilProvTelefono)
        val tilEmail = v.findViewById<TextInputLayout>(R.id.tilProvEmail)
        val tilDir = v.findViewById<TextInputLayout>(R.id.tilProvDireccion)
        val btnGuardar = v.findViewById<MaterialButton>(R.id.btnProveedorGuardar)
        val btnEliminar = v.findViewById<MaterialButton>(R.id.btnProveedorEliminar)

        if (p != null) {
            tilNombre.editText?.setText(p.nombre)
            tilNit.editText?.setText(p.nit ?: "")
            tilTel.editText?.setText(p.telefono ?: "")
            tilEmail.editText?.setText(p.email ?: "")
            tilDir.editText?.setText(p.direccion ?: "")
            btnEliminar.visibility = View.VISIBLE
            btnEliminar.setOnClickListener {
                confirmar("Eliminar proveedor", "¿Eliminar a ${p.nombre}? Sus compras anteriores se conservan.", "ELIMINAR") {
                    vm.eliminarProveedor(
                        p.id,
                        {
                            dialog.dismiss()
                            toast("Proveedor eliminado")
                            recargar()
                        },
                        { mensaje -> mostrarSheet("No se pudo eliminar", mensaje) }
                    )
                }
            }
        }

        btnGuardar.setOnClickListener {
            listOf(tilNombre, tilNit, tilTel, tilEmail).forEach { til -> til.error = null }
            val nombre = tilNombre.texto()
            val nit = tilNit.texto()
            val tel = tilTel.texto()
            val email = tilEmail.texto()
            val dir = tilDir.texto()

            if (nombre.length < 3) {
                tilNombre.error = "Mínimo 3 caracteres"
                return@setOnClickListener
            }
            if (nit.length < 5) {
                tilNit.error = "Ingresa el NIT"
                return@setOnClickListener
            }
            if (tel.length < 7 || !tel.all { c -> c.isDigit() }) {
                tilTel.error = "Solo números (mínimo 7 dígitos)"
                return@setOnClickListener
            }
            if (email.isNotEmpty() && !email.esEmailValido()) {
                tilEmail.error = "Correo no válido"
                return@setOnClickListener
            }

            btnGuardar.isEnabled = false
            vm.guardarProveedor(
                p?.id,
                ProveedorRequest(nombre, nit, tel, email, dir),
                {
                    dialog.dismiss()
                    toast("Proveedor guardado")
                    recargar()
                },
                { mensaje ->
                    btnGuardar.isEnabled = true
                    mostrarSheet("No se pudo guardar", mensaje)
                }
            )
        }
        dialog.mostrarExpandido()
    }
}