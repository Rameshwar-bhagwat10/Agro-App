package com.example.agrokrishiseva

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Data classes to match the JSON structure from OpenWeatherMap
data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Double
)

data class Weather(
    val description: String,
    val icon: String
)

// Interface defining the API endpoints
interface WeatherService {
    @GET("data/2.5/weather")
    fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String,
        @Query("units") units: String = "metric" // To get temperature in Celsius
    ): Call<WeatherResponse>
}
