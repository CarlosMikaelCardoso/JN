package com.example.jn

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseManager {

    private val db = FirebaseFirestore.getInstance()
    private val tanksCollection = db.collection("tanks")
    private val dailyActivityCollection = db.collection("atividades_diarias")
    private val statusDocument = db.collection("app_status").document("status")

    // --- Funções de Status ---
    fun getAppStatus(onComplete: (AppStatus?) -> Unit) {
        statusDocument.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    onComplete(snapshot.toObject<AppStatus>())
                } else {
                    // Se não existe, cria com a data de hoje
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val newStatus = AppStatus(diaAtivo = today)
                    statusDocument.set(newStatus).addOnSuccessListener {
                        onComplete(newStatus)
                    }
                }
            }
            .addOnFailureListener { onComplete(null) }
    }

    fun updateActiveDay(newDate: String) {
        statusDocument.update("diaAtivo", newDate)
    }

    // --- Funções de Tanque (sem alteração) ---
    fun loadTanks(onDataLoaded: (List<Tank>) -> Unit) {
        tanksCollection.get().addOnSuccessListener { snapshot ->
            onDataLoaded(snapshot.toObjects<Tank>())
        }.addOnFailureListener { onDataLoaded(emptyList()) }
    }

    fun addTank(tank: Tank, onComplete: (Boolean) -> Unit) {
        tanksCollection.add(tank)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // --- Funções de Atividade Diária ---
    fun getDailyActivity(date: String, onComplete: (DailyActivity?) -> Unit) {
        dailyActivityCollection.document(date).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    onComplete(snapshot.toObject<DailyActivity>())
                } else {
                    // Se o dia não tem atividade, retorna um objeto vazio
                    onComplete(DailyActivity())
                }
            }
            .addOnFailureListener { onComplete(null) }
    }

    fun addBatidaToDailyActivity(date: String, tankId: String, batida: Batida) {
        val activityDocument = dailyActivityCollection.document(date)
        // A chave agora usa "dot notation" para ir até o array correto dentro do mapa
        val fieldPath = "batidasPorTanque.$tankId"
        activityDocument.update(fieldPath, FieldValue.arrayUnion(batida))
            .addOnFailureListener {
                // Se o update falhar (ex: o campo não existe), usamos set com merge=true
                activityDocument.set(mapOf(
                    "batidasPorTanque" to mapOf(tankId to listOf(batida))
                ), com.google.firebase.firestore.SetOptions.merge())
            }
    }

    fun saveDailyRevenue(date: String, revenue: Double) {
        dailyActivityCollection.document(date).update("faturamentoTotal", revenue)
    }

    // NOVA FUNÇÃO PARA RESETAR A COLEÇÃO DE TANQUES
    fun resetTanksCollection(onComplete: () -> Unit) {
        // Pega todos os documentos da coleção 'tanks'
        tanksCollection.get()
            .addOnSuccessListener { querySnapshot ->
                // 1. Inicia uma operação em lote (batch)
                val batch: WriteBatch = db.batch()

                // 2. Adiciona uma operação de delete para cada tanque existente no lote
                for (document in querySnapshot) {
                    batch.delete(document.reference)
                }

                // 3. Adiciona uma operação de criação para o novo "Tanque 1" no lote
                val newTank = Tank(name = "Tanque 1")
                val newTankRef = tanksCollection.document() // Cria uma referência para um novo documento
                batch.set(newTankRef, newTank)

                // 4. Executa todas as operações (deletes e create) de uma vez só
                batch.commit()
                    .addOnSuccessListener {
                        println("Coleção de tanques resetada com sucesso.")
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        println("Erro ao resetar coleção de tanques: ${e.message}")
                        // Mesmo em caso de falha, chama o onComplete para não travar o app
                        onComplete()
                    }
            }
            .addOnFailureListener { e ->
                println("Erro ao buscar tanques para deletar: ${e.message}")
                onComplete()
            }
    }
}