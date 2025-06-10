package com.example.clapp

import android.media.MediaPlayer
import android.util.Base64
import java.io.File
import java.io.FileOutputStream

object AudioUtil {

    fun saveBase64ToMp3(base64String: String, outputFile: File): Boolean {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            FileOutputStream(outputFile).use { it.write(decodedBytes) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun playMp3File(file: File): Boolean {
        return try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            mediaPlayer.prepare()
            mediaPlayer.start()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
