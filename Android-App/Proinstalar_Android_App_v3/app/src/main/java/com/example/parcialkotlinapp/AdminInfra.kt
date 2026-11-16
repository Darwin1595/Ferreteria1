package com.example.parcialkotlinapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** Estados de una pantalla (RNF-02-02): cargando, con datos o con error. */
sealed class UiState<out T> {
    object Cargando : UiState<Nothing>()
    data class Exito<T>(val datos: T) : UiState<T>()
    data class Fallo(val mensaje: String, val sesionVencida: Boolean = false) : UiState<Nothing>()
}

/** Ejecuta una llamada Retrofit y publica Cargando -> Exito / Fallo. */
fun <T> Call<T>.publicarEn(destino: MutableLiveData<UiState<T>>) {
    destino.value = UiState.Cargando
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            val cuerpo = response.body()
            destino.value = when {
                response.code() == 401 -> UiState.Fallo("Tu sesión expiró. Inicia sesión de nuevo.", true)
                response.isSuccessful && cuerpo != null -> UiState.Exito(cuerpo)
                else -> UiState.Fallo(ApiErrors.mensaje(response))
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            destino.value = UiState.Fallo(ApiErrors.SIN_CONEXION)
        }
    })
}

/** Para acciones (crear, editar, eliminar): avisa el éxito o el mensaje de error. */
fun <T> Call<T>.ejecutar(alExito: (T?) -> Unit, alError: (String) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) alExito(response.body()) else alError(ApiErrors.mensaje(response))
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            alError(ApiErrors.SIN_CONEXION)
        }
    })
}

/** Datos actuales de un LiveData<UiState>, o null si aún no cargan. */
fun <T> LiveData<UiState<T>>.datos(): T? = (value as? UiState.Exito<T>)?.datos

fun String?.fechaHora(): String = (this ?: "").replace('T', ' ').take(16)

fun String?.guion(): String = if (this.isNullOrBlank()) "-" else this

fun TextInputLayout.texto(): String = editText?.text?.toString()?.trim() ?: ""

/** Crea una ventana inferior (bottom sheet) a partir de un layout. */
fun Activity.crearSheet(layout: Int): Pair<BottomSheetDialog, View> {
    val dialog = BottomSheetDialog(this)
    val v = layoutInflater.inflate(layout, null)
    dialog.setContentView(v)
    dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    (v.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
    return dialog to v
}

fun BottomSheetDialog.mostrarExpandido() {
    show()
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
}

fun Activity.irAlLoginPorSesionVencida() {
    SessionManager.cerrarSesion()
    toast("Tu sesión expiró. Inicia sesión de nuevo.")
    startActivity(
        Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
    finish()
}