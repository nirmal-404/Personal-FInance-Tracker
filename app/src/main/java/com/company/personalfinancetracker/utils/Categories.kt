package com.company.personalfinancetracker.utils

object Categories {

    val DEFAULT_CATEGORIES = listOf(
        "Food & Dining",
        "Transport",
        "Utilities",
        "Shopping",
        "Health",
        "Entertainment",
        "Education",
        "Savings",
        "Rent",
        "Salary",
        "Investment",
        "Others"
    )

    /**
     * Returns a list of categories filtered by type.
     * @param isExpense true for expense categories, false for income categories
     */
    fun getCategoriesByType(isExpense: Boolean): List<String> {
        return if (isExpense) {
            listOf(
                "Food & Dining",
                "Transport",
                "Utilities",
                "Shopping",
                "Health",
                "Entertainment",
                "Education",
                "Savings",
                "Rent",
                "Others"
            )
        } else {
            listOf(
                "Salary",
                "Investment",
                "Others"
            )
        }
    }

    /**
     * Returns whether a category is considered an expense.
     */
    fun isExpenseCategory(category: String): Boolean {
        return getCategoriesByType(true).contains(category)
    }

    /**
     * Returns whether a category is considered income.
     */
    fun isIncomeCategory(category: String): Boolean {
        return getCategoriesByType(false).contains(category)
    }
}
