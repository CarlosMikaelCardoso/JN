package com.example.jn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeliveryAdapter(
    private var deliveries: List<Delivery>,
    private val onItemClick: (Delivery) -> Unit
) : RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>() {

    class DeliveryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clientName: TextView = view.findViewById(R.id.textViewClientName)
        val address: TextView = view.findViewById(R.id.textViewAddress)
        val paymentMethod: TextView = view.findViewById(R.id.textViewPaymentMethod)
        val orderItems: TextView = view.findViewById(R.id.textViewOrderItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_delivery, parent, false)
        return DeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        val delivery = deliveries[position]
        holder.clientName.text = delivery.clientName
        holder.itemView.setOnClickListener { onItemClick(delivery) }

        val fullAddress = StringBuilder(delivery.address)
        if (!delivery.buildingName.isNullOrEmpty()) {
            fullAddress.append(", ${delivery.buildingName}")
        }
        fullAddress.append(", Nº ${delivery.houseNumber}")
        holder.address.text = fullAddress.toString()

        holder.paymentMethod.text = "Pagamento: ${delivery.paymentMethod}"

        // LÓGICA ATUALIZADA PARA MOSTRAR TODOS OS ITENS
        val itemsStr = StringBuilder()
        delivery.acaiItems.forEach { acaiItem ->
            itemsStr.append("• Açaí ${acaiItem.type}: ${acaiItem.quantity} L\n")
        }
        if (delivery.tapiocaQuantity > 0) itemsStr.append("• Tapioca: ${delivery.tapiocaQuantity} kg\n")
        if (delivery.farinhaDaguaQuantity > 0) itemsStr.append("• Farinha d'água: ${delivery.farinhaDaguaQuantity} kg")
        holder.orderItems.text = itemsStr.trim().toString()
    }

    override fun getItemCount() = deliveries.size

    fun updateData(newDeliveries: List<Delivery>) {
        deliveries = newDeliveries
        notifyDataSetChanged()
    }
}