package com.example.clapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.clapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.clapp.loginUtil.LoginUtil
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONArray
import androidx.activity.result.contract.ActivityResultContracts

class TwoFAActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val faceScanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.e("00", "FaceScanActivity finished, resultCode: ${result.resultCode}")

        send2FAPost()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("00", "Start 2fa activity")

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this@TwoFAActivity, FaceScanActivity::class.java)
        LoginUtil.faces = JSONArray()
        intent.putExtra("maxFaces", 1)
        faceScanLauncher.launch(intent)

        binding.loginBtn.setOnClickListener {

        }

        binding.goBackBtn0.setOnClickListener {
            finish()
        }

        binding.faceIdBtn1.setOnClickListener {

        }
    }

    private fun send2FAPost(){
        Log.e("00", "Resume 2fa activity")

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
            Log.e("00", "Send POST")
            val success: Boolean = LoginUtil.send2FARequest()

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
                    finish()
                }
            }
        }
    }

    override fun onResume(){
        super.onResume()
    }
}
