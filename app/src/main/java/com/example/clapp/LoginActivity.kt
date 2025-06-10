package com.example.clapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.clapp.databinding.ActivityLoginBinding
import com.example.clapp.loginUtil.LoginUtil
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "Device Token: $token")
        }

        binding.loginBtn.setOnClickListener {
            val username = binding.usernameEditText2.text.toString()
            val password = binding.passwordEditText.text.toString()

            runOnUiThread {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.loginBtn.isEnabled = false
                binding.faceIdBtn1.isEnabled = false
                binding.goBackBtn0.isEnabled = false
                binding.usernameEditText2.isEnabled = false
                binding.passwordEditText.isEnabled = false
            }

            CoroutineScope(Dispatchers.IO).launch {
                val success: Boolean = LoginUtil.sendLoginRequest(username, password)

                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.loginBtn.isEnabled = true
                    binding.faceIdBtn1.isEnabled = true
                    binding.goBackBtn0.isEnabled = true
                    binding.usernameEditText2.isEnabled = true
                    binding.passwordEditText.isEnabled = true

                    if (success) {
                        finish()
                    }
                    else {
                        Toast.makeText(this@LoginActivity, "Wrong face, username or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.goBackBtn0.setOnClickListener {
            finish()
        }

        binding.faceIdBtn1.setOnClickListener {
            val intent = Intent(this@LoginActivity, FaceScanActivity::class.java)
            LoginUtil.faces = JSONArray()
            intent.putExtra("maxFaces", 1)
            startActivity(intent)
        }

    }

    private fun toast(message: String) {
        runOnUiThread {
            android.widget.Toast.makeText(this@LoginActivity, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
