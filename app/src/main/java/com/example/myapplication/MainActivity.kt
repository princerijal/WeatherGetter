package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }

    @Composable
    fun AppNavigation() {
        val weatherView = ViewModelProvider(this)[WeatherViewModel::class.java]
        val context = LocalContext.current
        val locationUtils = LocationUtils(context)
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") { SplashScreen(navController) }
            composable("main") { WeatherPage(weatherView, locationUtils, context) }
        }
    }

    @Composable
    fun SplashScreen(navController: NavController) {
        LaunchedEffect(Unit) {
            delay(2000L)
            navController.navigate("main") { popUpTo("splash") { inclusive = true } }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Display the logo from drawable
            Image(
                painter = painterResource(id = R.drawable.logo), // Replace with your actual drawable name
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp) // Adjust size as needed
            )
        }
    }


}