package com.example.clapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.clapp.databinding.ActivityRegisterBinding
import com.example.clapp.loginUtil.LoginUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerBtn.setOnClickListener {
            val username = binding.emailEditText.text.toString()
            val email = binding.usernameEditText3.text.toString()
            val password = binding.passwordEditText.text.toString()

            runOnUiThread {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.registerBtn.isEnabled = false
                binding.goBackBtn1.isEnabled = false
                binding.faceScanBtn0.isEnabled = false
                binding.emailEditText.isEnabled = false
                binding.usernameEditText3.isEnabled = false
                binding.passwordEditText.isEnabled = false
            }

            CoroutineScope(Dispatchers.IO).launch {
                val success: Boolean = LoginUtil.sendRegisterRequest(username, password, email)

                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.registerBtn.isEnabled = true
                    binding.goBackBtn1.isEnabled = true
                    binding.faceScanBtn0.isEnabled = true
                    binding.emailEditText.isEnabled = true
                    binding.usernameEditText3.isEnabled = true
                    binding.passwordEditText.isEnabled = true

                    if (success) {
                        finish()
                    }
                }
            }
        }


        binding.goBackBtn1.setOnClickListener {
            finish()
        }

        binding.faceScanBtn0.setOnClickListener {
            val intent = Intent(this, FaceScanActivity::class.java)
            LoginUtil.faces = JSONArray()
            intent.putExtra("maxFaces", 100)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
    }
}
