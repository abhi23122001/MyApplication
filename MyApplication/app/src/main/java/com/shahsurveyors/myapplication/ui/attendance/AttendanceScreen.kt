package com.shahsurveyors.myapplication.ui.attendance

import android.Manifest
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.shahsurveyors.myapplication.ui.components.LoadingOverlay

@Composable
fun AttendanceScreen(viewModel: AttendanceViewModel, staffName: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
        var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val executor = ContextCompat.getMainExecutor(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        imageCapture = ImageCapture.Builder().build()

                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            Log.e("Camera", "Use case binding failed", e)
                        }
                    }, executor)
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Button(
                onClick = {
                    val capture = imageCapture ?: return@Button
                    capture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val buffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.capacity())
                                buffer.get(bytes)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                viewModel.punchAttendance(context, bitmap, staffName)
                                image.close()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("Camera", "Capture failed", exception)
                            }
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            ) {
                Text("PUNCH ATTENDANCE")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission required")
        }
    }

    LoadingOverlay(isLoading = viewModel.isLoading, statusText = viewModel.statusMessage ?: "Processing...")
}
