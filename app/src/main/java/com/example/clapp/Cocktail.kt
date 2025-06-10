package com.example.clapp

data class Drink(
    val _id: String,
    val name: String,
    val type: String,
    val image: String?,
    val price: Double
)

data class Cocktail(
    val _id: String,
    val name: String,
    val ingredients: List<Drink> = emptyList(),
    val image: String?,
    val price: Double,
    val custom: Boolean
)
