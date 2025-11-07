package com.example.midterm

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SignIn(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val (focusUsername, focusPassword) = remember { FocusRequester.createRefs() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F4)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hình đầu trang
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.35f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.highlands_signin),
                contentDescription = "Banner Trà sữa",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.size(30.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chào mừng trở lại 🍃",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4C1D95)
            )
            Text(
                text = "Đăng nhập để quản lý Trà sữa!",
                fontSize = 16.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.size(25.dp))

            // Email
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .focusRequester(focusUsername),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusPassword.requestFocus() }),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White,
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF8B5CF6)
                ),
                label = { Text(text = "Email", color = Color(0xFF8B5CF6)) }
            )

            Spacer(modifier = Modifier.size(10.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .focusRequester(focusPassword),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Toggle password",
                            tint = Color(0xFF8B5CF6)
                        )
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White,
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF8B5CF6)
                ),
                label = { Text(text = "Mật khẩu", color = Color(0xFF8B5CF6)) }
            )

            Spacer(modifier = Modifier.size(25.dp))

            // Nút đăng nhập
            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        firebaseAuth.signInWithEmailAndPassword(username, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // ✅ Điều hướng sang HomeScreen qua NavController
                                    navController.navigate(Screen.Home.rout) {
                                        popUpTo(Screen.Signin.rout) { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Đăng nhập thất bại: ${task.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show()
                    }
                }
                ,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
            ) {
                Text("Đăng nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.size(15.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Chưa có tài khoản?")
                TextButton(onClick = { navController.navigate(Screen.Signup.rout) }) {
                    Text(
                        text = "Đăng ký ngay!",
                        color = Color(0xFF8B5CF6),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
