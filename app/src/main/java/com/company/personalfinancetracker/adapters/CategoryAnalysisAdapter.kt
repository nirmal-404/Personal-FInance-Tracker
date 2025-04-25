package com.company.personalfinancetracker.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.personalfinancetracker.R
import com.company.personalfinancetracker.models.CategorySummary
import com.company.personalfinancetracker.utils.SharedPreferencesManager
import java.text.NumberFormat
import java.util.*

class CategoryAnalysisAdapter(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val categories: List<CategorySummary>
) : RecyclerView.Adapter<CategoryAnalysisAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvPercentage: TextView = view.findViewById(R.id.tvPercentage)
        val progressBar: ProgressBar = view.findViewById(R.id.progressCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_analysis, parent, false)

        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categorySummary = categories[position]

        holder.tvCategory.text = categorySummary.category

        // Format amount with currency symbol
        val currencySymbol = sharedPreferencesManager.getCurrency()
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        currencyFormat.currency = Currency.getInstance(currencySymbol)

        // Format amount with currency symbol
        holder.tvAmount.text = currencyFormat.format(categorySummary.amount)


        // Format percentage
        holder.tvPercentage.text = String.format("%.1f%%", categorySummary.percentage)

        // Set progress bar
        holder.progressBar.progress = categorySummary.percentage.toInt()
    }

    override fun getItemCount() = categories.size
}