package com.example.clapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.example.clapp.databinding.ActivityLoginBinding
import com.example.clapp.ui.theme.CLAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginActivity : ComponentActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CLAppTheme {
                Scaffold(modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF001F54))
                ) { innerPadding ->
                    AndroidView(
                        factory = { context ->
                            val binding = ActivityLoginBinding.inflate(LayoutInflater.from(context))
                            val view = binding.root

                            binding.loginBtn.setOnClickListener {
                                val username = binding.emailEditText.text.toString()
                                val password = binding.passwordEditText.text.toString()
                                CoroutineScope(Dispatchers.IO).launch {
                                    sendLoginRequest(username, password)
                                }
                            }

                            view
                        },
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }

    private fun sendLoginRequest(username: String, password: String) {
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://10.0.2.2:3001/users/login")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LoginResponse", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("LoginResponse", "Response: $responseData")
                } else {
                    Log.e("LoginResponse", "Error ${response.code}: ${response.message}")
                    val errorBody = response.body?.string()
                    Log.e("LoginResponse", "Error body: $errorBody")
                }
            }
        })
    }
}
