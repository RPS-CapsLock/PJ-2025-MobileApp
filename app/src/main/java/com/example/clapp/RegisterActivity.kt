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
            val password = binding.passwordEditText.text.toString()
            CoroutineScope(Dispatchers.IO).launch {
                val success: Boolean = LoginUtil.sendRegisterRequest(username, password)
                if (success) {
                    runOnUiThread {
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
        if (LoginUtil.faces.length() > 0) {
            val jsonString = LoginUtil.faces.toString()
            Log.d("FaceScan", "JSON of 50 faces: $jsonString")
        }
    }
}
