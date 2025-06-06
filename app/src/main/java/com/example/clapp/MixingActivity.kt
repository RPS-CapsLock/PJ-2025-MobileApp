package com.example.clapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MixingActivity : AppCompatActivity() {
    private var selectedCocktail1: Cocktail? = null
    private var selectedCocktail2: Cocktail? = null

    private lateinit var adapter1: CocktailAdapter
    private lateinit var adapter2: CocktailAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mixing_cocktails)

        val cocktails = loadCocktailsFromAssets(this)

        adapter1 = CocktailAdapter(cocktails) { selected ->
            selectedCocktail1 = selected
            adapter1.setSelectedCocktail(selected)
        }

        adapter2 = CocktailAdapter(cocktails) { selected ->
            selectedCocktail2 = selected
            adapter2.setSelectedCocktail(selected)
        }

        val recyclerView1 = findViewById<RecyclerView>(R.id.cocktails_1)
        recyclerView1.adapter = adapter1
        recyclerView1.layoutManager = LinearLayoutManager(this)

        val recyclerView2 = findViewById<RecyclerView>(R.id.cocktails_2)
        recyclerView2.adapter = adapter2
        recyclerView2.layoutManager = LinearLayoutManager(this)

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
    }
    private fun mixCocktails(first: Cocktail, second: Cocktail) {
        CartManager.cart.addMixedCocktail(first, second)
        Toast.makeText(this, "Added ${first.name} + ${second.name} to cart!", Toast.LENGTH_SHORT).show()
    }
}
