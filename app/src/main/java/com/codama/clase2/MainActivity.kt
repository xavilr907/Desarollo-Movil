package com.codama.clase2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Simulamos que el usuario ya está logueado
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            startActivity(Intent(this, InventoryActivity::class.java))
            finish()
        } else {
            // Aquí podrías implementar el login más adelante.
            sharedPref.edit().putBoolean("isLoggedIn", true).apply()
            startActivity(Intent(this, InventoryActivity::class.java))
            finish()
        }
    }
}
