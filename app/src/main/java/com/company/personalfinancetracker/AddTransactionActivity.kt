package com.company.personalfinancetracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.company.personalfinancetracker.models.Transaction
import com.company.personalfinancetracker.utils.Categories
import com.company.personalfinancetracker.utils.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvDate: TextView
    private lateinit var rgTransactionType: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private var selectedDate: Calendar = Calendar.getInstance()
    private var transactionId: String? = null
    private var editingTransaction: Transaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Show back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize shared preferences manager
        sharedPreferencesManager = SharedPreferencesManager(this)

        // Initialize views
        initializeViews()

        // Check if we're editing an existing transaction
        transactionId = intent.getStringExtra("TRANSACTION_ID")
        if (transactionId != null) {
            loadTransaction()
            supportActionBar?.title = "Edit Transaction"
            btnDelete.visibility = View.VISIBLE
        } else {
            supportActionBar?.title = "Add Transaction"
            btnDelete.visibility = View.GONE
        }

        // Setup category spinner
        setupCategorySpinner()

        // Setup date picker
        setupDatePicker()

        // Button click listeners
        setupButtonListeners()
    }

    private fun initializeViews() {
        etTitle = findViewById(R.id.etTitle)
        etAmount = findViewById(R.id.etAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        tvDate = findViewById(R.id.tvDate)
        rgTransactionType = findViewById(R.id.rgTransactionType)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)

        // Set current date as default
        updateDateDisplay()
    }

    private fun setupCategorySpinner() {
        val categories = Categories.DEFAULT_CATEGORIES
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        tvDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, month)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateDisplay()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(selectedDate.time)
    }

    private fun loadTransaction() {
        val transactions = sharedPreferencesManager.getTransactions()
        editingTransaction = transactions.find { it.id == transactionId }

        editingTransaction?.let { transaction ->
            etTitle.setText(transaction.title)
            etAmount.setText(transaction.amount.toString())

            // Set category in spinner
            val categoryPosition = Categories.DEFAULT_CATEGORIES.indexOf(transaction.category)
            if (categoryPosition != -1) {
                spinnerCategory.setSelection(categoryPosition)
            }

            // Set date
            selectedDate.timeInMillis = transaction.date
            updateDateDisplay()

            // Set transaction type
            rgTransactionType.check(
                if (transaction.isExpense) R.id.rbExpense else R.id.rbIncome
            )
        }
    }

    private fun setupButtonListeners() {
        btnSave.setOnClickListener {
            if (validateInput()) {
                saveTransaction()
                finish()
            }
        }

        btnDelete.setOnClickListener {
            deleteTransaction()
            finish()
        }
    }

    private fun validateInput(): Boolean {
        if (etTitle.text.isBlank()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return false
        }

        if (etAmount.text.isBlank()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return false
        }

        val amount = etAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveTransaction() {
        val title = etTitle.text.toString().trim()
        val amount = etAmount.text.toString().toDouble()
        val category = spinnerCategory.selectedItem.toString()
        val date = selectedDate.timeInMillis
        val isExpense = rgTransactionType.checkedRadioButtonId == R.id.rbExpense

        if (editingTransaction != null) {
            // Update existing transaction
            editingTransaction?.apply {
                this.title = title
                this.amount = amount
                this.category = category
                this.date = date
                this.isExpense = isExpense
            }

            sharedPreferencesManager.updateTransaction(editingTransaction!!)
        } else {
            // Create new transaction
            val newTransaction = Transaction(
                title = title,
                amount = amount,
                category = category,
                date = date,
                isExpense = isExpense
            )

            sharedPreferencesManager.saveTransaction(newTransaction)
        }

        Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
    }

    private fun deleteTransaction() {
        editingTransaction?.let {
            sharedPreferencesManager.deleteTransaction(it.id)
            Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}