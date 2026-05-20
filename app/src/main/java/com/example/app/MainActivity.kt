package com.example.app

import android.content.Intent
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
import com.example.app.Payment.PaymentReturnHandler
import com.example.app.ui.theme.APPTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PaymentReturnHandler.handleIntent(intent)
        setContent {
            APPTheme {
                val context = LocalContext.current
                val authMessage by AuthManager.authMessage.collectAsState()
                val pendingReferencia by PaymentReturnHandler.pendingReferencia.collectAsState()

                LaunchedEffect(authMessage) {
                    authMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        AuthManager.clearAuthMessage()
                    }
                }

                val navController = rememberNavController()

                LaunchedEffect(pendingReferencia) {
                    if (pendingReferencia == null) return@LaunchedEffect
                    val rutaActual = navController.currentDestination?.route.orEmpty()
                    if (!rutaActual.contains("PantallaPagos")) {
                        navController.navigate("PantallaPagos") {
                            launchSingleTop = true
                        }
                    }
                }

                Navegacion(navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        PaymentReturnHandler.handleIntent(intent)
    }
}



