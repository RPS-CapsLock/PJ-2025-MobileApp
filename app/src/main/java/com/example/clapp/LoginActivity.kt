package com.example.clapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
            intent.putExtra("maxFaces", 1)
            startActivity(intent)
        }

        binding.approveLoginBtn.setOnClickListener {
            sendLoginApproval()
        }
    }

    private fun sendLoginApproval() {
        val requestId = "test-request-id-123"

        val formBody = FormBody.Builder()
            .add("requestId", requestId)
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2:3001/faceid/approve")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NETWORK_ERROR", "Network Failure: ${e.localizedMessage}", e)
                runOnUiThread {
                    toast("Napaka pri pošiljanju potrditve!")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("NETWORK_SUCCESS", "Response: ${response.code} ${response.message}")
                runOnUiThread {
                    if (response.isSuccessful) {
                        toast("Prijava uspešno odobrena!")
                    } else {
                        toast("Napaka: ${response.message}")
                    }
                }
            }
        })
    }


    private fun toast(message: String) {
        runOnUiThread {
            android.widget.Toast.makeText(this@LoginActivity, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
