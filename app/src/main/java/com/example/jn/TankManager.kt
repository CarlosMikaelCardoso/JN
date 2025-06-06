package com.example.jn // Substitua pelo seu pacote

// Um objeto singleton para gerenciar a lista de tanques.
object TankManager {
    private val tanks = mutableListOf<Tank>()

    // Bloco de inicialização: Garante que o app sempre comece com um tanque.
    init {
        addNewTank()
    }

    // Retorna a lista completa de tanques.
    fun getTanks(): List<Tank> {
        return tanks
    }

    // Adiciona um novo tanque à lista.
    fun addNewTank() {
        val newTankNumber = tanks.size + 1
        tanks.add(Tank(name = "Tanque $newTankNumber"))
    }

    // Encontra um tanque específico pela sua posição na lista.
    fun getTankAt(position: Int): Tank? {
        return tanks.getOrNull(position)
    }

    fun getAcaiTypes(): List<String> {
        return listOf("Açaí Especial", "Açaí Médio", "Açaí Grosso", "Açaí Branco", "Bacaba")
    }
}