package com.example.jn

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TankManager {
    // Estado do App
    private var status: AppStatus? = null
    private var tanks = listOf<Tank>()
    var currentDayActivity: DailyActivity? = null

    // Carrega TODOS os dados iniciais necessários
    fun loadInitialData(onComplete: () -> Unit) {
        FirebaseManager.getAppStatus { appStatus ->
            status = appStatus
            val activeDay = status?.diaAtivo ?: return@getAppStatus

            FirebaseManager.loadTanks { loadedTanks ->
                tanks = loadedTanks

                FirebaseManager.getDailyActivity(activeDay) { dailyActivity ->
                    currentDayActivity = dailyActivity
                    onComplete()
                }
            }
        }
    }

    fun getTanks(): List<Tank> = tanks

    fun getBatidasForTank(tankId: String): List<Batida> {
        return currentDayActivity?.atividadesPorTanque?.get(tankId)?.batidas ?: emptyList()
    }

    fun getTankById(id: String): Tank? {
        // Procura na lista de tanques em memória pelo tanque com o ID correspondente
        return tanks.find { it.id == id }
    }

    fun getAcaiTypes(): List<String> {
        return listOf("Açaí Especial", "Açaí Médio", "Açaí Grosso", "Açaí Branco", "Bacaba")
    }

    fun getActiveDay(): String = status?.diaAtivo ?: "N/A"

    fun addBatidaToTank(tankId: String, batida: Batida) {
        val activeDay = status?.diaAtivo ?: return
        val tank = getTankById(tankId) ?: return // Pega o tanque para obter o nome

        // Passa o nome do tanque para o FirebaseManager
        FirebaseManager.addBatidaToDailyActivity(activeDay, tankId, tank.name, batida)

        // Atualiza o estado local
        if (currentDayActivity == null) {
            currentDayActivity = DailyActivity()
        }
        val tankActivity = currentDayActivity!!.atividadesPorTanque.getOrPut(tankId) {
            TankActivity(tankName = tank.name)
        }
        tankActivity.batidas.add(batida)
    }

    fun endDayAndStartNew(revenue: Double, onComplete: () -> Unit) {
        val oldActiveDay = status?.diaAtivo ?: return

        // 1. Salva o faturamento do dia que está terminando
        FirebaseManager.saveDailyRevenue(oldActiveDay, revenue)

        // 2. CHAMA A NOVA FUNÇÃO PARA RESETAR OS TANQUES
        FirebaseManager.resetTanksCollection {
            // Este código só será executado DEPOIS que os tanques forem resetados

            // 3. Calcula o próximo dia
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(oldActiveDay)!!
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val newActiveDay = sdf.format(calendar.time)

            // 4. Atualiza o dia ativo no Firestore
            FirebaseManager.updateActiveDay(newActiveDay)

            // 5. Recarrega todos os dados do app para o novo dia (agora com apenas o Tanque 1)
            loadInitialData {
                onComplete()
            }
        }
    }

    fun addNewTank(onComplete: () -> Unit) {
        val tankName = "Tanque ${tanks.size + 1}"
        val newTank = Tank(name = tankName)

        FirebaseManager.addTank(newTank) { success ->
            if (success) {
                loadInitialData {
                    onComplete()
                }
            }
        }
    }
}