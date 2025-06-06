package com.example.jn

// O mapa agora guarda um objeto TankActivity para cada ID de tanque
data class DailyActivity(
    val atividadesPorTanque: MutableMap<String, TankActivity> = mutableMapOf(),
    val faturamentoTotal: Double = 0.0
)