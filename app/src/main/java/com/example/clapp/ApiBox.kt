package com.example.clapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import android.util.Log

object ApiBox {

    private const val BASE_URL = "https://api-d4me-stage.direct4.me/"
    private const val ACCESS_TOKEN = "9ea96945-3a37-4638-a5d4-22e89fbc998f"
    private val client = OkHttpClient()

    suspend fun openBox(boxId: Int): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("deliveryId", 0)
                    put("boxId", boxId)
                    put("tokenFormat", 5)
                    put("latitude", 0)
                    put("longitude", 0)
                    put("qrCodeInfo", "string")
                    put("terminalSeed", 0)
                    put("isMultibox", false)
                    put("doorIndex", 0)
                    put("addAccessLog", false)
                }

                val mediaType = "application/json".toMediaTypeOrNull()
                val requestBody = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(BASE_URL + "sandbox/v1/Access/openbox")
                    .addHeader("Authorization", "Bearer $ACCESS_TOKEN")
                    .addHeader("Accept", "application/json")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val json = JSONObject(body)
                    val base64Audio = json.getString("data")
                    Result.success(base64Audio)
                } else {
                    Result.failure(Exception("Napaka: ${response.code}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun logBoxStatus(boxId: Int, status: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("status", status)
                }

                val mediaType = "application/json".toMediaTypeOrNull()
                val requestBody = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("http://164.8.160.246:3001/paketniki/68449b3edee2b80319648f50/log")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ApiBox", "Napaka pri pošiljanju loga", e)
                e.printStackTrace()
                false
            }
        }
    }
}
