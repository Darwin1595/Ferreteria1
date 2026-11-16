package com.proinstalar.app.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val telefono: String,
    val direccion: String
)

data class AuthResponse(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val token: String
)

data class Categoria(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val imagenUrl: String,
    val cantidadProductos: Int
)

data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val categoriaId: Int,
    val categoriaNombre: String,
    val imagenUrl: String,
    val estado: Boolean
)

data class TipoServicio(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precioBase: Double
)

data class SolicitudRequest(
    val clienteNombre: String,
    val clienteEmail: String,
    val clienteTelefono: String,
    val clienteDireccion: String,
    val tipoServicioId: Int,
    val total: Double,
    val notas: String
)

data class SolicitudResponse(
    val id: Int,
    val clienteNombre: String,
    val estado: String,
    val total: Double,
    val fechaCreacion: String
)
