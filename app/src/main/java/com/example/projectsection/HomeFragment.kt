package com.example.projectsection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherTemperature: TextView
    private lateinit var weatherDescription: TextView
    private lateinit var weatherProgressBar: ProgressBar
    private lateinit var welcomeText: TextView

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        initViews(view)
        setupButtons(view)
        setupWelcomeMessage()
        checkLocationPermission()
    }

    private fun initViews(view: View) {
        weatherTemperature = view.findViewById(R.id.weather_temperature)
        weatherDescription = view.findViewById(R.id.weather_description)
        weatherProgressBar = view.findViewById(R.id.weather_progress)
        welcomeText = view.findViewById(R.id.tv_welcome)
    }

    private fun setupButtons(view: View) {
        val browseProductsButton: Button = view.findViewById(R.id.btn_browse_products)
        val farmingTipsButton: Button = view.findViewById(R.id.btn_farming_tips)
        val quickOrderButton: Button = view.findViewById(R.id.btn_quick_order)

        browseProductsButton.setOnClickListener {
            startActivity(Intent(requireContext(), ProductsActivity::class.java))
        }

        farmingTipsButton.setOnClickListener {
            startActivity(Intent(requireContext(), TipsActivity::class.java))
        }

        quickOrderButton.setOnClickListener {
            Toast.makeText(requireContext(), "Quick Order feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupWelcomeMessage() {
        val sharedPreferences = requireActivity().getSharedPreferences("AgroApp", 0)
        val userName = sharedPreferences.getString("userName", "Farmer")
        welcomeText.text = "Welcome back, $userName!"
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
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
                weatherDescription.text = "Location permission denied"
                weatherTemperature.text = "--"
            }
        }
    }

    private fun getLastLocation() {
        weatherProgressBar.visibility = View.VISIBLE
        weatherDescription.text = "Fetching location..."
        
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                fetchWeather(location.latitude, location.longitude)
            } else {
                weatherProgressBar.visibility = View.GONE
                weatherTemperature.text = "--"
                weatherDescription.text = "Couldn't get location"
            }
        }.addOnFailureListener {
            weatherProgressBar.visibility = View.GONE
            weatherTemperature.text = "--"
            weatherDescription.text = "Failed to get location"
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
                            weatherDescription.text = "Weather data not available"
                        }
                    } else {
                        weatherTemperature.text = "--"
                        weatherDescription.text = "API Error: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    weatherProgressBar.visibility = View.GONE
                    weatherTemperature.text = "--"
                    weatherDescription.text = "Network error"
                }
            })
    }
}