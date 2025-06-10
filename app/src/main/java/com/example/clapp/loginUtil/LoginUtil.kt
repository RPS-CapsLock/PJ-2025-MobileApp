package com.example.clapp.loginUtil

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resumeWithException
import com.google.firebase.FirebaseApp

//val LINK_TO_SERVER = "http://127.0.0.1:3001"
val LINK_TO_SERVER = "https://3fdf-213-161-25-74.ngrok-free.app"

object LoginUtil {
    var userid: String = ""
        private set;
    var username: String = ""
        private set;
    var email: String = ""
        private set;
    var password: String = ""
        private set;
    private var fcmToken: String = ""
    var faces: JSONArray = JSONArray();
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.MINUTES)
        .readTimeout(10, java.util.concurrent.TimeUnit.MINUTES)
        .writeTimeout(10, java.util.concurrent.TimeUnit.MINUTES)
        .cookieJar(object : CookieJar {
            private val cookieStore = mutableMapOf<HttpUrl, List<Cookie>>()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url] = cookies
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url] ?: emptyList()
            }
        })
        .build()

    suspend fun getFcmToken(): String = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result, null)
            } else {
                cont.resumeWithException(task.exception ?: Exception("FCM token error"))
            }
        }
    }

    suspend fun sendLoginRequest(username0: String, password0: String): Boolean {
        fcmToken = getFcmToken();

        return suspendCancellableCoroutine { cont ->
            val json = JSONObject().apply {
                put("username", username0)
                put("password", password0)
                put("_2FA", true)
                put("usingPhoneApp", true)
                put("images", faces)
                put("fcmToken", fcmToken)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("${LINK_TO_SERVER}/users/login")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("LoginResponse", "Request failed: ${e.message}")
                    cont.resume(false, onCancellation = null)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        if (responseData != null) {
                            val responseJson = JSONObject(responseData)
                            userid = responseJson.getString("_id")
                            username = responseJson.getString("username")
                            password = responseJson.getString("password")
                            email = responseJson.getString("email")
                        }
                        Log.d("LoginResponse", "Response: $responseData")
                        cont.resume(userid != "", onCancellation = null)
                    } else {
                        Log.e("LoginResponse", "Error ${response.code}: ${response.message}")
                        Log.e("LoginResponse", "Error body: ${response.body?.string()}")
                        cont.resume(false, onCancellation = null)
                    }
                }
            })
        }
    }

    suspend fun send2FARequest(): Boolean {
        Log.e("00", "SEND 2FA REQ")

        return suspendCancellableCoroutine { cont ->
            val json = JSONObject().apply {
                put("_2FA", true)
                put("images", faces)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("${LINK_TO_SERVER}/users/login_2fa")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("LoginResponse", "Request failed: ${e.message}")
                    cont.resume(false, onCancellation = null)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        if (responseData != null) {
                            val responseJson = JSONObject(responseData)
                            userid = responseJson.getString("_id")
                            username = responseJson.getString("username")
                            password = responseJson.getString("password")
                            email = responseJson.getString("email")
                        }
                        Log.d("LoginResponse", "Response: $responseData")
                        cont.resume(userid != "", onCancellation = null)
                    } else {
                        Log.e("LoginResponse", "Error ${response.code}: ${response.message}")
                        Log.e("LoginResponse", "Error body: ${response.body?.string()}")
                        cont.resume(false, onCancellation = null)
                    }
                }
            })
        }
    }

    suspend fun sendRegisterRequest(username0: String, password0: String, email0: String): Boolean {
        fcmToken = getFcmToken();

        return suspendCancellableCoroutine { cont ->
            val json = JSONObject().apply {
                put("username", username0)
                put("password", password0)
                put("email", email0)
                put("_2FA", true)
                put("images", faces)
                put("fcmToken", fcmToken)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("${LINK_TO_SERVER}/users/")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("LoginResponse", "Request failed: ${e.message}")
                    cont.resume(false, onCancellation = null)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        if (responseData != null) {
                            val responseJson = JSONObject(responseData)
                            userid = responseJson.getString("_id")
                            username = responseJson.getString("username")
                            password = responseJson.getString("password")
                            email = responseJson.getString("email")
                        }
                        Log.d("LoginResponse", "Response: $responseData")
                        cont.resume(userid != "", onCancellation = null)
                    } else {
                        Log.e("LoginResponse", "Error ${response.code}: ${response.message}")
                        Log.e("LoginResponse", "Error body: ${response.body?.string()}")
                        cont.resume(false, onCancellation = null)
                    }
                }
            })
        }
    }
}