package com.company.personalfinancetracker.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.personalfinancetracker.R
import com.company.personalfinancetracker.models.Transaction
import com.company.personalfinancetracker.utils.SharedPreferencesManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val transactions: List<Transaction>,
    private val isCompactMode: Boolean,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val layoutId = if (isCompactMode) {
            R.layout.item_transaction_compact
        } else {
            R.layout.item_transaction
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)

        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvTitle.text = transaction.title



        // Get currency symbol from shared preferences
        val currencySymbol = sharedPreferencesManager.getCurrency()
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        currencyFormat.currency = Currency.getInstance(currencySymbol)

        // Format amount with currency symbol
        val formattedAmount = currencyFormat.format(transaction.amount)

        // Set amount color based on transaction type
        holder.tvAmount.text = formattedAmount
        holder.tvAmount.setTextColor(
            if (transaction.isExpense) Color.RED else Color.GREEN
        )

        holder.tvCategory.text = transaction.category

        // Format date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.tvDate.text = dateFormat.format(Date(transaction.date))

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
    }

    override fun getItemCount() = transactions.size
}