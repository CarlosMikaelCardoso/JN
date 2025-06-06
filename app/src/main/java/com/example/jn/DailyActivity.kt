package com.example.jn

// Altere o "Map" para "MutableMap" e "List" para "MutableList"
data class DailyActivity(
    val batidasPorTanque: MutableMap<String, MutableList<Batida>> = mutableMapOf(),
    val faturamentoTotal: Double = 0.0
)