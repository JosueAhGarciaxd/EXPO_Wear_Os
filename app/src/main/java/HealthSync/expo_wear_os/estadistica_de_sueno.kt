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
                    SELECT SUM(horas_dormidas) as total_horas,
                           COUNT(DISTINCT TRUNC(hora_dormir)) as dias_con_datos
                    FROM sueno
                    WHERE hora_dormir >= TRUNC(SYSDATE) - 7
                    AND hora_dormir < TRUNC(SYSDATE)
                """
                preparedStatement = connection.prepareStatement(sql)
                resultSet = preparedStatement.executeQuery()

                var totalHoras = 0f
                var diasConDatos = 0

                if (resultSet.next()) {
                    totalHoras = resultSet.getFloat("total_horas")
                    diasConDatos = resultSet.getInt("dias_con_datos")
                }

                runOnUiThread {
                    if (totalHoras > 0) {
                        txtEstadisticasSemana.text = String.format("Durante la semana has dormido %.2f horas", totalHoras)
                        if (diasConDatos > 0) {
                            val promedioDiario = totalHoras / diasConDatos
                            txtPromedioSueno.text = String.format("Promedio diario: %.2f horas (basado en %d días con datos)", promedioDiario, diasConDatos)
                        } else {
                            txtPromedioSueno.text = "No se pudo calcular el promedio diario"
                        }
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