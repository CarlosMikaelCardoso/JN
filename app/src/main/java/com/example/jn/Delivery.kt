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
    val isFinished: Boolean = false,
    val date: String = "",
    val acaiItems: List<AcaiOutput> = emptyList(),
    )