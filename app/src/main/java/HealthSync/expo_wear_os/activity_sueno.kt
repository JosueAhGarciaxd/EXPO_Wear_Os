package HealthSync.expo_wear_os

import Modelo.ClaseConexion
import android.app.TimePickerDialog
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.SQLException
import java.util.Calendar
import android.util.Log

class activity_sueno : AppCompatActivity() {
    private lateinit var txtHoraSueno: EditText
    private lateinit var txtResultado: TextView

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
            val horaSeleccionada = txtHoraSueno.text.toString()
            if (horaSeleccionada.isNotEmpty()) {
                guardarDatosEnBaseDeDatos(horaSeleccionada, 8) // Suponiendo que siempre son 8 horas de sueño
            } else {
                Toast.makeText(this, "Por favor, selecciona una hora de sueño", Toast.LENGTH_SHORT).show()
            }
        }

        val btnAtras = findViewById<ImageView>(R.id.imgatras)
        btnAtras.setOnClickListener {
            val intent = Intent(this, activity_menu::class.java)
            startActivity(intent)
        }
    }

    private fun showTimePickerDialog() {
        val calendario = Calendar.getInstance()
        val hour = calendario.get(Calendar.HOUR_OF_DAY)
        val minute = calendario.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val amPm = if (selectedHour < 12) "AM" else "PM"
                val hourIn12Format = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                val horaSeleccionada = String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm)
                txtHoraSueno.setText(horaSeleccionada)
                calcularHorasDeSueno(selectedHour, selectedMinute)
            },
            hour,
            minute,
            false // false para formato de 12 horas
        )

        timePickerDialog.setTitle("Selecciona la hora de dormir")
        timePickerDialog.show()
    }

    private fun calcularHorasDeSueno(hour: Int, minute: Int) {
        val horasDeSueno = 8
        val calendario = Calendar.getInstance()
        calendario.set(Calendar.HOUR_OF_DAY, hour)
        calendario.set(Calendar.MINUTE, minute)

        calendario.add(Calendar.HOUR_OF_DAY, horasDeSueno)

        val horaDespertar = calendario.get(Calendar.HOUR_OF_DAY)
        val minutoDespertar = calendario.get(Calendar.MINUTE)

        val amPmDespertar = if (horaDespertar < 12) "AM" else "PM"
        val hourIn12FormatDespertar = if (horaDespertar % 12 == 0) 12 else horaDespertar % 12
        val horaDespertarFormateada = String.format("%02d:%02d %s", hourIn12FormatDespertar, minutoDespertar, amPmDespertar)

        txtResultado.text = "Dormirás por $horasDeSueno horas.\nDespertarás aproximadamente a las: $horaDespertarFormateada"
    }

    private fun guardarDatosEnBaseDeDatos(horaDormir: String, horasDormidas: Int) {
        val dbHelper = ClaseConexion()

        Thread {
            var objConexion: Connection? = null
            var preparedStatement: PreparedStatement? = null
            try {
                objConexion = dbHelper.CadenaConexion()
                if (objConexion == null) {
                    Log.e("DB_ERROR", "No se pudo establecer la conexión a la base de datos.")
                    runOnUiThread {
                        Toast.makeText(this, "Error de conexión a la base de datos", Toast.LENGTH_LONG).show()
                    }
                    return@Thread
                } else {
                    Log.d("DB_SUCCESS", "Conexión establecida correctamente.")
                }

                val sql = "INSERT INTO sueno (hora_dormir, horas_dormidas) VALUES (?, ?)"
                preparedStatement = objConexion.prepareStatement(sql)

                val calendar = Calendar.getInstance()
                val horaFormateada = horaDormir.replace(" ", ":").split(":")
                calendar.set(Calendar.HOUR_OF_DAY, horaFormateada[0].toInt())
                calendar.set(Calendar.MINUTE, horaFormateada[1].toInt())
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                if (horaFormateada[2] == "PM" && calendar.get(Calendar.HOUR_OF_DAY) != 12) {
                    calendar.add(Calendar.HOUR_OF_DAY, 12)
                } else if (horaFormateada[2] == "AM" && calendar.get(Calendar.HOUR_OF_DAY) == 12) {
                    calendar.add(Calendar.HOUR_OF_DAY, -12)
                }

                val horaTimestamp = Timestamp(calendar.timeInMillis)

                Log.d("DB_INSERT", "Hora a insertar: $horaTimestamp")

                preparedStatement.setTimestamp(1, horaTimestamp)
                preparedStatement.setInt(2, horasDormidas)

                val filasInsertadas = preparedStatement.executeUpdate()
                Log.d("DB_INSERT", "Filas insertadas: $filasInsertadas")

                runOnUiThread {
                    if (filasInsertadas > 0) {
                        Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, estadistica_de_sueno::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "No se pudieron guardar los datos", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: SQLException) {
                Log.e("DB_ERROR", "Error de SQL al insertar datos: ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error al guardar los datos: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Error inesperado al insertar datos: ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error inesperado al guardar los datos", Toast.LENGTH_LONG).show()
                }
            } finally {
                try {
                    preparedStatement?.close()
                    objConexion?.close()
                    Log.d("DB_CLOSE", "Conexión y statement cerrados correctamente")
                } catch (e: SQLException) {
                    Log.e("DB_ERROR", "Error al cerrar la conexión: ${e.message}")
                }
            }
        }.start()
    }
}