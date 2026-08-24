package com.shahsurveyors.myapplication.ui.attendance

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shahsurveyors.myapplication.data.AttendanceRepository
import com.shahsurveyors.myapplication.data.StorageRepository
import java.util.concurrent.Executors

@Composable
fun AttendanceScreen(
    uid: String,
    userName: String,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    // ---------------------------------------------------------
    // REPOSITORIES
    // ---------------------------------------------------------

    val attendanceRepository = remember {
        AttendanceRepository()
    }

    val storageRepository = remember {
        StorageRepository()
    }

    // ---------------------------------------------------------
    // VIEWMODEL
    // ---------------------------------------------------------

    val attendanceViewModel: AttendanceViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {

                if (
                    modelClass.isAssignableFrom(
                        AttendanceViewModel::class.java
                    )
                ) {

                    @Suppress("UNCHECKED_CAST")

                    return AttendanceViewModel(
                        attendanceRepository,
                        storageRepository
                    ) as T
                }

                throw IllegalArgumentException(
                    "Unknown ViewModel class"
                )
            }
        }
    )

    // ---------------------------------------------------------
    // LOCAL STATE
    // ---------------------------------------------------------

    var siteName by remember {
        mutableStateOf("Main Office / Site A")
    }

    var cameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var locationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val scrollState = rememberScrollState()

    // ---------------------------------------------------------
    // CAMERA PERMISSION
    // ---------------------------------------------------------

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            cameraPermission = granted

            if (!granted) {
                cameraError = "Camera permission required"
            }
        }

    // ---------------------------------------------------------
    // LOCATION PERMISSION
    // ---------------------------------------------------------

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            locationPermission =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||
                        permissions[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true

            if (!locationPermission) {
                cameraError = "Location permission required"
            }
        }

    // ---------------------------------------------------------
    // CHECK ATTENDANCE STATUS
    // ---------------------------------------------------------

    LaunchedEffect(uid) {

        if (uid.isNotBlank()) {
            attendanceViewModel.checkStatus(uid)
        }
    }

    // ---------------------------------------------------------
    // REQUEST PERMISSIONS
    // ---------------------------------------------------------

    LaunchedEffect(Unit) {

        if (!cameraPermission) {

            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }

        if (!locationPermission) {

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------

    Surface(
        modifier = modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // -------------------------------------------------
            // TITLE
            // -------------------------------------------------

            Text(
                text = "Daily Attendance",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            // -------------------------------------------------
            // ATTENDANCE STATUS
            // -------------------------------------------------

            Text(
                text = "Status: ${attendanceViewModel.currentStatus}",
                style = MaterialTheme.typography.titleMedium
            )

            attendanceViewModel.punchInTime?.let { time ->

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Punch In: $time",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // -------------------------------------------------
            // SITE NAME
            // -------------------------------------------------

            OutlinedTextField(
                value = siteName,
                onValueChange = {
                    siteName = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Work Area / Site Name")
                },
                singleLine = true,
                enabled =
                    attendanceViewModel.currentStatus !=
                            "PUNCHED OUT"
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // -------------------------------------------------
            // CAMERA PREVIEW
            // -------------------------------------------------

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {

                if (cameraPermission) {

                    val lifecycleOwner = LocalLifecycleOwner.current

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),

                        factory = { ctx ->

                            val previewView =
                                PreviewView(ctx).apply {

                                    scaleType =
                                        PreviewView.ScaleType
                                            .FILL_CENTER
                                }

                            val cameraProviderFuture =
                                ProcessCameraProvider
                                    .getInstance(ctx)

                            cameraProviderFuture.addListener({

                                try {

                                    val cameraProvider =
                                        cameraProviderFuture.get()

                                    val preview =
                                        Preview.Builder()
                                            .build()

                                    preview.setSurfaceProvider(
                                        previewView.surfaceProvider
                                    )

                                    val capture =
                                        ImageCapture.Builder()
                                            .build()

                                    imageCapture = capture

                                    cameraProvider.unbindAll()

                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_FRONT_CAMERA,
                                        preview,
                                        capture
                                    )

                                } catch (e: Exception) {

                                    e.printStackTrace()

                                    cameraError =
                                        "Camera error: ${
                                            e.localizedMessage
                                                ?: "Unable to start camera"
                                        }"
                                }

                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        }
                    )

                } else {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Camera permission required",
                            color = Color.White
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {

                                cameraPermissionLauncher
                                    .launch(
                                        Manifest.permission.CAMERA
                                    )
                            }
                        ) {

                            Text("Allow Camera")
                        }
                    }
                }
            }

            // -------------------------------------------------
            // CAMERA ERROR
            // -------------------------------------------------

            cameraError?.let { error ->

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // -------------------------------------------------
            // GPS STATUS
            // -------------------------------------------------

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            if (!locationPermission) {

                Text(
                    text = "Location permission required",
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Button(
                    onClick = {

                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {

                    Text("Allow Location")
                }

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "GPS Ready",
                        color =
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // -------------------------------------------------
            // STATUS MESSAGE
            // -------------------------------------------------

            attendanceViewModel.statusMessage?.let { message ->

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (message.startsWith("Error")) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = message,
                        color =
                            if (message.startsWith("Error")) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // -------------------------------------------------
            // PUNCH BUTTON
            // -------------------------------------------------

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                enabled =
                    !attendanceViewModel.isLoading &&
                            cameraPermission &&
                            locationPermission &&
                            siteName.isNotBlank() &&
                            attendanceViewModel.currentStatus !=
                            "PUNCHED OUT",

                onClick = {

                    val capture = imageCapture

                    if (capture == null) {

                        cameraError =
                            "Camera is not ready"

                        return@Button
                    }

                    cameraError = null

                    capture.takePicture(
                        cameraExecutor,

                        object :
                            ImageCapture.OnImageCapturedCallback() {

                            override fun onCaptureSuccess(
                                image:
                                androidx.camera.core.ImageProxy
                            ) {

                                image.close()

                                // Keep Bitmap parameter
                                // compatible with ViewModel.
                                val bitmap =
                                    Bitmap.createBitmap(
                                        1,
                                        1,
                                        Bitmap.Config.ARGB_8888
                                    )

                                ContextCompat
                                    .getMainExecutor(context)
                                    .execute {

                                        attendanceViewModel
                                            .punchAttendance(
                                                context = context,
                                                uid = uid,
                                                userName = userName,
                                                bitmap = bitmap,
                                                siteName = siteName
                                            )
                                    }
                            }

                            override fun onError(
                                exception:
                                ImageCaptureException
                            ) {

                                ContextCompat
                                    .getMainExecutor(context)
                                    .execute {

                                        cameraError =
                                            "Camera error: ${
                                                exception.localizedMessage
                                                    ?: "Unable to capture photo"
                                            }"
                                    }
                            }
                        }
                    )
                }
            ) {

                if (attendanceViewModel.isLoading) {

                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(22.dp),
                        color = Color.White
                    )

                } else {

                    Text(
                        text =
                            when (
                                attendanceViewModel.currentStatus
                            ) {

                                "NOT PUNCHED" ->
                                    "PUNCH IN"

                                "PUNCHED IN" ->
                                    "PUNCH OUT"

                                else ->
                                    "COMPLETED"
                            }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}