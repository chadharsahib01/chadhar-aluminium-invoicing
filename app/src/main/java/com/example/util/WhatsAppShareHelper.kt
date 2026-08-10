package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppShareHelper {

    /**
     * Formats Pakistani/International phone numbers for WhatsApp JID/URL.
     * e.g., "0300-4439436" -> "923004439436"
     */
    fun formatPhoneNumberForWhatsApp(phone: String): String {
        var cleaned = phone.replace("[^0-9+]".toRegex(), "")
        if (cleaned.startsWith("0")) {
            cleaned = "92" + cleaned.substring(1)
        } else if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1)
        }
        return cleaned
    }

    /**
     * Attempts direct WhatsApp share with attached PDF.
     * Falls back to standard Android Share Sheet if direct share fails or WhatsApp is not installed.
     */
    fun sharePdfToWhatsApp(
        context: Context,
        pdfUri: Uri,
        clientPhone: String,
        clientName: String,
        documentNumber: String
    ) {
        val formattedPhone = formatPhoneNumberForWhatsApp(clientPhone)
        val jid = "$formattedPhone@s.whatsapp.net"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_TEXT, "Hello $clientName, please find attached document $documentNumber from Chadhar Aluminium.")
            putExtra("jid", jid) // Targets specific WhatsApp contact chat directly
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Try launching WhatsApp package directly
        sendIntent.setPackage("com.whatsapp")

        try {
            context.startActivity(sendIntent)
        } catch (e1: Exception) {
            // Try WhatsApp Business package
            try {
                sendIntent.setPackage("com.whatsapp.w4b")
                context.startActivity(sendIntent)
            } catch (e2: Exception) {
                // Fallback to standard chooser
                sharePdfGeneric(context, pdfUri, clientName, documentNumber)
            }
        }
    }

    /**
     * Generic Android Share Sheet launcher
     */
    fun sharePdfGeneric(
        context: Context,
        pdfUri: Uri,
        clientName: String,
        documentNumber: String
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, "Document $documentNumber - Chadhar Aluminium")
            putExtra(Intent.EXTRA_TEXT, "Hello $clientName, please find attached document $documentNumber from Chadhar Aluminium.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Document via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to share PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
