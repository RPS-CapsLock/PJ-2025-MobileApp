package com.example.clapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
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
        cancelButton.setOnClickListener {
            Toast.makeText(this, "Skeniranje preklicano", Toast.LENGTH_SHORT).show()
            finish()
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

    fun extractBoxIdFromUrl(url: String): Int? {
        val parts = url.split("/")
        return if (parts.size > 4) {
            val rawId = parts[4]
            rawId.trimStart('0').toIntOrNull()
        } else {
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val qrCodeContents = result.contents
                val boxId = extractBoxIdFromUrl(qrCodeContents)
                Toast.makeText(this, "Box ID prebran iz QR kode: $boxId", Toast.LENGTH_LONG).show()
                if (boxId != null) {
                    loadingDialog.show()
                    CoroutineScope(Dispatchers.Main).launch {
                        val response = ApiBox.openBox(boxId)
                        loadingDialog.dismiss()
                        response
                            .onSuccess { base64Audio ->
                                val outputFile = File(this@QRscanner.filesDir, "sound.mp3")
                                val saved = AudioUtil.saveBase64ToMp3(base64Audio, outputFile)
                                if (saved) {
                                    val played = AudioUtil.playMp3File(outputFile)
                                    if (played) {
                                        Toast.makeText(this@QRscanner, "Zvok predvajan uspešno!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(this@QRscanner, "Napaka pri predvajanju zvoka!", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(this@QRscanner, "Napaka pri shranjevanju zvoka!", Toast.LENGTH_LONG).show()
                                }
                            }
                            .onFailure { error ->
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
}
