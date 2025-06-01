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
import androidx.appcompat.app.AppCompatActivity

class MixingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mixing_cocktails)
    }
}

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
                            val view = LayoutInflater.from(context).inflate(R.layout.main, null)

                            val qrButton = view.findViewById<android.widget.Button>(R.id.qrScanButton)
                            qrButton.setOnClickListener {
                                val intent = android.content.Intent(context, QRscanner::class.java)
                                context.startActivity(intent)
                            }
                            val mixingButton = view.findViewById<android.widget.Button>(R.id.Mixing_button)
                            mixingButton.setOnClickListener {
                                val intent = android.content.Intent(context, MixingActivity::class.java)
                                context.startActivity(intent)
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
