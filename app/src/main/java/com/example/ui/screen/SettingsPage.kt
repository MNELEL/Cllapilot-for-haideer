package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ClassViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(viewModel: ClassViewModel) {
    val isLightMode = viewModel.selectedTheme.collectAsState().value == "MODERN"
    val appBg = if (isLightMode) com.example.ui.theme.CreamBeige else com.example.ui.theme.ChocolateBrown
    val baseTextColor = if (isLightMode) com.example.ui.theme.ChocolateBrown else Color.White

    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    // Mobile-first UI patterns for complex fields
    var selectedDate by remember { mutableStateOf("בחר תאריך") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Multi-select state
    val options = listOf("התראות דחיפה", "מיילים עדכניים", "סנכרון אוטומטי")
    var selectedOptions by remember { mutableStateOf(setOf<String>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBg)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "הגדרות חשבון (Settings)",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = baseTextColor
            )

            // Mobile-first Date Picker Trigger
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("העדפות כלליות", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp), // 48dp >= 44px
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text(selectedDate, color = Color.Black, fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("הגדרות התראות (Multi-select)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp) // Touch target minimum 48dp
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp)
                        ) {
                            Checkbox(
                                checked = selectedOptions.contains(option),
                                onCheckedChange = { checked ->
                                    selectedOptions = if (checked) {
                                        selectedOptions + option
                                    } else {
                                        selectedOptions - option
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("אזור מסוכן (Danger Zone)", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "מחיקת החשבון והנתונים מהמסד נתונים (Delete Account)",
                        fontSize = 14.sp, 
                        color = Color.DarkGray
                    )

                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp) // Mobile compatibility: Min 44px touch target
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("אישור מחיקת חשבון") },
            text = { Text("האם אתה בטוח שברצונך למחוק את החשבון ואת כל הנתונים במסד? פעולה זו אינה ניתנת לביטול.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearSeatingLayout() // Acts as Delete Data
                        viewModel.clearAllGrades()     // Acts as Delete Data
                        // Delete everything
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("מחק הכל")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("ביטול")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    selectedDate = "תאריך עודכן פנימית"
                }) {
                    Text("אישור")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("ביטול")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
