package com.example.jn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val batidas: List<Batida>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textViewHistoryItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_row, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val batida = batidas[position]

        // Constrói a string com todos os itens da batida
        val itemsString = batida.items.joinToString(separator = "; ") { output ->
            "%.1f L de %s".format(output.quantity, output.type)
        }

        // Formata o texto com o número da batida
        holder.textView.text = "%dª Batida: %s".format(position + 1, itemsString)
    }

    override fun getItemCount() = batidas.size
}