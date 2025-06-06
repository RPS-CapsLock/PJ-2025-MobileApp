package com.example.clapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MixingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mixing_cocktails)

        val cocktails = loadCocktailsFromAssets(this)
        val adapter1 = CocktailAdapter(cocktails) { selectedCocktail ->
            Toast.makeText(this, "Selected in list 1: ${selectedCocktail.name}", Toast.LENGTH_SHORT).show()
        }

        val adapter2 = CocktailAdapter(cocktails) { selectedCocktail ->
            Toast.makeText(this, "Selected in list 2: ${selectedCocktail.name}", Toast.LENGTH_SHORT).show()
        }

        val recyclerView1 = findViewById<RecyclerView>(R.id.cocktails_1)
        recyclerView1.adapter = adapter1
        recyclerView1.layoutManager = LinearLayoutManager(this)

        val recyclerView2 = findViewById<RecyclerView>(R.id.cocktails_2)
        recyclerView2.adapter = adapter2
        recyclerView2.layoutManager = LinearLayoutManager(this)

        val backButton = findViewById<android.widget.Button>(R.id.Back_button)
        backButton.setOnClickListener {
            finish()
        }

        val mixingButton = findViewById<android.widget.Button>(R.id.Mix_cocktails)
        mixingButton.setOnClickListener {
            Toast.makeText(this, "Mixing button clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}
