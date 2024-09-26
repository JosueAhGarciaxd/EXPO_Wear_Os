package HealthSync.expo_wear_os

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class activity_sueno : AppCompatActivity() {
    private lateinit var txtHoraSueno: EditText
    private lateinit var txtResultado: TextView
    private val sleepTracker = SleepTracker() // Instancia de SleepTracker
    private val userId = "usuarioPrueba" // ID del usuario ficticio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sueno)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtHoraSueno = findViewById(R.id.txtHoraSueno)
        txtResultado = findViewById(R.id.txtResultado)

        txtHoraSueno.setOnClickListener {
            showTimePickerDialog()
        }

        val btnSueno = findViewById<Button>(R.id.btnsueno)

        btnSueno.setOnClickListener {
            val intent = Intent(this, estadistica_de_sueno::class.java)
            startActivity(intent)
        }

        // Crear usuario de prueba al iniciar la actividad
        crearUsuarioPrueba()
    }

    private fun crearUsuarioPrueba() {
        sleepTracker.crearUsuarioPrueba() // Llama a la función para crear el usuario de prueba
    }

    private fun showTimePickerDialog() {
        val calendario = Calendar.getInstance()
        val hour = calendario.get(Calendar.HOUR_OF_DAY)
        val minute = calendario.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                // Formatear la hora seleccionada
                val amPm = if (selectedHour < 12) "AM" else "PM"
                val hourIn12Format = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                val horaSeleccionada = String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm)
                txtHoraSueno.setText(horaSeleccionada)

                // Calcular y mostrar las horas de sueño y la hora de despertar
                calcularHorasDeSueno(selectedHour, selectedMinute)
            },
            hour,
            minute,
            false // false para formato de 12 horas
        )

        timePickerDialog.setTitle("Selecciona la hora de dormir") // Título opcional
        timePickerDialog.show()
    }

    private fun calcularHorasDeSueno(hour: Int, minute: Int) {
        // Supongamos que el usuario quiere dormir 8 horas
        val horasDeSueno = 8
        val calendario = Calendar.getInstance()
        calendario.set(Calendar.HOUR_OF_DAY, hour)
        calendario.set(Calendar.MINUTE, minute)

        // Sumar las horas de sueño
        calendario.add(Calendar.HOUR_OF_DAY, horasDeSueno)

        // Obtener la hora de despertar
        val horaDespertar = calendario.get(Calendar.HOUR_OF_DAY)
        val minutoDespertar = calendario.get(Calendar.MINUTE)

        // Formatear la hora de despertar
        val amPmDespertar = if (horaDespertar < 12) "AM" else "PM"
        val hourIn12FormatDespertar = if (horaDespertar % 12 == 0) 12 else horaDespertar % 12
        val horaDespertarFormateada = String.format("%02d:%02d %s", hourIn12FormatDespertar, minutoDespertar, amPmDespertar)

        // Mostrar el resultado
        txtResultado.text = "Duerme $horasDeSueno horas.\nDespertar a las: $horaDespertarFormateada"

        // Guardar las horas dormidas en Firestore
        sleepTracker.guardarHorasDormidas(userId, horasDeSueno)
    }
}
