package com.example.clapp

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.clapp.ui.theme.CLAppTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.BitmapFactory
import com.example.clapp.loginUtil.LoginUtil

class FaceScanActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private val faceBase64List = ArrayList<String>()
    private var faceCount = 0
    private val maxFaces = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            CLAppTheme {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    AndroidView(
                        factory = { context ->
                            val previewView = PreviewView(context)
                            startCamera(previewView)
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "Scanning for face...",
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
        }
    }

    private fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, FaceAnalyzer())
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("FaceScan", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private inner class FaceAnalyzer : ImageAnalysis.Analyzer {
        private val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .enableTracking()
            .build()
        private val detector = FaceDetection.getClient(options)

        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            if (faceCount >= maxFaces) {
                imageProxy.close()
                return
            }

            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                detector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            val bitmap = imageProxyToBitmap(imageProxy)
                            if (bitmap != null) {
                                for (face in faces) {
                                    if (faceCount < maxFaces) {
                                        val faceBitmap = cropFace(bitmap, face.boundingBox)
                                        val base64 = bitmapToBase64(faceBitmap)
                                        faceBase64List.add(base64)
                                        faceCount++
                                        Log.d("FaceScan", "Captured face $faceCount")
                                        if (faceCount == maxFaces) {
                                            saveFacesToJson()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FaceScan", "Face detection failed", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }

        private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
            val buffer = imageProxy.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            val yuvImage = android.graphics.YuvImage(
                data,
                android.graphics.ImageFormat.NV21,
                imageProxy.width, imageProxy.height,
                null
            )
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                android.graphics.Rect(0, 0, imageProxy.width, imageProxy.height),
                100,
                out
            )
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        private fun cropFace(bitmap: Bitmap, rect: Rect): Bitmap {
            val x = rect.left.coerceAtLeast(0)
            val y = rect.top.coerceAtLeast(0)
            val width = rect.width().coerceAtMost(bitmap.width - x)
            val height = rect.height().coerceAtMost(bitmap.height - y)
            return Bitmap.createBitmap(bitmap, x, y, width, height)
        }

        private fun bitmapToBase64(bitmap: Bitmap): String {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.NO_WRAP)
        }

        private fun saveFacesToJson() {
            val jsonArray = JSONArray(faceBase64List)
            LoginUtil.faces = jsonArray;
            runOnUiThread {
                ProcessCameraProvider.getInstance(this@FaceScanActivity).get().unbindAll()

                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
