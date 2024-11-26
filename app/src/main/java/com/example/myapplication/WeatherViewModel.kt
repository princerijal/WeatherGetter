package com.example.myapplication
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.Constant
import com.example.myapplication.api.NetworkResponse
import com.example.myapplication.api.RetrofitInstance
import com.example.myapplication.api.WeatherModel
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val weatherApi = RetrofitInstance.weatherApi
    private val _result = MutableLiveData<NetworkResponse<WeatherModel>>()
    val weatherResult: LiveData<NetworkResponse<WeatherModel>> = _result


    fun getData(city: String) {

        _result.value = NetworkResponse.Loading

        viewModelScope.launch {
            try {
                val response = weatherApi.getWeather(Constant.apiKey, city)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _result.value = NetworkResponse.Success(it)
                    }
                } else {
                    _result.value = NetworkResponse.Error("Failed to load data")
                }

            } catch (e: Exception) {
                _result.value = NetworkResponse.Error(e.message ?: "An unexpected error occured")
            }
        }

    }
}