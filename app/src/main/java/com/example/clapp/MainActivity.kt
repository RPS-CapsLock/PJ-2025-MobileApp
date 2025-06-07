package com.example.clapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.example.clapp.ui.theme.CLAppTheme
import android.content.Intent

class MainActivity : ComponentActivity() {
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
        }

        NotificationHelper.createNotificationChannel(this)
        enableEdgeToEdge()

        setContent {
            CLAppTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF001F54))
                ) { innerPadding ->
                    AndroidView(
                        factory = { context ->
                            val view = LayoutInflater.from(context).inflate(R.layout.main, null)

                            val qrButton = view.findViewById<android.widget.Button>(R.id.qrScanButton)
                            qrButton.setOnClickListener {
                                context.startActivity(Intent(context, QRscanner::class.java))
                            }

                            val mixingButton = view.findViewById<android.widget.Button>(R.id.Mixing_button)
                            mixingButton.setOnClickListener {
                                context.startActivity(Intent(context, MixingActivity::class.java))
                            }

                            val buyButton = view.findViewById<android.widget.Button>(R.id.buy_button)
                            val handler = Handler(Looper.getMainLooper())
                            val updateBuyButtonState = object : Runnable {
                                override fun run() {
                                    buyButton.isEnabled = CartManager.cart.size() > 0
                                    handler.postDelayed(this, 1000)
                                }
                            }
                            handler.post(updateBuyButtonState)

                            buyButton.setOnClickListener {
                                if (CartManager.cart.size() > 0) {
                                    CartManager.cartsend.clear()
                                    CartManager.cartsend.addAll(CartManager.cart.getAllMixedCocktails())
                                    CartManager.clearCartManually()
                                    startPreparationTimer()
                                } else {
                                    Toast.makeText(context, "Cart is empty.", Toast.LENGTH_SHORT).show()
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

    private fun startPreparationTimer() {
        Toast.makeText(this, "Čas priprave 1 minut se je začel.", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            NotificationHelper.sendPreparationNotification(this)
            Toast.makeText(this, "Obvestilo poslano.", Toast.LENGTH_SHORT).show()
        }, 60_000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CLAppTheme {}
}
