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
    private val summaryRows = mutableListOf<Pair<AcaiTypeSummary, View>>()
    // Estrutura para guardar o resumo por tipo
    data class AcaiTypeSummary(val type: String, val totalLiters: Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tank_list)

        initializeViews()
        setupTankListView()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        if (tanksContentLayout.visibility == View.VISIBLE) {
            tankAdapter.updateData(TankManager.getTanks())
        }
    }

    private fun initializeViews() {
        tanksContentLayout = findViewById(R.id.tanks_content_layout)
        recyclerView = findViewById(R.id.recyclerViewTanks)
        fabAddTank = findViewById(R.id.fabAddTank)

        summaryContentLayout = findViewById(R.id.summary_content_layout)
        layoutCalculator = findViewById(R.id.layoutRevenueCalculator)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        textViewTotalLiters = findViewById(R.id.textViewTotalLiters)
        textViewTotalRevenue = findViewById(R.id.textViewTotalRevenue)
    }

    private fun setupTankListView() {
        val tanks = TankManager.getTanks()
        tankAdapter = TankAdapter(tanks) { position ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("TANK_INDEX", position)
            startActivity(intent)
        }
        recyclerView.adapter = tankAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAddTank.setOnClickListener {
            TankManager.addNewTank()
            tankAdapter.updateData(TankManager.getTanks())
        }
    }

    private fun setupSummaryView() {
        summaryRows.clear()
        layoutCalculator.removeAllViews()

        // **AJUSTE AQUI**
        val allOutputs = TankManager.getTanks().flatMap { it.batidas }.flatMap { it.items }
        val totalLitersOverall = allOutputs.sumOf { it.quantity }
        textViewTotalLiters.text = "Total de Litros Batidos: %.1f L".format(totalLitersOverall)

        textViewTotalRevenue.text = "Rendimento Total do Dia: R$ 0.00"

        val totalsByType = allOutputs
            .groupBy { it.type }
            .map { (type, outputs) ->
                AcaiTypeSummary(type, outputs.sumOf { it.quantity })
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
                    fabAddTank.visibility = View.VISIBLE
                    summaryContentLayout.visibility = View.GONE
                    tankAdapter.updateData(TankManager.getTanks())
                    true
                }
                R.id.navigation_summary -> {
                    tanksContentLayout.visibility = View.GONE
                    fabAddTank.visibility = View.GONE
                    summaryContentLayout.visibility = View.VISIBLE
                    setupSummaryView()
                    true
                }
                else -> false
            }
        }
    }
}