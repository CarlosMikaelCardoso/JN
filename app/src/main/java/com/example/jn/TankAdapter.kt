package com.example.jn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TankAdapter(
    private var tanks: List<Tank>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<TankAdapter.TankViewHolder>() {

    // ViewHolder agora também tem a referência para o nosso LinearLayout de resumo.
    class TankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tankName: TextView = view.findViewById(R.id.textViewTankName)
        val tankTotal: TextView = view.findViewById(R.id.textViewTankTotal)
        val typeBreakdownLayout: LinearLayout = view.findViewById(R.id.layoutTypeBreakdown)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tank, parent, false)
        return TankViewHolder(view)
    }

    // A lógica principal de exibição é atualizada aqui.
    override fun onBindViewHolder(holder: TankViewHolder, position: Int) {
        val tank = tanks[position]

        holder.tankName.text = tank.name
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }

        // Limpa as views antigas antes de adicionar novas
        holder.typeBreakdownLayout.removeAllViews()

        if (tank.outputs.isEmpty()) {
            holder.tankTotal.text = "Tanque vazio"
        } else {
            // 1. Calcula o total geral
            val totalGeral = tank.outputs.sumOf { it.quantity }
            holder.tankTotal.text = "Total Geral: %.1f L".format(totalGeral)

            // 2. Agrupa as saídas por tipo e soma as quantidades
            val totalsByType = tank.outputs
                .groupBy { it.type }
                .mapValues { entry -> entry.value.sumOf { it.quantity } }

            // 3. Cria uma TextView para cada tipo e adiciona ao layout
            totalsByType.forEach { (type, total) ->
                val textView = TextView(holder.itemView.context).apply {
                    text = "• $type: %.1f L".format(total)
                    textSize = 14f
                    setPadding(16, 4, 0, 4) // Adiciona um recuo
                }
                holder.typeBreakdownLayout.addView(textView)
            }
        }
    }

    override fun getItemCount() = tanks.size

    fun updateData(newTanks: List<Tank>) {
        tanks = newTanks
        notifyDataSetChanged()
    }
}