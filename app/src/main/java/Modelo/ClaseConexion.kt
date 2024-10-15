package Modelo

import java.sql.Connection
import java.sql.DriverManager

class ClaseConexion {
    fun CadenaConexion(): Connection? {
        return try {
            val url = "jdbc:oracle:thin:@10.10.2.140:1521:xe"
            val usuario = "reloj"
            val contraseña = "ITR2024"
            DriverManager.getConnection(url, usuario, contraseña)
        } catch (e: Exception) {
            println("ERROR : $e")
            null
        }
    }
}
