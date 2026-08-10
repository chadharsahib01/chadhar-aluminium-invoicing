package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.QuotationGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.WhatsAppShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewShareScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activeDoc by viewModel.activeDocument.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val isInvoice = activeDoc.documentType.equals("INVOICE", ignoreCase = true)
    val docTitle = if (isInvoice) "INVOICE ${activeDoc.documentNumber}" else "QUOTATION ${activeDoc.documentNumber}"
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
                                text = "Preview & Share (پیش نظارہ)",
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HUGE ACTION BUTTON: SHARE ON WHATSAPP ---
            item {
                Button(
                    onClick = {
                        val pdfUri = activeDoc.generatedPdfUri
                        if (pdfUri != null) {
                            WhatsAppShareHelper.sharePdfToWhatsApp(
                                context = context,
                                pdfUri = pdfUri,
                                clientPhone = activeDoc.clientPhone,
                                clientName = activeDoc.clientName,
                                documentNumber = activeDoc.documentNumber
                            )
                        } else {
                            Toast.makeText(context, "PDF generated! Tap again to share.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SHARE ON WHATSAPP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "واٹس ایپ پر گاہک کو بھیجیں",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // SECONDARY BUTTONS: Save PDF & Generic Share
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Save to Phone
                    OutlinedButton(
                        onClick = {
                            if (activeDoc.generatedPdfFile != null) {
                                Toast.makeText(
                                    context,
                                    "PDF saved to Downloads/ChadharAluminium (${activeDoc.generatedPdfFile?.name})",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        border = BorderStroke(1.dp, SleekCardBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Slate800)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save to Phone", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate800)
                    }

                    // Generic Share
                    OutlinedButton(
                        onClick = {
                            val pdfUri = activeDoc.generatedPdfUri
                            if (pdfUri != null) {
                                WhatsAppShareHelper.sharePdfGeneric(
                                    context = context,
                                    pdfUri = pdfUri,
                                    clientName = activeDoc.clientName,
                                    documentNumber = activeDoc.documentNumber
                                )
                            }
                        },
                        border = BorderStroke(1.dp, SleekCardBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Slate800)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Other Share", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate800)
                    }
                }
            }

            // --- DOCUMENT PREVIEW CARD ---
            item {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Header Box
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Slate900,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = settings.businessName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Owner: ${settings.ownerName} | Ph: ${settings.phoneNumber}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = primaryBgColor
                                    ) {
                                        Text(
                                            text = docTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = "Date: ${activeDoc.date}",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Client Box
                        Text(
                            text = "CLIENT: ${activeDoc.clientName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                        Text(
                            text = "PHONE: ${activeDoc.clientPhone}",
                            fontSize = 14.sp,
                            color = Slate500
                        )
                        if (activeDoc.clientAddress.isNotBlank()) {
                            Text(
                                text = "ADDRESS: ${activeDoc.clientAddress}",
                                fontSize = 13.sp,
                                color = Slate500
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = SleekCardBorder)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Items List Table Preview
                        Text(
                            text = "ITEMIZED LIST (${activeDoc.items.size} ITEMS):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        activeDoc.items.forEachIndexed { idx, item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Slate50,
                                border = BorderStroke(1.dp, SleekCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${idx + 1}. ${item.itemName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Slate900
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Qty: ${item.quantity} ${item.unit}  •  Rate: Rs. ${String.format("%,.2f", item.rate)}",
                                            fontSize = 12.sp,
                                            color = Slate500
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Rs. ${String.format("%,.2f", item.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = SleekCardBorder)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Totals Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Subtotal:", fontSize = 14.sp, color = Slate500)
                            Text(text = "Rs. ${String.format("%,.2f", activeDoc.subtotal)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                        }

                        if (activeDoc.discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Discount / Savings (رعایت):", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = QuotationGreen)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = QuotationGreen.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "- Rs. ${String.format("%,.2f", activeDoc.discount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = QuotationGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (activeDoc.taxEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Tax (${activeDoc.taxPercentage}%):", fontSize = 14.sp, color = Slate500)
                                Text(text = "+ Rs. ${String.format("%,.2f", activeDoc.taxAmount)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = primaryBgColor.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GRAND TOTAL:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = primaryBgColor
                                )
                                Text(
                                    text = "Rs. ${String.format("%,.2f", activeDoc.grandTotal)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = primaryBgColor
                                )
                            }
                        }

                        if (activeDoc.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Terms: ${activeDoc.notes}",
                                fontSize = 12.sp,
                                color = Slate500
                            )
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
