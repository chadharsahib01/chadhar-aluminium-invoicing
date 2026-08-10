package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SleekCardBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomItemDialog(
    onAddItem: (name: String, unit: String, rate: Double, quantity: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("sq.ft") }
    var rateStr by remember { mutableStateOf("") }
    var qtyStr by remember { mutableStateOf("1") }
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var showAreaCalc by remember { mutableStateOf(false) }

    if (showAreaCalc) {
        AreaCalculatorDialog(
            onAreaCalculated = { areaSqFt ->
                qtyStr = if (areaSqFt % 1.0 == 0.0) {
                    areaSqFt.toInt().toString()
                } else {
                    String.format("%.2f", areaSqFt)
                }
                showAreaCalc = false
            },
            onDismiss = { showAreaCalc = false }
        )
    }

    val unitsList = listOf("sq.ft", "running ft", "piece", "kg", "box", "roll", "liter", "lump sum")

    val rateVal = rateStr.toDoubleOrNull() ?: 0.0
    val qtyVal = qtyStr.toDoubleOrNull() ?: 0.0
    val totalAmount = qtyVal * rateVal

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null,
                tint = PrimaryBlue
            )
        },
        title = {
            Text(
                text = "+ Add Custom Item (نیا سامان)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name / Description", color = Slate500) },
                    placeholder = { Text("e.g. Custom ACP Corner Trim", color = Slate400) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = SleekCardBorder
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = unitDropdownExpanded,
                    onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit", color = Slate500) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate900,
                            unfocusedTextColor = Slate900,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SleekCardBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = unitDropdownExpanded,
                        onDismissRequest = { unitDropdownExpanded = false }
                    ) {
                        unitsList.forEach { unitItem ->
                            DropdownMenuItem(
                                text = { Text(unitItem, color = Slate900) },
                                onClick = {
                                    selectedUnit = unitItem
                                    unitDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Rate per unit (Rs.)", color = Slate500) },
                    placeholder = { Text("e.g. 500", color = Slate400) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = SleekCardBorder
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Quantity ($selectedUnit)", color = Slate500) },
                    placeholder = { Text("e.g. 1", color = Slate400) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = SleekCardBorder
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showAreaCalc = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📐 Area Calculator (لمبائی x چوڑائی)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                if (qtyVal > 0 && rateVal > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Item Subtotal:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            Text(
                                text = "Rs. ${String.format("%,.2f", totalAmount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && qtyVal > 0) {
                        onAddItem(name.trim(), selectedUnit, rateVal, qtyVal)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && qtyVal > 0,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Add Item (درج کریں)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Cancel", color = Slate700)
            }
        }
    )
}
