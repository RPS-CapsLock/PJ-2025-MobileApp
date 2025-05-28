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
import com.example.clapp.databinding.MainBinding
import android.content.Intent

class MainActivity : ComponentActivity() {
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
                            val binding = MainBinding.inflate(LayoutInflater.from(context))
                            val view = binding.root

                            val activity = this@MainActivity

                            binding.qrScanButton.setOnClickListener {
                                val intent = Intent(activity, QRscanner::class.java)
                                activity.startActivity(intent)
                            }

                            binding.loginBtn.setOnClickListener {
                                val intent = Intent(activity, Login::class.java)
                                activity.startActivity(intent)
                            }

                            binding.registerBtn.setOnClickListener {
                                val intent = Intent(activity, Login::class.java)
                                activity.startActivity(intent)
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CLAppTheme {
    }
}
