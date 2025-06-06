package com.example.jn

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    // --- Componentes da Tela Principal ---
    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var fabAddBatch: FloatingActionButton
    private lateinit var textViewTitle: TextView

    // --- Dados ---
    private var tankId: String? = null
    private var currentTank: Tank? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_acai)

        // Recebe o ID do tanque, que é uma String.
        tankId = intent.getStringExtra("TANK_ID")
        if (tankId == null) {
            Toast.makeText(this, "Erro: ID do tanque inválido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initializeViews()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        // Busca o tanque pelo ID.
        currentTank = TankManager.getTankById(tankId!!)
        if (currentTank == null) {
            Toast.makeText(this, "Erro: Tanque não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        textViewTitle.text = "Histórico do ${currentTank?.name}"
        setupRecyclerViews()
    }

    private fun initializeViews() {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory)
        fabAddBatch = findViewById(R.id.fabAddBatch)
        textViewTitle = findViewById(R.id.textViewTankHistoryTitle)
        textViewTitle.text = "Histórico do ${currentTank?.name}"
    }

    private fun setupRecyclerViews() {
        // Busca as batidas do dia para o tanque atual
        val dailyBatidas = TankManager.getBatidasForTank(tankId ?: "")
        historyAdapter = HistoryAdapter(dailyBatidas)
        recyclerViewHistory.adapter = historyAdapter
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)
    }

    private fun setupFab() {
        fabAddBatch.setOnClickListener {
            showAddBatchDialog()
        }
    }

    private fun showAddBatchDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_batch, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()

        // --- Componentes DENTRO do Diálogo ---
        val spinnerTipo: Spinner = dialogView.findViewById(R.id.spinnerTipoAcaiDialog)
        val spinnerQuantidade: Spinner = dialogView.findViewById(R.id.spinnerQuantidadeAcaiDialog)
        val btnAddItem: Button = dialogView.findViewById(R.id.buttonAddItemDialog)
        val btnSaveBatch: Button = dialogView.findViewById(R.id.buttonSaveBatchDialog)
        val btnCancel: Button = dialogView.findViewById(R.id.buttonCancelDialog)
        val rvCurrentBatch: RecyclerView = dialogView.findViewById(R.id.recyclerViewCurrentBatchDialog)

        // --- Lógica DENTRO do Diálogo ---
        val currentBatchItemsInDialog = mutableListOf<AcaiOutput>()
        val currentBatchAdapter = CurrentBatchAdapter(currentBatchItemsInDialog)
        rvCurrentBatch.adapter = currentBatchAdapter
        rvCurrentBatch.layoutManager = LinearLayoutManager(this)

        // Popula os spinners do diálogo
        populateSpinner(spinnerTipo, TankManager.getAcaiTypes().toTypedArray())
        val quantidades = arrayOf("0.5 L", "1.0 L", "1.5 L", "2.0 L", "2.5 L", "3.0 L", "3.5 L", "4.0 L", "4.5 L", "5.0 L")
        populateSpinner(spinnerQuantidade, quantidades)

        btnAddItem.setOnClickListener {
            val tipo = spinnerTipo.selectedItem.toString()
            val quantidadeStr = spinnerQuantidade.selectedItem.toString()
            val quantidade = quantidadeStr.replace(" L", "").toDoubleOrNull()

            if (quantidade != null && quantidade > 0) {
                currentBatchItemsInDialog.add(AcaiOutput(tipo, quantidade))
                currentBatchAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Selecione uma quantidade válida.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSaveBatch.setOnClickListener {
            if (currentBatchItemsInDialog.isEmpty()) {
                Toast.makeText(this, "Adicione pelo menos um item à batida.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newBatida = Batida(items = ArrayList(currentBatchItemsInDialog))

            // Usa o tankId para adicionar a batida
            tankId?.let { id ->
                TankManager.addBatidaToTank(id, newBatida)
            }

            Toast.makeText(this, "Batida salva com sucesso!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // Função auxiliar para popular qualquer spinner
    private fun populateSpinner(spinner: Spinner, data: Array<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
}