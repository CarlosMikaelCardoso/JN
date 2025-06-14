package com.example.jn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButtonToggleGroup


class MainActivity : AppCompatActivity() {

    // --- Componentes da Tela Principal ---
    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var fabAddBatch: FloatingActionButton
    private lateinit var textViewTitle: TextView

    // --- Dados ---
    private var tankId: String? = null
    private var currentTank: Tank? = null
    // Nova variável para controlar a modificação
    private var isModifiable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_acai)

        tankId = intent.getStringExtra("TANK_ID")
        // Pega o valor passado pela TankListActivity
        isModifiable = intent.getBooleanExtra("IS_MODIFIABLE", false)

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
        val dailyBatidas = TankManager.getBatidasForTank(tankId ?: "")
        historyAdapter = HistoryAdapter(dailyBatidas)
        recyclerViewHistory.adapter = historyAdapter
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)
    }

    // Lógica de bloqueio para o botão de adicionar
    private fun setupFab() {
        if (isModifiable) {
            fabAddBatch.visibility = View.VISIBLE
            fabAddBatch.setOnClickListener {
                showAddBatchDialog()
            }
        } else {
            fabAddBatch.visibility = View.GONE
        }
    }

    private fun showAddBatchDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_batch, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()

        // Usando ChipGroup, que corresponde ao layout final
        val chipGroupTipo: ChipGroup = dialogView.findViewById(R.id.chipGroupTipoAcai)
        val chipGroupQuantidade: ChipGroup = dialogView.findViewById(R.id.chipGroupQuantidade)

        val btnAddItem: Button = dialogView.findViewById(R.id.buttonAddItemDialog)
        val btnSaveBatch: Button = dialogView.findViewById(R.id.buttonSaveBatchDialog)
        val btnCancel: Button = dialogView.findViewById(R.id.buttonCancelDialog)
        val rvCurrentBatch: RecyclerView = dialogView.findViewById(R.id.recyclerViewCurrentBatchDialog)

        val currentBatchItemsInDialog = mutableListOf<AcaiOutput>()
        val currentBatchAdapter = CurrentBatchAdapter(currentBatchItemsInDialog)
        rvCurrentBatch.adapter = currentBatchAdapter
        rvCurrentBatch.layoutManager = LinearLayoutManager(this)

        btnAddItem.setOnClickListener {
            // Lógica correta para ChipGroup
            val selectedTipoId = chipGroupTipo.checkedChipId
            val selectedQuantidadeId = chipGroupQuantidade.checkedChipId

            if (selectedTipoId == View.NO_ID || selectedQuantidadeId == View.NO_ID) {
                Toast.makeText(this, "Por favor, selecione um tipo e uma quantidade.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val chipTipo = dialogView.findViewById<Chip>(selectedTipoId)
            val chipQuantidade = dialogView.findViewById<Chip>(selectedQuantidadeId)

            val tipo = chipTipo.text.toString()
            val quantidadeStr = chipQuantidade.text.toString()
            val quantidade = quantidadeStr.replace(" L", "").toDoubleOrNull()

            if (quantidade != null && quantidade > 0) {
                currentBatchItemsInDialog.add(AcaiOutput(tipo, quantidade))
                currentBatchAdapter.notifyDataSetChanged()
                chipGroupTipo.clearCheck()
                chipGroupQuantidade.clearCheck()
            } else {
                Toast.makeText(this, "Erro ao processar a quantidade.", Toast.LENGTH_SHORT).show()
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
            tankId?.let { id ->
                TankManager.addBatidaToTank(id, newBatida)
            }
            onResume()
            Toast.makeText(this, "Batida salva com sucesso!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun populateSpinner(spinner: Spinner, data: Array<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
}