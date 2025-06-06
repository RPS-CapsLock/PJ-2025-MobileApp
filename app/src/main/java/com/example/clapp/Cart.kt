package com.example.clapp

data class MixedCocktail(
    val first: Cocktail,
    val second: Cocktail
) {
    val name: String
        get() = "${first.name} + ${second.name}"
}

class Cart {
    private val mixedCocktails = mutableListOf<MixedCocktail>()

    fun addMixedCocktail(first: Cocktail, second: Cocktail) {
        mixedCocktails.add(MixedCocktail(first, second))
    }

    fun getAllMixedCocktails(): List<MixedCocktail> = mixedCocktails

    fun addAll(mixedCocktailsList: List<MixedCocktail>) {
        mixedCocktails.addAll(mixedCocktailsList)
    }

    fun clear() {
        mixedCocktails.clear()
    }

    fun size(): Int = mixedCocktails.size
}
