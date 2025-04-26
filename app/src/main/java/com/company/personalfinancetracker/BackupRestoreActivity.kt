package com.company.personalfinancetracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.personalfinancetracker.utils.FileHelper
import com.company.personalfinancetracker.utils.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*

class BackupRestoreActivity : AppCompatActivity() {

    private lateinit var tvLastBackup: TextView
    private lateinit var btnBackup: Button
    private lateinit var btnRestore: Button
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var fileHelper: FileHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_restore)

        // Show back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Backup & Restore"

        // Initialize managers
        sharedPreferencesManager = SharedPreferencesManager(this)
        fileHelper = FileHelper(this)

        // Initialize views
        tvLastBackup = findViewById(R.id.tvLastBackup)
        btnBackup = findViewById(R.id.btnBackup)
        btnRestore = findViewById(R.id.btnRestore)

        // Show last backup date
        updateLastBackupDate()

        // Setup button listeners
        setupButtonListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun updateLastBackupDate() {
        val lastBackupTime = sharedPreferencesManager.getLastBackupTime()

        if (lastBackupTime > 0) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(lastBackupTime))
            tvLastBackup.text = "Last backup: $formattedDate"
        } else {
            tvLastBackup.text = "No backup available"
        }
    }

    private fun setupButtonListeners() {
        btnBackup.setOnClickListener {
            performBackup()
        }

        btnRestore.setOnClickListener {
            pickBackupFile()
        }
    }

    private fun performBackup() {
        try {
            val transactions = sharedPreferencesManager.getTransactions()

            if (transactions.isEmpty()) {
                Toast.makeText(this, "No transactions to backup", Toast.LENGTH_SHORT).show()
                return
            }

            val jsonData = fileHelper.transactionsToJson(transactions)

            // Save to app's private storage (optional, for internal use)
            fileHelper.writeBackupFile(jsonData)

            // Also save to Downloads folder
            val success = fileHelper.writeBackupToDownloads(jsonData)

            if (success) {
                sharedPreferencesManager.saveLastBackupTime(System.currentTimeMillis())
                updateLastBackupDate()
                Toast.makeText(this, "Backup saved to Downloads", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Backup failed to save to Downloads", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickBackupFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, 1001)
    }

    private fun performRestore() {
        try {
            val jsonData = fileHelper.readBackupFromDownloads()

            if (jsonData == null) {
                Toast.makeText(this, "No backup file found in Downloads", Toast.LENGTH_SHORT).show()
                return
            }

            val transactions = fileHelper.jsonToTransactions(jsonData)

            if (transactions.isNotEmpty()) {
                sharedPreferencesManager.clearTransactions()

                for (transaction in transactions) {
                    sharedPreferencesManager.saveTransaction(transaction)
                }

                Toast.makeText(this, "Restored ${transactions.size} transactions", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No transactions found in backup", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return

            try {
                val inputStream = contentResolver.openInputStream(uri)
                val jsonData = inputStream?.bufferedReader().use { it?.readText() }

                if (!jsonData.isNullOrEmpty()) {
                    val transactions = fileHelper.jsonToTransactions(jsonData)
                    sharedPreferencesManager.clearTransactions()
                    transactions.forEach {
                        sharedPreferencesManager.saveTransaction(it)
                    }
                    Toast.makeText(this, "Restored ${transactions.size} transactions", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No data found in selected backup", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}