package bo.dolar.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Registro(
    val id: Long,
    val tipo: String,
    val compra: Double,
    val venta: Double,
    val fechaApi: String,
    val fechaConsulta: Long,
    val origen: String
)

/** BD SQLite local (dolar.db) con una sola tabla: historial. */
class DbHelper private constructor(ctx: Context) :
    SQLiteOpenHelper(ctx.applicationContext, "dolar.db", null, 1) {

    companion object {
        @Volatile private var inst: DbHelper? = null
        fun get(ctx: Context): DbHelper =
            inst ?: synchronized(this) { inst ?: DbHelper(ctx).also { inst = it } }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE historial(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo TEXT NOT NULL,
                compra REAL,
                venta REAL,
                fecha_api TEXT,
                fecha_consulta INTEGER NOT NULL,
                origen TEXT NOT NULL
            )"""
        )
        db.execSQL("CREATE INDEX idx_hist_tipo_fecha ON historial(tipo, fecha_consulta)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {}

    fun ultimo(tipo: String): Registro? =
        readableDatabase.rawQuery(
            "SELECT * FROM historial WHERE tipo=? ORDER BY fecha_consulta DESC, id DESC LIMIT 1",
            arrayOf(tipo)
        ).use { if (it.moveToFirst()) leer(it) else null }

    fun listar(limite: Int = 300): List<Registro> =
        readableDatabase.rawQuery(
            "SELECT * FROM historial ORDER BY fecha_consulta DESC, id DESC LIMIT $limite", null
        ).use { c -> buildList { while (c.moveToNext()) add(leer(c)) } }

    /** Inserta solo si el valor cambió respecto al último registro del mismo tipo. */
    fun insertarSiCambio(c: Cotizacion, origen: String): Boolean {
        val u = ultimo(c.tipo)
        if (u != null && igual(u.compra, c.compra) && igual(u.venta, c.venta) &&
            u.fechaApi == c.fechaActualizacion
        ) return false

        val cv = ContentValues().apply {
            put("tipo", c.tipo)
            if (c.compra.isNaN()) putNull("compra") else put("compra", c.compra)
            if (c.venta.isNaN()) putNull("venta") else put("venta", c.venta)
            put("fecha_api", c.fechaActualizacion)
            put("fecha_consulta", System.currentTimeMillis())
            put("origen", origen)
        }
        writableDatabase.insert("historial", null, cv)
        return true
    }

    private fun igual(a: Double, b: Double) = (a.isNaN() && b.isNaN()) || a == b

    private fun num(c: Cursor, col: String): Double {
        val i = c.getColumnIndexOrThrow(col)
        return if (c.isNull(i)) Double.NaN else c.getDouble(i)
    }

    private fun leer(c: Cursor) = Registro(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        tipo = c.getString(c.getColumnIndexOrThrow("tipo")),
        compra = num(c, "compra"),
        venta = num(c, "venta"),
        fechaApi = c.getString(c.getColumnIndexOrThrow("fecha_api")) ?: "",
        fechaConsulta = c.getLong(c.getColumnIndexOrThrow("fecha_consulta")),
        origen = c.getString(c.getColumnIndexOrThrow("origen"))
    )
}
