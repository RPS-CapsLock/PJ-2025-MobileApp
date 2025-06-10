package com.example.clapp

import android.os.Handler
import android.os.Looper
import android.widget.Toast

object CartManager {
    val cart = Cart()
    val cartsend = Cart()

    private var clearHandler: Handler? = null
    private var clearRunnable: Runnable? = null
    private var isTimerRunning = false

    fun addMixedCocktail(first: Cocktail, second: Cocktail) {
        cart.addMixedCocktail(first, second)

        if (!isTimerRunning) {
            isTimerRunning = true

            Toast.makeText(
                AppContextProvider.context,
                "Cart will be cleared after 20 minutes",
                Toast.LENGTH_LONG
            ).show()

            clearHandler = Handler(Looper.getMainLooper())
            clearRunnable = Runnable {
                cart.clear()
                Toast.makeText(
                    AppContextProvider.context,
                    "Cart cleared after 20 minutes",
                    Toast.LENGTH_SHORT
                ).show()
                isTimerRunning = false
            }
            clearHandler?.postDelayed(clearRunnable!!, 20 * 60 * 1000)
        }
    }

    fun clearCartManually() {
        clearHandler?.removeCallbacks(clearRunnable!!)
        cart.clear()
        isTimerRunning = false
    }
}
