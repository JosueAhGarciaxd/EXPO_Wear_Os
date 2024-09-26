package HealthSync.expo_wear_os

import android.icu.text.SimpleDateFormat
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Date
import java.util.Locale

class SleepTracker {
    private val db = FirebaseFirestore.getInstance()

    // para guardar horas dormidas
    fun guardarHorasDormidas(userId: String, horas: Int) {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())

        val sleepData = hashMapOf(
            "userId" to userId,
            "date" to fecha,
            "hours" to horas
        )

        db.collection("sleep_records")
            .add(sleepData)
            .addOnSuccessListener { documentReference ->
                Log.d("SleepTracker", "Registro guardado con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("SleepTracker", "Error al guardar registro", e)
            }
    }

    //Calcula el total horas dormidas
    fun calcularTotalHorasDormidas(userId: String, callback: (Int) -> Unit) {
        db.collection("sleep_records")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                var totalHoras = 0
                for (document in documents) {
                    val horas = document.getLong("hours")?.toInt() ?: 0
                    totalHoras += horas
                }
                callback(totalHoras)
            }
            .addOnFailureListener { e ->
                Log.w("SleepTracker", "Error al obtener registros", e)
                callback(0)
            }
    }

    //Calcula el total horas dormidas en la semana
    fun calcularTotalHorasDormidasEnSemana(userId: String, callback: (Int) -> Unit) {
        val fechaActual = System.currentTimeMillis()
        val unaSemanaAtras = fechaActual - (7 * 24 * 60 * 60 * 1000) // Milisegundos en una semana

        db.collection("sleep_records")
            .whereEqualTo("userId", userId)
            .whereGreaterThan("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(unaSemanaAtras)))
            .get()
            .addOnSuccessListener { documents ->
                var totalHoras = 0
                for (document in documents) {
                    val horas = document.getLong("hours")?.toInt() ?: 0
                    totalHoras += horas
                }
                callback(totalHoras)
            }
            .addOnFailureListener { e ->
                Log.w("SleepTracker", "Error al obtener registros", e)
                callback(0)
            }
    }

    //  crear un usuario de prueba
    //NO ME FUNCIONO LPTMD
    fun crearUsuarioPrueba() {
        val userId = "usuarioPrueba" // ID de usuario ficticio
        val userData = hashMapOf(
            "nombre" to "Usuario de Prueba",
            "email" to "prueba@ejemplo.com"
        )

        db.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d("SleepTracker", "Usuario de prueba creado con ID: $userId")
            }
            .addOnFailureListener { e ->
                Log.w("SleepTracker", "Error al crear usuario de prueba", e)
            }
    }
}
