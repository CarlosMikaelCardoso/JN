package com.example.jn // Substitua pelo nome do seu pacote

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerQuantidadeAcai: Spinner
    private lateinit var buttonAdicionar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_acai) // Define o layout XML para esta Activity

        // Inicializa os componentes da UI
        spinnerQuantidadeAcai = findViewById(R.id.spinnerQuantidadeAcai)
        buttonAdicionar = findViewById(R.id.buttonAdicionar)

        // Prepara os dados para o Spinner
        val quantidadesAcai = arrayOf("0.5 L", "1.0 L", "1.5 L", "2.0 L", "2.5 L", "3.0 L", "3.5 L", "4.0 L", "4.5 L", "5.0 L")

        // Cria um ArrayAdapter usando o array de strings e um layout de spinner padrão
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quantidadesAcai)

        // Especifica o layout a ser usado quando a lista de opções aparecer
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Aplica o adapter ao spinner
        spinnerQuantidadeAcai.adapter = adapter

        // Configura o listener de clique para o botão
        buttonAdicionar.setOnClickListener {
            // Pega o item selecionado no Spinner
            val quantidadeSelecionada = spinnerQuantidadeAcai.selectedItem.toString()

            // Exibe uma mensagem Toast com a quantidade selecionada
            Toast.makeText(this, "Saída de $quantidadeSelecionada de açaí adicionada!", Toast.LENGTH_LONG).show()

            // Aqui, futuramente, você adicionaria a lógica para salvar essa informação
            // Por exemplo, em um banco de dados ou em uma lista na memória.
        }
    }
}

