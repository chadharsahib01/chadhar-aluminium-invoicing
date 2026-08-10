package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentType: String, // "INVOICE" or "QUOTATION"
    val documentNumber: String, // e.g., "INV-2026-001" or "QUO-2026-001"
    val date: String,
    val clientName: String,
    val clientPhone: String,
    val clientAddress: String = "",
    val subtotal: Double,
    val discount: Double = 0.0,
    val taxEnabled: Boolean = false,
    val taxPercentage: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val pdfPath: String? = null,
    val dueDate: String = "",
    val paymentStatus: String = "Unpaid", // "Unpaid", "Partial", "Paid"
    val amountPaid: Double = 0.0,
    val discountType: String = "FIXED", // "FIXED" or "PERCENTAGE"
    val discountValue: Double = 0.0,
    val sitePhotosJson: String = "" // JSON list of image URIs
)
