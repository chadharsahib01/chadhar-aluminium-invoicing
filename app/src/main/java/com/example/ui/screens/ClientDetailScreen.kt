package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekCardBorder
import com.example.ui.viewmodel.MainViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    viewModel: MainViewModel,
    clientName: String,
    onNavigateBack: () -> Unit,
    onSelectDocument: (Long) -> Unit
) {
    val context = LocalContext.current
    val clients by viewModel.clients.collectAsState()
    val allDocs by viewModel.allDocuments.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val clientObj = clients.find { it.name.equals(clientName, ignoreCase = true) }
    val clientDocs = allDocs.filter { it.document.clientName.equals(clientName, ignoreCase = true) }

    val totalInvoiced = clientDocs.sumOf { it.document.grandTotal }
    val totalReceived = clientDocs.sumOf { it.document.amountPaid }
    val totalDue = (totalInvoiced - totalReceived).coerceAtLeast(0.0)

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
                                text = clientName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Slate900
                            )
                        },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Client Contact Info Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "CLIENT CONTACT (رابطہ)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Phone: ${clientObj?.phone?.ifBlank { "N/A" } ?: "N/A"}", fontSize = 14.sp, color = Slate700)
                        Text(text = "Address: ${clientObj?.address?.ifBlank { "N/A" } ?: "N/A"}", fontSize = 14.sp, color = Slate700)

                        if (!clientObj?.phone.isNullOrBlank() && totalDue > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val cleanPhone = clientObj!!.phone.replace("[^0-9]".toRegex(), "")
                                    val message = "Dear ${clientObj.name},\nThis is a polite payment reminder from ${settings.businessName}.\nYour total balance due is Rs. ${String.format("%,.2f", totalDue)}.\nKindly clear at your earliest convenience. Thank you!"
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${URLEncoder.encode(message, "UTF-8")}")
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Total Balance Reminder via WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Client Ledger Card (Feature 13)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "CLIENT LEDGER SUMMARY (گاہک کا اکاؤنٹ)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total Invoiced:", fontSize = 13.sp, color = Slate700)
                            Text(text = "Rs. ${String.format("%,.2f", totalInvoiced)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total Received:", fontSize = 13.sp, color = Slate700)
                            Text(text = "Rs. ${String.format("%,.2f", totalReceived)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF16A34A))
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SleekCardBorder)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total Balance Due:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                            Text(text = "Rs. ${String.format("%,.2f", totalDue)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (totalDue > 0) Color(0xFFDC2626) else Color(0xFF16A34A))
                        }
                    }
                }
            }

            // Client Document History
            item {
                Text(text = "DOCUMENT HISTORY (${clientDocs.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
            }

            if (clientDocs.isEmpty()) {
                item {
                    com.example.ui.components.EmptyStateCard(
                        icon = Icons.Default.ArrowBack,
                        title = "No Documents for Client",
                        description = "There are currently no recorded quotations or invoices for this client."
                    )
                }
            } else {
                items(clientDocs) { docWithItems ->
                    val doc = docWithItems.document
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SleekCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDocument(doc.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${doc.documentType} #${doc.documentNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                Text(text = "Date: ${doc.date}  •  Status: ${doc.paymentStatus}", fontSize = 12.sp, color = Slate500)
                                Text(text = "Total: Rs. ${String.format("%,.2f", doc.grandTotal)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                        }
                    }
                }
            }
        }
    }
}
