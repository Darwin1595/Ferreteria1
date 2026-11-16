package com.example.parcialkotlinapp

import com.example.parcialkotlinapp.models.CartItem
import com.example.parcialkotlinapp.models.Producto

/** Carrito en memoria compartido por todas las pantallas. */
object CartManager {
    private val items = LinkedHashMap<Int, CartItem>()

    fun lista(): List<CartItem> = items.values.toList()
    fun totalItems(): Int = items.values.sumOf { it.cantidad }
    fun total(): Double = items.values.sumOf { it.producto.precio * it.cantidad }
    fun isEmpty(): Boolean = items.isEmpty()

    /** Devuelve false si ya se alcanzó el stock disponible. */
    fun agregar(p: Producto): Boolean {
        val actual = items[p.id]
        if (actual == null) {
            if (p.stock < 1) return false
            items[p.id] = CartItem(p, 1)
            return true
        }
        if (actual.cantidad >= p.stock) return false
        actual.cantidad++
        return true
    }

    fun restar(id: Int) {
        val actual = items[id] ?: return
        if (actual.cantidad > 1) actual.cantidad-- else items.remove(id)
    }

    fun quitar(id: Int) { items.remove(id) }
    fun clear() = items.clear()
}
