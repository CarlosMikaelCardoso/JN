package com.example.jn // Substitua pelo seu pacote

// Define a estrutura de um Tanque.
// name: O nome do tanque (ex: "Tanque 1")
// outputs: Uma lista mutável para guardar os valores das saídas em litros (ex: [0.5, 1.0, 2.5])
data class Tank(val name: String, val outputs: MutableList<Double> = mutableListOf())