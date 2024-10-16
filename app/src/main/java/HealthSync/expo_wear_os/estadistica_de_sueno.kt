package HealthSync.expo_wear_os

import Modelo.ClaseConexion
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

class estadistica_de_sueno : AppCompatActivity() {
    private lateinit var txtEstadisticasSemana: TextView
    private lateinit var txtPromedioSueno: TextView
    private lateinit var btnCargarEstadisticas: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadistica_de_sueno)

        txtEstadisticasSemana = findViewById(R.id.txtEstadisticasSemana)
        txtPromedioSueno = findViewById(R.id.txtPromedioSueno)
        btnCargarEstadisticas = findViewById(R.id.btnCargarEstadisticas)

        btnCargarEstadisticas.setOnClickListener {
            cargarDatosSueno()
        }

        // Cargar datos automáticamente al abrir la actividad
        cargarDatosSueno()
    }

    private fun cargarDatosSueno() {
        Thread {
            val dbHelper = ClaseConexion()
            var connection: Connection? = null
            var preparedStatement: PreparedStatement? = null
            var resultSet: ResultSet? = null

            try {
                connection = dbHelper.CadenaConexion() ?: run {
                    runOnUiThread { txtEstadisticasSemana.text = "Error de conexión a la base de datos" }
                    return@Thread
                }

                val sql = """
                    SELECT hora_dormir, horas_dormidas
                    FROM sueno
                    WHERE hora_dormir >= SYSDATE - 7
                    ORDER BY hora_dormir DESC
                """
                preparedStatement = connection.prepareStatement(sql)
                resultSet = preparedStatement.executeQuery()

                val estadisticas = StringBuilder()
                var totalHoras = 0f
                var diasRegistrados = 0
                val dateFormat = SimpleDateFormat("EEEE dd/MM", Locale("es", "ES"))

                while (resultSet.next()) {
                    val horaDormir = resultSet.getTimestamp("hora_dormir")
                    val horasDormidas = resultSet.getFloat("horas_dormidas")

                    estadisticas.append("${dateFormat.format(horaDormir)}: ${String.format("%.2f", horasDormidas)} horas\n")
                    totalHoras += horasDormidas
                    diasRegistrados++
                }

                val promedio = if (diasRegistrados > 0) totalHoras / diasRegistrados else 0f

                runOnUiThread {
                    if (diasRegistrados > 0) {
                        txtEstadisticasSemana.text = estadisticas.toString()
                        txtPromedioSueno.text = String.format("Total de horas dormidas: %.2f horas\nPromedio: %.2f horas", totalHoras, promedio)
                    } else {
                        txtEstadisticasSemana.text = "No hay datos de sueño en los últimos 7 días"
                        txtPromedioSueno.text = ""
                    }
                }

            } catch (e: SQLException) {
                e.printStackTrace()
                runOnUiThread { txtEstadisticasSemana.text = "Error al cargar los datos: ${e.message}" }
            } finally {
                try {
                    resultSet?.close()
                    preparedStatement?.close()
                    connection?.close()
                } catch (closeException: SQLException) {
                    closeException.printStackTrace()
                }
            }
        }.start()
    }
}
