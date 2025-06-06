package com.example.jn

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.Locale
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar

class TankListActivity : AppCompatActivity() {

    // --- Componentes ---
    private lateinit var tanksContentLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var tankAdapter: TankAdapter
    private lateinit var fabAddTank: FloatingActionButton
    private lateinit var summaryContentLayout: ScrollView
    private lateinit var layoutCalculator: LinearLayout
    private lateinit var buttonCalculate: Button
    private lateinit var textViewTotalLiters: TextView
    private lateinit var textViewTotalRevenue: TextView

    // --- Dados ---
    private val summaryRows = mutableListOf<Pair<AcaiTypeSummary, View>>()
    private var lastCalculatedRevenue: Double = 0.0

    data class AcaiTypeSummary(val type: String, val totalLiters: Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tank_list)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        initializeViews()
        showLoading(true)

        TankManager.loadInitialData {
            runOnUiThread {
                setupTankListView()
                setupBottomNavigation()
                supportActionBar?.title = "Dia Ativo: ${TankManager.getActiveDay()}"
                showLoading(false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::tankAdapter.isInitialized) {
            tankAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_end_day -> {
                showEndDayConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    private fun showLoading(isLoading: Boolean) {
        tanksContentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        summaryContentLayout.visibility = View.GONE
    }

    private fun setupTankListView() {
        val tanks = TankManager.getTanks()
        tankAdapter = TankAdapter(tanks) { tank ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("TANK_ID", tank.id)
            startActivity(intent)
        }
        recyclerView.adapter = tankAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAddTank.setOnClickListener {
            showLoading(true)
            TankManager.addNewTank {
                runOnUiThread {
                    tankAdapter.updateData(TankManager.getTanks())
                    showLoading(false)
                }
            }
        }
    }

    private fun getAllOutputsForToday(): List<AcaiOutput> {
        val activity = TankManager.currentDayActivity ?: return emptyList()
        // 1. Acessa a propriedade correta: 'atividadesPorTanque'
        // 2. Para cada 'tankActivity', pega a sua lista de 'batidas'
        // 3. Para cada 'batida', pega a sua lista de 'items'
        return activity.atividadesPorTanque.values
            .flatMap { tankActivity -> tankActivity.batidas }
            .flatMap { batida -> batida.items }
    }

    private fun setupSummaryView() {
        summaryRows.clear()
        layoutCalculator.removeAllViews()
        lastCalculatedRevenue = 0.0

        val allOutputs = getAllOutputsForToday()
        val totalLitersOverall = allOutputs.sumOf { it.quantity }
        textViewTotalLiters.text = "Total de Litros Batidos: %.1f L".format(totalLitersOverall)
        textViewTotalRevenue.text = "Rendimento Total do Dia: R$ 0.00"

        val totalsByType = allOutputs
            .groupBy { it.type }
            .map { (type, outputs) -> AcaiTypeSummary(type, outputs.sumOf { it.quantity }) }

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
        lastCalculatedRevenue = grandTotalRevenue
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
                    summaryContentLayout.visibility = View.GONE
                    true
                }
                R.id.navigation_summary -> {
                    tanksContentLayout.visibility = View.GONE
                    summaryContentLayout.visibility = View.VISIBLE
                    setupSummaryView()
                    true
                }
                else -> false
            }
        }
    }

    private fun showEndDayConfirmationDialog() {
        val totalRevenue = lastCalculatedRevenue

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar Encerramento do Dia")
        builder.setMessage("O faturamento total calculado foi de R$ %.2f.\n\nTem certeza que deseja encerrar o dia? Esta ação não pode ser desfeita.".format(totalRevenue))

        builder.setPositiveButton("Sim, Encerrar") { _, _ ->
            showLoading(true)
            TankManager.endDayAndStartNew(totalRevenue) {
                runOnUiThread {
                    tankAdapter.updateData(TankManager.getTanks())
                    supportActionBar?.title = "Dia Ativo: ${TankManager.getActiveDay()}"
                    showLoading(false)
                }
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }
}