package com.example.parcialkotlinapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Patterns
import android.widget.ImageView
import android.widget.Toast
import com.google.gson.JsonParser
import retrofit2.Response
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

/** Formato de pesos colombianos, p. ej. $ 245.000 */
fun Double.cop(): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
    nf.maximumFractionDigits = 0
    return "$ " + nf.format(this)
}

fun String.esEmailValido(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()

/** Carga la imagen del producto desde res/drawable (prod_<nombre>.png). */
fun ImageView.cargarImagenProducto(imagenUrl: String?) {
    val base = (imagenUrl ?: "").substringBeforeLast('.').lowercase(Locale.ROOT)
    val id = if (base.isNotEmpty()) resources.getIdentifier("prod_$base", "drawable", context.packageName) else 0
    setImageResource(if (id != 0) id else R.drawable.logo_proinstalar_img)
}

object ApiErrors {
    /** Extrae un mensaje legible del cuerpo de error del backend. */
    fun mensaje(resp: Response<*>): String {
        val cuerpo = try { resp.errorBody()?.string() } catch (e: Exception) { null }
        if (!cuerpo.isNullOrBlank()) {
            try {
                val obj = JsonParser().parse(cuerpo).asJsonObject
                if (obj.has("mensaje")) return obj.get("mensaje").asString
                if (obj.has("errors")) return "Revisa los datos ingresados"
            } catch (e: Exception) { /* cuerpo no es JSON */ }
        }
        return when (resp.code()) {
            401 -> "Credenciales incorrectas o sesión vencida"
            403 -> "No tienes permiso para esta acción"
            404 -> "Recurso no encontrado"
            else -> "Error del servidor (${resp.code()})"
        }
    }

    const val SIN_CONEXION =
        "No se pudo conectar con el servidor. Verifica que la API esté corriendo (puerto 5000)."
}

/**
 * Si el servidor responde 401 (token vencido o usuario inexistente), cierra la sesión
 * y manda al login. Devuelve true si manejó el caso.
 */
fun Activity.manejarNoAutorizado(resp: Response<*>): Boolean {
    if (resp.code() != 401) return false
    SessionManager.cerrarSesion()
    Toast.makeText(this, "Tu sesión expiró. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
    startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    finish()
    return true
}

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

/** Ventana bonita que sube desde abajo, para errores y avisos. */
fun Activity.mostrarSheet(
    titulo: String,
    mensaje: String,
    error: Boolean = true,
    textoPrimario: String = "ENTENDIDO",
    textoSecundario: String? = null,
    accionSecundaria: (() -> Unit)? = null
) {
    val dialog = BottomSheetDialog(this)
    val v = layoutInflater.inflate(R.layout.dialog_mensaje, null)

    val icono = v.findViewById<ImageView>(R.id.imgSheet)
    icono.setImageResource(if (error) R.drawable.ic_error else R.drawable.ic_check_circle)
    icono.imageTintList = ColorStateList.valueOf(Color.parseColor(if (error) "#DC2626" else "#1B4FA0"))

    v.findViewById<TextView>(R.id.tvSheetTitulo).text = titulo
    v.findViewById<TextView>(R.id.tvSheetMensaje).text = mensaje

    val primario = v.findViewById<MaterialButton>(R.id.btnSheetPrimario)
    primario.text = textoPrimario
    primario.setOnClickListener { dialog.dismiss() }

    val secundario = v.findViewById<TextView>(R.id.btnSheetSecundario)
    if (textoSecundario != null) {
        secundario.visibility = View.VISIBLE
        secundario.text = textoSecundario
        secundario.setOnClickListener {
            dialog.dismiss()
            accionSecundaria?.invoke()
        }
    }

    dialog.setContentView(v)
    (v.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
    dialog.show()
}
