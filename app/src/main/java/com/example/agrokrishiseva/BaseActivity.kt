package com.example.agrokrishiseva

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply saved locale
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this))
    }
    
    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }
}
