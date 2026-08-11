package com.example.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.entity.DocumentEntity
import com.example.data.entity.DocumentItemEntity
import com.example.data.repository.BusinessSettings
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    data class PdfResult(
        val file: File,
        val uri: Uri
    )

    fun generatePdf(
        context: Context,
        document: DocumentEntity,
        items: List<DocumentItemEntity>,
        settings: BusinessSettings
    ): PdfResult? {
        if (document.documentType.equals("SHEET_QUOTATION", ignoreCase = true)) {
            return generateSheetQuotationPdf(context, document, items, settings)
        }

        val pdfDocument = PdfDocument()

        // Standard A4 dimensions in points: 595 x 842
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // --- COLOR PALETTE ---
        val tealPrimary = Color.parseColor("#0F4C5C")     // Rich Teal / Navy Header
        val tealAccent = Color.parseColor("#14746F")      // Secondary Teal Accent
        val cyanTopStrip = Color.parseColor("#2DD4BF")    // Top accent border strip
        val textDark = Color.parseColor("#0F172A")        // Slate 900
        val textMuted = Color.parseColor("#475569")       // Slate 600
        val bgLight = Color.parseColor("#F8FAFC")         // Slate 50
        val bgRowAlt = Color.parseColor("#F1F5F9")        // Slate 100 for alternate rows
        val borderColor = Color.parseColor("#CBD5E1")     // Slate 300 grid border

        // --- PAINTS ---
        val headerBgPaint = Paint().apply {
            color = tealPrimary
            isAntiAlias = true
        }

        val topStripPaint = Paint().apply {
            color = cyanTopStrip
            isAntiAlias = true
        }

        val textWhiteTitle = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textWhiteSub = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 9.5f
            isAntiAlias = true
        }

        val textWhiteDocTitle = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val docMetaRight = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 10f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val sectionTitlePaint = Paint().apply {
            color = tealPrimary
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPrimaryBold = Paint().apply {
            color = textDark
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPrimaryRegular = Paint().apply {
            color = textDark
            textSize = 10f
            isAntiAlias = true
        }

        val textMutedRegular = Paint().apply {
            color = textMuted
            textSize = 10f
            isAntiAlias = true
        }

        val discountGreenText = Paint().apply {
            color = Color.parseColor("#15803D")
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val discountGreenRight = Paint().apply {
            color = Color.parseColor("#15803D")
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            isAntiAlias = true
        }

        val cardBgPaint = Paint().apply {
            color = bgLight
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val tableHeaderBgPaint = Paint().apply {
            color = tealPrimary
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val tableHeaderCellText = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val tableHeaderCellTextRight = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val rowWhitePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val rowAltPaint = Paint().apply {
            color = bgRowAlt
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val textRightAlign = Paint().apply {
            color = textDark
            textSize = 9.5f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val grandTotalBgPaint = Paint().apply {
            color = tealPrimary
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val grandTotalLabelPaint = Paint().apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val grandTotalValPaint = Paint().apply {
            color = Color.WHITE
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        // --- 1. TOP HEADER BAND ---
        val headerHeight = 115f
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), headerHeight, headerBgPaint)
        // Accent cyan top strip
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 5f, topStripPaint)

        // Logo
        var startTextX = 25f
        var logoBitmap: Bitmap? = null

        if (!settings.logoUri.isNullOrEmpty()) {
            try {
                val logoUri = Uri.parse(settings.logoUri)
                val inputStream = context.contentResolver.openInputStream(logoUri)
                logoBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (logoBitmap == null) {
            try {
                logoBitmap = BitmapFactory.decodeResource(context.resources, com.example.R.drawable.chadhar_logo_transparent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (logoBitmap != null) {
            val maxW = 75f
            val maxH = 65f
            val srcW = logoBitmap.width.toFloat()
            val srcH = logoBitmap.height.toFloat()
            val scale = Math.min(maxW / srcW, maxH / srcH)
            val dstW = (srcW * scale).toInt().coerceAtLeast(1)
            val dstH = (srcH * scale).toInt().coerceAtLeast(1)
            val scaledBitmap = Bitmap.createScaledBitmap(logoBitmap, dstW, dstH, true)
            
            // Vertically center logo within header top band (height 115f)
            val logoY = (115f - dstH) / 2f
            canvas.drawBitmap(scaledBitmap, 25f, logoY, null)
            startTextX = 25f + dstW + 14f
        }

        // Business Name & Owner Info (Left Side)
        canvas.drawText(settings.businessName, startTextX, 48f, textWhiteTitle)
        canvas.drawText("Owner: ${settings.ownerName}  |  Phone: ${settings.phoneNumber}", startTextX, 68f, textWhiteSub)
        if (settings.address.isNotBlank()) {
            canvas.drawText(settings.address, startTextX, 83f, textWhiteSub)
        }

        // Document Type & Meta (Right Side)
        val isInvoice = document.documentType.equals("INVOICE", ignoreCase = true)
        val docTypeTitle = if (isInvoice) "INVOICE" else "QUOTATION"

        canvas.drawText(docTypeTitle, pageWidth - 25f, 44f, textWhiteDocTitle)
        canvas.drawText("No: ${document.documentNumber}", pageWidth - 25f, 62f, docMetaRight)
        canvas.drawText("Date: ${document.date}", pageWidth - 25f, 76f, docMetaRight)
        if (document.dueDate.isNotBlank()) {
            val dateLabel = if (isInvoice) "Due Date: " else "Valid Until: "
            canvas.drawText("$dateLabel${document.dueDate}", pageWidth - 25f, 90f, docMetaRight)
        }

        // --- 2. CLIENT DETAILS BLOCK ("BILL TO") ---
        var currentY = headerHeight + 16f
        val clientBoxHeight = 65f
        val clientBoxRect = RectF(25f, currentY, pageWidth - 25f, currentY + clientBoxHeight)

        canvas.drawRoundRect(clientBoxRect, 6f, 6f, cardBgPaint)
        canvas.drawRoundRect(clientBoxRect, 6f, 6f, borderPaint)

        canvas.drawText("BILL TO / CLIENT DETAILS", 35f, currentY + 20f, sectionTitlePaint)

        canvas.drawText("Client Name: ", 35f, currentY + 38f, textPrimaryBold)
        canvas.drawText(document.clientName, 105f, currentY + 38f, textPrimaryRegular)

        canvas.drawText("Phone Number: ", 35f, currentY + 54f, textPrimaryBold)
        canvas.drawText(document.clientPhone, 115f, currentY + 54f, textPrimaryRegular)

        if (document.clientAddress.isNotBlank()) {
            canvas.drawText("Address: ", 300f, currentY + 38f, textPrimaryBold)
            canvas.drawText(document.clientAddress, 348f, currentY + 38f, textPrimaryRegular)
        }

        currentY += clientBoxHeight + 16f

        // --- 3. ITEMIZED TABLE ---
        val tableLeft = 25f
        val tableRight = pageWidth - 25f
        val headerRowHeight = 24f
        val dataRowHeight = 22f

        // Column Coordinates
        val colNoX = tableLeft + 8f
        val colItemX = tableLeft + 35f
        val colUnitX = tableLeft + 260f
        val colQtyX = tableLeft + 320f
        val colRateX = tableLeft + 375f
        val colAmountRightX = tableRight - 10f

        // Draw Table Header Banner
        val tableHeaderRect = RectF(tableLeft, currentY, tableRight, currentY + headerRowHeight)
        canvas.drawRect(tableHeaderRect, tableHeaderBgPaint)

        canvas.drawText("#", colNoX, currentY + 16f, tableHeaderCellText)
        canvas.drawText("Item Description", colItemX, currentY + 16f, tableHeaderCellText)
        canvas.drawText("Unit", colUnitX, currentY + 16f, tableHeaderCellText)
        canvas.drawText("Qty", colQtyX, currentY + 16f, tableHeaderCellText)
        canvas.drawText("Rate (Rs.)", colRateX, currentY + 16f, tableHeaderCellText)
        canvas.drawText("Amount (Rs.)", colAmountRightX, currentY + 16f, tableHeaderCellTextRight)

        currentY += headerRowHeight

        // Draw Table Data Rows
        items.forEachIndexed { index, item ->
            val rowRect = RectF(tableLeft, currentY, tableRight, currentY + dataRowHeight)
            val isEven = index % 2 == 0
            canvas.drawRect(rowRect, if (isEven) rowWhitePaint else rowAltPaint)
            canvas.drawRect(rowRect, borderPaint)

            canvas.drawText("${index + 1}", colNoX, currentY + 15f, textPrimaryRegular)

            var displayItemName = item.itemName
            if (displayItemName.length > 38) {
                displayItemName = displayItemName.take(35) + "..."
            }
            canvas.drawText(displayItemName, colItemX, currentY + 15f, textPrimaryBold)
            canvas.drawText(item.unit, colUnitX, currentY + 15f, textPrimaryRegular)
            canvas.drawText(formatDouble(item.quantity), colQtyX, currentY + 15f, textPrimaryRegular)
            canvas.drawText(formatMoney(item.rate), colRateX, currentY + 15f, textPrimaryRegular)
            canvas.drawText(formatMoney(item.amount), colAmountRightX, currentY + 15f, textRightAlign)

            currentY += dataRowHeight
        }

        // Draw outer border for the entire table
        canvas.drawRect(tableLeft, currentY - (items.size * dataRowHeight + headerRowHeight), tableRight, currentY, borderPaint)

        currentY += 16f

        // --- 4. TOTALS & NOTES SECTION ---
        val summaryWidth = 220f
        val summaryLeft = tableRight - summaryWidth

        // Subtotal
        canvas.drawText("Subtotal:", summaryLeft, currentY, textPrimaryBold)
        canvas.drawText("Rs. ${formatMoney(document.subtotal)}", tableRight - 10f, currentY, textRightAlign)
        currentY += 18f

        // Discount (if any)
        if (document.discount > 0) {
            val discountLabel = if (document.discountType == "PERCENTAGE" && document.discountValue > 0) {
                "Discount (${formatDouble(document.discountValue)}%):"
            } else {
                "Discount / Savings:"
            }
            canvas.drawText(discountLabel, summaryLeft, currentY, discountGreenText)
            canvas.drawText("- Rs. ${formatMoney(document.discount)}", tableRight - 10f, currentY, discountGreenRight)
            currentY += 18f
        }

        // Tax (if enabled & > 0)
        if (document.taxEnabled && document.taxAmount > 0) {
            val taxLabel = "Tax / GST (${formatDouble(document.taxPercentage)}%):"
            canvas.drawText(taxLabel, summaryLeft, currentY, textMutedRegular)
            canvas.drawText("+ Rs. ${formatMoney(document.taxAmount)}", tableRight - 10f, currentY, textRightAlign)
            currentY += 18f
        }

        // Grand Total Box
        val grandTotalRect = RectF(summaryLeft - 10f, currentY - 4f, tableRight, currentY + 24f)
        canvas.drawRoundRect(grandTotalRect, 5f, 5f, grandTotalBgPaint)

        canvas.drawText("GRAND TOTAL:", summaryLeft, currentY + 14f, grandTotalLabelPaint)
        canvas.drawText("Rs. ${formatMoney(document.grandTotal)}", tableRight - 10f, currentY + 14f, grandTotalValPaint)

        // Notes / Terms Box (Left side opposite Totals)
        if (document.notes.isNotBlank()) {
            val notesBoxWidth = summaryLeft - tableLeft - 20f
            val notesBoxRect = RectF(tableLeft, currentY - 38f, tableLeft + notesBoxWidth, currentY + 24f)
            canvas.drawRoundRect(notesBoxRect, 5f, 5f, cardBgPaint)
            canvas.drawRoundRect(notesBoxRect, 5f, 5f, borderPaint)

            canvas.drawText("TERMS & CONDITIONS:", tableLeft + 10f, currentY - 22f, sectionTitlePaint)

            val maxCharsPerLine = 42
            val noteLines = document.notes.chunked(maxCharsPerLine)
            noteLines.take(3).forEachIndexed { lineIdx, line ->
                canvas.drawText(line, tableLeft + 10f, currentY - 6f + (lineIdx * 13f), textMutedRegular)
            }
        }

        // --- 5. BOTTOM FOOTER ---
        val footerY = pageHeight - 40f

        // Draw Signature / Stamp image if configured
        if (!settings.stampUri.isNullOrEmpty()) {
            try {
                val stampUri = Uri.parse(settings.stampUri)
                val inputStream = context.contentResolver.openInputStream(stampUri)
                val stampBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (stampBitmap != null) {
                    val scaledStamp = Bitmap.createScaledBitmap(stampBitmap, 75, 45, true)
                    canvas.drawBitmap(scaledStamp, tableRight - 85f, footerY - 60f, null)
                    canvas.drawText("Authorized Signature", tableRight - 10f, footerY - 14f, Paint().apply {
                        color = textMuted
                        textSize = 8f
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val footerDividerPaint = Paint().apply {
            color = borderColor
            strokeWidth = 1f
        }
        canvas.drawLine(tableLeft, footerY - 12f, tableRight, footerY - 12f, footerDividerPaint)

        val footerTextBold = Paint().apply {
            color = textDark
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val footerTextSub = Paint().apply {
            color = textMuted
            textSize = 9f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        canvas.drawText("Thank you for doing business with ${settings.businessName}!", pageWidth / 2f, footerY + 2f, footerTextBold)
        canvas.drawText("For inquiries, contact ${settings.ownerName} at ${settings.phoneNumber}", pageWidth / 2f, footerY + 16f, footerTextSub)

        // Bottom Decorative Teal Band
        val bottomBandPaint = Paint().apply {
            color = tealPrimary
            isAntiAlias = true
        }
        canvas.drawRect(0f, pageHeight - 8f, pageWidth.toFloat(), pageHeight.toFloat(), bottomBandPaint)

        pdfDocument.finishPage(page)

        // Save PDF File
        return try {
            val docsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices")
            if (!docsDir.exists()) {
                docsDir.mkdirs()
            }

            val fileName = "${document.documentNumber}_${document.clientName.replace("[^a-zA-Z0-9]".toRegex(), "_")}.pdf"
            val pdfFile = File(docsDir, fileName)

            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

            saveToPublicStorage(context, pdfFile, fileName)

            PdfResult(file = pdfFile, uri = contentUri)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun generatePaymentReceiptPdf(
        context: Context,
        document: DocumentEntity,
        settings: BusinessSettings,
        amountReceivedNow: Double = document.amountPaid,
        paymentDate: String = document.date
    ): PdfResult? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val tealPrimary = Color.parseColor("#0F4C5C")
        val cyanTopStrip = Color.parseColor("#2DD4BF")
        val textDark = Color.parseColor("#0F172A")
        val textMuted = Color.parseColor("#475569")
        val bgLight = Color.parseColor("#F8FAFC")
        val borderColor = Color.parseColor("#CBD5E1")

        val headerBgPaint = Paint().apply { color = tealPrimary; isAntiAlias = true }
        val topStripPaint = Paint().apply { color = cyanTopStrip; isAntiAlias = true }
        val textWhiteTitle = Paint().apply { color = Color.WHITE; textSize = 20f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val textWhiteSub = Paint().apply { color = Color.parseColor("#E2E8F0"); textSize = 9.5f; isAntiAlias = true }
        val textWhiteDocTitle = Paint().apply { color = Color.WHITE; textSize = 20f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.RIGHT; isAntiAlias = true }
        val docMetaRight = Paint().apply { color = Color.parseColor("#E2E8F0"); textSize = 10f; textAlign = Paint.Align.RIGHT; isAntiAlias = true }
        val textPrimaryBold = Paint().apply { color = textDark; textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val textPrimaryRegular = Paint().apply { color = textDark; textSize = 10.5f; isAntiAlias = true }
        val borderPaint = Paint().apply { color = borderColor; style = Paint.Style.STROKE; strokeWidth = 0.8f; isAntiAlias = true }
        val cardBgPaint = Paint().apply { color = bgLight; style = Paint.Style.FILL; isAntiAlias = true }

        // Header
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, headerBgPaint)
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 5f, topStripPaint)

        canvas.drawText(settings.businessName, 25f, 45f, textWhiteTitle)
        canvas.drawText("Owner: ${settings.ownerName}  |  Phone: ${settings.phoneNumber}", 25f, 65f, textWhiteSub)

        canvas.drawText("PAYMENT RECEIPT", pageWidth - 25f, 45f, textWhiteDocTitle)
        canvas.drawText("Receipt Date: $paymentDate", pageWidth - 25f, 65f, docMetaRight)
        canvas.drawText("Ref Invoice: ${document.documentNumber}", pageWidth - 25f, 80f, docMetaRight)

        // Receipt Content Card
        val cardRect = RectF(25f, 130f, pageWidth - 25f, 380f)
        canvas.drawRoundRect(cardRect, 8f, 8f, cardBgPaint)
        canvas.drawRoundRect(cardRect, 8f, 8f, borderPaint)

        var y = 160f
        canvas.drawText("Received From: ", 45f, y, textPrimaryBold)
        canvas.drawText("${document.clientName} (${document.clientPhone})", 160f, y, textPrimaryRegular)

        y += 30f
        canvas.drawText("Amount Received: ", 45f, y, textPrimaryBold)
        canvas.drawText("Rs. ${formatMoney(amountReceivedNow)}", 160f, y, Paint().apply { color = Color.parseColor("#15803D"); textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true })

        y += 30f
        canvas.drawText("Invoice Total: ", 45f, y, textPrimaryBold)
        canvas.drawText("Rs. ${formatMoney(document.grandTotal)}", 160f, y, textPrimaryRegular)

        y += 30f
        canvas.drawText("Total Paid To Date: ", 45f, y, textPrimaryBold)
        canvas.drawText("Rs. ${formatMoney(document.amountPaid)}", 160f, y, textPrimaryRegular)

        y += 30f
        val remaining = (document.grandTotal - document.amountPaid).coerceAtLeast(0.0)
        canvas.drawText("Remaining Balance: ", 45f, y, textPrimaryBold)
        canvas.drawText("Rs. ${formatMoney(remaining)}", 160f, y, Paint().apply { color = if (remaining > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A"); textSize = 12f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true })

        y += 35f
        canvas.drawText("Status: ${document.paymentStatus.uppercase()}", 45f, y, textPrimaryBold)

        // Stamp
        if (!settings.stampUri.isNullOrEmpty()) {
            try {
                val stampUri = Uri.parse(settings.stampUri)
                val inputStream = context.contentResolver.openInputStream(stampUri)
                val stampBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (stampBitmap != null) {
                    val scaledStamp = Bitmap.createScaledBitmap(stampBitmap, 80, 50, true)
                    canvas.drawBitmap(scaledStamp, pageWidth - 140f, 300f, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Footer
        val footerY = pageHeight - 40f
        canvas.drawText("Thank you for your payment!", pageWidth / 2f, footerY, Paint().apply { color = textDark; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER; isAntiAlias = true })
        canvas.drawText("Contact ${settings.phoneNumber} for any queries.", pageWidth / 2f, footerY + 16f, Paint().apply { color = textMuted; textSize = 9f; textAlign = Paint.Align.CENTER; isAntiAlias = true })

        pdfDocument.finishPage(page)

        return try {
            val docsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Receipts")
            if (!docsDir.exists()) docsDir.mkdirs()
            val fileName = "Receipt_${document.documentNumber}_${System.currentTimeMillis()}.pdf"
            val pdfFile = File(docsDir, fileName)
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)
            saveToPublicStorage(context, pdfFile, fileName)
            PdfResult(file = pdfFile, uri = contentUri)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun saveToPublicStorage(context: Context, pdfFile: File, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ChadharAluminium")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        pdfFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatDouble(valNum: Double): String {
        return if (valNum % 1.0 == 0.0) {
            valNum.toLong().toString()
        } else {
            String.format("%.2f", valNum)
        }
    }

    private fun formatMoney(valNum: Double): String {
        return String.format("%,.2f", valNum)
    }

    private fun generateSheetQuotationPdf(
        context: Context,
        document: DocumentEntity,
        items: List<DocumentItemEntity>,
        settings: BusinessSettings
    ): PdfResult? {
        val pdfDocument = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // --- PAINTS & FONTS ---
        val textHeaderTitle = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textHeaderSub = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textDocTitle = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isUnderlineText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val textMetaBold = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textMetaVal = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            isAntiAlias = true
        }

        val tableHeaderBg = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            isAntiAlias = true
        }

        val tableHeaderFont = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val borderStroke = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val cellTextFont = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            isAntiAlias = true
        }

        val cellTextFontBold = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val cellTextRight = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        // --- 1. HEADER SECTION WITH LOGO ---
        val margin = 30f
        var currentY = 35f

        var logoBitmap: Bitmap? = null
        if (!settings.logoUri.isNullOrEmpty()) {
            try {
                val logoUri = Uri.parse(settings.logoUri)
                val inputStream = context.contentResolver.openInputStream(logoUri)
                logoBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (logoBitmap == null) {
            try {
                logoBitmap = BitmapFactory.decodeResource(context.resources, com.example.R.drawable.chadhar_logo_transparent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var textStartX = margin
        if (logoBitmap != null) {
            val maxW = 85f
            val maxH = 68f
            val srcW = logoBitmap.width.toFloat()
            val srcH = logoBitmap.height.toFloat()
            val scale = Math.min(maxW / srcW, maxH / srcH)
            val dstW = (srcW * scale).toInt().coerceAtLeast(1)
            val dstH = (srcH * scale).toInt().coerceAtLeast(1)
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, dstW, dstH, true)
            
            canvas.drawBitmap(scaledLogo, margin, currentY, null)
            textStartX = margin + dstW + 14f
        }
        canvas.drawText(settings.businessName, textStartX, currentY + 16f, textHeaderTitle)

        val shopAddr = if (settings.address.isNotBlank()) settings.address else "Shop No # 1, Pak Watan Market, Main Road Ghori VIP, Express Way Islamabad"
        canvas.drawText(shopAddr, textStartX, currentY + 30f, textHeaderSub)

        val phone1 = settings.phoneNumber.ifBlank { "0300-4439436" }
        val phone2 = settings.phoneNumber2.ifBlank { "0318-4439436" }
        canvas.drawText("PH: $phone1 - $phone2", textStartX, currentY + 42f, textHeaderSub)

        val email = settings.email.ifBlank { "tasawrali04@gmail.com" }
        canvas.drawText("EMAIL: $email", textStartX, currentY + 54f, textHeaderSub)

        currentY += 75f

        // Document Title: QUOTATION
        canvas.drawText("QUOTATION", pageWidth / 2f, currentY, textDocTitle)

        currentY += 25f

        // --- 2. METADATA SECTION (2 COLUMNS) ---
        val leftMetaX = margin
        val rightMetaX = 380f

        // Left Column
        canvas.drawText("QUOT NO:", leftMetaX, currentY, textMetaBold)
        canvas.drawText(document.documentNumber, leftMetaX + 65f, currentY, textMetaVal)

        canvas.drawText("CLIENT:", leftMetaX, currentY + 16f, textMetaBold)
        canvas.drawText(document.clientName, leftMetaX + 65f, currentY + 16f, textMetaVal)

        canvas.drawText("SITE:", leftMetaX, currentY + 32f, textMetaBold)
        canvas.drawText(document.siteLocation.ifBlank { "N/A" }, leftMetaX + 65f, currentY + 32f, textMetaVal)

        // Right Column
        canvas.drawText("DATE:", rightMetaX, currentY, textMetaBold)
        canvas.drawText(document.date, rightMetaX + 75f, currentY, textMetaVal)

        canvas.drawText("MOBILE NO:", rightMetaX, currentY + 16f, textMetaBold)
        canvas.drawText(document.clientPhone.ifBlank { "N/A" }, rightMetaX + 75f, currentY + 16f, textMetaVal)

        currentY += 48f

        // --- 3. ITEMS TABLE ---
        val tableLeft = margin
        val tableRight = pageWidth - margin
        val colSrW = 28f
        val colAreaW = 75f
        val colRateW = 75f
        val colTotalW = 85f
        val colDescW = (tableRight - tableLeft) - colSrW - colAreaW - colRateW - colTotalW

        val colSrX = tableLeft
        val colDescX = colSrX + colSrW
        val colAreaX = colDescX + colDescW
        val colRateX = colAreaX + colAreaW
        val colTotalX = colRateX + colRateW

        // Table Header
        val headerHeight = 22f
        val headerRect = RectF(tableLeft, currentY, tableRight, currentY + headerHeight)
        canvas.drawRect(headerRect, tableHeaderBg)
        canvas.drawRect(headerRect, borderStroke)

        // Vertical lines for header
        canvas.drawLine(colDescX, currentY, colDescX, currentY + headerHeight, borderStroke)
        canvas.drawLine(colAreaX, currentY, colAreaX, currentY + headerHeight, borderStroke)
        canvas.drawLine(colRateX, currentY, colRateX, currentY + headerHeight, borderStroke)
        canvas.drawLine(colTotalX, currentY, colTotalX, currentY + headerHeight, borderStroke)

        canvas.drawText("Sr.", colSrX + 6f, currentY + 15f, tableHeaderFont)
        canvas.drawText("Description", colDescX + 6f, currentY + 15f, tableHeaderFont)
        canvas.drawText("Area Sq Ft", colAreaX + 6f, currentY + 15f, tableHeaderFont)
        canvas.drawText("Rate Sq Ft", colRateX + 6f, currentY + 15f, tableHeaderFont)
        canvas.drawText("Total", colTotalX + 6f, currentY + 15f, tableHeaderFont)

        currentY += headerHeight

        // Table Data Rows
        items.forEachIndexed { idx, item ->
            val lines = wrapText(item.itemName, colDescW - 12f, cellTextFont)
            val rowHeight = (lines.size * 12f + 12f).coerceAtLeast(24f)

            val rowRect = RectF(tableLeft, currentY, tableRight, currentY + rowHeight)
            canvas.drawRect(rowRect, borderStroke)

            canvas.drawLine(colDescX, currentY, colDescX, currentY + rowHeight, borderStroke)
            canvas.drawLine(colAreaX, currentY, colAreaX, currentY + rowHeight, borderStroke)
            canvas.drawLine(colRateX, currentY, colRateX, currentY + rowHeight, borderStroke)
            canvas.drawLine(colTotalX, currentY, colTotalX, currentY + rowHeight, borderStroke)

            // Sr.
            canvas.drawText("${idx + 1}", colSrX + 6f, currentY + 14f, cellTextFont)

            // Description lines
            lines.forEachIndexed { lIdx, lineStr ->
                val fontToUse = if (lIdx == 0 && lines.size > 1) cellTextFontBold else cellTextFont
                canvas.drawText(lineStr, colDescX + 6f, currentY + 14f + (lIdx * 12f), fontToUse)
            }

            // Area Sq Ft
            canvas.drawText(formatDouble(item.quantity), colAreaX + 6f, currentY + 14f, cellTextFont)

            // Rate Sq Ft
            canvas.drawText(formatMoney(item.rate), colRateX + 6f, currentY + 14f, cellTextFont)

            // Total
            canvas.drawText(formatMoney(item.amount), colTotalX + colTotalW - 6f, currentY + 14f, cellTextRight)

            currentY += rowHeight
        }

        currentY += 16f

        // --- 4. TOTALS BOX (BOTTOM-RIGHT) ---
        val totalsBoxW = 230f
        val totalsBoxLeft = tableRight - totalsBoxW
        val totalLabelW = 120f
        val totalValX = totalsBoxLeft + totalLabelW

        val extraDiscount = document.discount
        val receivedAmount = document.amountPaid
        val estimatedTotal = (document.subtotal - extraDiscount - receivedAmount).coerceAtLeast(0.0)

        val totalsRows = listOf(
            Pair("GROSS TOTAL :", formatMoney(document.subtotal)),
            Pair("EXTRA DISCOUNT :", formatMoney(extraDiscount)),
            Pair("RECEIVED AMOUNT :", formatMoney(receivedAmount)),
            Pair("ESTIMATED TOTAL :", formatMoney(estimatedTotal))
        )

        val totalsRowH = 20f
        totalsRows.forEach { (label, value) ->
            val rRect = RectF(totalsBoxLeft, currentY, tableRight, currentY + totalsRowH)
            val lblRect = RectF(totalsBoxLeft, currentY, totalValX, currentY + totalsRowH)

            canvas.drawRect(lblRect, tableHeaderBg)
            canvas.drawRect(rRect, borderStroke)
            canvas.drawLine(totalValX, currentY, totalValX, currentY + totalsRowH, borderStroke)

            canvas.drawText(label, totalsBoxLeft + 6f, currentY + 14f, tableHeaderFont)
            canvas.drawText("Rs. $value", tableRight - 6f, currentY + 14f, cellTextRight)

            currentY += totalsRowH
        }

        // --- 5. ADDITIONAL NOTES (if present) ---
        if (document.notes.isNotBlank()) {
            currentY += 12f
            canvas.drawText("Additional Notes / Other Details:", margin, currentY, textMetaBold)
            currentY += 14f
            val noteLines = wrapText(document.notes, pageWidth - (2 * margin), cellTextFont)
            noteLines.forEach { nLine ->
                canvas.drawText(nLine, margin, currentY, cellTextFont)
                currentY += 12f
            }
        }

        // --- 6. PAGE FOOTER ---
        val footerY = pageHeight - 35f

        val dashPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(6f, 4f), 0f)
            isAntiAlias = true
        }
        canvas.drawLine(margin, footerY - 15f, pageWidth - margin, footerY - 15f, dashPaint)

        val websiteStr = settings.website.ifBlank { "www.chadharaluminium.com.pk" }
        val footerWebFont = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(websiteStr, pageWidth / 2f, footerY, footerWebFont)

        pdfDocument.finishPage(page)

        return try {
            val docsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices")
            if (!docsDir.exists()) {
                docsDir.mkdirs()
            }

            val fileName = "${document.documentNumber}_${document.clientName.replace("[^a-zA-Z0-9]".toRegex(), "_")}.pdf"
            val pdfFile = File(docsDir, fileName)

            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            val pdfUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            saveToPublicStorage(context, pdfFile, fileName)

            PdfResult(pdfFile, pdfUri)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            if (paragraph.isBlank()) {
                result.add("")
                continue
            }
            val words = paragraph.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val measure = paint.measureText(testLine)
                if (measure > maxWidth && currentLine.isNotEmpty()) {
                    result.add(currentLine)
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine)
            }
        }
        return result
    }
}
