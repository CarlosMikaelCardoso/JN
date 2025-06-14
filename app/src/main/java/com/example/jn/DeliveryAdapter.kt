package com.example.jn

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class DeliveryAdapter(
    private var deliveries: MutableList<Delivery>,
    private val onItemClick: (Delivery) -> Unit
) : RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>() {

    // Nova variável para controlar a modificação
    private var isModifiable: Boolean = true

    // Nova função para a Activity poder atualizar este estado
    fun setModifiable(isModifiable: Boolean) {
        this.isModifiable = isModifiable
    }

    class DeliveryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clientName: TextView = view.findViewById(R.id.textViewClientName)
        val address: TextView = view.findViewById(R.id.textViewAddress)
        val paymentMethod: TextView = view.findViewById(R.id.textViewPaymentMethod)
        val orderItems: TextView = view.findViewById(R.id.textViewOrderItems)
        val observation: TextView = view.findViewById(R.id.textViewObservation)
        val markAsFinishedButton: Button = view.findViewById(R.id.buttonMarkAsFinished)
        val layout: LinearLayout = view.findViewById(R.id.delivery_item_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_delivery, parent, false)
        return DeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        val delivery = deliveries[position]
        holder.clientName.text = delivery.clientName

        val fullAddress = StringBuilder(delivery.address)
        if (!delivery.buildingName.isNullOrEmpty()) {
            fullAddress.append(", ${delivery.buildingName}")
        }
        fullAddress.append(", Nº ${delivery.houseNumber}")
        holder.address.text = fullAddress.toString()

        holder.paymentMethod.text = "Pagamento: ${delivery.paymentMethod}"

        val itemsStr = StringBuilder()
        delivery.acaiItems.forEach { acaiItem ->
            itemsStr.append("• Açaí ${acaiItem.type}: ${acaiItem.quantity} L\n")
        }
        if (delivery.tapiocaQuantity > 0) itemsStr.append("• Tapioca: ${delivery.tapiocaQuantity} kg\n")
        if (delivery.farinhaDaguaQuantity > 0) itemsStr.append("• Farinha d'água: ${delivery.farinhaDaguaQuantity} kg")
        holder.orderItems.text = itemsStr.trim().toString()

        if (delivery.observation.isNotBlank()) {
            holder.observation.visibility = View.VISIBLE
            holder.observation.text = "Obs: ${delivery.observation}"
        } else {
            holder.observation.visibility = View.GONE
        }

        // Lógica de bloqueio
        if (delivery.finished) {
            holder.markAsFinishedButton.visibility = View.GONE
            holder.layout.setBackgroundColor(Color.parseColor("#C8C8C8"))
            holder.itemView.setOnClickListener(null)
        } else {
            // Apenas mostra o botão e permite cliques se o dia for modificável
            if (isModifiable) {
                holder.markAsFinishedButton.visibility = View.VISIBLE
                holder.layout.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.item_background)
                holder.itemView.setOnClickListener { onItemClick(delivery) }

                holder.markAsFinishedButton.setOnClickListener {
                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Confirmar Entrega")
                        .setMessage("Tem certeza que deseja marcar este pedido como entregue?")
                        .setPositiveButton("Sim, Entregue") { dialog, _ ->
                            markDeliveryAsFinished(delivery, position)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Não", null)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show()
                }
            } else {
                // Em dias passados, esconde o botão e desativa o clique
                holder.markAsFinishedButton.visibility = View.GONE
                holder.itemView.setOnClickListener(null)
                holder.layout.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.item_background)
            }
        }
    }

    private fun markDeliveryAsFinished(delivery: Delivery, position: Int) {
        val updatedDelivery = delivery.copy(finished = true)

        FirebaseManager.updateDelivery(updatedDelivery) { success ->
            if (success) {
                deliveries[position] = updatedDelivery
                notifyItemChanged(position)
            } else {
                Toast.makeText(null, "Falha ao atualizar o pedido. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = deliveries.size

    fun updateData(newDeliveries: List<Delivery>) {
        this.deliveries = newDeliveries.toMutableList()
        notifyDataSetChanged()
    }
}