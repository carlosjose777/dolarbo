package bo.dolar.app

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private lateinit var db: DbHelper
    private lateinit var adapter: HistorialAdapter
    private lateinit var tvEstado: TextView
    private lateinit var tvAuto: TextView
    private lateinit var btnActualizar: Button
    private lateinit var progress: ProgressBar

    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var consultando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DbHelper.get(this)
        tvEstado = findViewById(R.id.tvEstado)
        tvAuto = findViewById(R.id.tvAuto)
        btnActualizar = findViewById(R.id.btnActualizar)
        progress = findViewById(R.id.progress)

        adapter = HistorialAdapter(this)
        val lv = findViewById<ListView>(R.id.lvHistorial)
        lv.adapter = adapter
        lv.emptyView = findViewById(R.id.tvVacio)

        btnActualizar.setOnClickListener { consultar() }

        Programador.asegurar(this)
        refrescarVista()
    }

    /** Cada vez que el usuario entra (o vuelve) a la app, se consulta. */
    override fun onResume() {
        super.onResume()
        consultar()
    }

    private fun consultar() {
        if (consultando) return
        consultando = true
        progress.visibility = View.VISIBLE
        btnActualizar.isEnabled = false
        tvEstado.text = "Consultando…"

        executor.execute {
            val r = runCatching { Repositorio.actualizar(this, Repositorio.ORIGEN_APP) }
            main.post {
                consultando = false
                if (isDestroyed) return@post
                progress.visibility = View.GONE
                btnActualizar.isEnabled = true
                tvEstado.text = r.fold(
                    { "Actualizado: ${Formato.fechaHora(System.currentTimeMillis())}" },
                    { "Sin conexión. Mostrando el último valor guardado." }
                )
                refrescarVista()
            }
        }
    }

    private fun refrescarVista() {
        pintarCard(R.id.cardOficial, "Oficial (BCB)", db.ultimo("oficial"))
        pintarCard(R.id.cardBinance, "Binance (P2P)", db.ultimo("binance"))
        adapter.setData(db.listar())

        val ultima = Repositorio.ultimaAuto(this)
        tvAuto.text = "Consulta automática diaria a las 19:00 · última: " +
            if (ultima == 0L) "aún no se ejecutó" else Formato.fechaHora(ultima)
    }

    private fun pintarCard(id: Int, titulo: String, r: Registro?) {
        val card = findViewById<View>(id)
        card.findViewById<TextView>(R.id.tvTitulo).text = titulo
        card.findViewById<TextView>(R.id.tvVenta).text =
            if (r == null) "—" else "Bs ${Formato.num(r.venta)}"
        card.findViewById<TextView>(R.id.tvCompra).text =
            if (r == null) "Sin datos aún" else "Compra: ${Formato.num(r.compra)}"
        card.findViewById<TextView>(R.id.tvFecha).text =
            r?.let { "Fuente: ${Formato.fechaApi(it.fechaApi)}" } ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}
