package com.federico.mylibrary.book

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.federico.mylibrary.R
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun IsbnScannerScreen(
    navController: NavHostController,
    onIsbnDetected: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13)
                .build()
        )
    }
    var detected by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            errorMessage = context.getString(R.string.camera_permission_denied)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission && errorMessage == null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = androidx.camera.core.Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val analyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        analyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                            processImageProxy(scanner, imageProxy) { isbn ->
                                if (!detected) {
                                    detected = true
                                    onIsbnDetected(isbn)
                                }
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analyzer
                            )
                        } catch (e: Exception) {
                            errorMessage = e.message
                            Log.e("CameraX", "Errore binding camera", e)
                        }

                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // Riquadro guida centrato
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(250.dp)
                        .height(100.dp)
                        .border(2.dp, Color.White)
                )
            }
        }

        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = onCancel,
                title = { Text(stringResource(R.string.error_prefix)) },
                text = { Text(errorMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = onCancel) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }

        if (!detected && errorMessage == null && hasPermission) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
@SuppressLint("UnsafeOptInUsageError")
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onIsbnDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val code = barcodes.firstOrNull { it.rawValue?.length == 13 }
                code?.rawValue?.let(onIsbnDetected)
            }
            .addOnFailureListener { Log.e("Scanner", "Errore scanner", it) }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}
