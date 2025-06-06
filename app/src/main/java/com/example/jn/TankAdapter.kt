package com.example.jn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TankAdapter(
    private var tanks: List<Tank>,
    private val onItemClick: (Tank) -> Unit
) : RecyclerView.Adapter<TankAdapter.TankViewHolder>() {

    class TankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tankName: TextView = view.findViewById(R.id.textViewTankName)
        val tankTotal: TextView = view.findViewById(R.id.textViewTankTotal)
        val typeBreakdownLayout: LinearLayout = view.findViewById(R.id.layoutTypeBreakdown)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tank, parent, false)
        return TankViewHolder(view)
    }

    override fun onBindViewHolder(holder: TankViewHolder, position: Int) {
        val tank = tanks[position]
        holder.tankName.text = tank.name
        holder.itemView.setOnClickListener { onItemClick(tank) }
        holder.typeBreakdownLayout.removeAllViews()

        // Pega as batidas do dia para este tanque específico
        val dailyBatidas = TankManager.getBatidasForTank(tank.id)

        if (dailyBatidas.isEmpty()) {
            holder.tankTotal.text = "Tanque vazio hoje"
        } else {
            val allItems = dailyBatidas.flatMap { it.items }
            val totalGeral = allItems.sumOf { it.quantity }
            holder.tankTotal.text = "Total Hoje: %.1f L".format(totalGeral)

            val totalsByType = allItems
                .groupBy { it.type }
                .mapValues { entry -> entry.value.sumOf { it.quantity } }

            totalsByType.forEach { (type, total) ->
                val textView = TextView(holder.itemView.context).apply {
                    text = "• $type: %.1f L".format(total)
                    textSize = 14f
                    setPadding(16, 4, 0, 4)
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