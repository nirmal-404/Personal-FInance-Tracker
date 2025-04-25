package com.company.personalfinancetracker

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.personalfinancetracker.adapters.TransactionAdapter
import com.company.personalfinancetracker.models.Transaction
import com.company.personalfinancetracker.utils.SharedPreferencesManager

class ViewTransactionsActivity : AppCompatActivity() {

    private lateinit var rvTransactions: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private var transactions = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_transactions)

        // Show back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "All Transactions"

        // Initialize shared preferences manager
        sharedPreferencesManager = SharedPreferencesManager(this)

        // Initialize RecyclerView
        rvTransactions = findViewById(R.id.rvTransactions)

        // Setup adapter
        transactionAdapter = TransactionAdapter(sharedPreferencesManager, transactions, false) { transaction ->
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("TRANSACTION_ID", transaction.id)
            startActivity(intent)
        }

        rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@ViewTransactionsActivity)
            adapter = transactionAdapter
        }

        // Load transactions
        loadTransactions()

        // Setup filter buttons
        setupFilterButtons()
    }

    private fun loadTransactions() {
        transactions.clear()
        transactions.addAll(sharedPreferencesManager.getTransactions())

        // Sort by date (most recent first)
        transactions.sortByDescending { it.date }

        transactionAdapter.notifyDataSetChanged()
    }

    private fun setupFilterButtons() {
        findViewById<Button>(R.id.btnAll).setOnClickListener {
            loadTransactions()
        }

        findViewById<Button>(R.id.btnIncome).setOnClickListener {
            filterTransactions(false)
        }

        findViewById<Button>(R.id.btnExpenses).setOnClickListener {
            filterTransactions(true)
        }
    }

    private fun filterTransactions(isExpense: Boolean) {
        transactions.clear()
        transactions.addAll(sharedPreferencesManager.getTransactions().filter { it.isExpense == isExpense })

        // Sort by date (most recent first)
        transactions.sortByDescending { it.date }

        transactionAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }
}