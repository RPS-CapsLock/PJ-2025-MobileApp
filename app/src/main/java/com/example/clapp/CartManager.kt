package com.example.clapp

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val coroutineScope = CoroutineScope(Dispatchers.Main)
private const val link = "http://10.0.2.2:3001" // Adjust as necessary

object CartManager {
    private val client = OkHttpClient()
    private const val CART_URL = "$link/cart"

    private val clearHandler = Handler(Looper.getMainLooper())
    private var clearRunnable: Runnable? = null
    private var isTimerRunning = false

    suspend fun addMixedCocktail(userId: String, first: Cocktail, second: Cocktail) {
        Log.d("CartManager", "Starting to add mixed cocktail for userId=$userId")
        try {
            addCocktailToCart(userId, first._id)
            Log.d("CartManager", "Added first cocktail: ${first._id}")
            showToastOnMain("Added ${first.name} to cart")

            addCocktailToCart(userId, second._id)
            Log.d("CartManager", "Added second cocktail: ${second._id}")
            showToastOnMain("Added ${second.name} to cart")

            if (!isTimerRunning) {
                isTimerRunning = true
                showToastOnMain("Cart will be cleared after 20 minutes")

                clearRunnable = Runnable {
                    coroutineScope.launch {
                        try {
                            clearCartManually(userId)
                            showToastOnMain("Cart cleared after 20 minutes")
                            Log.d("CartManager", "Cart cleared automatically after 20 min")
                        } catch (e: Exception) {
                            Log.e("CartManager", "Failed to clear cart automatically: ${e.message}")
                        } finally {
                            isTimerRunning = false
                        }
                    }
                }
                clearHandler.postDelayed(clearRunnable!!, 20 * 60 * 1000)
            }
        } catch (e: Exception) {
            Log.e("CartManager", "Error adding mixed cocktail: ${e.message}")
            showToastOnMain("Error adding cocktails: ${e.message}")
            throw e
        }
    }

    private suspend fun addCocktailToCart(userId: String, cocktailId: String) {
        Log.d("CartManager", "Attempting to add cocktail $cocktailId to cart for user $userId")
        val json = JSONObject().apply {
            put("userId", userId)
            put("cocktailId", cocktailId)
        }

        suspendCancellableCoroutine<Unit> { cont ->
            val mediaType = "application/json".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$CART_URL/add")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("CartManager", "Network failure adding cocktail $cocktailId: ${e.message}")
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (it.isSuccessful) {
                            Log.d("CartManager", "Successfully added cocktail $cocktailId to cart")
                            cont.resume(Unit)
                        } else {
                            Log.e("CartManager", "Failed to add cocktail $cocktailId: HTTP ${it.code}")
                            cont.resumeWithException(IOException("Failed to add to cart: ${it.code}"))
                        }
                    }
                }
            })
        }
    }

    suspend fun clearCartManually(userId: String) {
        Log.d("CartManager", "Attempting to clear cart for user $userId")
        clearRunnable?.let { clearHandler.removeCallbacks(it) }
        suspendCancellableCoroutine<Unit> { cont ->
            val request = Request.Builder()
                .url("$CART_URL/$userId")
                .delete()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("CartManager", "Failed network call to clear cart: ${e.message}")
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (it.isSuccessful) {
                            Log.d("CartManager", "Cart cleared manually")
                            cont.resume(Unit)
                        } else {
                            Log.e("CartManager", "Failed to clear cart: HTTP ${it.code}")
                            cont.resumeWithException(IOException("Failed to clear cart: ${it.code}"))
                        }
                    }
                }
            })
        }
        isTimerRunning = false
    }

    suspend fun fetchCart(userId: String): List<Cocktail> = suspendCancellableCoroutine { cont ->
        val url = "$CART_URL/$userId"
        Log.d("CartManager", "Fetching cart for user $userId from $url")
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CartManager", "Failed to fetch cart: ${e.message}")
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val err = "Failed to fetch cart: HTTP ${it.code}"
                        Log.e("CartManager", err)
                        cont.resumeWithException(IOException(err))
                        return
                    }

                    val bodyString = it.body?.string()
                    if (bodyString == null) {
                        val err = "Empty response body while fetching cart"
                        Log.e("CartManager", err)
                        cont.resumeWithException(IOException(err))
                        return
                    }

                    try {
                        val jsonArray = JSONObject("{\"cart\":$bodyString}").getJSONArray("cart")
                        val cocktails = mutableListOf<Cocktail>()
                        for (i in 0 until jsonArray.length()) {
                            val jsonCocktail = jsonArray.getJSONObject(i)
                            val cocktail = Cocktail.fromJson(jsonCocktail)
                            cocktails.add(cocktail)
                        }
                        cont.resume(cocktails)
                    } catch (e: Exception) {
                        Log.e("CartManager", "Error parsing cart JSON: ${e.message}")
                        cont.resumeWithException(e)
                    }
                }
            }
        })
    }

    private fun showToastOnMain(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(AppContextProvider.context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
