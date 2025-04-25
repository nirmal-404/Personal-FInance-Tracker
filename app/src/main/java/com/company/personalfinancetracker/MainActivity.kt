package com.company.personalfinancetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.personalfinancetracker.adapters.TransactionAdapter
import com.company.personalfinancetracker.models.Transaction
import com.company.personalfinancetracker.utils.NotificationHelper
import com.company.personalfinancetracker.utils.SharedPreferencesManager
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalanceAmount: TextView
    private lateinit var tvIncomeAmount: TextView
    private lateinit var tvExpenseAmount: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var progressBudget: ProgressBar
    private lateinit var rvRecentTransactions: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var notificationHelper: NotificationHelper

    private var transactions = mutableListOf<Transaction>()
    private var monthlyBudget = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Wire up the Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        // Initialize shared preferences manager
        sharedPreferencesManager = SharedPreferencesManager(this)
        notificationHelper = NotificationHelper(this)

        // Create notification channel for Android 8.0+
        createNotificationChannel()

        // Initialize views
        initializeViews()

        // Load transactions and update UI
        loadTransactions()
        updateDashboard()

        // Check budget status
        checkBudgetStatus()
    }

    private fun initializeViews() {
        tvBalanceAmount = findViewById(R.id.tvBalanceAmount)
        tvIncomeAmount = findViewById(R.id.tvIncomeAmount)
        tvExpenseAmount = findViewById(R.id.tvExpenseAmount)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        progressBudget = findViewById(R.id.progressBudget)
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions)

        // Setup RecyclerView
        transactionAdapter = TransactionAdapter(sharedPreferencesManager, transactions, true) { transaction ->
            // On click listener for editing transaction
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("TRANSACTION_ID", transaction.id)
            startActivity(intent)
        }

        rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = transactionAdapter
        }

        // Button click listeners
        findViewById<Button>(R.id.btnAddTransaction).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewAll).setOnClickListener {
            startActivity(Intent(this, ViewTransactionsActivity::class.java))
        }
    }

    private fun loadTransactions() {
        transactions.clear()
        transactions.addAll(sharedPreferencesManager.getTransactions())

        // Sort by date (most recent first) and limit to 5 transactions
        transactions.sortByDescending { it.date }
        if (transactions.size > 5) {
            val recentTransactions = transactions.subList(0, 5).toMutableList()
            transactions.clear()
            transactions.addAll(recentTransactions)
        }

        transactionAdapter.notifyDataSetChanged()
    }

    private fun updateDashboard() {
        val allTransactions = sharedPreferencesManager.getTransactions()
        var totalIncome = 0.0
        var totalExpense = 0.0

        for (transaction in allTransactions) {
            if (transaction.isExpense) {
                totalExpense += transaction.amount
            } else {
                totalIncome += transaction.amount
            }
        }

        val balance = totalIncome - totalExpense
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

        // Get currency symbol from shared preferences
        val currencySymbol = sharedPreferencesManager.getCurrency()
        currencyFormat.currency = Currency.getInstance(currencySymbol)

        // Update UI with formatted amounts
        tvBalanceAmount.text = currencyFormat.format(balance)
        tvIncomeAmount.text = currencyFormat.format(totalIncome)
        tvExpenseAmount.text = currencyFormat.format(totalExpense)

        // Update budget display
        monthlyBudget = sharedPreferencesManager.getMonthlyBudget()
        if (monthlyBudget > 0) {
            val budgetUsedPercentage = (totalExpense / monthlyBudget * 100).toInt()
            progressBudget.progress = budgetUsedPercentage.coerceAtMost(100)
            tvBudgetStatus.text = "Budget: ${currencyFormat.format(totalExpense)} / ${currencyFormat.format(monthlyBudget)}"
        } else {
            progressBudget.progress = 0
            tvBudgetStatus.text = "No budget set"
        }
    }

    private fun checkBudgetStatus() {
        if (monthlyBudget <= 0) return

        val allTransactions = sharedPreferencesManager.getTransactions()
        var totalExpense = 0.0

        // Calculate current month expenses only
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        for (transaction in allTransactions) {
            if (transaction.isExpense) {
                val transactionCalendar = Calendar.getInstance().apply {
                    timeInMillis = transaction.date
                }

                if (transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                    transactionCalendar.get(Calendar.YEAR) == currentYear) {
                    totalExpense += transaction.amount
                }
            }
        }

        // Check budget thresholds
        val budgetPercentage = (totalExpense / monthlyBudget) * 100

        when {
            budgetPercentage >= 100 -> {
                notificationHelper.showBudgetNotification(
                    "Budget Exceeded!",
                    "You've exceeded your monthly budget."
                )
            }
            budgetPercentage >= 80 -> {
                notificationHelper.showBudgetNotification(
                    "Budget Warning",
                    "You've used ${budgetPercentage.toInt()}% of your monthly budget."
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Alerts"
            val descriptionText = "Notifications for budget alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("BUDGET_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_budget -> {
                startActivity(Intent(this, BudgetSettingsActivity::class.java))
                true
            }
            R.id.menu_categories -> {
                startActivity(Intent(this, CategoryAnalysisActivity::class.java))
                true
            }
            R.id.menu_backup -> {
                startActivity(Intent(this, BackupRestoreActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
        updateDashboard()
        checkBudgetStatus()
    }
}