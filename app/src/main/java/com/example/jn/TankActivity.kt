package com.example.jn

// Guarda o nome do tanque e a lista de batidas daquele dia
data class TankActivity(
    val tankName: String = "",
    val batidas: MutableList<Batida> = mutableListOf()
)