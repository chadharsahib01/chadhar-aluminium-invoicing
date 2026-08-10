package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ClientEntity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsListScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onSelectClient: (String) -> Unit // name
) {
    val clients by viewModel.clients.collectAsState()
    val allDocs by viewModel.allDocuments.collectAsState()
    var searchClientText by remember { mutableStateOf("") }

    val filteredClients = clients.filter {
        searchClientText.isBlank() ||
                it.name.contains(searchClientText, ignoreCase = true) ||
                it.phone.contains(searchClientText, ignoreCase = true)
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
                            Text(
                                text = "Client Address Book (گاہکوں کی فہرست)",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchClientText,
                onValueChange = { searchClientText = it },
                placeholder = { Text("Search client name or phone...", color = Slate400) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate500) },
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

            if (filteredClients.isEmpty()) {
                com.example.ui.components.EmptyStateCard(
                    icon = Icons.Default.Person,
                    title = "No Clients Found",
                    description = "Clients are automatically added to your address book when you save an invoice or quotation."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredClients) { client ->
                        val clientDocs = allDocs.filter { it.document.clientName.equals(client.name, ignoreCase = true) }
                        val docCount = clientDocs.size
                        val totalInvoiced = clientDocs.sumOf { it.document.grandTotal }
                        val totalPaid = clientDocs.sumOf { it.document.amountPaid }
                        val remainingDue = (totalInvoiced - totalPaid).coerceAtLeast(0.0)

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SleekCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectClient(client.name) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = PrimaryBlue.copy(alpha = 0.1f),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(text = client.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Slate500, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = if (client.phone.isNotBlank()) client.phone else "No Phone", fontSize = 13.sp, color = Slate500)
                                        }
                                        Text(text = "$docCount Document(s)  •  Balance Due: Rs. ${String.format("%,.0f", remainingDue)}", fontSize = 12.sp, color = if (remainingDue > 0) Color(0xFFDC2626) else PrimaryBlue)
                                    }
                                }

                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                            }
                        }
                    }
                }
            }
        }
    }
}
