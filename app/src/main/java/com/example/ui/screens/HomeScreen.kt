package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DocumentEntity
import com.example.data.entity.DocumentWithItems
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.OnboardingDialog
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.QuotationGreen
import com.example.ui.theme.QuotationGreenLight
import com.example.ui.theme.QuotationOrange
import com.example.ui.theme.QuotationOrangeLight
import com.example.ui.theme.RedAccent
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToNewDocument: (docType: String) -> Unit,
    onNavigateToEditDocument: (docId: Long) -> Unit,
    onNavigateToPreview: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToClients: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()

    var docToDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    var partialPaymentDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var partialAmountText by remember { mutableStateOf("") }

    if (settings.showTutorial) {
        OnboardingDialog(onDismiss = { viewModel.dismissTutorial() })
    }

    // Partial Payment Dialog
    partialPaymentDoc?.let { doc ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { partialPaymentDoc = null },
            title = { Text("Enter Partial Payment", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate900) },
            text = {
                Column {
                    Text("Total Invoice: Rs. ${String.format("%,.2f", doc.grandTotal)}", fontSize = 13.sp, color = Slate700)
                    Text("Current Paid: Rs. ${String.format("%,.2f", doc.amountPaid)}", fontSize = 13.sp, color = Slate700)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = partialAmountText,
                        onValueChange = { partialAmountText = it },
                        label = { Text("Amount Received (Rs.)", color = Slate500) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate900,
                            unfocusedTextColor = Slate900,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SleekCardBorder
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rec = partialAmountText.toDoubleOrNull() ?: 0.0
                        viewModel.updatePaymentStatus(doc.id, "Partial", rec)
                        partialPaymentDoc = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Save Partial Payment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(onClick = { partialPaymentDoc = null }) {
                    Text("Cancel", color = Slate700)
                }
            }
        )
    }

    docToDelete?.let { doc ->
        DeleteConfirmDialog(
            title = "Delete ${doc.documentNumber}?",
            message = "Are you sure you want to delete ${doc.documentNumber} for client '${doc.clientName}'?",
            onConfirm = {
                viewModel.deleteDocument(doc)
                docToDelete = null
            },
            onDismiss = { docToDelete = null }
        )
    }

    Scaffold(
        containerColor = SleekBackground,
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 0.5.dp
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = settings.businessName.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = "Dashboard",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = Slate900
                                )
                            }
                        },
                        actions = {
                            // Client Address Book Button
                            IconButton(onClick = onNavigateToClients) {
                                Surface(
                                    shape = CircleShape,
                                    color = Slate50,
                                    border = BorderStroke(1.dp, SleekCardBorder),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Clients",
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Language Switcher Button
                            IconButton(
                                onClick = {
                                    val newLang = if (settings.language == "en") "ur" else "en"
                                    viewModel.updateSettings(settings.copy(language = newLang))
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Slate50,
                                    border = BorderStroke(1.dp, SleekCardBorder),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (settings.language == "en") "UR" else "EN",
                                            color = Slate800,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Dark Mode Toggle Button
                            IconButton(
                                onClick = {
                                    viewModel.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (settings.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = "Toggle Theme",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Settings Button
                            IconButton(onClick = onNavigateToSettings) {
                                Surface(
                                    shape = CircleShape,
                                    color = Slate50,
                                    border = BorderStroke(1.dp, SleekCardBorder),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = Slate700,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                    HorizontalDivider(color = SleekCardBorder, thickness = 1.dp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // --- GREETING SECTION ---
                Column {
                    Text(
                        text = "Assalam-o-Alaikum,",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = Slate900
                        )
                    )
                    Text(
                        text = "${settings.ownerName} • ${settings.phoneNumber}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Slate500,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            // --- FEATURE 8: SIMPLE MONTHLY SUMMARY CARD ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "THIS MONTH SUMMARY (${monthlySummary.monthName})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Slate500,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${monthlySummary.totalInvoices} Invoices",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PrimaryBlue
                            )
                        }

                        HorizontalDivider(color = SleekCardBorder, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Total Invoiced", fontSize = 12.sp, color = Slate500)
                                Text(
                                    text = "Rs. ${String.format("%,.0f", monthlySummary.totalAmountInvoiced)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Received", fontSize = 12.sp, color = Slate500)
                                Text(
                                    text = "Rs. ${String.format("%,.0f", monthlySummary.totalAmountReceived)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF16A34A)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Pending Due", fontSize = 12.sp, color = Slate500)
                                Text(
                                    text = "Rs. ${String.format("%,.0f", monthlySummary.totalPendingBalance)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (monthlySummary.totalPendingBalance > 0) Color(0xFFDC2626) else Slate900
                                )
                            }
                        }
                    }
                }
            }

            // --- LOSS AVERSION: DATA SAFETY BACKUP REMINDER ---
            if (documents.size >= 5 && !settings.hasExportedBackup) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = QuotationOrangeLight),
                        border = BorderStroke(1.dp, QuotationOrange.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Data Safety Reminder (${documents.size} Saved Invoices)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Slate900
                                )
                            }
                            Text(
                                text = "If you lose or change your phone, you could lose all saved invoices. Export a backup now to keep them safe.",
                                fontSize = 12.sp,
                                color = Slate700
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        viewModel.exportBackupJson(context)
                                        viewModel.markBackupExported()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = QuotationOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("EXPORT BACKUP (بیک اپ کریں)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // --- TWO PRIMARY ACTION CARDS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // New Invoice Card
                    Card(
                        onClick = {
                            viewModel.startNewDocument("INVOICE")
                            onNavigateToNewDocument("INVOICE")
                        },
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "New Invoice",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "نئی انوائس",
                                    fontSize = 12.sp,
                                    color = PrimaryBlueLight,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // New Quote Card
                    Card(
                        onClick = {
                            viewModel.startNewDocument("QUOTATION")
                            onNavigateToNewDocument("QUOTATION")
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, SleekCardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = PrimaryBlueLight,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "New Quote",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = "نیا کوٹیشن",
                                    fontSize = 12.sp,
                                    color = Slate400,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // --- RECENT HISTORY HEADER & FILTER SEARCH ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT HISTORY",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                fontSize = 12.sp,
                                letterSpacing = 1.2.sp
                            )
                        )

                        Text(
                            text = "${documents.size} Records",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search client, doc #, date...", color = Slate400, fontSize = 14.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate900,
                            unfocusedTextColor = Slate900,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SleekCardBorder
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Filter Pills
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filterType == "ALL",
                            onClick = { viewModel.setFilterType("ALL") },
                            label = { Text("All", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Slate900,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Slate500
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterType == "ALL",
                                borderColor = SleekCardBorder
                            )
                        )
                        FilterChip(
                            selected = filterType == "INVOICE",
                            onClick = { viewModel.setFilterType("INVOICE") },
                            label = { Text("Invoices", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlueLight,
                                selectedLabelColor = PrimaryBlue,
                                containerColor = Color.White,
                                labelColor = Slate500
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterType == "INVOICE",
                                borderColor = SleekCardBorder
                            )
                        )
                        FilterChip(
                            selected = filterType == "QUOTATION",
                            onClick = { viewModel.setFilterType("QUOTATION") },
                            label = { Text("Quotations", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = QuotationOrangeLight,
                                selectedLabelColor = QuotationOrange,
                                containerColor = Color.White,
                                labelColor = Slate500
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterType == "QUOTATION",
                                borderColor = SleekCardBorder
                            )
                        )
                    }
                }
            }

            // --- DOCUMENT CARDS ---
            if (documents.isEmpty()) {
                item {
                    com.example.ui.components.EmptyStateCard(
                        icon = Icons.Default.Description,
                        title = "No Invoices or Quotations Yet",
                        description = "Create professional aluminium & glass quotes or bills in seconds.",
                        actionLabel = "Create First Invoice",
                        actionIcon = Icons.Default.Add,
                        onActionClick = { onNavigateToNewDocument("INVOICE") }
                    )
                }
            } else {
                items(documents, key = { it.document.id }) { docWithItems ->
                    SleekDocumentCard(
                        docWithItems = docWithItems,
                        context = context,
                        onViewPdf = {
                            viewModel.loadDocumentForEdit(docWithItems.document.id)
                            onNavigateToPreview()
                        },
                        onShareWhatsApp = {
                            viewModel.loadDocumentForEdit(docWithItems.document.id)
                            onNavigateToPreview()
                        },
                        onEdit = {
                            viewModel.loadDocumentForEdit(docWithItems.document.id)
                            onNavigateToNewDocument(docWithItems.document.documentType)
                        },
                        onDelete = {
                            docToDelete = docWithItems.document
                        },
                        onDuplicate = {
                            viewModel.duplicateDocument(docWithItems.document.id)
                        },
                        onConvertToInvoice = {
                            viewModel.convertQuotationToInvoice(docWithItems.document.id)
                        },
                        onMarkStatus = { status ->
                            if (status == "Partial") {
                                partialPaymentDoc = docWithItems.document
                                partialAmountText = ""
                            } else {
                                val amt = if (status == "Paid") docWithItems.document.grandTotal else 0.0
                                viewModel.updatePaymentStatus(docWithItems.document.id, status, amt)
                            }
                        },
                        onGenerateReceipt = {
                            scope.launch {
                                val receiptUri = viewModel.generatePaymentReceipt(context, docWithItems.document.id)
                                if (receiptUri != null) {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(receiptUri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SleekDocumentCard(
    docWithItems: DocumentWithItems,
    context: Context,
    onViewPdf: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onConvertToInvoice: () -> Unit,
    onMarkStatus: (String) -> Unit,
    onGenerateReceipt: () -> Unit
) {
    val doc = docWithItems.document
    val isInvoice = doc.documentType.equals("INVOICE", ignoreCase = true)
    
    // Status Badge Logic
    val statusColor = when (doc.paymentStatus) {
        "Paid" -> Color(0xFF16A34A)
        "Partial" -> QuotationOrange
        else -> Color(0xFFDC2626)
    }
    val statusBg = when (doc.paymentStatus) {
        "Paid" -> QuotationGreenLight
        "Partial" -> QuotationOrangeLight
        else -> RedAccent.copy(alpha = 0.1f)
    }

    var isExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Badge Icon
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isInvoice) statusBg else QuotationOrangeLight,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isInvoice) {
                                if (doc.paymentStatus == "Paid") Icons.Default.CheckCircle else Icons.Default.Pending
                            } else Icons.Default.Pending,
                            contentDescription = null,
                            tint = if (isInvoice) statusColor else QuotationOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Center Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = doc.clientName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Payment Status Colored Badge (Feature 2)
                        if (isInvoice) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusBg,
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = doc.paymentStatus,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "#${doc.documentNumber}",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val itemSummaryText = if (docWithItems.items.isNotEmpty()) {
                            "${doc.date} • ${docWithItems.items.first().itemName}"
                        } else {
                            doc.date
                        }

                        Text(
                            text = itemSummaryText,
                            fontSize = 13.sp,
                            color = Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Rs. ${String.format("%,.0f", doc.grandTotal)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Slate900,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }

            // Expanded Quick Actions Row
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SleekCardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // View PDF
                    Button(
                        onClick = onViewPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Convert Quote to Invoice (Feature 3)
                    if (!isInvoice) {
                        Button(
                            onClick = onConvertToInvoice,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(38.dp)
                        ) {
                            Text(text = "→ Invoice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Payment Status Menu / Receipt (Feature 2 & 14)
                    if (isInvoice) {
                        Box {
                            Button(
                                onClick = { statusMenuExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = statusBg),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(text = doc.paymentStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                            }

                            androidx.compose.material3.DropdownMenu(
                                expanded = statusMenuExpanded,
                                onDismissRequest = { statusMenuExpanded = false }
                            ) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Mark Unpaid") },
                                    onClick = { statusMenuExpanded = false; onMarkStatus("Unpaid") }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Mark Partial") },
                                    onClick = { statusMenuExpanded = false; onMarkStatus("Partial") }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Mark Paid") },
                                    onClick = { statusMenuExpanded = false; onMarkStatus("Paid") }
                                )
                            }
                        }

                        if (doc.paymentStatus == "Paid" || doc.paymentStatus == "Partial") {
                            Button(
                                onClick = onGenerateReceipt,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(text = "Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                        }
                    }

                    // Duplicate Button (Feature 5)
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Slate50, RoundedCornerShape(10.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Duplicate", tint = Slate800, modifier = Modifier.size(18.dp))
                    }

                    // Edit Icon
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Slate50, RoundedCornerShape(10.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Slate800, modifier = Modifier.size(18.dp))
                    }

                    // Delete Icon
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(38.dp)
                            .background(RedAccent.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
