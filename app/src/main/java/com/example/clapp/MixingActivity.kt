package com.example.clapp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

fun hasInternetPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.INTERNET
    ) == PackageManager.PERMISSION_GRANTED
}

class MixingActivity : AppCompatActivity() {
    private var selectedCocktail1: Cocktail? = null
    private var selectedCocktail2: Cocktail? = null

    private lateinit var adapter1: CocktailAdapter
    private lateinit var adapter2: CocktailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mixing_cocktails)

        adapter1 = CocktailAdapter(emptyList()) { selected ->
            selectedCocktail1 = selected
            adapter1.setSelectedCocktail(selected)
        }

        adapter2 = CocktailAdapter(emptyList()) { selected ->
            selectedCocktail2 = selected
            adapter2.setSelectedCocktail(selected)
        }

        val recyclerView1 = findViewById<RecyclerView>(R.id.cocktails_1)
        recyclerView1.adapter = adapter1
        recyclerView1.layoutManager = LinearLayoutManager(this)

        val recyclerView2 = findViewById<RecyclerView>(R.id.cocktails_2)
        recyclerView2.adapter = adapter2
        recyclerView2.layoutManager = LinearLayoutManager(this)

        if (hasInternetPermission(this)) {
            Log.d("CocktailFetch", "Internet permission granted")
        }

        findViewById<android.widget.Button>(R.id.Back_button).setOnClickListener { finish() }

        findViewById<android.widget.Button>(R.id.Mix_cocktails).setOnClickListener {
            if (selectedCocktail1 != null && selectedCocktail2 != null) {
                mixCocktails(selectedCocktail1!!, selectedCocktail2!!)
                selectedCocktail1 = null
                selectedCocktail2 = null
                adapter1.clearSelection()
                adapter2.clearSelection()
            } else {
                Toast.makeText(this, "Select one cocktail from each list!", Toast.LENGTH_SHORT).show()
            }
        }

        loadCocktailsFromBackend()
    }

    private fun mixCocktails(first: Cocktail, second: Cocktail) {
        lifecycleScope.launch {
            try {
                val client = OkHttpClient()
                val link = "http://10.0.2.2:3001"

                Log.d("MixingActivity", "Requesting userId from $link/users/session/userid")

                val response = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("$link/users/session/userid")
                        .build()
                    client.newCall(request).execute()
                }

                // Replace with a valid ObjectId from your MongoDB users collection for testing
                val fallbackUserId = "64a5f8a08f1b9c1234567890"

                val userId = if (!response.isSuccessful) {
                    if (response.code == 401) {
                        Log.w("MixingActivity", "Unauthorized, defaulting userId to fallback valid ObjectId: $fallbackUserId")
                        fallbackUserId
                    } else {
                        throw Exception("Failed to get session user ID: HTTP ${response.code}")
                    }
                } else {
                    val bodyString = response.body?.string() ?: throw Exception("Empty response body")
                    Log.d("MixingActivity", "Response body for userId request: $bodyString")
                    val json = JSONObject(bodyString)
                    json.optString("userId", fallbackUserId)
                }

                Log.d("MixingActivity", "Obtained userId: $userId")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MixingActivity, "Adding cocktails to cart...", Toast.LENGTH_SHORT).show()
                }

                withContext(Dispatchers.IO) {
                    CartManager.addMixedCocktail(userId, first, second)
                }

                Log.d("MixingActivity", "Cocktails added to cart successfully")

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MixingActivity,
                        "Added ${first.name} + ${second.name} to cart!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("MixingActivity", "Error adding cocktails to cart", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MixingActivity,
                        "Failed to add cocktails to cart: ${e.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    private fun loadCocktailsFromBackend() {
        lifecycleScope.launch {
            try {
                val cocktails = CocktailUtil.fetchCocktails()
                adapter1.updateData(cocktails)
                adapter2.updateData(cocktails)
            } catch (e: Exception) {
                Toast.makeText(this@MixingActivity, "Failed to load cocktails: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
