package com.example.jn // Substitua pelo seu pacote

// O Tanque agora armazena uma lista de Batidas, não mais de AcaiOutputs diretamente.
data class Tank(val name: String, val batidas: MutableList<Batida> = mutableListOf())