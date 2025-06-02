package com.example.clapp

import android.content.Intent
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
import com.example.clapp.loginUtil.LoginUtil

class LoginActivity : ComponentActivity() {

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

                            val activity =

                            binding.loginBtn.setOnClickListener {
                                val username = binding.emailEditText.text.toString()
                                val password = binding.passwordEditText.text.toString()
                                CoroutineScope(Dispatchers.IO).launch {
                                    val success: Boolean = LoginUtil.sendLoginRequest(username, password)
                                    if (success)
                                        finish();
                                }
                            }

                            binding.goBackBtn0.setOnClickListener {
                                finish();
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
}
