package com.example.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.ui.viewmodel.ClassViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ParentPortalScreen(viewModel: ClassViewModel) {
    val themeState by viewModel.selectedTheme.collectAsState()
    val students by viewModel.students.collectAsState()
    
    val primaryColor = if (themeState == "MODERN") Color(0xFFA5B4FC) else Color(0xFFFCD34D)
    val darkBg = if (themeState == "MODERN") Color(0xFF1E1B4B) else Color(0xFF2D2319)
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf("HOMEWORK") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
            .padding(16.dp)
    ) {
        // Top Navbar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Share, contentDescription = "Parent Portal", tint = primaryColor)
            Text(
                "מעקב הורים ומשימות",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { selectedTab = "HOMEWORK" },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "HOMEWORK") primaryColor else Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.weight(1f)
            ) {
                Text("מטלות ושיעורי בית", color = if (selectedTab == "HOMEWORK") Color.Black else Color.White, fontSize = 12.sp)
            }
            Button(
                onClick = { selectedTab = "CALENDAR" },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "CALENDAR") primaryColor else Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.weight(1f)
            ) {
                Text("יומן פדגוגי עומסים", color = if (selectedTab == "CALENDAR") Color.Black else Color.White, fontSize = 12.sp)
            }
        }

        if (selectedTab == "HOMEWORK") {
            // Mocked list of assignments mapped state. 
            // We just use student lists locally to build a UI.
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("סטטוס משימות דינאמי - 'דף עבודה שבועי במקרא'", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(students) { student ->
                    // mock logic based on hashCode to generate random looking but stable statuses
                    val isSubmitted = (student.id.hashCode() % 3) != 0

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSubmitted) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("הוגש ונבדק", color = Color(0xFF10B981), fontSize = 12.sp)
                                    // Simulated Parent Read Receipt
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Done, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = {
                                            // Trigger WhatsApp implicit deep link
                                            val number = "972500000000" // mocked
                                            val url = "https://wa.me/$number?text=הורה יקר, תזכורת: ${student.name} לא פרסם את מטלת השבוע. נשמח לשיתוף פעולה."
                                            val i = Intent(Intent.ACTION_VIEW)
                                            i.data = Uri.parse(url)
                                            context.startActivity(i)
                                        },
                                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF128C7E))
                                    ) {
                                        Icon(Icons.Default.Email, contentDescription = "WhatsApp Remind", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("חסר (ממתין)", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(student.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(student.name.take(1), color = primaryColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Calendar View Overlay Engine
            Column(modifier = Modifier.fillMaxWidth().weight(1f), horizontalAlignment = Alignment.End) {
                Text("יומן פדגוגי - עומס משימות חודשי", color = primaryColor, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calendar Mock UI
                val weekDays = listOf("א", "ב", "ג", "ד", "ה", "ו")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    weekDays.reversed().forEach { day ->
                        Text(day, color = Color.LightGray, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                for (row in 0..4) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (col in 5 downTo 0) {
                            val isExam = (row == 2 && col == 3)
                            val isAssignment = (row == 3 && col == 1)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isExam -> Color.Red.copy(alpha = 0.4f)
                                            isAssignment -> primaryColor.copy(alpha = 0.4f)
                                            else -> Color.White.copy(alpha = 0.05f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val d = row * 6 + col + 1
                                if (d <= 30) {
                                    Text(d.toString(), color = Color.White, fontSize = 10.sp)
                                }
                                if (isExam) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp).align(Alignment.BottomEnd).padding(2.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("מקרא אינדיקציות (עומס לימודי):", color = Color.LightGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Text("מבחן מוסדי - שיא עומס", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(12.dp).background(Color.Red.copy(alpha = 0.4f)))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Text("הגשת מטלה - עומס רגיל", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(12.dp).background(primaryColor.copy(alpha = 0.4f)))
                }
            }
        }
    }
}
