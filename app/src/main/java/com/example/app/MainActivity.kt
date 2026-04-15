package com.example.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.app.Auth.AuthManager
import com.example.app.ui.theme.APPTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            APPTheme {
                val context = LocalContext.current
                val authMessage by AuthManager.authMessage.collectAsState()

                LaunchedEffect(authMessage) {
                    authMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        AuthManager.clearAuthMessage()
                    }
                }

                val navController = rememberNavController()
                Navegacion(navController = navController)
            }
        }
    }
}



