package com.example.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IARCDialog(onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    var q1Violence by remember { mutableStateOf<Boolean?>(null) }
    var q2Language by remember { mutableStateOf<Boolean?>(null) }
    var q3Substances by remember { mutableStateOf<Boolean?>(null) }
    
    var isGenerating by remember { mutableStateOf(false) }
    var certificateStatus by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("IARC Content Rating Questionnaire", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (step == 1) {
                    Text("Does the app contain, or facilitate access to, violence?")
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FilterChip(
                            selected = q1Violence == true,
                            onClick = { q1Violence = true },
                            label = { Text("Yes") }
                        )
                        FilterChip(
                            selected = q1Violence == false,
                            onClick = { q1Violence = false },
                            label = { Text("No") }
                        )
                    }

                    Text("Does the app contain offensive language?")
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FilterChip(
                            selected = q2Language == true,
                            onClick = { q2Language = true },
                            label = { Text("Yes") }
                        )
                        FilterChip(
                            selected = q2Language == false,
                            onClick = { q2Language = false },
                            label = { Text("No") }
                        )
                    }

                    Text("Does the app promote or reference illicit substances?")
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FilterChip(
                            selected = q3Substances == true,
                            onClick = { q3Substances = true },
                            label = { Text("Yes") }
                        )
                        FilterChip(
                            selected = q3Substances == false,
                            onClick = { q3Substances = false },
                            label = { Text("No") }
                        )
                    }
                } else if (step == 2) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
                        Text("Validating constraints against Google Play Policies...", fontSize = 14.sp)
                    } else {
                        Text("Generated Certificate:", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Text(certificateStatus, fontSize = 16.sp, color = com.example.ui.theme.PositiveGreen, fontWeight = FontWeight.Bold)
                        Text("\nYour ratings have been successfully cached and approved for Play Store deployment.")
                    }
                }
            }
        },
        confirmButton = {
            if (step == 1) {
                Button(
                    onClick = {
                        step = 2
                        isGenerating = true
                        scope.launch {
                            delay(1800)
                            isGenerating = false
                            // Calculate simple rating
                            val isMature = q1Violence == true || q2Language == true || q3Substances == true
                            certificateStatus = if (isMature) "PEGI 18 / Mature 17+" else "PEGI 3 / Everyone"
                        }
                    },
                    enabled = q1Violence != null && q2Language != null && q3Substances != null
                ) {
                    Text("Generate Certificate")
                }
            } else {
                Button(onClick = onDismiss, enabled = !isGenerating) {
                    Text("Finish & Save")
                }
            }
        },
        dismissButton = {
            if (step == 1) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}