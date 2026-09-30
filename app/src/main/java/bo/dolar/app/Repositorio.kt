package bo.dolar.app

import android.content.Context

object Repositorio {
    const val ORIGEN_APP = "app"
    const val ORIGEN_AUTO = "auto"
    private const val PREFS = "dolar_prefs"
    private const val KEY_ULTIMA_AUTO = "ultima_auto"

    /** Consulta la API y guarda en la BD. Lanza excepción si no hay conexión. */
    fun actualizar(ctx: Context, origen: String): List<Cotizacion> {
        val lista = DolarApi.obtenerTodas()
        val db = DbHelper.get(ctx)
        lista.forEach { db.insertarSiCambio(it, origen) }
        if (origen == ORIGEN_AUTO) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putLong(KEY_ULTIMA_AUTO, System.currentTimeMillis()).apply()
        }
        return lista
    }

    fun ultimaAuto(ctx: Context): Long =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_ULTIMA_AUTO, 0L)
}
