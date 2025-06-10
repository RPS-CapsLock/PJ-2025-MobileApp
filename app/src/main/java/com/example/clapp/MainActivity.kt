package com.example.clapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.clapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("Notification", "POST_NOTIFICATIONS permission granted.")
        } else {
            Log.w("Notification", "POST_NOTIFICATIONS permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default_channel_id"
            val channelName = "Privzeti Kanal"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        binding.mixbutton.setOnClickListener {
            startActivity(Intent(this, MixingActivity::class.java))
        }

        binding.buybutton.setOnClickListener {
            if (CartManager.cart.size() > 0) {
                CartManager.cartsend.clear()
                CartManager.cartsend.addAll(CartManager.cart.getAllMixedCocktails())
                CartManager.clearCartManually()
                startPreparationTimer()
            } else {
                Toast.makeText(this, "Cart is empty.", Toast.LENGTH_SHORT).show()
            }
        }

        val handler = Handler(Looper.getMainLooper())
        val updateBuyButtonState = object : Runnable {
            override fun run() {
                binding.buybutton.isEnabled = CartManager.cart.size() > 0
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateBuyButtonState)
    }

    private fun startPreparationTimer() {
        Toast.makeText(this, "Čas priprave 1 minut se je začel.", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            NotificationHelper.sendPreparationNotification(this)
            Toast.makeText(this, "Obvestilo poslano.", Toast.LENGTH_SHORT).show()
        }, 60_000)
    }
}
