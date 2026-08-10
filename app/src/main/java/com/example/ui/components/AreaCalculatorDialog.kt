package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun AreaCalculatorDialog(
    onAreaCalculated: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var lengthText by remember { mutableStateOf("") }
    var widthText by remember { mutableStateOf("") }

    val lengthVal = lengthText.toDoubleOrNull() ?: 0.0
    val widthVal = widthText.toDoubleOrNull() ?: 0.0
    val calculatedArea = lengthVal * widthVal

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Area Calculator (لمبائی x چوڑائی)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Slate900
            )
        },
        text = {
            Column {
                Text(
                    text = "Calculate total sq.ft by entering Length and Width in feet:",
                    fontSize = 13.sp,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = lengthText,
                        onValueChange = { lengthText = it },
                        label = { Text("Length (ft)", color = Slate500) },
                        placeholder = { Text("e.g. 10", color = Slate400) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate900,
                            unfocusedTextColor = Slate900,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SleekCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = widthText,
                        onValueChange = { widthText = it },
                        label = { Text("Width (ft)", color = Slate500) },
                        placeholder = { Text("e.g. 15", color = Slate400) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate900,
                            unfocusedTextColor = Slate900,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SleekCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = PrimaryBlue.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Total Calculated Area:", fontSize = 12.sp, color = Slate500)
                        Text(
                            text = "${String.format("%.2f", calculatedArea)} sq.ft",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (calculatedArea > 0) {
                        onAreaCalculated(calculatedArea)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = calculatedArea > 0
            ) {
                Text("Use Area (${String.format("%.1f", calculatedArea)} sq.ft)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Slate700)
            }
        }
    )
}
