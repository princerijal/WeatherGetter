package com.example.myapplication

import SavedWeather
import WeatherDatabaseHelper
import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.api.NetworkResponse
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

        val dbHelper = WeatherDatabaseHelper(context)
        val locationUtils = LocationUtils(context)
        val lViewModel = ViewModelProvider(this)[LocationViewModel::class.java]

        val navController = rememberNavController()
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                // Main content area
                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("splash") { SplashScreen(navController) }
                        composable("home") { HomeScreen(
                            locationUtils,
                            context,
                            lViewModel,
                            weatherView
                        ) }
                        composable("search") { SearchScreen(
                            viewModel = weatherView
                        ) }
                        composable("saved") { SavedScreen(
                            context = context,
                            dbHelper = dbHelper
                        ) }
                        composable("main") {
                            WeatherPage(
                                weatherView,
                                locationUtils,
                                context,
                                lViewModel = lViewModel

                            )
                        }
                    }
                }

                // Bottom navigation bar
                BottomNavigationBar(navController)
            }
        }
    }

    @Composable
    fun SplashScreen(navController: NavController) {
        LaunchedEffect(Unit) {
            delay(2000L)
            navController.navigate("home") { popUpTo("splash") { inclusive = true } }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo), // Replace with your actual drawable name
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp) // Adjust size as needed
            )
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable


    fun HomeScreen(
        locationUtils: LocationUtils,
        context: Context,
        lViewModel: LocationViewModel,
        viewModel: WeatherViewModel,
        modifier: Modifier = Modifier
    ) {
        val location = lViewModel.location.value
        var city: String by remember { mutableStateOf("") }
        val address = location?.let { locationUtils.reverseGeocodeLocation(it) }
        city = address ?: ""

        val weatherResult = viewModel.weatherResult.observeAsState()
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                ) {
                    // Permission granted, can now use the location
                    locationUtils.requestLocationUpdates(lViewModel)
                } else {
                    val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )

                    val message = if (rationalRequired) {
                        "Location Permission is required to use current location"
                    } else {
                        "Please change the location permission from settings"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        )

        // Automatically fetch location and weather details when the screen opens
        LaunchedEffect(Unit) {
            if (locationUtils.hasLocationPermission(context)) {
                locationUtils.requestLocationUpdates(lViewModel)
                lViewModel.location.value?.let { loc ->
                    val updatedCity = locationUtils.reverseGeocodeLocation(loc)
                    updatedCity?.let {
                        city = it
                        viewModel.getData(city)
                    }
                }
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "WeatherGetter",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .padding(8.dp)
                                .size(32.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Blue,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .padding(9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val result = weatherResult.value) {
                    is NetworkResponse.Error -> {
                        Text(text = "Error: ${result.message}")
                    }
                    NetworkResponse.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is NetworkResponse.Success -> {
                        WeatherDetails(
                            data = result.data,
                            context = this@MainActivity
                        )
                    }
                    null -> {}
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)

    @Composable
    fun SearchScreen(
        viewModel: WeatherViewModel,
        modifier: Modifier = Modifier
    ) {
        var city: String by remember { mutableStateOf("") }
        val weatherResult = viewModel.weatherResult.observeAsState()


        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "WeatherGetter",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .padding(8.dp)
                                .size(32.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Blue,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .padding(9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = "Search Weather") },
                            navigationIcon = {
                                IconButton(onClick = {
                                    // Navigate back to Current Location screen
                                    // You can use NavController here
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(innerPadding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text(text = "Enter city name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        IconButton(onClick = {
                            if (city.isNotEmpty()) {
                                viewModel.getData(city)
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Please enter a city name",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        }

                        when (val result = weatherResult.value) {
                            is NetworkResponse.Error -> {
                                Text(text = "Error: ${result.message}")
                            }

                            NetworkResponse.Loading -> {
                                CircularProgressIndicator()
                            }

                            is NetworkResponse.Success -> {
                                WeatherDetails(
                                    data = result.data,
                                    context = this@MainActivity
                                )
                            }

                            null -> {}
                        }
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SavedScreen(context: Context, dbHelper: WeatherDatabaseHelper) {
        // Mutable state for saved weather list
        val savedWeatherList =
            remember { mutableStateListOf(*dbHelper.getAllWeather().toTypedArray()) }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "WeatherGetter",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .padding(8.dp)
                                .size(32.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Blue,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .padding(9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Saved Weather Data",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (savedWeatherList.isEmpty()) {
                        // Empty state view
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No saved weather data available.",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        // List of saved weather data
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(savedWeatherList) { weather ->
                                WeatherCard(
                                    weather = weather,
                                    onDelete = {
                                        val rowsDeleted = dbHelper.deleteWeather(weather.id)
                                        if (rowsDeleted > 0) {
                                            Toast.makeText(
                                                context,
                                                "Weather deleted successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            savedWeatherList.remove(weather) // Remove item from list
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Failed to delete weather.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun WeatherCard(weather: SavedWeather, onDelete: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${weather.location}, ${weather.country}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = weather.temperature,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = weather.condition,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(text = "Delete")
                }
            }
        }
    }

    @Composable
    fun BottomNavigationBar(navController: NavController) {
        Surface(
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavigationBarItem(
                    navController = navController,
                    route = "home",
                    iconRes = R.drawable.ic_home,
                    label = "Home"
                )
                NavigationBarItem(
                    navController = navController,
                    route = "search",
                    iconRes = R.drawable.ic_search,
                    label = "Search"
                )
                NavigationBarItem(
                    navController = navController,
                    route = "saved",
                    iconRes = R.drawable.ic_saved,
                    label = "Saved"
                )
            }
        }
    }

    @Composable
    fun NavigationBarItem(
        navController: NavController,
        route: String,
        iconRes: Int,
        label: String
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .clickable { navController.navigate(route) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
