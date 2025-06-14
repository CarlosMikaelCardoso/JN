package com.example.jn

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TankManager {
    // Estado do App
    private var status: AppStatus? = null
    private var tanks = listOf<Tank>()
    var currentDayActivity: DailyActivity? = null

    // Variáveis para a nova lógica
    private var realActiveDay: String = ""
    private var viewedDay: String = ""

    // Carrega os dados iniciais necessários
    fun loadInitialData(onComplete: () -> Unit) {
        FirebaseManager.getAppStatus { appStatus ->
            status = appStatus
            // Inicializa ambos os dias com o valor do Firebase
            realActiveDay = appStatus?.diaAtivo ?: "N/A"
            viewedDay = appStatus?.diaAtivo ?: "N/A"

            val dayToLoad = viewedDay // Carrega os dados do dia que estamos vendo

            FirebaseManager.loadTanks { loadedTanks ->
                tanks = loadedTanks

                FirebaseManager.getDailyActivity(dayToLoad) { dailyActivity ->
                    currentDayActivity = dailyActivity
                    onComplete()
                }
            }
        }
    }

    // ##### NOVA FUNÇÃO #####
    // Muda apenas o dia de visualização, sem tocar no Firebase
    fun changeViewedDay(newDate: String, onComplete: () -> Unit) {
        viewedDay = newDate
        // Atualiza o status local para a UI refletir a data correta no título
        status = status?.copy(diaAtivo = newDate)

        // Busca as atividades do novo dia selecionado
        FirebaseManager.getDailyActivity(newDate) { dailyActivity ->
            currentDayActivity = dailyActivity
            onComplete()
        }
    }


    fun getTanks(): List<Tank> = tanks

    fun getBatidasForTank(tankId: String): List<Batida> {
        return currentDayActivity?.atividadesPorTanque?.get(tankId)?.batidas ?: emptyList()
    }

    fun getTankById(id: String): Tank? {
        return tanks.find { it.id == id }
    }

    fun getAcaiTypes(): List<String> {
        return listOf("Açaí Popular", "Açaí Médio", "Açaí Grosso", "Açaí Branco", "Bacaba")
    }

    // Retorna o dia que está sendo VISUALIZADO
    fun getActiveDay(): String = viewedDay

    // Retorna o dia de trabalho REAL
    fun getRealActiveDay(): String = realActiveDay

    fun addBatidaToTank(tankId: String, batida: Batida) {
        val activeDay = viewedDay // Adiciona a batida no dia que está vendo
        val tank = getTankById(tankId) ?: return

        FirebaseManager.addBatidaToDailyActivity(activeDay, tankId, tank.name, batida)

        if (currentDayActivity == null) {
            currentDayActivity = DailyActivity()
        }
        val tankActivity = currentDayActivity!!.atividadesPorTanque.getOrPut(tankId) {
            TankActivity(tankName = tank.name)
        }
        tankActivity.batidas.add(batida)
    }

    fun endDayAndStartNew(revenue: Double, onComplete: () -> Unit) {
        val oldActiveDay = realActiveDay // Usa o dia real

        // 1. Salva o faturamento
        FirebaseManager.saveDailyRevenue(oldActiveDay, revenue)

        // 2. Calcula o próximo dia
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(oldActiveDay)!!
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val newActiveDay = sdf.format(calendar.time)

        // 3. Atualiza o dia de trabalho REAL no Firebase
        FirebaseManager.updateActiveDay(newActiveDay) {
            // Após atualizar, recarrega tudo para o novo dia de trabalho
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
                // Recarrega os dados para mostrar o novo tanque
                loadInitialData {
                    onComplete()
                }
            }
        }
    }
}