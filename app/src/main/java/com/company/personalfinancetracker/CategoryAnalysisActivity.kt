package com.company.personalfinancetracker

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.personalfinancetracker.adapters.CategoryAnalysisAdapter
import com.company.personalfinancetracker.models.CategorySummary
import com.company.personalfinancetracker.utils.Categories
import com.company.personalfinancetracker.utils.SharedPreferencesManager
import java.util.*

class CategoryAnalysisActivity : AppCompatActivity() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAnalysisAdapter
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private var categorySummaries = mutableListOf<CategorySummary>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_analysis)

        // Show back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Category Analysis"

        // Initialize shared preferences manager
        sharedPreferencesManager = SharedPreferencesManager(this)

        // Initialize RecyclerView
        rvCategories = findViewById(R.id.rvCategories)

        // Setup adapter
        categoryAdapter = CategoryAnalysisAdapter(sharedPreferencesManager, categorySummaries)

        rvCategories.apply {
            layoutManager = LinearLayoutManager(this@CategoryAnalysisActivity)
            adapter = categoryAdapter
        }

        // Load category data
        analyzeCategories()
    }

    private fun analyzeCategories() {
        val transactions = sharedPreferencesManager.getTransactions()
        val categoryMap = mutableMapOf<String, Double>()
        var totalExpenses = 0.0

        // Calculate expense totals by category
        for (transaction in transactions) {
            if (transaction.isExpense) {
                val currentAmount = categoryMap[transaction.category] ?: 0.0
                categoryMap[transaction.category] = currentAmount + transaction.amount
                totalExpenses += transaction.amount
            }
        }

        // If no expenses, show empty state
        if (totalExpenses == 0.0) {
            for (category in Categories.DEFAULT_CATEGORIES) {
                categorySummaries.add(CategorySummary(category, 0.0, 0.0))
            }
        } else {
            // Create category summaries with percentages
            for ((category, amount) in categoryMap) {
                val percentage = (amount / totalExpenses) * 100
                categorySummaries.add(CategorySummary(category, amount, percentage))
            }

            // Add any missing categories with zero values
            for (category in Categories.DEFAULT_CATEGORIES) {
                if (!categoryMap.containsKey(category)) {
                    categorySummaries.add(CategorySummary(category, 0.0, 0.0))
                }
            }
        }

        // Sort by amount (highest first)
        categorySummaries.sortByDescending { it.amount }

        categoryAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}