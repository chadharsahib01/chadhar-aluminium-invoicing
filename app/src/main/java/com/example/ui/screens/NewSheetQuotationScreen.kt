package com.example.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ItemQuantityDialog
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.QuotationGreen
import com.example.ui.theme.RedAccent
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekCardBorder
import com.example.ui.viewmodel.DraftItem
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewSheetQuotationScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeDoc by viewModel.activeDocument.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf<Int?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    if (isGeneratingPdf) {
        com.example.ui.components.LoadingProgressDialog("Saving & Generating Sheet Quotation PDF...")
    }

    if (showSuccessDialog) {
        com.example.ui.components.SuccessCheckmarkDialog(
            title = "Sheet Quotation Saved!",
            message = "PDF generated successfully.",
            onDismiss = {
                showSuccessDialog = false
                onNavigateToPreview()
            }
        )
    }

    val hasUnsavedChanges = activeDoc.clientName.isNotBlank() || activeDoc.items.isNotEmpty() || activeDoc.siteLocation.isNotBlank()
    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(text = "Leave without saving?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate900) },
            text = { Text("You have unsaved changes in this Sheet Quotation. Are you sure you want to discard them?", color = Slate700) },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text("Discard Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUnsavedDialog = false }) {
                    Text("Keep Editing", color = Slate700)
                }
            }
        )
    }

    // Contact Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            try {
                val cursor = context.contentResolver.query(
                    contactUri,
                    arrayOf(
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        ContactsContract.Contacts._ID
                    ),
                    null, null, null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)) ?: ""
                        val hasPhone = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                        val contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))

                        var phoneNumber = ""
                        if (hasPhone > 0) {
                            val phoneCursor = context.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(contactId),
                                null
                            )
                            phoneCursor?.use { pc ->
                                if (pc.moveToFirst()) {
                                    phoneNumber = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                                }
                            }
                        }
                        viewModel.updateClientDetails(
                            name = if (activeDoc.clientName.isBlank()) name else activeDoc.clientName,
                            phone = if (phoneNumber.isNotBlank()) phoneNumber else activeDoc.clientPhone,
                            address = activeDoc.clientAddress
                        )
                    }
                }
            } catch (e: Exception) {
                // Fallback gracefully
            }
        }
    }

    // Date Picker Dialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val formatted = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time)
            viewModel.updateDate(formatted)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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
                                    text = "SHEET QUOTATION",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = "New Sheet Quotation (نئی شیٹ کوٹیشن)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Slate900
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (hasUnsavedChanges) showUnsavedDialog = true else onNavigateBack()
                            }) {
                                Surface(
                                    shape = CircleShape,
                                    color = Slate50,
                                    border = BorderStroke(1.dp, SleekCardBorder),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Slate800,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- DOCUMENT NUMBER & DATE ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "DOCUMENT METADATA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Slate500,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = activeDoc.documentNumber,
                                onValueChange = { viewModel.updateDocumentNumber(it) },
                                label = { Text("Quotation No.", color = Slate500) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = SleekCardBorder
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = activeDoc.date,
                                onValueChange = { viewModel.updateDate(it) },
                                label = { Text("Date", color = Slate500) },
                                singleLine = true,
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { datePickerDialog.show() }) {
                                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Select Date", tint = PrimaryBlue)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = SleekCardBorder
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- CLIENT & SITE DETAILS ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CLIENT & SITE INFORMATION (گاہک اور سائٹ)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Slate500,
                            letterSpacing = 0.8.sp
                        )

                        OutlinedTextField(
                            value = activeDoc.clientName,
                            onValueChange = { viewModel.updateClientDetails(it, activeDoc.clientPhone, activeDoc.clientAddress) },
                            label = { Text("Client Name *", color = Slate500) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = SleekCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = activeDoc.clientPhone,
                            onValueChange = { viewModel.updateClientDetails(activeDoc.clientName, it, activeDoc.clientAddress) },
                            label = { Text("Mobile Number", color = Slate500) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            trailingIcon = {
                                IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                                    Icon(imageVector = Icons.Default.Contacts, contentDescription = "Pick Contact", tint = PrimaryBlue)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = SleekCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = activeDoc.siteLocation,
                            onValueChange = { viewModel.updateSiteLocation(it) },
                            label = { Text("Site / Job Location (سائٹ کی جگہ)", color = Slate500) },
                            placeholder = { Text("e.g., Ghori VIP Phase 4, Islamabad", color = Slate400) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = SleekCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // --- ITEMS TABLE SECTION ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ITEMS LIST (${activeDoc.items.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate500,
                        letterSpacing = 0.8.sp
                    )

                    Button(
                        onClick = {
                            editingItemIndex = null
                            showAddItemDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Sheet Item", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (activeDoc.items.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate50),
                        border = BorderStroke(1.dp, SleekCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null, tint = Slate400, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No sheet items added yet.", fontWeight = FontWeight.Medium, color = Slate500, fontSize = 14.sp)
                                Text("Tap '+ Add Sheet Item' above to insert cladding/aluminum sheet items.", fontSize = 12.sp, color = Slate400)
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(activeDoc.items) { index, item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SleekCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PrimaryBlueLight,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = PrimaryBlue
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.itemName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Slate900,
                                        lineHeight = 18.sp
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            editingItemIndex = index
                                            showAddItemDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeItemFromDraft(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            HorizontalDivider(color = SleekCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Area (Sq Ft)", fontSize = 11.sp, color = Slate500)
                                    Text("${String.format("%,.2f", item.quantity)} sq.ft", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate800)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Rate / Sq Ft", fontSize = 11.sp, color = Slate500)
                                    Text("Rs. ${String.format("%,.2f", item.rate)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate800)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Amount", fontSize = 11.sp, color = Slate500)
                                    Text("Rs. ${String.format("%,.2f", item.amount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                                }
                            }
                        }
                    }
                }
            }

            // --- TOTALS SECTION MATCHING REAL PAPER TEMPLATE EXACTLY ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "TOTALS & PAYMENT CALCULATION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Slate500,
                            letterSpacing = 0.8.sp
                        )

                        // 1. Gross Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gross Total (کل رقم)", fontWeight = FontWeight.Medium, color = Slate700, fontSize = 14.sp)
                            Text(
                                "Rs. ${String.format("%,.2f", activeDoc.subtotal)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Slate900
                            )
                        }

                        HorizontalDivider(color = SleekCardBorder)

                        // 2. Extra Discount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Extra Discount (ڈسکاؤنٹ)", fontWeight = FontWeight.Medium, color = Slate700, fontSize = 14.sp)
                                Text("Subracted from Gross Total", fontSize = 11.sp, color = Slate400)
                            }
                            OutlinedTextField(
                                value = if (activeDoc.discountValue > 0) activeDoc.discountValue.toString() else "",
                                onValueChange = {
                                    val disc = it.toDoubleOrNull() ?: 0.0
                                    viewModel.updateDiscount("FIXED", disc)
                                },
                                placeholder = { Text("0.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.width(130.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = SleekCardBorder
                                )
                            )
                        }

                        HorizontalDivider(color = SleekCardBorder)

                        // 3. Received Amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Received Amount (وصول شدہ ایڈوانس)", fontWeight = FontWeight.Medium, color = Slate700, fontSize = 14.sp)
                                Text("Advance paid by client", fontSize = 11.sp, color = Slate400)
                            }
                            OutlinedTextField(
                                value = if (activeDoc.amountPaid > 0) activeDoc.amountPaid.toString() else "",
                                onValueChange = {
                                    val rec = it.toDoubleOrNull() ?: 0.0
                                    val status = if (rec >= activeDoc.grandTotal) "Paid" else if (rec > 0) "Partial" else "Unpaid"
                                    viewModel.updatePaymentStatus(activeDoc.editingDocId ?: 0L, status, rec)
                                },
                                placeholder = { Text("0.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.width(130.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = SleekCardBorder
                                )
                            )
                        }

                        HorizontalDivider(color = SleekCardBorder)

                        // 4. Estimated Total
                        val estimatedTotal = (activeDoc.subtotal - activeDoc.discount - activeDoc.amountPaid).coerceAtLeast(0.0)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PrimaryBlueLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Estimated Total (بقیہ اندازہً رقم)", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 13.sp)
                                    Text("Gross − Discount − Received", fontSize = 10.sp, color = Slate500)
                                }
                                Text(
                                    "Rs. ${String.format("%,.2f", estimatedTotal)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }

            // --- ADDITIONAL NOTES / OTHER DETAILS ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Additional Notes / Other Details (optional)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Slate900
                        )
                        Text(
                            text = "Extra job information to appear on PDF below totals",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                        OutlinedTextField(
                            value = activeDoc.notes,
                            onValueChange = { viewModel.updateNotes(it) },
                            placeholder = { Text("Enter any extra notes or special job conditions...", color = Slate400, fontSize = 13.sp) },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = SleekCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // --- ACTION BUTTON: SAVE & GENERATE PDF ---
            item {
                Button(
                    onClick = {
                        if (activeDoc.clientName.isBlank()) {
                            android.widget.Toast.makeText(context, "Please enter Client Name first", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (activeDoc.items.isEmpty()) {
                            android.widget.Toast.makeText(context, "Please add at least one item", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        scope.launch {
                            isGeneratingPdf = true
                            viewModel.saveAndGeneratePdf(context)
                            isGeneratingPdf = false
                            showSuccessDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SAVE SHEET QUOTATION & VIEW PDF",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- ADD / EDIT SHEET ITEM DIALOG ---
    if (showAddItemDialog) {
        val existingItem = editingItemIndex?.let { activeDoc.items.getOrNull(it) }

        var brand by remember { mutableStateOf("") }
        var thickness by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var standardDesc by remember {
            mutableStateOf(
                "Supply and installation with all necessary materials such as angles, weather sealant, screws, hardware, labor charges and complete pasting and finishing, scaffolding steps included."
            )
        }
        var areaSqFtText by remember { mutableStateOf(existingItem?.quantity?.let { if (it > 0) it.toString() else "" } ?: "") }
        var rateSqFtText by remember { mutableStateOf(existingItem?.rate?.let { if (it > 0) it.toString() else "" } ?: "") }
        var showAreaCalcDialog by remember { mutableStateOf(false) }

        // Parse existing item name if editing
        remember(existingItem) {
            if (existingItem != null) {
                val lines = existingItem.itemName.split("\n")
                if (lines.size > 1) {
                    val header = lines[0]
                    standardDesc = lines.subList(1, lines.size).joinToString("\n")
                    // Parse header e.g., "DUBOND 4mm Panel — Code DU83"
                    val parts = header.split(" Panel — Code ")
                    if (parts.size == 2) {
                        code = parts[1].trim()
                        val brandAndThickness = parts[0].trim().split(" ")
                        if (brandAndThickness.size >= 2) {
                            brand = brandAndThickness[0]
                            thickness = brandAndThickness.subList(1, brandAndThickness.size).joinToString(" ")
                        } else {
                            brand = parts[0]
                        }
                    } else {
                        brand = header
                    }
                } else {
                    standardDesc = existingItem.itemName
                }
            }
        }

        if (showAreaCalcDialog) {
            ItemQuantityDialog(
                itemName = "Sheet Cladding Area",
                unit = "sq.ft",
                rate = rateSqFtText.toDoubleOrNull() ?: 0.0,
                onConfirm = { qty, rate ->
                    areaSqFtText = if (qty > 0) String.format(Locale.ENGLISH, "%.2f", qty) else ""
                    if (rate > 0) {
                        rateSqFtText = String.format(Locale.ENGLISH, "%.2f", rate)
                    }
                    showAreaCalcDialog = false
                },
                onDismiss = { showAreaCalcDialog = false }
            )
        }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = {
                Text(
                    text = if (editingItemIndex == null) "Add Sheet Item (شیٹ آئٹم شامل کریں)" else "Edit Sheet Item",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Slate900
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Brand input + chip suggestions
                    item {
                        Text("Sheet Brand (برانڈ)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate700)
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            placeholder = { Text("e.g. DUBOND, Alucobond, Goldstar") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf("DUBOND", "Alucobond", "Goldstar", "Master", "Prime").forEach { b ->
                                AssistChip(
                                    onClick = { brand = b },
                                    label = { Text(b, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Panel Thickness + chip suggestions
                    item {
                        Text("Panel Thickness (موٹائی)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate700)
                        OutlinedTextField(
                            value = thickness,
                            onValueChange = { thickness = it },
                            placeholder = { Text("e.g. 3mm, 4mm, 6mm") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf("3mm", "4mm", "6mm").forEach { t ->
                                AssistChip(
                                    onClick = { thickness = t },
                                    label = { Text(t, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Sheet Code
                    item {
                        Text("Sheet Code (کوڈ / کلر)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate700)
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            placeholder = { Text("e.g. DU83, Silver, Champagne") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Standard Description
                    item {
                        Text("Standard Description (تفصیل)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate700)
                        OutlinedTextField(
                            value = standardDesc,
                            onValueChange = { standardDesc = it },
                            shape = RoundedCornerShape(10.dp),
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Area & Rate Fields
                    item {
                        HorizontalDivider(color = SleekCardBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Area Sq Ft *", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate700)
                                OutlinedTextField(
                                    value = areaSqFtText,
                                    onValueChange = { areaSqFtText = it },
                                    placeholder = { Text("0.0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rate / Sq Ft *", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate700)
                                OutlinedTextField(
                                    value = rateSqFtText,
                                    onValueChange = { rateSqFtText = it },
                                    placeholder = { Text("0.0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Area Calculator Trigger
                        OutlinedButton(
                            onClick = { showAreaCalcDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PrimaryBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("📐 Area Calculator (Length × Width)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                        }
                    }

                    // Combined Preview
                    item {
                        val headerText = buildString {
                            if (brand.isNotBlank()) append(brand.trim())
                            if (thickness.isNotBlank()) {
                                if (isNotEmpty()) append(" ")
                                append(thickness.trim())
                                if (!thickness.contains("panel", ignoreCase = true)) append(" Panel")
                            }
                            if (code.isNotBlank()) {
                                if (isNotEmpty()) append(" — Code ")
                                append(code.trim())
                            }
                        }
                        val fullPreview = if (headerText.isNotBlank()) "$headerText\n$standardDesc" else standardDesc
                        val areaVal = areaSqFtText.toDoubleOrNull() ?: 0.0
                        val rateVal = rateSqFtText.toDoubleOrNull() ?: 0.0
                        val totalVal = areaVal * rateVal

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate50,
                            border = BorderStroke(1.dp, SleekCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("ITEM DESCRIPTION PREVIEW:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400)
                                Text(fullPreview, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate800)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Calculated Total:", fontSize = 11.sp, color = Slate500)
                                    Text("Rs. ${String.format("%,.2f", totalVal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val areaVal = areaSqFtText.toDoubleOrNull() ?: 0.0
                        val rateVal = rateSqFtText.toDoubleOrNull() ?: 0.0

                        if (areaVal <= 0) {
                            android.widget.Toast.makeText(context, "Please enter Area Sq Ft", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val headerText = buildString {
                            if (brand.isNotBlank()) append(brand.trim())
                            if (thickness.isNotBlank()) {
                                if (isNotEmpty()) append(" ")
                                append(thickness.trim())
                                if (!thickness.contains("panel", ignoreCase = true)) append(" Panel")
                            }
                            if (code.isNotBlank()) {
                                if (isNotEmpty()) append(" — Code ")
                                append(code.trim())
                            }
                        }
                        val finalFullDesc = if (headerText.isNotBlank()) "$headerText\n$standardDesc" else standardDesc

                        if (editingItemIndex != null) {
                            val currentItems = activeDoc.items.toMutableList()
                            val idx = editingItemIndex!!
                            if (idx in currentItems.indices) {
                                val updated = currentItems[idx].copy(
                                    itemName = finalFullDesc,
                                    quantity = areaVal,
                                    rate = rateVal,
                                    amount = areaVal * rateVal
                                )
                                currentItems[idx] = updated
                                viewModel.updateDraftItems(currentItems)
                            }
                        } else {
                            viewModel.addItemToDraft(finalFullDesc, "sq.ft", rateVal, areaVal)
                        }

                        showAddItemDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(if (editingItemIndex == null) "Add Item" else "Update Item", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel", color = Slate700)
                }
            }
        )
    }
}
