package HealthSync.expo_wear_os

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // para que duere 2.5 segundos
        Handler().postDelayed({
            val intent = Intent(this, activity_menu::class.java)
            startActivity(intent)
            finish()
        }, 2500) //  2.5 segundos
    }

}

