package com.example.agrokrishiseva

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    
    private const val SELECTED_LANGUAGE = "selected_language"
    
    fun setLocale(context: Context, languageCode: String): Context {
        saveLanguage(context, languageCode)
        return updateResources(context, languageCode)
    }
    
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("AgroApp", Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }
    
    private fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("AgroApp", Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
    }
    
    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
}
