package com.shahsurveyors.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.R
import com.shahsurveyors.myapplication.ui.components.LoadingOverlay
import com.shahsurveyors.myapplication.ui.theme.ErrorRed
import com.shahsurveyors.myapplication.ui.theme.ShahBlack
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGrey
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGrey
import com.shahsurveyors.myapplication.ui.theme.ShahWhite

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onSignupClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    /*
     * LOGIN SUCCESS
     */

    LaunchedEffect(viewModel.isUserLoggedIn) {

        if (viewModel.isUserLoggedIn) {
            onLoginSuccess()
        }
    }

    Scaffold(
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
                    .padding(horizontal = 32.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                /*
                 * LOGO
                 */

                Image(

                    painter =
                        painterResource(
                            id = R.drawable.app_logo
                        ),

                    contentDescription =
                        "SHAH Logo",

                    modifier =
                        Modifier.size(100.dp)
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /*
                 * TITLE
                 */

                Text(

                    text = "Welcome Back",

                    style =
                        MaterialTheme.typography
                            .headlineMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color = ShahBlack
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(

                    text =
                        "Login to your SHAH ERP account",

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color = ShahDarkGrey
                )

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                /*
                 * LOGIN CARD
                 */

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

                            singleLine = true,

                            enabled =
                                !viewModel.isLoading,

                            shape =
                                RoundedCornerShape(12.dp),

                            isError =
                                viewModel.authError != null
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

                            singleLine = true,

                            enabled =
                                !viewModel.isLoading,

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
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        /*
                         * FORGOT PASSWORD
                         */

                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.End
                        ) {

                            TextButton(

                                onClick = {
                                    // Forgot password will be
                                    // connected with Firebase
                                    // in the next authentication step.
                                },

                                enabled =
                                    !viewModel.isLoading
                            ) {

                                Text(
                                    text =
                                        "Forgot Password?",

                                    color =
                                        ShahGreen,

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        /*
                         * LOGIN BUTTON
                         */

                        Button(

                            onClick = {

                                viewModel.login(
                                    email = email,
                                    pass = password
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
                                        "VERIFYING..."
                                    } else {
                                        "LOGIN"
                                    },

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }

                /*
                 * ERROR
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
                            ErrorRed.copy(
                                alpha = 0.08f
                            ),

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
                                ErrorRed,

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
                        Modifier.height(20.dp)
                )

                /*
                 * REQUEST ACCESS
                 */

                TextButton(

                    onClick = onSignupClick,

                    enabled =
                        !viewModel.isLoading
                ) {

                    Text(
                        text =
                            "Don't have an account? ",

                        color =
                            ShahDarkGrey
                    )

                    Text(

                        text =
                            "Request Access",

                        color =
                            ShahGreen,

                        fontWeight =
                            FontWeight.Bold
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
            "Verifying..."
    )
}