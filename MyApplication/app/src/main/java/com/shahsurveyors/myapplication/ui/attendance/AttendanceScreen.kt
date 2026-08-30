package com.shahsurveyors.myapplication.ui.attendance

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.shahsurveyors.myapplication.ui.theme.ErrorRed
import com.shahsurveyors.myapplication.ui.theme.ShahBlack
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGrey
import com.shahsurveyors.myapplication.ui.theme.ShahMediumGrey
import com.shahsurveyors.myapplication.ui.theme.ShahWhite
import com.shahsurveyors.myapplication.ui.theme.SuccessGreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    staffName: String,
    onBack: () -> Unit = {}
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(false)
    }

    var hasLocationPermission by remember {
        mutableStateOf(false)
    }

    var permissionRequested by remember {
        mutableStateOf(false)
    }

    var workArea by remember {
        mutableStateOf("Main Office / Site A")
    }

    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            hasCameraPermission =
                permissions[Manifest.permission.CAMERA] == true

            hasLocationPermission =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            permissionRequested = true
        }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daily Attendance",
                        color = ShahWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShahWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShahDarkGreen
                )
            )
        },
        containerColor = ShahGrey
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // =====================================================
            // EMPLOYEE HEADER
            // =====================================================

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ShahWhite
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = ShahGreen.copy(alpha = 0.12f)
                    ) {

                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = ShahGreen,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(14.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = staffName,
                            color = ShahBlack,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )

                        Text(
                            text = viewModel.currentStatus,
                            color =
                                if (
                                    viewModel.currentStatus == "PUNCHED IN"
                                ) {
                                    SuccessGreen
                                } else {
                                    ShahMediumGrey
                                },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (viewModel.punchInTime != null) {

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {

                            Text(
                                text = "IN TIME",
                                color = ShahMediumGrey,
                                fontSize = 10.sp
                            )

                            Text(
                                text = viewModel.punchInTime ?: "",
                                color = ShahBlack,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }


            // =====================================================
            // CAMERA AREA
            // =====================================================

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black)
            ) {

                if (
                    hasCameraPermission &&
                    hasLocationPermission
                ) {

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),

                        factory = { ctx ->

                            val previewView =
                                PreviewView(ctx)

                            val cameraProviderFuture =
                                ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener(

                                {

                                    try {

                                        val cameraProvider =
                                            cameraProviderFuture.get()

                                        val preview =
                                            Preview.Builder()
                                                .build()
                                                .also { previewUseCase ->

                                                    previewUseCase
                                                        .setSurfaceProvider(
                                                            previewView
                                                                .surfaceProvider
                                                        )
                                                }

                                        val capture =
                                            ImageCapture.Builder()
                                                .setCaptureMode(
                                                    ImageCapture
                                                        .CAPTURE_MODE_MINIMIZE_LATENCY
                                                )
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

                                        Log.e(
                                            "AttendanceCamera",
                                            "Camera setup failed",
                                            e
                                        )
                                    }

                                },

                                ContextCompat.getMainExecutor(ctx)
                            )

                            previewView
                        }
                    )


                    // =================================================
                    // GPS STATUS
                    // =================================================

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(14.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(10.dp)
                    ) {

                        Row(
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 7.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(6.dp)
                            )

                            Text(
                                text = "GPS Active",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }


                    // =================================================
                    // PUNCH BUTTON
                    // =================================================

                    FloatingActionButton(

                        onClick = {

                            if (viewModel.isLoading) {
                                return@FloatingActionButton
                            }

                            val capture =
                                imageCapture
                                    ?: return@FloatingActionButton

                            captureAttendancePhoto(
                                context = context,
                                imageCapture = capture,
                                viewModel = viewModel,
                                staffName = staffName,
                                workArea = workArea
                            )
                        },

                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(20.dp),

                        containerColor =
                            if (
                                viewModel.currentStatus ==
                                "PUNCHED IN"
                            ) {
                                ErrorRed
                            } else {
                                ShahGreen
                            },

                        contentColor = ShahWhite

                    ) {

                        Text(
                            text =
                                if (
                                    viewModel.currentStatus ==
                                    "PUNCHED IN"
                                ) {
                                    "OUT"
                                } else {
                                    "IN"
                                },
                            fontWeight = FontWeight.Bold
                        )
                    }

                } else {

                    // =================================================
                    // PERMISSION SCREEN
                    // =================================================

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally,

                        verticalArrangement =
                            Arrangement.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.CameraAlt,

                            contentDescription = null,

                            tint = ShahWhite,

                            modifier =
                                Modifier.size(50.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        Text(
                            text =
                                if (permissionRequested) {
                                    "Camera and Location permission required"
                                } else {
                                    "Requesting permissions..."
                                },

                            color = ShahWhite,
                            fontSize = 14.sp
                        )

                        if (permissionRequested) {

                            Spacer(
                                modifier =
                                    Modifier.height(16.dp)
                            )

                            Button(

                                onClick = {

                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },

                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            ShahGreen
                                    )

                            ) {

                                Text(
                                    text =
                                        "ALLOW PERMISSIONS"
                                )
                            }
                        }
                    }
                }
            }


            // =====================================================
            // WORK AREA
            // =====================================================

            Column(
                modifier =
                    Modifier.padding(16.dp)
            ) {

                OutlinedTextField(

                    value =
                        workArea,

                    onValueChange = {
                        workArea = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine = true,

                    label = {
                        Text(
                            text =
                                "Work Area / Site Name"
                        )
                    },

                    leadingIcon = {

                        Icon(
                            imageVector =
                                Icons.Default.LocationOn,

                            contentDescription = null,

                            tint =
                                ShahGreen
                        )
                    },

                    shape =
                        RoundedCornerShape(12.dp)
                )


                // =================================================
                // PUNCH OUT SUMMARY
                // =================================================

                if (
                    viewModel.currentStatus ==
                    "PUNCHED OUT"
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Surface(

                        modifier =
                            Modifier.fillMaxWidth(),

                        color =
                            SuccessGreen.copy(
                                alpha = 0.10f
                            ),

                        shape =
                            RoundedCornerShape(12.dp)

                    ) {

                        Column(

                            modifier =
                                Modifier.padding(14.dp),

                            horizontalAlignment =
                                Alignment.CenterHorizontally

                        ) {

                            Text(
                                text =
                                    "Total Working Hours",

                                color =
                                    SuccessGreen,

                                fontSize = 11.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    viewModel.totalHoursToday,

                                color =
                                    ShahBlack,

                                fontSize = 22.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }


                // =================================================
                // STATUS MESSAGE
                // =================================================

                if (
                    !viewModel.statusMessage.isNullOrBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            viewModel.statusMessage ?: "",

                        modifier =
                            Modifier.fillMaxWidth(),

                        color =
                            ShahMediumGrey,

                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}


// =================================================================
// CAPTURE ATTENDANCE PHOTO
// =================================================================

private fun captureAttendancePhoto(

    context: Context,

    imageCapture: ImageCapture,

    viewModel: AttendanceViewModel,

    staffName: String,

    workArea: String

) {

    val photoFile =
        File.createTempFile(
            "attendance_",
            ".jpg",
            context.cacheDir
        )

    val outputOptions =
        ImageCapture.OutputFileOptions
            .Builder(photoFile)
            .build()

    imageCapture.takePicture(

        outputOptions,

        ContextCompat.getMainExecutor(context),

        object :
            ImageCapture.OnImageSavedCallback {

            override fun onImageSaved(
                outputFileResults:
                ImageCapture.OutputFileResults
            ) {

                try {

                    val bitmap =
                        BitmapFactory.decodeFile(
                            photoFile.absolutePath
                        )

                    if (bitmap == null) {

                        Log.e(
                            "AttendanceCamera",
                            "Bitmap decoding failed"
                        )

                        return
                    }

                    viewModel.punchAttendance(

                        context = context,

                        bitmap = bitmap,

                        staffName = staffName,

                        workArea = workArea
                    )

                } catch (e: Exception) {

                    Log.e(
                        "AttendanceCamera",
                        "Photo processing failed",
                        e
                    )

                } finally {

                    photoFile.delete()
                }
            }

            override fun onError(
                exception: ImageCaptureException
            ) {

                Log.e(
                    "AttendanceCamera",
                    "Photo capture failed",
                    exception
                )
            }
        }
    )
}