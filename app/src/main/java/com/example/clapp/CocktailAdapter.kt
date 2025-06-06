package com.example.clapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CocktailAdapter(
    private val cocktails: List<Cocktail>,
    private val onItemClick: (Cocktail) -> Unit
) : RecyclerView.Adapter<CocktailAdapter.CocktailViewHolder>() {

    private var selectedCocktail: Cocktail? = null

    class CocktailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cocktailName: TextView = view.findViewById(R.id.cocktailName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CocktailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cocktail_item, parent, false)
        return CocktailViewHolder(view)
    }

    override fun onBindViewHolder(holder: CocktailViewHolder, position: Int) {
        val cocktail = cocktails[position]
        holder.cocktailName.text = cocktail.name

        if (cocktail == selectedCocktail) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFDDDDFF")) // example highlight
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            selectedCocktail = cocktail
            notifyDataSetChanged()
            onItemClick(cocktail)
        }
    }

    override fun getItemCount() = cocktails.size

    fun setSelectedCocktail(cocktail: Cocktail?) {
        selectedCocktail = cocktail
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedCocktail = null
        notifyDataSetChanged()
    }
}
