package com.example.jn

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButtonToggleGroup


class TankListActivity : AppCompatActivity() {

    // --- Componentes ---
    private lateinit var tanksContentLayout: LinearLayout
    private lateinit var recyclerViewTanks: RecyclerView
    private lateinit var tankAdapter: TankAdapter
    private lateinit var deliveriesContentLayout: LinearLayout
    private lateinit var recyclerViewDeliveries: RecyclerView
    private lateinit var deliveryAdapter: DeliveryAdapter
    private lateinit var summaryContentLayout: ScrollView
    private lateinit var layoutCalculator: LinearLayout
    private lateinit var buttonCalculate: Button
    private lateinit var textViewTotalLiters: TextView
    private lateinit var textViewTotalRevenue: TextView
    private lateinit var fabAdd: FloatingActionButton

    // --- Dados ---
    private val summaryRows = mutableListOf<Pair<AcaiTypeSummary, View>>()
    private var lastCalculatedRevenue: Double = 0.0
    private var isCurrentDayModifiable: Boolean = false

    data class AcaiTypeSummary(val type: String, val totalLiters: Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tank_list)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        initializeViews()
        showLoading(true)
        setupTankListView()
        setupDeliveryListView()
        TankManager.loadInitialData {
            runOnUiThread {
                updateUiForDate()
                setupBottomNavigation()
                showLoading(false, R.id.navigation_tanks)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::tankAdapter.isInitialized) {
            updateUiForDate()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val endDayItem = menu.findItem(R.id.action_end_day)
        endDayItem.isVisible = isCurrentDayModifiable
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_end_day -> {
                showEndDayConfirmationDialog(); true
            }
            R.id.action_change_day -> {
                showDatePickerDialog(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUiForDate() {
        val realActiveDay = TankManager.getRealActiveDay()
        val currentlyViewedDay = TankManager.getActiveDay()

        // A lógica de comparação agora é perfeita
        isCurrentDayModifiable = (realActiveDay == currentlyViewedDay)
        supportActionBar?.title = "Dia: $currentlyViewedDay"

        fabAdd.visibility = if (isCurrentDayModifiable) View.VISIBLE else View.GONE
        invalidateOptionsMenu()

        tankAdapter.updateData(TankManager.getTanks())
        deliveryAdapter.setModifiable(isCurrentDayModifiable)

        val selectedId = findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId
        when (selectedId) {
            R.id.navigation_deliveries -> loadDeliveriesForActiveDay()
            R.id.navigation_summary -> setupSummaryView()
        }
    }

    private fun initializeViews() {
        tanksContentLayout = findViewById(R.id.tanks_content_layout)
        recyclerViewTanks = findViewById(R.id.recyclerViewTanks)
        deliveriesContentLayout = findViewById(R.id.deliveries_content_layout)
        recyclerViewDeliveries = findViewById(R.id.recyclerViewDeliveries)
        summaryContentLayout = findViewById(R.id.summary_content_layout)
        layoutCalculator = findViewById(R.id.layoutRevenueCalculator)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        textViewTotalLiters = findViewById(R.id.textViewTotalLiters)
        textViewTotalRevenue = findViewById(R.id.textViewTotalRevenue)
        fabAdd = findViewById(R.id.fabAdd)
    }

    private fun showLoading(isLoading: Boolean, activeViewId: Int? = null) {
        findViewById<View>(R.id.loadingIndicator).visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            tanksContentLayout.visibility = View.GONE
            deliveriesContentLayout.visibility = View.GONE
            summaryContentLayout.visibility = View.GONE
        } else if (activeViewId != null) {
            tanksContentLayout.visibility = if (activeViewId == R.id.navigation_tanks) View.VISIBLE else View.GONE
            deliveriesContentLayout.visibility = if (activeViewId == R.id.navigation_deliveries) View.VISIBLE else View.GONE
            summaryContentLayout.visibility = if (activeViewId == R.id.navigation_summary) View.VISIBLE else View.GONE
        }
    }

    private fun setupTankListView() {
        tankAdapter = TankAdapter(emptyList()) { tank ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("TANK_ID", tank.id)
            intent.putExtra("IS_MODIFIABLE", isCurrentDayModifiable)
            startActivity(intent)
        }
        recyclerViewTanks.adapter = tankAdapter
        recyclerViewTanks.layoutManager = LinearLayoutManager(this)
    }

    private fun setupDeliveryListView() {
        deliveryAdapter = DeliveryAdapter(mutableListOf()) { delivery ->
            if(isCurrentDayModifiable) {
                showAddDeliveryDialog(delivery)
            } else {
                Toast.makeText(this, "Não é possível editar entregas de dias anteriores.", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerViewDeliveries.adapter = deliveryAdapter
        recyclerViewDeliveries.layoutManager = LinearLayoutManager(this)
    }

    private fun loadDeliveriesForActiveDay() {
        val activeDay = TankManager.getActiveDay()
        FirebaseManager.loadDeliveries(activeDay) { deliveries ->
            runOnUiThread { deliveryAdapter.updateData(deliveries) }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            updateUiForDate()
            when (item.itemId) {
                R.id.navigation_tanks -> {
                    showLoading(false, R.id.navigation_tanks)
                    setupFabListener(R.id.navigation_tanks)
                    true
                }
                R.id.navigation_deliveries -> {
                    showLoading(false, R.id.navigation_deliveries)
                    setupFabListener(R.id.navigation_deliveries)
                    loadDeliveriesForActiveDay()
                    true
                }
                R.id.navigation_summary -> {
                    showLoading(false, R.id.navigation_summary)
                    fabAdd.visibility = View.GONE
                    setupSummaryView()
                    true
                }
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.navigation_tanks
        setupFabListener(R.id.navigation_tanks)
    }

    private fun setupFabListener(currentViewId: Int) {
        fabAdd.setOnClickListener {
            when (currentViewId) {
                R.id.navigation_tanks -> {
                    showLoading(true)
                    TankManager.addNewTank {
                        runOnUiThread {
                            updateUiForDate()
                            showLoading(false, R.id.navigation_tanks)
                        }
                    }
                }
                R.id.navigation_deliveries -> {
                    showAddDeliveryDialog()
                }
            }
        }
    }

    private fun showAddDeliveryDialog(deliveryToEdit: Delivery? = null) {
        if (!isCurrentDayModifiable && deliveryToEdit == null) {
            Toast.makeText(this, "Não é possível adicionar entregas em dias anteriores.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_delivery, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val currentAcaiItems = mutableListOf<AcaiOutput>()
        if (deliveryToEdit != null) {
            currentAcaiItems.addAll(deliveryToEdit.acaiItems)
        }

        val rvAcaiItems: RecyclerView = dialogView.findViewById(R.id.recyclerViewAcaiItems)
        val currentBatchAdapter = CurrentBatchAdapter(currentAcaiItems)
        rvAcaiItems.adapter = currentBatchAdapter
        rvAcaiItems.layoutManager = LinearLayoutManager(this)

        val btnAddAcaiItem: Button = dialogView.findViewById(R.id.buttonAddAcaiItem)
        btnAddAcaiItem.setOnClickListener {
            showAddAcaiItemSubDialog { acaiOutput ->
                currentAcaiItems.add(acaiOutput)
                currentBatchAdapter.notifyDataSetChanged()
            }
        }

        val clientNameEditText = dialogView.findViewById<EditText>(R.id.editTextClientName)
        val addressEditText = dialogView.findViewById<EditText>(R.id.editTextAddress)
        val buildingNameEditText = dialogView.findViewById<EditText>(R.id.editTextBuildingName)
        val houseNumberEditText = dialogView.findViewById<EditText>(R.id.editTextHouseNumber)
        val paymentMethodEditText = dialogView.findViewById<EditText>(R.id.editTextPaymentMethod)
        val tapiocaQtyEditText = dialogView.findViewById<EditText>(R.id.editTextTapioca)
        val farinhaQtyEditText = dialogView.findViewById<EditText>(R.id.editTextFarinha)
        val observationEditText = dialogView.findViewById<EditText>(R.id.editTextObservation)

        if (deliveryToEdit != null) {
            clientNameEditText.setText(deliveryToEdit.clientName)
            addressEditText.setText(deliveryToEdit.address)
            buildingNameEditText.setText(deliveryToEdit.buildingName)
            houseNumberEditText.setText(deliveryToEdit.houseNumber)
            paymentMethodEditText.setText(deliveryToEdit.paymentMethod)
            tapiocaQtyEditText.setText(deliveryToEdit.tapiocaQuantity.toString())
            farinhaQtyEditText.setText(deliveryToEdit.farinhaDaguaQuantity.toString())
            observationEditText.setText(deliveryToEdit.observation)
        }

        builder.setTitle(if (deliveryToEdit == null) "Nova Entrega" else "Editar Entrega")
        builder.setPositiveButton("Salvar") { dialog, _ ->
            val clientName = clientNameEditText.text.toString()
            val address = addressEditText.text.toString()
            val buildingName = buildingNameEditText.text.toString()
            val houseNumber = houseNumberEditText.text.toString()
            val paymentMethod = paymentMethodEditText.text.toString()
            val tapiocaQty = tapiocaQtyEditText.text.toString().toIntOrNull() ?: 0
            val farinhaQty = farinhaQtyEditText.text.toString().toDoubleOrNull() ?: 0.0
            val observation = observationEditText.text.toString()

            if (clientName.isBlank() || address.isBlank() || houseNumber.isBlank() || currentAcaiItems.isEmpty()) {
                Toast.makeText(this, "Preencha os dados do cliente e adicione ao menos um item de açaí.", Toast.LENGTH_LONG).show()
                return@setPositiveButton
            }

            val delivery = Delivery(
                id = deliveryToEdit?.id ?: "",
                clientName = clientName,
                address = address,
                buildingName = buildingName.ifEmpty { null },
                houseNumber = houseNumber,
                paymentMethod = paymentMethod,
                acaiItems = currentAcaiItems,
                tapiocaQuantity = tapiocaQty,
                farinhaDaguaQuantity = farinhaQty,
                date = TankManager.getActiveDay(),
                observation = observation,
                finished = deliveryToEdit?.finished ?: false
            )

            if (deliveryToEdit == null) {
                FirebaseManager.addDelivery(delivery) { success ->
                    if (success) {
                        Toast.makeText(this, "Entrega salva!", Toast.LENGTH_SHORT).show()
                        loadDeliveriesForActiveDay()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Erro ao salvar.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                FirebaseManager.updateDelivery(delivery) { success ->
                    if (success) {
                        Toast.makeText(this, "Entrega atualizada!", Toast.LENGTH_SHORT).show()
                        loadDeliveriesForActiveDay()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Erro ao atualizar.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    private fun showAddAcaiItemSubDialog(onItemAdded: (AcaiOutput) -> Unit) {
        val subDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_batch, null)

        subDialogView.findViewById<View>(R.id.buttonCancelDialog).visibility = View.GONE
        subDialogView.findViewById<View>(R.id.buttonSaveBatchDialog).visibility = View.GONE
        subDialogView.findViewById<View>(R.id.recyclerViewCurrentBatchDialog).visibility = View.GONE
        subDialogView.findViewById<View>(R.id.textViewCurrentBatchTitle).visibility = View.GONE
        subDialogView.findViewById<View>(R.id.textViewBatida).visibility = View.GONE
        subDialogView.findViewById<View>(R.id.textViewPedido).visibility = View.VISIBLE

        val builder = AlertDialog.Builder(this)
            .setView(subDialogView)
            .setTitle("Adicionar Item de Açaí")

        // Usando ChipGroup, que corresponde ao layout final
        val chipGroupTipo: ChipGroup = subDialogView.findViewById(R.id.chipGroupTipoAcai)
        val chipGroupQuantidade: ChipGroup = subDialogView.findViewById(R.id.chipGroupQuantidade)
        val btnAddItem: Button = subDialogView.findViewById(R.id.buttonAddItemDialog)

        val dialog = builder.create()

        btnAddItem.text = "Adicionar ao Pedido"
        btnAddItem.setOnClickListener {
            val selectedTipoId = chipGroupTipo.checkedChipId
            val selectedQuantidadeId = chipGroupQuantidade.checkedChipId

            if (selectedTipoId == View.NO_ID || selectedQuantidadeId == View.NO_ID) {
                Toast.makeText(this, "Por favor, selecione um tipo e uma quantidade.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val chipTipo = subDialogView.findViewById<Chip>(selectedTipoId)
            val chipQuantidade = subDialogView.findViewById<Chip>(selectedQuantidadeId)
            val tipo = chipTipo.text.toString()
            val quantidadeStr = chipQuantidade.text.toString()
            val quantidade = quantidadeStr.replace(" L", "").toDoubleOrNull()

            if (quantidade != null && quantidade > 0) {
                onItemAdded(AcaiOutput(tipo, quantidade))
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Selecione uma quantidade válida.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun setupSummaryView() {
        summaryRows.clear()
        layoutCalculator.removeAllViews()
        lastCalculatedRevenue = 0.0
        val allOutputs = getAllOutputsForToday()
        val totalLitersOverall = allOutputs.sumOf { it.quantity }
        textViewTotalLiters.text = "Total de Litros Batidos: %.1f L".format(totalLitersOverall)
        textViewTotalRevenue.text = "Rendimento Total do Dia: ${formatCurrency(0.0)}"
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

    private fun getAllOutputsForToday(): List<AcaiOutput> {
        val activity = TankManager.currentDayActivity ?: return emptyList()
        return activity.atividadesPorTanque.values
            .flatMap { it.batidas }
            .flatMap { it.items }
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

    private fun showEndDayConfirmationDialog() {
        calculateFinalRevenue()
        val totalRevenue = lastCalculatedRevenue

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar Encerramento do Dia")
        builder.setMessage("O faturamento total calculado foi de ${formatCurrency(totalRevenue)}.\n\nTem certeza que deseja encerrar o dia? Esta ação não pode ser desfeita.")
        builder.setPositiveButton("Sim, Encerrar") { _, _ ->
            showLoading(true)
            TankManager.endDayAndStartNew(totalRevenue) {
                runOnUiThread {
                    val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
                    bottomNav.selectedItemId = R.id.navigation_tanks
                    updateUiForDate()
                    showLoading(false, R.id.navigation_tanks)
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }

    private fun showDatePickerDialog() {
        val activeDay = TankManager.getActiveDay()
        val parts = activeDay.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val day = parts[2].toInt()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, newYear, newMonth, newDay ->
                val newDate = String.format("%04d-%02d-%02d", newYear, newMonth + 1, newDay)
                if (newDate != TankManager.getActiveDay()) {
                    changeViewedDay(newDate) // Nome da função corrigido
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    // ##### FUNÇÃO ATUALIZADA #####
    // Agora chama a nova função do TankManager e não mexe mais com o Firebase diretamente.
    private fun changeViewedDay(newDate: String) {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val selectedId = bottomNav.selectedItemId
        showLoading(true)

        TankManager.changeViewedDay(newDate) {
            runOnUiThread {
                updateUiForDate()
                showLoading(false, selectedId)
            }
        }
    }
}