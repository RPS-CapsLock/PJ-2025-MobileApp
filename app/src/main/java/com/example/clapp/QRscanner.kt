package com.example.clapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class QRscanner : AppCompatActivity() {

    private lateinit var loadingDialog: ProgressDialog
    private lateinit var playButton: Button
    private var audioFile: File? = null
    private var boxId: Int? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrscanner)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadingDialog = ProgressDialog(this)
        loadingDialog.setMessage("Obdelujem, prosim počakajte...")
        loadingDialog.setCancelable(false)

        val cancelButton = findViewById<Button>(R.id.cancelButton)
        playButton = findViewById(R.id.playButton)
        playButton.visibility = View.GONE // najprej skrit

        cancelButton.setOnClickListener {
            Toast.makeText(this, "Skeniranje preklicano", Toast.LENGTH_SHORT).show()
            finish()
        }

        playButton.setOnClickListener {
            audioFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    playAudio(file)
                } else {
                    Toast.makeText(this, "Zvokovna datoteka je prazna ali ne obstaja!", Toast.LENGTH_LONG).show()
                }
            }
        }

        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Skeniraj QR kodo")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    private fun playAudio(file: File) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    showSuccessDialog()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Napaka pri predvajanju: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Paketnik")
        builder.setMessage("Ali ste uspešno odprli paketnik?")
        builder.setPositiveButton("Da") { dialog, _ ->
            sendLog("opened")
            dialog.dismiss()
        }
        builder.setNegativeButton("Ne") { dialog, _ ->
            sendLog("failed")
            dialog.dismiss()
        }
        builder.show()
    }

    private fun sendLog(status: String) {
        boxId?.let { id ->
            CoroutineScope(Dispatchers.IO).launch {
                val success = ApiBox.logBoxStatus(id, status)
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this@QRscanner, "Log uspešno poslan!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@QRscanner, "Napaka pri pošiljanju loga!", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
            }
        }
    }

    private fun extractBoxIdFromUrl(url: String): Int? {
        val parts = url.split("/")
        return if (parts.size > 4) {
            val rawId = parts[4]
            rawId.trimStart('0').toIntOrNull()
        } else {
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val qrCodeContents = result.contents
                boxId = extractBoxIdFromUrl(qrCodeContents)

                if (boxId != null) {
                    loadingDialog.show()
                    CoroutineScope(Dispatchers.Main).launch {
                        val response = ApiBox.openBox(boxId!!)
                        loadingDialog.dismiss()
                        response.onSuccess { base64Audio ->
                            audioFile = File(filesDir, "sound.mp3")
                            Log.d("DEBUG", "Base64 audio response: $base64Audio")
                            val saved = AudioUtil.saveBase64ToMp3(base64Audio, audioFile!!)
                            if (saved) {
                                playButton.visibility = View.VISIBLE
                                Toast.makeText(this@QRscanner, "Pripravljen za predvajanje!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@QRscanner, "Napaka pri shranjevanju zvoka!", Toast.LENGTH_LONG).show()
                            }
                        }.onFailure { error ->
                            Toast.makeText(this@QRscanner, "Napaka: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Neveljavna QR koda: ni številka", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Skeniranje preklicano", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}
