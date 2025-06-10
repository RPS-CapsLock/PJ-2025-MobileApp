
package com.example.clapp.loginUtil

import android.util.Log
import com.example.clapp.MyApp
package com.example.clapp

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message received: ${remoteMessage.data}")

        remoteMessage.notification?.let  {
            val title = it.title ?: "Title"
            val body = it.body ?: "Message body"
            MyApp().showNotification(applicationContext, title, body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
    }
}
