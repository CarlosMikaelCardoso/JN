package com.example.jn

import com.google.firebase.Timestamp
import java.util.Date

data class Batida(
    val items: List<AcaiOutput> = emptyList(),
    val timestamp: Timestamp = Timestamp(Date()) // <-- ADICIONE APENAS ESTA LINHA
)