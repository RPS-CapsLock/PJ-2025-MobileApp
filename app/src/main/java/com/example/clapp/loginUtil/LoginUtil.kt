package com.example.clapp.loginUtil

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object LoginUtil {
    var userid: String = ""
        private set;
    var username: String = ""
        private set;
    var password: String = ""
        private set;
    var faces: JSONArray = JSONArray();
    private val client = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            private val cookieStore = mutableMapOf<HttpUrl, List<Cookie>>()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url] = cookies
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url] ?: emptyList()
            }
        })
        .build()
    fun sendLoginRequest(username0: String, password0: String): Boolean {
        val json = JSONObject().apply {
            put("username", username0)
            put("password", password0)
            put("_2FA", true)
            put("images", faces)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://127.0.0.1:3001/users/login")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LoginResponse", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val responseJson = JSONObject(responseData);
                        userid = responseJson.getString("_id");
                        username = responseJson.getString("username");
                        password = responseJson.getString("password");
                    }
                    Log.d("LoginResponse", "Response: $responseData")
                } else {
                    Log.e("LoginResponse", "Error ${response.code}: ${response.message}")
                    val errorBody = response.body?.string()
                    Log.e("LoginResponse", "Error body: $errorBody")
                }
            }
        })
        if(userid != "" && username != "" && password != "")
            return true;
        return false;
    }
    fun sendRegisterRequest(username0: String, password0: String): Boolean {
        val json = JSONObject().apply {
            put("username", username0)
            put("password", password0)
            put("_2FA", true)
            put("images", faces)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://127.0.0.1:3001/users/")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LoginResponse", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null){
                        val responseJson = JSONObject(responseData);
                        userid = responseJson.getString("_id");
                        username = responseJson.getString("username");
                        password = responseJson.getString("password");
                    }
                    Log.d("LoginResponse", "Response: $responseData")
                } else {
                    Log.e("LoginResponse", "Error ${response.code}: ${response.message}")
                    val errorBody = response.body?.string()
                    Log.e("LoginResponse", "Error body: $errorBody")
                }
            }
        })
        if(userid != "" && username != "" && password != "")
            return true;
        return false;
    }
}