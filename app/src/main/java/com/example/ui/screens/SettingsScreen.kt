package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.CatalogItemEntity
import com.example.ui.components.AddCustomItemDialog
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.RedAccent
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekCardBorder
import com.example.ui.viewmodel.MainViewModel
import com.example.util.WhatsAppShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val catalogItems by viewModel.catalogItems.collectAsState()

    var businessName by remember(settings.businessName) { mutableStateOf(settings.businessName) }
    var ownerName by remember(settings.ownerName) { mutableStateOf(settings.ownerName) }
    var phoneNumber by remember(settings.phoneNumber) { mutableStateOf(settings.phoneNumber) }
    var address by remember(settings.address) { mutableStateOf(settings.address) }
    var logoUriStr by remember(settings.logoUri) { mutableStateOf(settings.logoUri) }
    var defaultNotes by remember(settings.defaultNotes) { mutableStateOf(settings.defaultNotes) }

    var nextInvNum by remember(settings.nextInvoiceNumber) { mutableStateOf(settings.nextInvoiceNumber.toString()) }
    var nextQuoNum by remember(settings.nextQuotationNumber) { mutableStateOf(settings.nextQuotationNumber.toString()) }

    var showAddCatalogDialog by remember { mutableStateOf(false) }
    var isExportingCsv by remember { mutableStateOf(false) }

    if (isExportingCsv) {
        com.example.ui.components.LoadingProgressDialog("Generating Accountant CSV...")
    }

    // Image Picker Launcher for Logo
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            logoUriStr = uri.toString()
            viewModel.updateSettings(settings.copy(logoUri = uri.toString()))
            Toast.makeText(context, "Logo updated!", Toast.LENGTH_SHORT).show()
        }
    }

    // Image Picker Launcher for Signature
    val signaturePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateSettings(settings.copy(stampUri = uri.toString()))
            Toast.makeText(context, "Digital Signature updated!", Toast.LENGTH_SHORT).show()
        }
    }

    // JSON File Import Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val success = viewModel.importBackupJson(context, uri)
                if (success) {
                    Toast.makeText(context, "Data successfully restored!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to import backup file.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showAddCatalogDialog) {
        AddCustomItemDialog(
            onAddItem = { name, unit, rate, _ ->
                viewModel.addCustomCatalogItem(name, unit, rate, addToDraft = false)
            },
            onDismiss = { showAddCatalogDialog = false }
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
                        title = { Text("Settings (سیٹنگز)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate900) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: BUSINESS DETAILS ---
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BUSINESS DETAILS (کاروبار کی معلومات)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                        }

                        // Logo Section
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = PrimaryBlueLight,
                                modifier = Modifier.size(60.dp)
                            ) {
                                if (!logoUriStr.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = logoUriStr,
                                        contentDescription = "Logo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                } else {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = PrimaryBlue)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            OutlinedButton(
                                onClick = { logoPickerLauncher.launch("image/*") },
                                border = BorderStroke(1.dp, SleekCardBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (logoUriStr.isNullOrEmpty()) "+ Upload Logo" else "Change Logo",
                                    color = Slate800,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business Name", color = Slate500) },
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

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner Name", color = Slate500) },
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

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number", color = Slate500) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Business Address (Optional)", color = Slate500) },
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

                        OutlinedTextField(
                            value = defaultNotes,
                            onValueChange = { defaultNotes = it },
                            label = { Text("Default Notes / Terms", color = Slate500) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = SleekCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.updateSettings(
                                    settings.copy(
                                        businessName = businessName,
                                        ownerName = ownerName,
                                        phoneNumber = phoneNumber,
                                        address = address,
                                        logoUri = logoUriStr,
                                        defaultNotes = defaultNotes
                                    )
                                )
                                Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "SAVE BUSINESS INFO", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // --- SECTION 1B: DIGITAL SIGNATURE & STAMP ---
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
                            text = "DIGITAL SIGNATURE / STAMP (دستخط / سٹیمپ)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = PrimaryBlueLight,
                                modifier = Modifier.size(80.dp, 50.dp)
                            ) {
                                if (!settings.signatureUri.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = settings.signatureUri,
                                        contentDescription = "Signature",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("No Signature", fontSize = 11.sp, color = Slate500)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            OutlinedButton(
                                onClick = { signaturePickerLauncher.launch("image/*") },
                                border = BorderStroke(1.dp, SleekCardBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (settings.signatureUri.isNullOrEmpty()) "+ Upload Signature" else "Change Signature",
                                    color = Slate800,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 1C: SECURITY PIN LOCK ---
            item {
                val currentPin = settings.pinCode
                val currentEnabled = settings.isPinEnabled
                var pinInput by remember(currentPin) { mutableStateOf(currentPin) }
                var isPinEnabled by remember(currentEnabled) { mutableStateOf(currentEnabled) }

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
                            text = "APP SECURITY PIN LOCK (پن لاک)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable 4-Digit PIN Lock", fontSize = 13.sp, color = Slate800)
                            androidx.compose.material3.Switch(
                                checked = isPinEnabled,
                                onCheckedChange = { isPinEnabled = it }
                            )
                        }

                        if (isPinEnabled) {
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { newValue ->
                                    if (newValue.length <= 4) {
                                        pinInput = newValue.filter { ch -> ch.isDigit() }
                                    }
                                },
                                label = { Text("4-Digit PIN Code", color = Slate500) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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

                        Button(
                            onClick = {
                                viewModel.updateSettings(
                                    settings.copy(
                                        isPinEnabled = isPinEnabled,
                                        pinCode = pinInput,
                                        appPin = if (isPinEnabled) pinInput else null
                                    )
                                )
                                Toast.makeText(context, "PIN Lock settings saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE PIN SETTINGS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- SECTION 1D: DARK MODE APPERANCE ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (settings.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Dark Mode Theme (ڈارک موڈ)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = if (settings.isDarkMode) "Enabled" else "Disabled",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }

                        androidx.compose.material3.Switch(
                            checked = settings.isDarkMode,
                            onCheckedChange = { isChecked ->
                                viewModel.updateSettings(settings.copy(isDarkMode = isChecked))
                            }
                        )
                    }
                }
            }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Numbers, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DOCUMENT NUMBERING (نمبرنگ)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = nextInvNum,
                                onValueChange = { nextInvNum = it.filter { c -> c.isDigit() } },
                                label = { Text("Next Invoice #", color = Slate500) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = SleekCardBorder
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = nextQuoNum,
                                onValueChange = { nextQuoNum = it.filter { c -> c.isDigit() } },
                                label = { Text("Next Quotation #", color = Slate500) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = SleekCardBorder
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                val invVal = nextInvNum.toIntOrNull() ?: 1
                                val quoVal = nextQuoNum.toIntOrNull() ?: 1
                                viewModel.updateSettings(
                                    settings.copy(
                                        nextInvoiceNumber = invVal,
                                        nextQuotationNumber = quoVal
                                    )
                                )
                                Toast.makeText(context, "Numbering updated!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(text = "UPDATE NUMBERING", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // --- SECTION 3: BACKUP & RESTORE DATA ---
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BACKUP & RESTORE DATA (بیک اپ)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Export
                            Button(
                                onClick = {
                                    scope.launch {
                                        val backupUri = viewModel.exportBackupJson(context)
                                        if (backupUri != null) {
                                            WhatsAppShareHelper.sharePdfGeneric(context, backupUri, "Backup", "Chadhar_Backup")
                                        } else {
                                            Toast.makeText(context, "Failed to create backup.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Export Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Import
                            OutlinedButton(
                                onClick = { importLauncher.launch("application/json") },
                                border = BorderStroke(1.dp, SleekCardBorder),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = Slate800, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Restore Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate800)
                            }
                        }

                        // Feature 16: Accountant CSV Export
                        Button(
                            onClick = {
                                scope.launch {
                                    isExportingCsv = true
                                    val csvUri = viewModel.exportCsvForAccountant(context)
                                    isExportingCsv = false
                                    if (csvUri != null) {
                                        WhatsAppShareHelper.sharePdfGeneric(context, csvUri, "CSV Report", "Accountant_Report")
                                    } else {
                                        Toast.makeText(context, "No documents to export.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("EXPORT EXCEL/CSV FOR ACCOUNTANT (اکاؤنٹنٹ رپورٹس)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- SECTION 4: MANAGING CATALOG ITEM PRICES ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CATALOG ITEM RATES (قیمت کی فہرست)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )

                            IconButton(onClick = { showAddCatalogDialog = true }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = PrimaryBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        catalogItems.forEach { catItem ->
                            CatalogRateRow(
                                item = catItem,
                                onSaveRate = { newRate ->
                                    viewModel.updateCatalogItem(catItem.copy(defaultRate = newRate))
                                },
                                onDelete = {
                                    viewModel.deleteCatalogItem(catItem)
                                }
                            )
                            HorizontalDivider(color = SleekCardBorder, modifier = Modifier.padding(vertical = 4.dp))
                        }
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
private fun CatalogRateRow(
    item: CatalogItemEntity,
    onSaveRate: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var rateStr by remember(item.defaultRate) { mutableStateOf(item.defaultRate.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(text = item.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate900)
            Text(text = "Unit: ${item.defaultUnit}", fontSize = 11.sp, color = Slate500)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = rateStr,
                onValueChange = {
                    rateStr = it
                    val newR = it.toDoubleOrNull() ?: 0.0
                    onSaveRate(newR)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = SleekCardBorder
                ),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(90.dp)
                    .height(48.dp)
            )

            if (item.isCustom) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent)
                }
            }
        }
    }
}
