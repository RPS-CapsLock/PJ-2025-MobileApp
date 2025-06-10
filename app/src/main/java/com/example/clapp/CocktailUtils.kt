package com.example.clapp

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
const val LINK_TO_SERVER = "http://10.0.2.2:3001"

object CocktailUtil {
    private val client = OkHttpClient()
    private const val COCKTAILS_URL = "${LINK_TO_SERVER}/cocktails"

    suspend fun fetchCocktails(): List<Cocktail> = suspendCancellableCoroutine { cont ->
        val request = Request.Builder().url(COCKTAILS_URL).get().build()
        Log.d("CocktailFetch", "Fetching from URL: $COCKTAILS_URL")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CocktailFetch", "Failed to fetch cocktails: ${e.message}")
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        val cocktailsJson = JSONArray(body)
                        val cocktails = mutableListOf<Cocktail>()

                        for (i in 0 until cocktailsJson.length()) {
                            val cocktailObj = cocktailsJson.getJSONObject(i)

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
                        Log.e("CocktailFetch", "JSON parsing error: ${e.message}")
                        cont.resumeWithException(e)
                    }
                } else {
                    cont.resumeWithException(IOException("Unexpected response: ${response.code}"))
                }
            }
        })
    }
}
