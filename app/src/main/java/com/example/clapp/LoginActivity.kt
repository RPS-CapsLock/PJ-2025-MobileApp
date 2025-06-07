package com.example.clapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
            intent.putExtra("maxFaces", 1) // Samo 1 slika
            startActivity(intent)
        }
    }
}
