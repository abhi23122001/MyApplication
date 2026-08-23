package com.shahsurveyors.myapplication.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.components.GlassCard
import com.shahsurveyors.myapplication.ui.components.LoadingOverlay

@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dept by remember { mutableStateOf("SURVEY") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create Account", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))
            
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Department", style = MaterialTheme.typography.labelMedium)
                Row {
                    RadioButton(selected = dept == "SURVEY", onClick = { dept = "SURVEY" })
                    Text("Survey", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = dept == "MARKETING", onClick = { dept = "MARKETING" })
                    Text("Marketing", modifier = Modifier.align(Alignment.CenterVertically))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.signup(name, email, password, dept) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SUBMIT REQUEST")
                }
                TextButton(onClick = onBackToLogin) {
                    Text("Back to Login")
                }
            }
            
            viewModel.authError?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    LoadingOverlay(isLoading = viewModel.isLoading, statusText = "Sending Request...")
}
