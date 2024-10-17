package HealthSync.expo_wear_os

import Modelo.ClaseConexion
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

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


        val btnAtras = findViewById<ImageView>(R.id.imgatras2)
        btnAtras.setOnClickListener {
            val intent = Intent(this, activity_sueno::class.java)
            startActivity(intent)
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

                // Consulta SQL para obtener las horas de sueño de los últimos 7 días
                val sql = """
                    SELECT TRUNC(hora_dormir) as dia,
                           SUM(LEAST(horas_dormidas, 8)) as horas_dormidas
                    FROM sueno
                    WHERE hora_dormir >= (SELECT MAX(hora_dormir) FROM sueno) - INTERVAL '7' DAY
                    GROUP BY TRUNC(hora_dormir)
                    ORDER BY dia DESC
                """
                preparedStatement = connection.prepareStatement(sql)
                resultSet = preparedStatement.executeQuery()

                var totalHoras = 0f
                var diasConDatos = 0

                while (resultSet.next()) {
                    totalHoras += resultSet.getFloat("horas_dormidas")
                    diasConDatos++
                }

                runOnUiThread {
                    if (totalHoras > 0) {
                        txtEstadisticasSemana.text = String.format("Durante la semana has dormido %.2f horas", totalHoras)
                        if (diasConDatos > 0) {
                            val promedioDiario = totalHoras / diasConDatos
                            //txtPromedioSueno.text = String.format("Promedio diario: %.2f horas (basado en %d días con datos)", promedioDiario, diasConDatos)
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
