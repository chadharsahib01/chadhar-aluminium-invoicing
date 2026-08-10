package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import java.util.Calendar

data class BusinessSettings(
    val businessName: String = "Chadhar Aluminium",
    val ownerName: String = "Tasawar Ali Chadhar",
    val phoneNumber: String = "0300-4439436",
    val address: String = "",
    val logoUri: String? = null,
    val stampUri: String? = null,
    val defaultNotes: String = "50% advance required. Rate valid for 15 days.",
    val language: String = "en", // "en" or "ur"
    val showTutorial: Boolean = true,
    val nextInvoiceNumber: Int = 1,
    val nextQuotationNumber: Int = 1,
    val hasExportedBackup: Boolean = false,
    val appPin: String? = null,
    val isPinEnabled: Boolean = false,
    val isDarkMode: Boolean = false,
    val pinCode: String = "",
    val lastNumberingYear: Int = Calendar.getInstance().get(Calendar.YEAR)
) {
    val signatureUri: String?
        get() = stampUri
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<BusinessSettings> = _settings.asStateFlow()

    fun loadSettings(): BusinessSettings {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val savedYear = prefs.getInt("lastNumberingYear", currentYear)
        
        var nextInv = prefs.getInt("nextInvoiceNumber", 1)
        var nextQuo = prefs.getInt("nextQuotationNumber", 1)

        // Yearly reset if calendar year changed
        if (currentYear > savedYear) {
            nextInv = 1
            nextQuo = 1
            prefs.edit()
                .putInt("lastNumberingYear", currentYear)
                .putInt("nextInvoiceNumber", 1)
                .putInt("nextQuotationNumber", 1)
                .apply()
        }

        return BusinessSettings(
            businessName = prefs.getString("businessName", "Chadhar Aluminium") ?: "Chadhar Aluminium",
            ownerName = prefs.getString("ownerName", "Tasawar Ali Chadhar") ?: "Tasawar Ali Chadhar",
            phoneNumber = prefs.getString("phoneNumber", "0300-4439436") ?: "0300-4439436",
            address = prefs.getString("address", "") ?: "",
            logoUri = prefs.getString("logoUri", null),
            stampUri = prefs.getString("stampUri", null),
            defaultNotes = prefs.getString("defaultNotes", "50% advance required. Rate valid for 15 days.")
                ?: "50% advance required. Rate valid for 15 days.",
            language = prefs.getString("language", "en") ?: "en",
            showTutorial = prefs.getBoolean("showTutorial", true),
            nextInvoiceNumber = nextInv,
            nextQuotationNumber = nextQuo,
            hasExportedBackup = prefs.getBoolean("hasExportedBackup", false),
            appPin = prefs.getString("appPin", null),
            isPinEnabled = prefs.getBoolean("isPinEnabled", false),
            isDarkMode = prefs.getBoolean("isDarkMode", false),
            pinCode = prefs.getString("pinCode", "") ?: "",
            lastNumberingYear = currentYear
        )
    }

    fun updateSettings(newSettings: BusinessSettings) {
        val updatedStamp = newSettings.stampUri
        prefs.edit()
            .putString("businessName", newSettings.businessName)
            .putString("ownerName", newSettings.ownerName)
            .putString("phoneNumber", newSettings.phoneNumber)
            .putString("address", newSettings.address)
            .putString("logoUri", newSettings.logoUri)
            .putString("stampUri", updatedStamp)
            .putString("defaultNotes", newSettings.defaultNotes)
            .putString("language", newSettings.language)
            .putBoolean("showTutorial", newSettings.showTutorial)
            .putInt("nextInvoiceNumber", newSettings.nextInvoiceNumber)
            .putInt("nextQuotationNumber", newSettings.nextQuotationNumber)
            .putBoolean("hasExportedBackup", newSettings.hasExportedBackup)
            .putString("appPin", newSettings.appPin)
            .putBoolean("isPinEnabled", newSettings.isPinEnabled)
            .putBoolean("isDarkMode", newSettings.isDarkMode)
            .putString("pinCode", newSettings.pinCode)
            .putInt("lastNumberingYear", newSettings.lastNumberingYear)
            .apply()

        _settings.value = newSettings
    }

    fun incrementDocNumber(docType: String): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        var currentSettings = settings.value

        // Check if year changed
        if (currentYear > currentSettings.lastNumberingYear) {
            currentSettings = currentSettings.copy(
                lastNumberingYear = currentYear,
                nextInvoiceNumber = 1,
                nextQuotationNumber = 1
            )
        }

        val docNumStr: String
        if (docType.equals("INVOICE", ignoreCase = true)) {
            val count = currentSettings.nextInvoiceNumber
            docNumStr = "INV-$currentYear-${count.toString().padStart(3, '0')}"
            updateSettings(currentSettings.copy(nextInvoiceNumber = count + 1, lastNumberingYear = currentYear))
        } else {
            val count = currentSettings.nextQuotationNumber
            docNumStr = "QUO-$currentYear-${count.toString().padStart(3, '0')}"
            updateSettings(currentSettings.copy(nextQuotationNumber = count + 1, lastNumberingYear = currentYear))
        }
        return docNumStr
    }

    fun dismissTutorial() {
        val updated = settings.value.copy(showTutorial = false)
        updateSettings(updated)
    }
}
