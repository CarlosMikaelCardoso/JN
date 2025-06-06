package com.example.jn

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerTipoAcai: Spinner // Novo Spinner para o tipo
    private lateinit var spinnerQuantidadeAcai: Spinner
    private lateinit var buttonAdicionar: Button
    private var tankIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_acai)

        tankIndex = intent.getIntExtra("TANK_INDEX", -1)
        if (tankIndex == -1) {
            Toast.makeText(this, "Erro: Tanque não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Inicializa os componentes da UI
        spinnerTipoAcai = findViewById(R.id.spinnerTipoAcai)
        spinnerQuantidadeAcai = findViewById(R.id.spinnerQuantidadeAcai)
        buttonAdicionar = findViewById(R.id.buttonAdicionar)

        // Popula o Spinner de tipos de açaí
        populateTypeSpinner()

        // Popula o Spinner de quantidades
        populateQuantitySpinner()

        buttonAdicionar.setOnClickListener {
            addAcaiOutput()
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
            val tank = TankManager.getTankAt(tankIndex)
            if (tank != null) {
                // Cria o objeto AcaiOutput com tipo e quantidade
                val newOutput = AcaiOutput(type = tipoSelecionado, quantity = valorNumerico)

                // Adiciona a nova saída ao tanque
                tank.outputs.add(newOutput)

                Toast.makeText(this, "$valorNumerico L de $tipoSelecionado adicionado(s) ao ${tank.name}!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao encontrar o tanque.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}