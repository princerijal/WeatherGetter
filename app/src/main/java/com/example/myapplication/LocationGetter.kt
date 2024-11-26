//package com.example.myapplication
//
//import android.content.Context
//import android.location.Address
//import android.location.Geocoder
//import java.util.Locale
//
//fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String {
//    val geocoder = Geocoder(context, Locale.getDefault())
//    return try {
//        val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
//            val address = addresses?.get(0)
//            val addressLine = address?.getAddressLine(0) // Full address
//            val city = address?.locality // City
//            val state = address?.adminArea // State
//            val country = address?.countryName // Country
//
//            // Return a formatted address string
//            "$addressLine, $city, $state, $country"
//        } else {
//            "Address not found"
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//        "Unable to get address"
//    }
//}
