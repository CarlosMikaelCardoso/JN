package com.example.jn // Substitua pelo nome do seu pacote

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Renomear MainActivity para AdicionarAcaiActivity seria uma boa prática no futuro!
class MainActivity : AppCompatActivity() {

    private lateinit var spinnerQuantidadeAcai: Spinner
    private lateinit var buttonAdicionar: Button
    private var tankIndex: Int = -1 // Variável para guardar o índice do tanque

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_acai)

        // Pega o índice do tanque que foi enviado pela TankListActivity
        tankIndex = intent.getIntExtra("TANK_INDEX", -1)

        // Se o índice for inválido, fecha a tela para evitar erros.
        if (tankIndex == -1) {
            Toast.makeText(this, "Erro: Tanque não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        spinnerQuantidadeAcai = findViewById(R.id.spinnerQuantidadeAcai)
        buttonAdicionar = findViewById(R.id.buttonAdicionar)

        val quantidadesAcai = arrayOf("0.5 L", "1.0 L", "1.5 L", "2.0 L", "2.5 L", "3.0 L", "3.5 L", "4.0 L", "4.5 L", "5.0 L")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quantidadesAcai)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerQuantidadeAcai.adapter = adapter

        buttonAdicionar.setOnClickListener {
            // Pega o item selecionado
            val quantidadeSelecionadaString = spinnerQuantidadeAcai.selectedItem.toString()

            // Converte a string "X.X L" para um número Double
            val valorNumerico = quantidadeSelecionadaString.replace(" L", "").toDoubleOrNull()

            if (valorNumerico != null) {
                // Encontra o tanque correto usando o TankManager
                val tank = TankManager.getTankAt(tankIndex)
                if (tank != null) {
                    // Adiciona a saída ao tanque
                    tank.outputs.add(valorNumerico)
                    Toast.makeText(this, "Saída de $valorNumerico L adicionada ao ${tank.name}!", Toast.LENGTH_LONG).show()
                    finish() // Fecha a tela de adição e volta para a lista
                } else {
                    Toast.makeText(this, "Erro ao encontrar o tanque.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}