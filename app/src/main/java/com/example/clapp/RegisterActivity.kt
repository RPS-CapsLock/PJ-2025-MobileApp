package com.example.clapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.clapp.ui.theme.CLAppTheme
import android.view.LayoutInflater
import android.widget.TextView
import com.example.clapp.databinding.ActivityRegisterBinding
import android.content.Intent
import com.example.clapp.MainActivity
import com.example.clapp.loginUtil.LoginUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RegisterActivity : ComponentActivity() {
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
                            val binding = ActivityRegisterBinding.inflate(LayoutInflater.from(context))
                            val view = binding.root

                            val activity = this@RegisterActivity

                            binding.registerBtn.setOnClickListener {
                                val username = binding.emailEditText.text.toString()
                                val password = binding.passwordEditText.text.toString()
                                CoroutineScope(Dispatchers.IO).launch {
                                    val success: Boolean = LoginUtil.sendRegisterRequest(username, password)
                                    if (success)
                                        finish();
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
}