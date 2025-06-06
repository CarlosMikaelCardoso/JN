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

    // Guarda os dados calculados e as views para fácil acesso
    private val summaryRows = mutableListOf<Pair<AcaiTypeSummary, View>>()

    // Estrutura para guardar o resumo por tipo
    data class AcaiTypeSummary(val type: String, val totalLiters: Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // Inicializa as views principais
        layoutCalculator = findViewById(R.id.layoutRevenueCalculator)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        textViewTotalLiters = findViewById(R.id.textViewTotalLiters)
        textViewTotalRevenue = findViewById(R.id.textViewTotalRevenue)

        // Prepara os dados e constrói a UI
        setupSummaryData()

        buttonCalculate.setOnClickListener {
            calculateFinalRevenue()
        }
    }

    private fun setupSummaryData() {
        // 1. Pega todas as saídas de todos os tanques
        val allOutputs = TankManager.getTanks().flatMap { it.outputs }

        // 2. Calcula o total de litros geral
        val totalLitersOverall = allOutputs.sumOf { it.quantity }
        textViewTotalLiters.text = "Total de Litros Batidos: %.1f L".format(totalLitersOverall)

        // 3. Agrupa por tipo e calcula o total de litros para cada um
        val totalsByType = allOutputs
            .groupBy { it.type }
            .map { (type, outputs) ->
                AcaiTypeSummary(type, outputs.sumOf { it.quantity })
            }

        // 4. Cria uma linha na UI para cada tipo de açaí
        totalsByType.forEach { summaryData ->
            val inflater = LayoutInflater.from(this)
            val rowView = inflater.inflate(R.layout.item_summary_row, layoutCalculator, false)

            val typeInfoTextView: TextView = rowView.findViewById(R.id.textViewSummaryTypeInfo)
            typeInfoTextView.text = "${summaryData.type}: %.1f L".format(summaryData.totalLiters)

            // Adiciona a linha ao layout e guarda a referência
            layoutCalculator.addView(rowView)
            summaryRows.add(Pair(summaryData, rowView))
        }
    }

    private fun calculateFinalRevenue() {
        var grandTotalRevenue = 0.0

        // Itera por cada linha de resumo que criamos
        summaryRows.forEach { (summaryData, rowView) ->
            val priceEditText: EditText = rowView.findViewById(R.id.editTextPrice)
            val revenueTextView: TextView = rowView.findViewById(R.id.textViewRevenue)

            val priceString = priceEditText.text.toString()
            val price = priceString.toDoubleOrNull() ?: 0.0 // Se o campo estiver vazio, considera o preço 0

            val revenueForType = summaryData.totalLiters * price
            grandTotalRevenue += revenueForType

            // Formata o valor como moeda local (BRL)
            revenueTextView.text = formatCurrency(revenueForType)
        }

        // Atualiza o texto do rendimento total
        textViewTotalRevenue.text = "Rendimento Total do Dia: ${formatCurrency(grandTotalRevenue)}"
    }

    private fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return format.format(value)
    }
}