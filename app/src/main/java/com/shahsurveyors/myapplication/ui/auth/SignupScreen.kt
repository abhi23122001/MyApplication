package com.shahsurveyors.myapplication.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.components.LoadingOverlay
import com.shahsurveyors.myapplication.ui.theme.ErrorRed
import com.shahsurveyors.myapplication.ui.theme.ShahBlack
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGrey
import com.shahsurveyors.myapplication.ui.theme.ShahMediumGrey
import com.shahsurveyors.myapplication.ui.theme.ShahWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var dept by remember {
        mutableStateOf("SURVEY")
    }

    val isSuccessMessage =
        viewModel.authError?.contains(
            "Registration submitted successfully",
            ignoreCase = true
        ) == true

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Request Access",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackToLogin,
                        enabled = !viewModel.isLoading
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShahWhite
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = ShahDarkGreen
                    )
            )
        },

        containerColor = ShahGrey

    ) { paddingValues ->

        Box(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),

            contentAlignment = Alignment.Center
        ) {

            Column(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "New Account Request",
                    style =
                        MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ShahBlack
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text =
                        "Admin approval is required before login.",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color = ShahMediumGrey
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(16.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor = ShahWhite
                        ),

                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                ) {

                    Column(

                        modifier =
                            Modifier.padding(24.dp)
                    ) {

                        /*
                         * FULL NAME
                         */

                        OutlinedTextField(

                            value = name,

                            onValueChange = {
                                name = it
                            },

                            label = {
                                Text("Full Name")
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            enabled =
                                !viewModel.isLoading,

                            singleLine = true,

                            shape =
                                RoundedCornerShape(12.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        /*
                         * EMAIL
                         */

                        OutlinedTextField(

                            value = email,

                            onValueChange = {
                                email = it
                            },

                            label = {
                                Text("Email Address")
                            },

                            placeholder = {
                                Text("example@email.com")
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            enabled =
                                !viewModel.isLoading,

                            singleLine = true,

                            shape =
                                RoundedCornerShape(12.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        /*
                         * PASSWORD
                         */

                        OutlinedTextField(

                            value = password,

                            onValueChange = {
                                password = it
                            },

                            label = {
                                Text("Password")
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            enabled =
                                !viewModel.isLoading,

                            singleLine = true,

                            shape =
                                RoundedCornerShape(12.dp),

                            visualTransformation =
                                if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },

                            trailingIcon = {

                                IconButton(

                                    onClick = {
                                        passwordVisible =
                                            !passwordVisible
                                    },

                                    enabled =
                                        !viewModel.isLoading
                                ) {

                                    Icon(

                                        imageVector =
                                            if (passwordVisible) {
                                                Icons.Default.VisibilityOff
                                            } else {
                                                Icons.Default.Visibility
                                            },

                                        contentDescription =
                                            if (passwordVisible) {
                                                "Hide password"
                                            } else {
                                                "Show password"
                                            }
                                    )
                                }
                            },

                            supportingText = {

                                Text(
                                    text =
                                        "Minimum 6 characters"
                                )
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        /*
                         * DEPARTMENT
                         */

                        Text(

                            text = "Department",

                            style =
                                MaterialTheme.typography
                                    .labelMedium,

                            color = ShahGreen,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            RadioButton(

                                selected =
                                    dept == "SURVEY",

                                onClick = {
                                    dept = "SURVEY"
                                },

                                enabled =
                                    !viewModel.isLoading,

                                colors =
                                    RadioButtonDefaults.colors(
                                        selectedColor =
                                            ShahGreen
                                    )
                            )

                            Text(
                                text = "Survey",
                                style =
                                    MaterialTheme.typography
                                        .bodyMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(16.dp)
                            )

                            RadioButton(

                                selected =
                                    dept == "MARKETING",

                                onClick = {
                                    dept = "MARKETING"
                                },

                                enabled =
                                    !viewModel.isLoading,

                                colors =
                                    RadioButtonDefaults.colors(
                                        selectedColor =
                                            ShahGreen
                                    )
                            )

                            Text(
                                text = "Marketing",
                                style =
                                    MaterialTheme.typography
                                        .bodyMedium
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(24.dp)
                        )

                        /*
                         * SUBMIT
                         */

                        Button(

                            onClick = {

                                viewModel.signup(
                                    name = name,
                                    email = email,
                                    pass = password,
                                    dept = dept
                                )
                            },

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),

                            enabled =
                                !viewModel.isLoading,

                            shape =
                                RoundedCornerShape(12.dp),

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        ShahGreen,

                                    contentColor =
                                        ShahWhite
                                )
                        ) {

                            Text(

                                text =
                                    if (viewModel.isLoading) {
                                        "SUBMITTING..."
                                    } else {
                                        "SUBMIT REQUEST"
                                    },

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }

                /*
                 * RESULT MESSAGE
                 */

                if (
                    !viewModel.authError
                        .isNullOrBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Surface(

                        color =
                            if (isSuccessMessage) {
                                ShahGreen.copy(
                                    alpha = 0.10f
                                )
                            } else {
                                ErrorRed.copy(
                                    alpha = 0.08f
                                )
                            },

                        shape =
                            RoundedCornerShape(10.dp),

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(

                            text =
                                viewModel.authError
                                    ?: "",

                            color =
                                if (isSuccessMessage) {
                                    ShahGreen
                                } else {
                                    ErrorRed
                                },

                            modifier =
                                Modifier.padding(12.dp),

                            style =
                                MaterialTheme.typography
                                    .bodySmall,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                TextButton(

                    onClick = onBackToLogin,

                    enabled =
                        !viewModel.isLoading
                ) {

                    Text(
                        text = "Already have an account? ",
                        color = ShahMediumGrey
                    )

                    Text(
                        text = "Login",
                        color = ShahGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    /*
     * LOADING
     */

    LoadingOverlay(

        isLoading =
            viewModel.isLoading,

        statusText =
            "Creating account..."
    )
}