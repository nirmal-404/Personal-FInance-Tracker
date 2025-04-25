package com.company.personalfinancetracker

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.personalfinancetracker.utils.SharedPreferencesManager

class BudgetSettingsActivity : AppCompatActivity() {

    private lateinit var etMonthlyBudget: EditText
    private lateinit var spinnerCurrency: Spinner
    private lateinit var btnSaveSettings: Button
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_settings)

        // Show back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Budget Settings"

        // Initialize shared preferences manager
        sharedPreferencesManager = SharedPreferencesManager(this)

        // Initialize views
        etMonthlyBudget = findViewById(R.id.etMonthlyBudget)
        spinnerCurrency = findViewById(R.id.spinnerCurrency)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)

        // Setup currency spinner
        setupCurrencySpinner()

        // Load current settings
        loadSettings()

        // Button click listener
        btnSaveSettings.setOnClickListener {
            if (validateInput()) {
                saveSettings()
                finish()
            }
        }
    }

    private fun setupCurrencySpinner() {
        val currencies = arrayOf("LKR", "USD", "EUR", "GBP", "JPY", "INR", "CAD", "AUD")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter
    }

    private fun loadSettings() {
        val monthlyBudget = sharedPreferencesManager.getMonthlyBudget()
        val currency = sharedPreferencesManager.getCurrency()

        if (monthlyBudget > 0) {
            etMonthlyBudget.setText(monthlyBudget.toString())
        }

        // Set currency in spinner
        val currencies = resources.getStringArray(R.array.currencies)
        val currencyPosition = currencies.indexOf(currency)
        if (currencyPosition != -1) {
            spinnerCurrency.setSelection(currencyPosition)
        }
    }

    private fun validateInput(): Boolean {
        if (etMonthlyBudget.text.isBlank()) {
            Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
            return false
        }

        val budget = etMonthlyBudget.text.toString().toDoubleOrNull()
        if (budget == null || budget <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveSettings() {
        val budget = etMonthlyBudget.text.toString().toDouble()
        val currency = spinnerCurrency.selectedItem.toString()

        sharedPreferencesManager.setMonthlyBudget(budget)
        sharedPreferencesManager.setCurrency(currency)

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}