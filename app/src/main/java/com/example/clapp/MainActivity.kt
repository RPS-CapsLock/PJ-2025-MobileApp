package com.example.clapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.clapp.ui.theme.CLAppTheme
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : ComponentActivity() {
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATIONS)
            }
        }
        createNotificationChannel()
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
                                val intent = android.content.Intent(context, QRscanner::class.java)
                                context.startActivity(intent)
                            }

                            val mixingButton = view.findViewById<android.widget.Button>(R.id.Mixing_button)
                            mixingButton.setOnClickListener {
                                val intent = android.content.Intent(context, MixingActivity::class.java)
                                context.startActivity(intent)
                            }
                            val buyButton = view.findViewById<android.widget.Button>(R.id.buy_button)
                            buyButton.isEnabled = CartManager.cart.size() > 0
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
            sendNotification()
            Toast.makeText(this, "Obvestilo poslano.", Toast.LENGTH_SHORT).show()
        }, 1 * 60 * 1000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "preparation_channel",
                "Čas priprave",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikacije za čas priprave"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    private fun sendNotification() {
        val intent = Intent(this, QRscanner::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "preparation_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Priprava končana!")
            .setContentText("Klikni za odklep paketnika.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        with(NotificationManagerCompat.from(this)) {
            notify(1001, notification)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            } else {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show()
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
