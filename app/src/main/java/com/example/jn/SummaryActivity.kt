package com.example.jn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

class SummaryActivity : AppCompatActivity() {

    private lateinit var layoutCalculator: LinearLayout
    private lateinit var buttonCalculate: Button
    private lateinit var textViewTotalLiters: TextView
    private lateinit var textViewTotalRevenue: TextView

    private val summaryRows = mutableListOf<Pair<AcaiTypeSummary, View>>()

    data class AcaiTypeSummary(val type: String, val totalLiters: Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        layoutCalculator = findViewById(R.id.layoutRevenueCalculator)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        textViewTotalLiters = findViewById(R.id.textViewTotalLiters)
        textViewTotalRevenue = findViewById(R.id.textViewTotalRevenue)

        setupSummaryData()

        buttonCalculate.setOnClickListener {
            calculateFinalRevenue()
        }
    }

    private fun setupSummaryData() {
        summaryRows.clear()
        layoutCalculator.removeAllViews()

        val activity = TankManager.currentDayActivity
        if (activity == null) {
            textViewTotalLiters.text = "Sem atividade registrada hoje"
            return
        }
        val allOutputs = activity.atividadesPorTanque.values
            .flatMap { tankActivity -> tankActivity.batidas }
            .flatMap { batida -> batida.items }

        val totalLitersOverall = allOutputs.sumOf { it.quantity }
        textViewTotalLiters.text = "Total de Litros Batidos: %.1f L".format(totalLitersOverall)

        val totalsByType = allOutputs
            .groupBy { it.type }
            .map { (type, outputs) ->
                AcaiTypeSummary(type, outputs.sumOf { it.quantity })
            }

        totalsByType.forEach { summaryData ->
            val inflater = LayoutInflater.from(this)
            val rowView = inflater.inflate(R.layout.item_summary_row, layoutCalculator, false)

            val typeInfoTextView: TextView = rowView.findViewById(R.id.textViewSummaryTypeInfo)
            typeInfoTextView.text = "${summaryData.type}: %.1f L".format(summaryData.totalLiters)

            layoutCalculator.addView(rowView)
            summaryRows.add(Pair(summaryData, rowView))
        }
    }

    private fun calculateFinalRevenue() {
        var grandTotalRevenue = 0.0

        summaryRows.forEach { (summaryData, rowView) ->
            val priceEditText: EditText = rowView.findViewById(R.id.editTextPrice)
            val revenueTextView: TextView = rowView.findViewById(R.id.textViewRevenue)

            val priceString = priceEditText.text.toString()
            val price = priceString.toDoubleOrNull() ?: 0.0

            val revenueForType = summaryData.totalLiters * price
            grandTotalRevenue += revenueForType

            revenueTextView.text = formatCurrency(revenueForType)
        }

        textViewTotalRevenue.text = "Rendimento Total do Dia: ${formatCurrency(grandTotalRevenue)}"
    }

    private fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return format.format(value)
    }
}