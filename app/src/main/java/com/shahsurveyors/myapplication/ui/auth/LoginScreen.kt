package com.shahsurveyors.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.R
import com.shahsurveyors.myapplication.ui.theme.*

@Composable
fun LoginScreen(viewModel: AuthViewModel, onSignupClick: () -> Unit, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.isUserLoggedIn) {
        if (viewModel.isUserLoggedIn) onLoginSuccess()
    }

    Scaffold(containerColor = ShahGrey) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding).background(ShahGrey)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 34.dp, bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ShahDarkGreen,
                    shadowElevation = 8.dp
                ) {
                    Image(
                        painterResource(R.drawable.app_logo),
                        "SHAH Logo",
                        Modifier.padding(15.dp).size(72.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ShahBlack
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Sign in to your SHAH ERP account",
                    color = ShahDarkGrey,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ShahWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ShahGreen.copy(alpha = .10f)
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    null,
                                    tint = ShahGreen,
                                    modifier = Modifier.padding(8.dp).size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Account Login", fontWeight = FontWeight.Bold, color = ShahBlack)
                                Text("Use your registered credentials", fontSize = 10.sp, color = ShahMediumGrey)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("example@email.com") },
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !viewModel.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            isError = !viewModel.authError.isNullOrBlank()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !viewModel.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !viewModel.isLoading) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        "Toggle password"
                                    )
                                }
                            }
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = {}, enabled = !viewModel.isLoading) {
                                Text("Forgot Password?", color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Button(
                            onClick = { viewModel.login(email.trim(), password) },
                            enabled = !viewModel.isLoading && email.isNotBlank() && password.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShahGreen)
                        ) {
                            if (viewModel.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = ShahWhite, strokeWidth = 2.dp)
                            else Text("SIGN IN", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Don't have an account?", color = ShahDarkGrey, fontSize = 12.sp)
                            TextButton(onClick = onSignupClick, enabled = !viewModel.isLoading) {
                                Text("Create Account", color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
                if (!viewModel.authError.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = ErrorRed.copy(alpha = .08f)
                    ) {
                        Text(
                            viewModel.authError.orEmpty(),
                            modifier = Modifier.padding(12.dp),
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Secure access • SHAH Surveyors ERP", style = MaterialTheme.typography.labelSmall, color = ShahMediumGrey)
            }
        }
    }
}
