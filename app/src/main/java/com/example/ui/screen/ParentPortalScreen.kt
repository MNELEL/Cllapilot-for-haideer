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
    
    val primaryColor = if (themeState == "MODERN") com.example.ui.theme.GoldGingerStart else com.example.ui.theme.GoldGingerStart
    val darkBg = if (themeState == "MODERN") com.example.ui.theme.ChocolateBrown.copy(alpha=0.9f) else com.example.ui.theme.ChocolateBrown
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf("HOMEWORK") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
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
                color = com.example.ui.theme.ChocolateBrown
            )
        }

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { com.example.ui.SoundManager.playClick();  selectedTab = "HOMEWORK" },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "HOMEWORK") primaryColor else Color.White.copy(alpha = 0.8f)),
                modifier = Modifier.weight(1f)
            ) {
                Text("מטלות ושיעורי בית", color = if (selectedTab == "HOMEWORK") Color.Black else Color.White, fontSize = 12.sp)
            }
            Button(
                onClick = { com.example.ui.SoundManager.playClick();  selectedTab = "CALENDAR" },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "CALENDAR") primaryColor else Color.White.copy(alpha = 0.8f)),
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
                    Text("סטטוס משימות דינאמי - 'דף עבודה שבועי במקרא'", color = com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(students) { student ->
                    // mock logic based on hashCode to generate random looking but stable statuses
                    val isSubmitted = (student.id.hashCode() % 3) != 0

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left column: submission elements (mimicking shrink-0 and proportional layout weight)
                            Row(
                                modifier = Modifier.weight(1.1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                if (isSubmitted) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = com.example.ui.theme.PositiveGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "הוגש ונבדק",
                                        color = com.example.ui.theme.PositiveGreen,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    // Simulated Parent Read Receipt
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Done, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { com.example.ui.SoundManager.playClick(); 
                                                // Trigger WhatsApp implicit deep link
                                                val number = "972500000000" // mocked
                                                val url = "https://wa.me/$number?text=הורה יקר, תזכורת: ${student.name} לא פרסם את מטלת השבוע. נשמח לשיתוף פעולה."
                                                val i = Intent(Intent.ACTION_VIEW)
                                                i.data = Uri.parse(url)
                                                context.startActivity(i)
                                            },
                                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF128C7E))
                                        ) {
                                            Icon(Icons.Default.Email, contentDescription = "WhatsApp Remind", tint = com.example.ui.theme.ChocolateBrown, modifier = Modifier.size(14.dp))
                                        }
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC0392B), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "חסר",
                                                color = com.example.ui.theme.ChocolateBrown,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Right column: student info (mimicking min-w-0 / truncate)
                            Row(
                                modifier = Modifier.weight(0.9f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    student.name,
                                    color = com.example.ui.theme.ChocolateBrown,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.8f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(student.name.take(1), fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
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
                        Text(day, color = com.example.ui.theme.MochaTaupe, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
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
                                            else -> Color.White.copy(alpha = 0.8f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val d = row * 6 + col + 1
                                if (d <= 30) {
                                    Text(d.toString(), color = com.example.ui.theme.ChocolateBrown, fontSize = 10.sp)
                                }
                                if (isExam) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = com.example.ui.theme.ChocolateBrown, modifier = Modifier.size(10.dp).align(Alignment.BottomEnd).padding(2.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("מקרא אינדיקציות (עומס לימודי):", color = com.example.ui.theme.MochaTaupe, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Text("מבחן מוסדי - שיא עומס", color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(12.dp).background(Color.Red.copy(alpha = 0.4f)))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Text("הגשת מטלה - עומס רגיל", color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(12.dp).background(primaryColor.copy(alpha = 0.4f)))
                }
            }
        }
    }
}
