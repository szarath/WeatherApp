package com.ets.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val apiKey = "7c9a738de49a0462964c227513c2c235"
    private lateinit var weatherTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherTextView = findViewById(R.id.weatherTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check location permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, get location
            fetchWeatherForCurrentLocation()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun fetchWeatherForCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Fetch weather data using location coordinates
                    FetchWeatherTask().execute(latitude, longitude)
                } else {
                    weatherTextView.text = "Location not available"
                }
            }
    }

    inner class FetchWeatherTask : AsyncTask<Double, Void, String>() {

        override fun doInBackground(vararg params: Double?): String {
            val latitude = params[0]
            val longitude = params[1]
            val weatherData: String = try {
                // Replace "YOUR_OPENWEATHERMAP_API_KEY" with your actual API key
                URL("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey")
                    .readText(Charsets.UTF_8)
            } catch (e: Exception) {
                e.toString()
            }
            return weatherData
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            parseWeatherData(result)
        }
    }

    private fun parseWeatherData(weatherData: String) {
        try {
            val jsonObject = JSONObject(weatherData)
            val main = jsonObject.getJSONObject("main")
            val temperature = main.getString("temp")
            val description = jsonObject.getJSONArray("weather")
                .getJSONObject(0)
                .getString("description")

            val weatherInfo = "Temperature: $temperature°C\nDescription: $description"
            weatherTextView.text = weatherInfo
        } catch (e: Exception) {
            e.printStackTrace()
            weatherTextView.text = "Error parsing weather data"
        }
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}
