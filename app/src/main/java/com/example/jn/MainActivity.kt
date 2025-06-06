package com.example.jn

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerTipoAcai: Spinner
    private lateinit var spinnerQuantidadeAcai: Spinner
    private lateinit var buttonAdicionar: Button

    // --- NOVOS COMPONENTES PARA O HISTÓRICO ---
    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private var currentTank: Tank? = null
    // -----------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_acai)

        // Pega o índice do tanque e o objeto Tank correspondente
        val tankIndex = intent.getIntExtra("TANK_INDEX", -1)
        currentTank = TankManager.getTankAt(tankIndex)

        if (currentTank == null) {
            Toast.makeText(this, "Erro: Tanque não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Inicializa os componentes da UI
        spinnerTipoAcai = findViewById(R.id.spinnerTipoAcai)
        spinnerQuantidadeAcai = findViewById(R.id.spinnerQuantidadeAcai)
        buttonAdicionar = findViewById(R.id.buttonAdicionar)
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory) // Pega a referência do RecyclerView

        // Popula os Spinners
        populateTypeSpinner()
        populateQuantitySpinner()

        // Configura a visualização do histórico
        setupHistoryView()

        buttonAdicionar.setOnClickListener {
            addAcaiOutput()
        }
    }

    // --- NOVO MÉTODO PARA CONFIGURAR O HISTÓRICO ---
    private fun setupHistoryView() {
        currentTank?.let { tank ->
            historyAdapter = HistoryAdapter(tank.outputs)
            recyclerViewHistory.adapter = historyAdapter
            recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        }
    }

    private fun populateTypeSpinner() {
        val acaiTypes = TankManager.getAcaiTypes()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, acaiTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoAcai.adapter = adapter
    }

    private fun populateQuantitySpinner() {
        val quantidadesAcai = arrayOf("0.5 L", "1.0 L", "1.5 L", "2.0 L", "2.5 L", "3.0 L", "3.5 L", "4.0 L", "4.5 L", "5.0 L")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quantidadesAcai)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerQuantidadeAcai.adapter = adapter
    }

    private fun addAcaiOutput() {
        val tipoSelecionado = spinnerTipoAcai.selectedItem.toString()
        val quantidadeSelecionadaString = spinnerQuantidadeAcai.selectedItem.toString()
        val valorNumerico = quantidadeSelecionadaString.replace(" L", "").toDoubleOrNull()

        if (valorNumerico != null) {
            currentTank?.let { tank ->
                val newOutput = AcaiOutput(type = tipoSelecionado, quantity = valorNumerico)
                tank.outputs.add(newOutput)

                // --- ATUALIZAÇÃO EM TEMPO REAL ---
                // Notifica o adapter que um novo item foi inserido na última posição.
                historyAdapter.notifyItemInserted(tank.outputs.size - 1)
                // Rola a lista para que o novo item fique visível.
                recyclerViewHistory.scrollToPosition(tank.outputs.size - 1)
                // -----------------------------------

                Toast.makeText(this, "Saída adicionada!", Toast.LENGTH_SHORT).show()

                // MUDANÇA: Não vamos mais fechar a tela automaticamente.
                // O usuário agora pode adicionar várias saídas e ver o histórico crescer.
                // finish() // Linha removida!

            } ?: run {
                Toast.makeText(this, "Erro ao encontrar o tanque.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}