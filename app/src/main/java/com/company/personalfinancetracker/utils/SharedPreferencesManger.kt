package com.company.personalfinancetracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.company.personalfinancetracker.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "finance_tracker_prefs"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_BUDGET_PERIOD = "budget_period"
        private const val KEY_CURRENCY = "currency"
    }

    init {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Transaction methods

    fun saveTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()

        // Check if transaction with this ID already exists
        val existingIndex = transactions.indexOfFirst { it.id == transaction.id }

        if (existingIndex >= 0) {
            // Update existing transaction
            transactions[existingIndex] = transaction
        } else {
            // Add new transaction
            transactions.add(transaction)
        }

        // Save updated list
        saveAllTransactions(transactions)
    }

    fun getTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(KEY_TRANSACTIONS, null) ?: return emptyList()

        return try {
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getTransaction(id: String): Transaction? {
        return getTransactions().find { it.id == id }
    }

    fun updateTransaction(updatedTransaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }

        if (index != -1) {
            transactions[index] = updatedTransaction
            saveAllTransactions(transactions)
        } else {
            throw IllegalArgumentException("Transaction with ID ${updatedTransaction.id} not found")
        }
    }

    fun deleteTransaction(transactionId: String) {
        val transactions = getTransactions().toMutableList()
        transactions.removeIf { it.id == transactionId }
        saveAllTransactions(transactions)
    }

    fun clearTransactions() {
        sharedPreferences.edit().remove(KEY_TRANSACTIONS).apply()
    }

    private fun saveAllTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    // Budget methods

    fun setMonthlyBudget(amount: Double) {
        sharedPreferences.edit()
            .putFloat(KEY_MONTHLY_BUDGET, amount.toFloat())
            .apply()
    }

    fun getMonthlyBudget(): Double {
        return sharedPreferences.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
    }

    fun setBudgetPeriod(period: String) {
        sharedPreferences.edit()
            .putString(KEY_BUDGET_PERIOD, period)
            .apply()
    }

    fun getBudgetPeriod(): String {
        return sharedPreferences.getString(KEY_BUDGET_PERIOD, "Monthly") ?: "Monthly"
    }

    // Currency settings

    fun setCurrency(currencyCode: String) {
        sharedPreferences.edit()
            .putString(KEY_CURRENCY, currencyCode)
            .apply()
    }

    fun getCurrency(): String {
        // Default to USD if not set
        return sharedPreferences.getString(KEY_CURRENCY, "USD") ?: "USD"
    }

    // Backup methods

    fun saveLastBackupTime(timestamp: Long) {
        sharedPreferences.edit().putLong(KEY_LAST_BACKUP_TIME, timestamp).apply()
    }

    fun getLastBackupTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_BACKUP_TIME, 0L)
    }

    // Utility method to calculate current month's expenses
    fun getCurrentMonthExpenses(): Double {
        val transactions = getTransactions()
        var totalExpense = 0.0

        // Calculate current month expenses only
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        for (transaction in transactions) {
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

        return totalExpense
    }

    // Utility method to get budget usage percentage
    fun getBudgetUsagePercentage(): Int {
        val monthlyBudget = getMonthlyBudget()
        if (monthlyBudget <= 0) return 0

        val currentExpenses = getCurrentMonthExpenses()
        return ((currentExpenses / monthlyBudget) * 100).toInt().coerceAtMost(100)
    }
}