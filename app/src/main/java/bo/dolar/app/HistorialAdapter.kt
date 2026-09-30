package bo.dolar.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class HistorialAdapter(ctx: Context) : ArrayAdapter<Registro>(ctx, 0) {

    fun setData(lista: List<Registro>) {
        clear()
        addAll(lista)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_historial, parent, false)
        val r = getItem(position)!!
        val sufijo = if (r.origen == Repositorio.ORIGEN_AUTO) "  ·  automática" else ""
        v.findViewById<TextView>(R.id.tvItemTipo).text =
            r.tipo.replaceFirstChar { it.uppercase() }
        v.findViewById<TextView>(R.id.tvItemFecha).text = Formato.fechaHora(r.fechaConsulta) + sufijo
        v.findViewById<TextView>(R.id.tvItemValores).text =
            "C ${Formato.num(r.compra)}  /  V ${Formato.num(r.venta)}"
        return v
    }
}
