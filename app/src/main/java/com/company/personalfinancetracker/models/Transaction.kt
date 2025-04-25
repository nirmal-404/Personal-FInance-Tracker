package com.company.personalfinancetracker.models

import java.util.UUID

// Transaction.kt
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var amount: Double,
    var category: String,
    var date: Long,
    var isExpense: Boolean
)

// Category.kt
object Categories {
    val DEFAULT_CATEGORIES = listOf(
        "Food", "Transport", "Bills", "Entertainment",
        "Shopping", "Health", "Education", "Salary", "Other"
    )
}