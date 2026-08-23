package com.shahsurveyors.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.R
import com.shahsurveyors.myapplication.ui.components.GlassCard
import com.shahsurveyors.myapplication.ui.components.LoadingOverlay

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onSignupClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.isUserLoggedIn) {
        if (viewModel.isUserLoggedIn) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Shah Surveyors ERP", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))
            
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("LOGIN")
                }
                TextButton(onClick = onSignupClick) {
                    Text("Don't have an account? Sign Up")
                }
            }
            
            viewModel.authError?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    LoadingOverlay(isLoading = viewModel.isLoading, statusText = "Verifying Credentials...")
}
