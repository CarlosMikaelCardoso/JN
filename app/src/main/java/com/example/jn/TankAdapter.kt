package com.example.jn // Substitua pelo seu pacote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TankAdapter(
    private var tanks: List<Tank>,
    private val onItemClick: (Int) -> Unit // Função a ser chamada quando um item for clicado
) : RecyclerView.Adapter<TankAdapter.TankViewHolder>() {

    // ViewHolder: Mantém as referências para os componentes visuais de cada item (evita findViewById repetidos).
    class TankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tankName: TextView = view.findViewById(R.id.textViewTankName)
        val tankTotal: TextView = view.findViewById(R.id.textViewTankTotal)
    }

    // Cria um novo ViewHolder (chamado quando o RecyclerView precisa de um novo item visual).
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tank, parent, false)
        return TankViewHolder(view)
    }

    // Vincula os dados de um tanque a um ViewHolder.
    override fun onBindViewHolder(holder: TankViewHolder, position: Int) {
        val tank = tanks[position]
        val totalOutput = tank.outputs.sum() // Calcula a soma das saídas

        holder.tankName.text = tank.name
        holder.tankTotal.text = "Total: %.1f L".format(totalOutput)

        // Define o que acontece ao clicar no item.
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    // Retorna o número total de itens na lista.
    override fun getItemCount() = tanks.size

    // Função para atualizar a lista de tanques e notificar o RecyclerView.
    fun updateData(newTanks: List<Tank>) {
        tanks = newTanks
        notifyDataSetChanged()
    }
}