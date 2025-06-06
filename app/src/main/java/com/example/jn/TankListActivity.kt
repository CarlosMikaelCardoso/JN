package com.example.jn

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.Locale

class TankListActivity : AppCompatActivity() {

    // --- Componentes da Tela de Tanques ---
    private lateinit var tanksContentLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var tankAdapter: TankAdapter
    private lateinit var fabAddTank: FloatingActionButton

    // --- Componentes da Tela de Resumo ---
    private lateinit var summaryContentLayout: ScrollView
    private lateinit var layoutCalculator: LinearLayout
    private lateinit var buttonCalculate: Button
    private lateinit var textViewTotalLiters: TextView
    private lateinit var textViewTotalRevenue: TextView
    private val summaryRows = mutableListOf<Pair<SummaryActivity.AcaiTypeSummary, View>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tank_list)

        // Inicializa todos os componentes da UI de ambas as telas
        initializeViews()

        // Configura a tela inicial (lista de tanques)
        setupTankListView()

        // Configura o listener da navegação inferior
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        // Este metod será chamado toda vez que a tela voltar ao foco.
        // Verificamos se a visualização da lista de tanques está ativa.
        if (tanksContentLayout.visibility == View.VISIBLE) {
            // Se estiver, atualizamos o adaptador com os dados mais recentes do TankManager.
            // Isso força o RecyclerView a se redesenhar.
            tankAdapter.updateData(TankManager.getTanks())
        }
    }

    private fun initializeViews() {
        // Views da Lista de Tanques
        tanksContentLayout = findViewById(R.id.tanks_content_layout)
        recyclerView = findViewById(R.id.recyclerViewTanks)
        fabAddTank = findViewById(R.id.fabAddTank)

        // Views do Resumo
        summaryContentLayout = findViewById(R.id.summary_content_layout)
        layoutCalculator = findViewById(R.id.layoutRevenueCalculator)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        textViewTotalLiters = findViewById(R.id.textViewTotalLiters)
        textViewTotalRevenue = findViewById(R.id.textViewTotalRevenue)
    }

    private fun setupTankListView() {
        // Configura o RecyclerView
        val tanks = TankManager.getTanks()
        tankAdapter = TankAdapter(tanks) { position ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("TANK_INDEX", position)
            startActivity(intent)
        }
        recyclerView.adapter = tankAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configura o clique do botão para adicionar um novo tanque
        fabAddTank.setOnClickListener {
            TankManager.addNewTank()
            tankAdapter.updateData(TankManager.getTanks())
        }
    }

    private fun setupSummaryView() {
        // Limpa dados e views antigas antes de reconstruir
        summaryRows.clear()
        layoutCalculator.removeAllViews()

        val allOutputs = TankManager.getTanks().flatMap { it.outputs }
        val totalLitersOverall = allOutputs.sumOf { it.quantity }
        textViewTotalLiters.text = "Total de Litros Batidos: %.1f L".format(totalLitersOverall)

        // Zera o resultado final ao reabrir a aba
        textViewTotalRevenue.text = "Rendimento Total do Dia: R$ 0.00"

        val totalsByType = allOutputs
            .groupBy { it.type }
            .map { (type, outputs) ->
                SummaryActivity.AcaiTypeSummary(type, outputs.sumOf { it.quantity })
            }

        totalsByType.forEach { summaryData ->
            val rowView = LayoutInflater.from(this).inflate(R.layout.item_summary_row, layoutCalculator, false)
            val typeInfoTextView: TextView = rowView.findViewById(R.id.textViewSummaryTypeInfo)
            typeInfoTextView.text = "${summaryData.type}: %.1f L".format(summaryData.totalLiters)

            layoutCalculator.addView(rowView)
            summaryRows.add(Pair(summaryData, rowView))
        }

        buttonCalculate.setOnClickListener {
            calculateFinalRevenue()
        }
    }

    private fun calculateFinalRevenue() {
        var grandTotalRevenue = 0.0
        summaryRows.forEach { (summaryData, rowView) ->
            val priceEditText: EditText = rowView.findViewById(R.id.editTextPrice)
            val revenueTextView: TextView = rowView.findViewById(R.id.textViewRevenue)
            val price = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
            val revenueForType = summaryData.totalLiters * price
            grandTotalRevenue += revenueForType
            revenueTextView.text = formatCurrency(revenueForType)
        }
        textViewTotalRevenue.text = "Rendimento Total do Dia: ${formatCurrency(grandTotalRevenue)}"
    }

    private fun formatCurrency(value: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
    }

    private fun setupBottomNavigation() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_tanks -> {
                    tanksContentLayout.visibility = View.VISIBLE
                    fabAddTank.visibility = View.VISIBLE // Mostra o botão de adicionar
                    summaryContentLayout.visibility = View.GONE
                    // Atualiza a lista de tanques caso algo tenha mudado
                    tankAdapter.updateData(TankManager.getTanks())
                    true
                }
                R.id.navigation_summary -> {
                    tanksContentLayout.visibility = View.GONE
                    fabAddTank.visibility = View.GONE // Esconde o botão de adicionar
                    summaryContentLayout.visibility = View.VISIBLE
                    // Prepara a tela de resumo com os dados mais recentes
                    setupSummaryView()
                    true
                }
                else -> false
            }
        }
    }
}