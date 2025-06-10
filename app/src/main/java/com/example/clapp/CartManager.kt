package com.example.clapp

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val coroutineScope = CoroutineScope(Dispatchers.Main)

object CartManager {
    private val client = OkHttpClient()
    private const val CART_URL = "$LINK_TO_SERVER/cart"

    private val clearHandler = Handler(Looper.getMainLooper())
    private var clearRunnable: Runnable? = null
    private var isTimerRunning = false

    suspend fun addMixedCocktail(userId: String, first: Cocktail, second: Cocktail) {
        val json = JSONObject().apply {
            put("userId", userId)
            put("cocktails", listOf(first._id, second._id))
        }

        suspendCancellableCoroutine<Unit> { cont ->
            val mediaType = "application/json".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$CART_URL/addMixed")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (it.isSuccessful) {
                            Log.d("CartManager", "Added mixed cocktail to cart")
                            cont.resume(Unit)
                        } else {
                            cont.resumeWithException(IOException("Failed to add to cart: ${it.code}"))
                        }
                    }
                }
            })
        }

        if (!isTimerRunning) {
            isTimerRunning = true
            Toast.makeText(
                AppContextProvider.context,
                "Cart will be cleared after 20 minutes",
                Toast.LENGTH_LONG
            ).show()

            clearRunnable = Runnable {
                coroutineScope.launch {
                    clearCartManually(userId)
                    Toast.makeText(
                        AppContextProvider.context,
                        "Cart cleared after 20 minutes",
                        Toast.LENGTH_SHORT
                    ).show()
                    isTimerRunning = false
                }
            }
            clearHandler.postDelayed(clearRunnable!!, 20 * 60 * 1000)
        }
    }

    suspend fun clearCartManually(userId: String) {
        clearRunnable?.let { clearHandler.removeCallbacks(it) }
        suspendCancellableCoroutine<Unit> { cont ->
            val request = Request.Builder()
                .url("$CART_URL/clear/$userId")
                .delete()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (it.isSuccessful) {
                            Log.d("CartManager", "Cart cleared manually")
                            cont.resume(Unit)
                        } else {
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
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    if (it.isSuccessful && body != null) {
                        try {
                            val jsonArray = JSONArray(body)
                            val cocktails = mutableListOf<Cocktail>()

                            for (i in 0 until jsonArray.length()) {
                                val cocktailObj = jsonArray.getJSONObject(i)
                                val ingredientsJson = cocktailObj.optJSONArray("ingredients") ?: JSONArray()
                                val ingredients = mutableListOf<Drink>()

                                for (j in 0 until ingredientsJson.length()) {
                                    val drinkObj = ingredientsJson.getJSONObject(j)
                                    ingredients.add(
                                        Drink(
                                            _id = drinkObj.getString("_id"),
                                            name = drinkObj.getString("name"),
                                            type = drinkObj.getString("type"),
                                            image = drinkObj.optString("image", null),
                                            price = drinkObj.getDouble("price")
                                        )
                                    )
                                }

                                cocktails.add(
                                    Cocktail(
                                        _id = cocktailObj.getString("_id"),
                                        name = cocktailObj.getString("name"),
                                        ingredients = ingredients,
                                        image = cocktailObj.optString("image", null),
                                        price = cocktailObj.getDouble("price"),
                                        custom = cocktailObj.getBoolean("custom")
                                    )
                                )
                            }
                            cont.resume(cocktails)
                        } catch (e: Exception) {
                            cont.resumeWithException(e)
                        }
                    } else {
                        cont.resumeWithException(IOException("Unexpected response code: ${it.code}"))
                    }
                }
            }
        })
    }
}
