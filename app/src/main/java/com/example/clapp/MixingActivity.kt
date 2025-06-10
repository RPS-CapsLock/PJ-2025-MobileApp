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
import kotlinx.coroutines.launch
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request

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

        if(hasInternetPermission(this)){
            Log.d("CocktailFetch", "success")
        }

        val backButton = findViewById<android.widget.Button>(R.id.Back_button)
        backButton.setOnClickListener { finish() }

        val mixingButton = findViewById<android.widget.Button>(R.id.Mix_cocktails)
        mixingButton.setOnClickListener {
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

                val request = Request.Builder()
                    .url("${link}/users/session/userid")
                    .build()

                val response = client.newCall(request).execute()

                val userId = if (!response.isSuccessful) {
                    if (response.code == 401) {
                        "552"
                    } else {
                        throw Exception("Failed to get session user ID: HTTP ${response.code}")
                    }
                } else {
                    val bodyString = response.body?.string() ?: throw Exception("Empty response body")
                    val json = JSONObject(bodyString)
                    json.optString("userId", "552")
                }

                CartManager.addMixedCocktail(userId, first, second)

                Toast.makeText(
                    this@MixingActivity,
                    "Added ${first.name} + ${second.name} to cart!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@MixingActivity,
                    "Failed to add cocktails to cart: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
