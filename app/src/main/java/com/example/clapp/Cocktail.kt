package com.example.clapp

import org.json.JSONArray
import org.json.JSONObject

data class Drink(
    val _id: String,
    val name: String,
    val type: String,
    val image: String?,
    val price: Double
) {
    companion object {
        fun fromJson(json: JSONObject): Drink {
            return Drink(
                _id = json.optString("_id", ""),
                name = json.optString("name", ""),
                type = json.optString("type", ""),
                image = json.optString("image", null),
                price = json.optDouble("price", 0.0)
            )
        }
    }
}

data class Cocktail(
    val _id: String,
    val name: String,
    val ingredients: List<Drink> = emptyList(),
    val image: String?,
    val price: Double,
    val custom: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject): Cocktail {
            val ingredientsJson = json.optJSONArray("ingredients")
            val ingredientsList = mutableListOf<Drink>()

            if (ingredientsJson != null) {
                for (i in 0 until ingredientsJson.length()) {
                    val drinkJson = ingredientsJson.getJSONObject(i)
                    val drink = Drink.fromJson(drinkJson)
                    ingredientsList.add(drink)
                }
            }

            return Cocktail(
                _id = json.optString("_id", ""),
                name = json.optString("name", ""),
                ingredients = ingredientsList,
                image = json.optString("image", null),
                price = json.optDouble("price", 0.0),
                custom = json.optBoolean("custom", false)
            )
        }
    }
}
