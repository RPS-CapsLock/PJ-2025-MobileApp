package com.example.clapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CocktailAdapter(
    private val cocktails: List<Cocktail>,
    private val onItemClick: (Cocktail) -> Unit
) : RecyclerView.Adapter<CocktailAdapter.CocktailViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class CocktailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(
                holder.itemView.context.getColor(R.color.selected_background)
            )
        } else {
            holder.itemView.setBackgroundColor(
                holder.itemView.context.getColor(android.R.color.transparent)
            )
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            onItemClick(cocktail)
        }
    }

    override fun getItemCount() = cocktails.size
}
