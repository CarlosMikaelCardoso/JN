package com.example.jn

import com.google.firebase.firestore.DocumentId

data class Delivery(
    @DocumentId
    val id: String = "",
    val clientName: String = "",
    val address: String = "",
    val buildingName: String? = null,
    val houseNumber: String = "",
    val paymentMethod: String = "",
    val tapiocaQuantity: Int = 0,
    val farinhaDaguaQuantity: Double = 0.0,
    val finished: Boolean = false, // MUDANÇA: de 'isFinished' para 'finished'
    val date: String = "",
    val acaiItems: List<AcaiOutput> = emptyList(),
    val observation: String = ""
)