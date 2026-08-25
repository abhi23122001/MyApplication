package com.shahsurveyors.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import com.shahsurveyors.myapplication.ui.theme.*

@Composable
fun LoginScreen(viewModel: AuthViewModel,onSignupClick:()->Unit,onLoginSuccess:()->Unit){
    var email by remember{mutableStateOf("")}
    var password by remember{mutableStateOf("")}
    var passwordVisible by remember{mutableStateOf(false)}
    LaunchedEffect(viewModel.isUserLoggedIn){if(viewModel.isUserLoggedIn)onLoginSuccess()}
    Scaffold(containerColor=ShahGrey){padding->Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
        Surface(shape=RoundedCornerShape(24.dp),color=ShahDarkGreen){Image(painterResource(R.drawable.app_logo),"SHAH Logo",Modifier.padding(16.dp).size(76.dp))}
        Spacer(Modifier.height(14.dp));Text("Welcome Back",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold,color=ShahBlack);Spacer(Modifier.height(4.dp));Text("Sign in to your SHAH ERP account",color=ShahDarkGrey);Spacer(Modifier.height(22.dp))
        Card(Modifier.fillMaxWidth(),RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=ShahWhite),elevation=CardDefaults.cardElevation(3.dp)){Column(Modifier.padding(20.dp)){Text("Account Login",fontWeight=FontWeight.Bold,color=ShahGreen,style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(14.dp))
            OutlinedTextField(email,{email=it},label={Text("Email Address")},placeholder={Text("example@email.com")},leadingIcon={Icon(Icons.Default.Email,null)},modifier=Modifier.fillMaxWidth(),singleLine=true,enabled=!viewModel.isLoading,shape=RoundedCornerShape(14.dp),isError=!viewModel.authError.isNullOrBlank())
            Spacer(Modifier.height(12.dp));OutlinedTextField(password,{password=it},label={Text("Password")},leadingIcon={Icon(Icons.Default.Lock,null)},modifier=Modifier.fillMaxWidth(),singleLine=true,enabled=!viewModel.isLoading,shape=RoundedCornerShape(14.dp),visualTransformation=if(passwordVisible)VisualTransformation.None else PasswordVisualTransformation(),trailingIcon={IconButton(onClick={passwordVisible=!passwordVisible},enabled=!viewModel.isLoading){Icon(if(passwordVisible)Icons.Default.VisibilityOff else Icons.Default.Visibility,"Toggle password")}})
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){TextButton(onClick={},enabled=!viewModel.isLoading){Text("Forgot Password?",color=ShahGreen,fontWeight=FontWeight.Bold)}}
            Button(onClick={viewModel.login(email=email.trim(),pass=password)},enabled=!viewModel.isLoading&&email.isNotBlank()&&password.isNotBlank(),modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=ShahGreen)){if(viewModel.isLoading)CircularProgressIndicator(Modifier.size(20.dp),color=ShahWhite,strokeWidth=2.dp)else Text("SIGN IN",fontWeight=FontWeight.ExtraBold)}
            Spacer(Modifier.height(8.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){Text("Don't have an account?",color=ShahDarkGrey,style=MaterialTheme.typography.bodySmall);TextButton(onClick=onSignupClick,enabled=!viewModel.isLoading){Text("Create Account",color=ShahGreen,fontWeight=FontWeight.Bold)}}
        }
        if(!viewModel.authError.isNullOrBlank()){Spacer(Modifier.height(12.dp));Text(viewModel.authError!!,color=ErrorRed,style=MaterialTheme.typography.bodySmall)}
        Spacer(Modifier.height(12.dp));Text("Secure access • SHAH Surveyors ERP",style=MaterialTheme.typography.labelSmall,color=ShahMediumGrey)
    }}
}
