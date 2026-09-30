package bo.dolar.app

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formato {
    private val loc = Locale("es", "BO")

    fun num(v: Double): String = if (v.isNaN()) "—" else String.format(loc, "%.2f", v)

    fun fechaHora(ms: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", loc).format(Date(ms))

    fun fechaApi(s: String): String =
        if (s.length >= 16) s.substring(0, 16).replace('T', ' ') else s
}
