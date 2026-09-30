package bo.dolar.app

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class Cotizacion(
    val tipo: String,
    val compra: Double,
    val venta: Double,
    val fechaActualizacion: String
)

/** Cliente mínimo para https://bo.dolarapi.com (sin librerías externas). */
object DolarApi {
    private const val BASE = "https://bo.dolarapi.com/v1/dolares/"
    val TIPOS = listOf("oficial", "binance")

    fun obtener(tipo: String): Cotizacion {
        val conn = URL(BASE + tipo).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        conn.setRequestProperty("Accept", "application/json")
        try {
            if (conn.responseCode != 200) throw IOException("HTTP ${conn.responseCode}")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val j = JSONObject(body)
            return Cotizacion(
                tipo = tipo,
                compra = j.optDouble("compra"),
                venta = j.optDouble("venta"),
                fechaActualizacion = j.optString("fechaActualizacion")
            )
        } finally {
            conn.disconnect()
        }
    }

    /** Consulta todos los tipos; falla solo si ninguno respondió. */
    fun obtenerTodas(): List<Cotizacion> {
        val res = mutableListOf<Cotizacion>()
        var error: Exception? = null
        for (t in TIPOS) {
            try { res += obtener(t) } catch (e: Exception) { error = e }
        }
        if (res.isEmpty()) throw error ?: IOException("Sin datos")
        return res
    }
}
