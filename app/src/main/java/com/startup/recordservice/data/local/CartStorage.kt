package com.startup.recordservice.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists client cart per logged-in user.
 *
 * Important: This is NOT cleared on logout, so the cart survives logout/login until user clears it.
 */
@Singleton
class CartStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private fun key(userId: String) = "cart_items_$userId"

    fun saveCart(userId: String, items: List<com.startup.recordservice.ui.viewmodel.ExploreCartItem>) {
        val json = gson.toJson(items)
        prefs.edit().putString(key(userId), json).apply()
    }

    fun loadCart(userId: String): List<com.startup.recordservice.ui.viewmodel.ExploreCartItem> {
        val json = prefs.getString(key(userId), null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<com.startup.recordservice.ui.viewmodel.ExploreCartItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clearCart(userId: String) {
        prefs.edit().remove(key(userId)).apply()
    }
}

