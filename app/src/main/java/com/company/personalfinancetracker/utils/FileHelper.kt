package com.company.personalfinancetracker.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.company.personalfinancetracker.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*

class FileHelper(private val context: Context) {

    private val gson = Gson()
    private val backupFileName = "finance_tracker_backup.json"

    companion object {
        private const val TAG = "FileHelper"
    }

    // Convert transactions to JSON string
    fun transactionsToJson(transactions: List<Transaction>): String {
        return gson.toJson(transactions)
    }

    // Convert JSON string to transactions list
    fun jsonToTransactions(jsonData: String): List<Transaction> {
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(jsonData, type)
    }

    // Write backup file to app's private storage
    fun writeBackupFile(jsonData: String) {
        try {
            val backupFile = File(context.filesDir, backupFileName)
            FileOutputStream(backupFile).use { fos ->
                fos.write(jsonData.toByteArray())
            }
            Log.d(TAG, "Backup file created at: ${backupFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing backup file: ${e.message}")
            throw e
        }
    }

    // Write backup file to Downloads folder
    fun writeBackupToDownloads(jsonData: String): Boolean {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, backupFileName)
            FileOutputStream(file).use { fos ->
                fos.write(jsonData.toByteArray())
            }
            Log.d(TAG, "Backup saved to Downloads at: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to Downloads: ${e.message}")
            false
        }
    }

    // Read backup file from app's private storage
    fun readBackupFile(): String? {
        try {
            val backupFile = File(context.filesDir, backupFileName)

            if (!backupFile.exists()) {
                Log.d(TAG, "Backup file not found at: ${backupFile.absolutePath}")
                return null
            }

            val bytes = ByteArray(backupFile.length().toInt())
            FileInputStream(backupFile).use { fis ->
                fis.read(bytes)
            }

            return String(bytes)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading backup file: ${e.message}")
            throw e
        }
    }

    // Check if backup file exists
    fun backupExists(): Boolean {
        val backupFile = File(context.filesDir, backupFileName)
        return backupFile.exists()
    }

    // Get backup file timestamp
    fun getBackupFileTimestamp(): Long {
        val backupFile = File(context.filesDir, backupFileName)
        return if (backupFile.exists()) backupFile.lastModified() else 0
    }
}
