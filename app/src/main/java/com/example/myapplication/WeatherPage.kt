package com.example.myapplication

import android.text.style.BackgroundColorSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.api.NetworkResponse
import com.example.myapplication.api.WeatherModel
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherPage(
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
                        painter = painterResource(id = R.drawable.logo), // Replace with your logo
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(32.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue, // Background color
                    titleContentColor = Color.White, // Title text color
                    navigationIconContentColor = Color.White // Icon color
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(text = "Search the Location") }
                )

                IconButton(onClick = {
                    viewModel.getData(city)
                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search for any Location"
                    )
                }
            }

            when (val result = weatherResult.value) {
                is NetworkResponse.Error -> {
                    Text(text = "Error: ${result.message}")
                }

                NetworkResponse.Loading -> {
                    CircularProgressIndicator()
                }

                is NetworkResponse.Success -> {
                    WeatherDetails(data = result.data)
                }

                null -> {}
            }
        }
    }
}

@Composable
fun WeatherDetails(data: WeatherModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 9.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Icon for location",
                modifier = Modifier.size(50.dp)
            )
            Text(text = data.location.name, fontSize = 30.sp)

            Text(text = ", ${data.location.country}", fontSize = 30.sp)
        }

        Spacer(modifier = Modifier.height(20.dp)) // Adjusted height

        Text(
            text = "${data.current.temp_c} °C",
            fontSize = 70.sp,
            fontWeight = FontWeight.Bold,
            // Corrected the property name
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        AsyncImage(
            model = "https:${data.current.condition.icon}",
            contentDescription = "Icon for weather condition",
            modifier = Modifier.size(100.dp)
        )

        Text(
            text = data.current.condition.text,
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))
        Card{
            Column(
                modifier = Modifier.fillMaxWidth()
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ){

                    WeatherValues(data.current.wind_kph, "Wind Speed")
                    WeatherValues(data.current.humidity, "Humidity")


                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ){

                    WeatherValues(data.current.cloud, "Cloud Percentage")
                    WeatherValues(data.current.feelslike_c, "FeelS Like ")


                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ){

                    WeatherValues(data.location.localtime.split(" ")[0], "Local Date")
                    WeatherValues(data.location.localtime.split(" ")[1], "Local Time")


                }

            }
        }

    }


}

@Composable
fun WeatherValues(value : String, key : String){
    Column(
        modifier = Modifier.padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = key, fontWeight = FontWeight.SemiBold, color = Color.Gray, fontSize = 20.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 26.sp)
    }
}

