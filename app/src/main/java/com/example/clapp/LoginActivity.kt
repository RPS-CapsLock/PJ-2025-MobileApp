package com.example.clapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.clapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.clapp.loginUtil.LoginUtil
import org.json.JSONArray

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            val username = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val success: Boolean = LoginUtil.sendLoginRequest(username, password)
                if (success) {
                    finish()
                }
            }
        }

        binding.goBackBtn0.setOnClickListener {
            finish()
        }

        binding.faceIdBtn1.setOnClickListener {
            val intent = Intent(this@LoginActivity, FaceScanActivity::class.java)
            LoginUtil.faces = JSONArray()
            intent.putExtra("maxFaces", 1) // Samo 1 slika
            startActivity(intent)
        }
    }
}
