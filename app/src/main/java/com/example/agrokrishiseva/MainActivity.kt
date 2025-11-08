package com.example.agrokrishiseva

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherTemperature: TextView
    private lateinit var weatherDescription: TextView
    private lateinit var weatherProgressBar: ProgressBar

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherTemperature = findViewById(R.id.weather_temperature)
        weatherDescription = findViewById(R.id.weather_description)
        weatherProgressBar = findViewById(R.id.weather_progress)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupButtons()
        checkLocationPermission()
    }

    private fun setupButtons() {
        val browseProductsButton: Button = findViewById(R.id.btn_browse_products)
        val farmingTipsButton: Button = findViewById(R.id.btn_farming_tips)

        browseProductsButton.setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
        }

        farmingTipsButton.setOnClickListener {
            startActivity(Intent(this, TipsActivity::class.java))
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLastLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            } else {
                weatherDescription.text = "Location permission was denied."
                weatherTemperature.text = "--"
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLastLocation() {
        weatherProgressBar.visibility = View.VISIBLE
        weatherDescription.text = "Fetching location..."
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return // This should not happen if permission flow is correct
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                fetchWeather(location.latitude, location.longitude)
            } else {
                weatherProgressBar.visibility = View.GONE
                weatherTemperature.text = "--"
                weatherDescription.text = "Couldn\'t get location. Is GPS on?"
            }
        }.addOnFailureListener {
            weatherProgressBar.visibility = View.GONE
            weatherTemperature.text = "--"
            weatherDescription.text = "Failed to get location."
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        weatherDescription.text = "Fetching weather..."
        val apiKey = "e74de41f0e24a797f8a692c77a629b4f"

        RetrofitClient.instance.getCurrentWeather(lat, lon, apiKey)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    weatherProgressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        if (weatherResponse != null && weatherResponse.weather.isNotEmpty()) {
                            weatherTemperature.text = "${weatherResponse.main.temp.toInt()}°C"
                            weatherDescription.text = weatherResponse.weather.first().description.replaceFirstChar { it.uppercase() }
                        } else {
                            weatherTemperature.text = "--"
                            weatherDescription.text = "Weather data not available."
                        }
                    } else {
                        weatherTemperature.text = "--"
                        weatherDescription.text = "API Error: ${response.code()}. The key might still be activating."
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    weatherProgressBar.visibility = View.GONE
                    weatherTemperature.text = "--"
                    weatherDescription.text = "Network error. Check connection."
                }
            })
    }
}
