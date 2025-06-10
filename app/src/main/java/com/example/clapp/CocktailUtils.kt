package com.example.clapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun loadCocktailsFromAssets(context: Context): List<Cocktail> {
    val jsonString = context.assets.open("json/cocktails.json").bufferedReader().use { it.readText() }
    val gson = Gson()
    val listType = object : TypeToken<List<Cocktail>>() {}.type
    return gson.fromJson(jsonString, listType)
}
