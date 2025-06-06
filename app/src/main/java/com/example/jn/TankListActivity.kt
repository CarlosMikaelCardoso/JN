package com.example.jn // Substitua pelo seu pacote

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TankListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tankAdapter: TankAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tank_list)

        // Encontra os componentes no layout
        recyclerView = findViewById(R.id.recyclerViewTanks)
        val fabAddTank: FloatingActionButton = findViewById(R.id.fabAddTank)

        // Configura o RecyclerView
        setupRecyclerView()

        // Configura o clique do botão para adicionar um novo tanque
        fabAddTank.setOnClickListener {
            TankManager.addNewTank()
            // Atualiza a lista no adapter para refletir a adição
            tankAdapter.updateData(TankManager.getTanks())
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza a lista sempre que a tela se torna visível.
        // Isso garante que os totais sejam atualizados após adicionar uma saída.
        tankAdapter.updateData(TankManager.getTanks())
    }

    private fun setupRecyclerView() {
        // Pega a lista de tanques do nosso gerenciador
        val tanks = TankManager.getTanks()

        // Cria o adapter, passando a lista e a ação de clique
        tankAdapter = TankAdapter(tanks) { position ->
            // Ação a ser executada quando um item da lista é clicado
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("TANK_INDEX", position) // Envia o índice do tanque para a próxima tela
            startActivity(intent)
        }

        // Define o adapter e o layout manager para o RecyclerView
        recyclerView.adapter = tankAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}