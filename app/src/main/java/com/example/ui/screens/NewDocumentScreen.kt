package com.example.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.data.entity.CatalogItemEntity
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddCustomItemDialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewDocumentScreen(
    viewModel: MainViewModel,
    docType: String,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeDoc by viewModel.activeDocument.collectAsState()
    val catalogItems by viewModel.catalogItems.collectAsState()

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var pendingSelectedItem by remember { mutableStateOf<CatalogItemEntity?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var catalogDropdownExpanded by remember { mutableStateOf(false) }
    var catalogSearchText by remember { mutableStateOf("") }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    if (isGeneratingPdf) {
        com.example.ui.components.LoadingProgressDialog("Saving & Generating PDF...")
    }

    if (showSuccessDialog) {
        com.example.ui.components.SuccessCheckmarkDialog(
            title = "Document Saved!",
            message = "PDF generated successfully.",
            onDismiss = {
                showSuccessDialog = false
                onNavigateToPreview()
            }
        )
    }

    // Loss Aversion: Intercept back press when unsaved changes exist
    val hasUnsavedChanges = activeDoc.clientName.isNotBlank() || activeDoc.items.isNotEmpty()
    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = {
                Text(
                    text = "Leave without saving?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Slate900
                )
            },
            text = {
                Text(
                    text = "You'll lose this quotation/invoice if you leave now!\n(توجہ: یہ ڈیٹا ضائع ہو جائے گا)",
                    fontSize = 14.sp,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text("Leave & Discard", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showUnsavedDialog = false },
                    border = BorderStroke(1.dp, PrimaryBlue)
                ) {
                    Text("Stay & Edit", fontWeight = FontWeight.Bold, color = PrimaryBlue)
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
                e.printStackTrace()
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
            val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time)
            viewModel.updateDocumentMeta(activeDoc.documentNumber, formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (showAddCustomDialog) {
        AddCustomItemDialog(
            onAddItem = { name, unit, rate, quantity ->
                viewModel.addCustomCatalogItem(name, unit, rate, quantity, addToDraft = true)
            },
            onDismiss = { showAddCustomDialog = false }
        )
    }

    pendingSelectedItem?.let { selectedItem ->
        com.example.ui.components.ItemQuantityDialog(
            itemName = selectedItem.name,
            unit = selectedItem.defaultUnit,
            rate = selectedItem.defaultRate,
            onConfirm = { quantity, finalRate ->
                viewModel.addItemToDraft(selectedItem.name, selectedItem.defaultUnit, finalRate, quantity)
                pendingSelectedItem = null
            },
            onDismiss = {
                pendingSelectedItem = null
            }
        )
    }

    val isInvoice = docType.equals("INVOICE", ignoreCase = true)
    val headerTitle = if (isInvoice) "Create Invoice (انوائس)" else "Create Quotation (کوٹیشن)"
    val primaryBgColor = if (isInvoice) PrimaryBlue else QuotationGreen

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
                            Text(
                                text = headerTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Slate900
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (hasUnsavedChanges) {
                                    showUnsavedDialog = true
                                } else {
                                    onNavigateBack()
                                }
                            }) {
                                Surface(
                                    shape = CircleShape,
                                    color = Slate50,
                                    border = BorderStroke(1.dp, SleekCardBorder),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Slate800, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                    HorizontalDivider(color = SleekCardBorder, thickness = 1.dp)
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    HorizontalDivider(color = SleekCardBorder, thickness = 1.dp)
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                if (activeDoc.clientName.isNotBlank() && activeDoc.items.isNotEmpty()) {
                                    scope.launch {
                                        isGeneratingPdf = true
                                        viewModel.saveAndGeneratePdf(context)
                                        isGeneratingPdf = false
                                        showSuccessDialog = true
                                    }
                                }
                            },
                            enabled = activeDoc.clientName.isNotBlank() && activeDoc.items.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBgColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PREVIEW & SHARE PDF (پی ڈی ایف)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 0: GOAL GRADIENT 3-STEP PROGRESS BAR ---
            item {
                FlowStepBar(
                    hasClient = activeDoc.clientName.isNotBlank(),
                    hasItems = activeDoc.items.isNotEmpty(),
                    primaryColor = primaryBgColor
                )
            }

            // --- SECTION 1: DOCUMENT META (Doc # & Date) ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = activeDoc.documentNumber,
                                onValueChange = { viewModel.updateDocumentMeta(it, activeDoc.date) },
                                label = { Text("Doc Number", color = Slate500) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = primaryBgColor,
                                    unfocusedBorderColor = SleekCardBorder
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = activeDoc.date,
                                onValueChange = { viewModel.updateDocumentMeta(activeDoc.documentNumber, it) },
                                label = { Text("Date", color = Slate500) },
                                trailingIcon = {
                                    IconButton(onClick = { datePickerDialog.show() }) {
                                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = Slate500)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = primaryBgColor,
                                    unfocusedBorderColor = SleekCardBorder
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- SECTION 2: CLIENT DETAILS ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CLIENT DETAILS (گاہک کی تفصیلات)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )

                        OutlinedTextField(
                            value = activeDoc.clientName,
                            onValueChange = { viewModel.updateClientDetails(it, activeDoc.clientPhone, activeDoc.clientAddress) },
                            label = { Text("Client Name (گاہک کا نام) *", color = Slate500) },
                            placeholder = { Text("e.g. Muhammad Usman", color = Slate400) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = primaryBgColor,
                                unfocusedBorderColor = SleekCardBorder
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = activeDoc.clientPhone,
                                onValueChange = { viewModel.updateClientDetails(activeDoc.clientName, it, activeDoc.clientAddress) },
                                label = { Text("Phone Number (واٹس ایپ نمبر) *", color = Slate500) },
                                placeholder = { Text("0300-1234567", color = Slate400) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = primaryBgColor,
                                    unfocusedBorderColor = SleekCardBorder
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { contactPickerLauncher.launch(null) },
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(52.dp)
                                    .background(PrimaryBlueLight, RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = "Pick Contact",
                                    tint = PrimaryBlue
                                )
                            }
                        }

                        OutlinedTextField(
                            value = activeDoc.clientAddress,
                            onValueChange = { viewModel.updateClientDetails(activeDoc.clientName, activeDoc.clientPhone, it) },
                            label = { Text("Address (پتہ - Optional)", color = Slate500) },
                            placeholder = { Text("e.g. Main Market, Lahore", color = Slate400) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = primaryBgColor,
                                unfocusedBorderColor = SleekCardBorder
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // --- SECTION 3: ADD ITEMS SECTION ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ADD CLADDING ITEMS (سامان درج کریں)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )

                        // Searchable Dropdown
                        ExposedDropdownMenuBox(
                            expanded = catalogDropdownExpanded,
                            onExpandedChange = { catalogDropdownExpanded = !catalogDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = catalogSearchText,
                                onValueChange = {
                                    catalogSearchText = it
                                    catalogDropdownExpanded = true
                                },
                                label = { Text("Select Pre-loaded Item (یا تلاش کریں)", color = Slate500) },
                                placeholder = { Text("e.g. ACP Sheet 4mm, Frame, Labor...", color = Slate400) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catalogDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = primaryBgColor,
                                    unfocusedBorderColor = SleekCardBorder
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            val filteredCatalog = catalogItems.filter {
                                catalogSearchText.isBlank() || it.name.contains(catalogSearchText, ignoreCase = true)
                            }

                            ExposedDropdownMenu(
                                expanded = catalogDropdownExpanded && filteredCatalog.isNotEmpty(),
                                onDismissRequest = { catalogDropdownExpanded = false }
                            ) {
                                filteredCatalog.take(10).forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                                Text(text = "Unit: ${item.defaultUnit} | Rate: Rs. ${item.defaultRate}", fontSize = 12.sp, color = Slate500)
                                            }
                                        },
                                        onClick = {
                                            pendingSelectedItem = item
                                            catalogSearchText = ""
                                            catalogDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Button for Custom Item
                        OutlinedButton(
                            onClick = { showAddCustomDialog = true },
                            border = BorderStroke(1.dp, PrimaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "+ ADD CUSTOM ITEM (نیا سامان)", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }
                }
            }

            // --- SECTION 4: ITEMS TABLE ROWS ---
            if (activeDoc.items.isEmpty()) {
                item {
                    com.example.ui.components.EmptyStateCard(
                        icon = Icons.Default.AddCircleOutline,
                        title = "No Items Added",
                        description = "Select a pre-loaded aluminium/glass item from the dropdown above or tap '+ Add Custom Item'."
                    )
                }
            } else {
                itemsIndexed(activeDoc.items) { index, item ->
                    ItemRowCard(
                        index = index,
                        item = item,
                        primaryColor = primaryBgColor,
                        onUpdate = { qty, rate ->
                            viewModel.updateDraftItem(index, qty, rate)
                        },
                        onDelete = {
                            viewModel.removeDraftItem(index)
                        }
                    )
                }
            }

            // --- SECTION 5: CALCULATIONS & TOTALS ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Subtotal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Subtotal:", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            Text(
                                text = "Rs. ${String.format("%,.2f", activeDoc.subtotal)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        // Discount Field
                        var discountText by remember(activeDoc.discount) {
                            mutableStateOf(if (activeDoc.discount > 0) activeDoc.discount.toString() else "")
                        }
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = {
                                discountText = it
                                val dVal = it.toDoubleOrNull() ?: 0.0
                                viewModel.updateTotalsAndNotes(dVal, activeDoc.taxEnabled, activeDoc.taxPercentage, activeDoc.notes)
                            },
                            label = { Text("Discount (رعایت) - Rs.", color = Slate500) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = primaryBgColor,
                                unfocusedBorderColor = SleekCardBorder
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Tax Switch Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Apply Tax / GST (ٹیکس)", fontWeight = FontWeight.SemiBold, color = Slate900)
                                Text(
                                    text = "GST (${activeDoc.taxPercentage}%)",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                            Switch(
                                checked = activeDoc.taxEnabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.updateTotalsAndNotes(activeDoc.discount, isChecked, activeDoc.taxPercentage, activeDoc.notes)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = primaryBgColor)
                            )
                        }

                        // Grand Total Box
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = primaryBgColor.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GRAND TOTAL (کل رقم):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = primaryBgColor
                                )
                                Text(
                                    text = "Rs. ${String.format("%,.2f", activeDoc.grandTotal)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = primaryBgColor
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 6: TERMS & NOTES ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TERMS & NOTES (شرائط)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = activeDoc.notes,
                            onValueChange = {
                                viewModel.updateTotalsAndNotes(activeDoc.discount, activeDoc.taxEnabled, activeDoc.taxPercentage, it)
                            },
                            placeholder = { Text("e.g. 50% advance required. Rate valid for 15 days.", color = Slate400) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = primaryBgColor,
                                unfocusedBorderColor = SleekCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ItemRowCard(
    index: Int,
    item: DraftItem,
    primaryColor: Color,
    onUpdate: (qty: Double, rate: Double) -> Unit,
    onDelete: () -> Unit
) {
    var qtyStr by remember(item.quantity) { mutableStateOf(if (item.quantity == 0.0) "" else item.quantity.toString()) }
    var rateStr by remember(item.rate) { mutableStateOf(if (item.rate == 0.0) "" else item.rate.toString()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Item Number + Title + Unit Pill + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${index + 1}. ${item.itemName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate50,
                        border = BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Text(
                            text = "Unit: ${item.unit}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inputs Row: 2-column Row for Quantity and Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity field
                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = {
                        qtyStr = it
                        val qVal = it.toDoubleOrNull() ?: 0.0
                        val rVal = rateStr.toDoubleOrNull() ?: 0.0
                        onUpdate(qVal, rVal)
                    },
                    label = { Text("Qty (${item.unit})", color = Slate500, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = SleekCardBorder
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )

                // Rate field
                OutlinedTextField(
                    value = rateStr,
                    onValueChange = {
                        rateStr = it
                        val qVal = qtyStr.toDoubleOrNull() ?: 0.0
                        val rVal = it.toDoubleOrNull() ?: 0.0
                        onUpdate(qVal, rVal)
                    },
                    label = { Text("Rate (Rs.)", color = Slate500, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = SleekCardBorder
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlighted Amount Banner inside Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = primaryColor.copy(alpha = 0.06f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Item Subtotal:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate500
                    )
                    Text(
                        text = "Rs. ${String.format("%,.2f", item.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun FlowStepBar(
    hasClient: Boolean,
    hasItems: Boolean,
    primaryColor: Color
) {
    val targetProgress = when {
        hasClient && hasItems -> 1.0f
        hasClient || hasItems -> 0.66f
        else -> 0.33f
    }
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 350),
        label = "StepProgress"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepBadge(
                    stepNum = "1",
                    label = "Client Info",
                    isComplete = hasClient,
                    isActive = !hasClient,
                    accentColor = primaryColor
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(16.dp)
                )
                StepBadge(
                    stepNum = "2",
                    label = "Add Items",
                    isComplete = hasItems,
                    isActive = hasClient && !hasItems,
                    accentColor = primaryColor
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(16.dp)
                )
                StepBadge(
                    stepNum = "3",
                    label = "PDF Share",
                    isComplete = false,
                    isActive = hasClient && hasItems,
                    accentColor = primaryColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = primaryColor,
                trackColor = Slate100
            )
        }
    }
}

@Composable
private fun StepBadge(
    stepNum: String,
    label: String,
    isComplete: Boolean,
    isActive: Boolean,
    accentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = when {
                isComplete -> QuotationGreen
                isActive -> accentColor
                else -> Slate100
            },
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isComplete) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = stepNum,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isActive) Color.White else Slate500
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isActive || isComplete) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive || isComplete) Slate900 else Slate500
        )
    }
}
