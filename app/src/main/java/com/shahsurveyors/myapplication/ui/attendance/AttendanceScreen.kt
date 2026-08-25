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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shahsurveyors.myapplication.data.AttendanceRepository
import com.shahsurveyors.myapplication.data.StorageRepository
import com.shahsurveyors.myapplication.ui.theme.*
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(uid: String, userName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val attendanceRepository = remember { AttendanceRepository() }
    val storageRepository = remember { StorageRepository() }
    val attendanceViewModel: AttendanceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AttendanceViewModel(attendanceRepository, storageRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    })

    var siteName by remember { mutableStateOf("Main Office / Site A") }
    var cameraPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var locationPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scrollState = rememberScrollState()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraPermission = granted
        if (!granted) cameraError = "Camera permission required"
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        locationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!locationPermission) cameraError = "Location permission required"
    }

    LaunchedEffect(uid) { if (uid.isNotBlank()) attendanceViewModel.checkStatus(uid) }
    LaunchedEffect(Unit) {
        if (!cameraPermission) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        if (!locationPermission) locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Attendance", color = ShahWhite)
                        Text("Daily check-in & field verification", color = ShahWhite.copy(alpha = .72f), style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = { IconButton(onClick = { }) { Icon(Icons.Default.AccessTime, null, tint = ShahWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier.fillMaxSize().padding(padding).background(ShahGrey).verticalScroll(scrollState).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(shape = RoundedCornerShape(18.dp), color = ShahWhite, tonalElevation = 1.dp) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = when (attendanceViewModel.currentStatus) {
                        "PUNCHED IN" -> SuccessGreen.copy(alpha = .12f)
                        "PUNCHED OUT" -> ShahMediumGrey.copy(alpha = .12f)
                        else -> WarningAmber.copy(alpha = .12f)
                    }) {
                        Icon(
                            when (attendanceViewModel.currentStatus) { "PUNCHED IN" -> Icons.Default.CheckCircle; "PUNCHED OUT" -> Icons.Default.TaskAlt; else -> Icons.Default.Schedule },
                            null,
                            tint = when (attendanceViewModel.currentStatus) { "PUNCHED IN" -> SuccessGreen; "PUNCHED OUT" -> ShahMediumGrey; else -> WarningAmber },
                            modifier = Modifier.padding(11.dp).size(25.dp)
                        )
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(userName, style = MaterialTheme.typography.titleMedium)
                        Text("Today's attendance", style = MaterialTheme.typography.bodySmall, color = ShahMediumGrey)
                    }
                    Text(attendanceViewModel.currentStatus, style = MaterialTheme.typography.labelLarge, color = when (attendanceViewModel.currentStatus) { "PUNCHED IN" -> SuccessGreen; "PUNCHED OUT" -> ShahMediumGrey; else -> WarningAmber })
                }
            }

            attendanceViewModel.punchInTime?.let {
                Surface(shape = RoundedCornerShape(14.dp), color = ShahGreen.copy(alpha = .06f)) {
                    Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Login, null, tint = ShahGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Punch In", color = ShahMediumGrey, modifier = Modifier.weight(1f))
                        Text(it, style = MaterialTheme.typography.titleSmall, color = ShahBlack)
                    }
                }
            }

            Text("Work Location", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(value = siteName, onValueChange = { siteName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Work Area / Site Name") }, leadingIcon = { Icon(Icons.Default.LocationOn, null) }, singleLine = true, enabled = attendanceViewModel.currentStatus != "PUNCHED OUT", shape = RoundedCornerShape(14.dp))

            Text("Verification Photo", style = MaterialTheme.typography.titleSmall)
            Box(Modifier.fillMaxWidth().aspectRatio(.86f).clip(RoundedCornerShape(20.dp)).background(Color.Black), contentAlignment = Alignment.Center) {
                if (cameraPermission) {
                    val lifecycleOwner = LocalLifecycleOwner.current
                    AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
                        val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
                        val future = ProcessCameraProvider.getInstance(ctx)
                        future.addListener({
                            try {
                                val provider = future.get()
                                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                                val capture = ImageCapture.Builder().build()
                                imageCapture = capture
                                provider.unbindAll()
                                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, capture)
                            } catch (e: Exception) { cameraError = "Camera error: ${e.localizedMessage ?: "Unable to start camera"}" }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    })
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, null, tint = ShahWhite, modifier = Modifier.size(34.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Camera permission required", color = ShahWhite)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Allow Camera") }
                    }
                }
            }

            if (!locationPermission) {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.errorContainer) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOff, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(8.dp))
                        Text("Location permission is required for attendance.", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                        TextButton(onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }) { Text("Allow") }
                    }
                }
            } else {
                Surface(shape = RoundedCornerShape(14.dp), color = ShahGreen.copy(alpha = .07f)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, null, tint = ShahGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("GPS ready for location verification", color = ShahDarkGreen, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            cameraError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            attendanceViewModel.statusMessage?.let { message ->
                Surface(shape = RoundedCornerShape(14.dp), color = if (message.startsWith("Error")) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (message.startsWith("Error")) Icons.Default.ErrorOutline else Icons.Default.Info, null)
                        Spacer(Modifier.width(8.dp)); Text(message, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !attendanceViewModel.isLoading && cameraPermission && locationPermission && siteName.isNotBlank() && attendanceViewModel.currentStatus != "PUNCHED OUT",
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    val capture = imageCapture
                    if (capture == null) { cameraError = "Camera is not ready"; return@Button }
                    cameraError = null
                    capture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                            image.close()
                            val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                            ContextCompat.getMainExecutor(context).execute { attendanceViewModel.punchAttendance(context, uid, userName, bitmap, siteName) }
                        }
                        override fun onError(exception: ImageCaptureException) { ContextCompat.getMainExecutor(context).execute { cameraError = "Camera error: ${exception.localizedMessage ?: "Unable to capture photo"}" } }
                    })
                }
            ) {
                if (attendanceViewModel.isLoading) CircularProgressIndicator(Modifier.size(22.dp), color = ShahWhite) else Text(when (attendanceViewModel.currentStatus) { "NOT PUNCHED" -> "PUNCH IN"; "PUNCHED IN" -> "PUNCH OUT"; else -> "COMPLETED" })
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
