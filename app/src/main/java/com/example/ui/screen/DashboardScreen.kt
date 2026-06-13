package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StudentEntity
import com.example.ui.viewmodel.ClassViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: ClassViewModel) {
    var showStudentModal by remember { mutableStateOf<StudentEntity?>(null) }
    val studentList by viewModel.students.collectAsState()
    val attendanceLogs by viewModel.attendanceLogs.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMsg by viewModel.syncMessage.collectAsState()

    // Compute metrics
    val totalStudents = studentList.size
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val todayLogs = attendanceLogs.filter { it.date == today }
    val presentCount = todayLogs.count { it.status == "PRESENT" }
    val absentCount = todayLogs.count { it.status == "ABSENT" }
    val lateCount = todayLogs.count { it.status == "LATE" }

    val attendanceRate = if (totalStudents > 0) {
        ((presentCount + lateCount).toDouble() / totalStudents * 100).toInt().coerceAtMost(100)
    } else 0

    val primaryColor = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        com.example.ui.theme.GoldGingerStart // modern soft violet
    } else {
        com.example.ui.theme.GoldGingerStart // conservative soft amber
    }

    val darkBg = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        com.example.ui.theme.ChocolateBrown.copy(alpha=0.9f) // modern classic dark indigo background depth
    } else {
        com.example.ui.theme.ChocolateBrown // traditional Torah academy warm background
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Generous padding
            verticalArrangement = Arrangement.spacedBy(24.dp) // Generous gaps
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(28.dp), spotColor = Color(0x2664748B)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha=0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Minimalist Blue Geometric Logo
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(com.example.ui.theme.CreamBeige) // Indigo-50
                                .border(
                                    width = 1.dp,
                                    color = com.example.ui.theme.MochaTaupe,
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(36.dp)) {
                                drawRoundRect(
                                    color = com.example.ui.theme.GoldGingerEnd, // Soft Indigo
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                                )
                                drawArc(
                                    color = com.example.ui.theme.ChocolateBrown,
                                    startAngle = 0f,
                                    sweepAngle = -180f,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                                    size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 18.dp.toPx()),
                                    topLeft = androidx.compose.ui.geometry.Offset(9.dp.toPx(), 12.dp.toPx())
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(com.example.ui.theme.CreamBeige)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "V2.0",
                                        color = com.example.ui.theme.GoldGingerEnd,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "ClassPro",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = com.example.ui.theme.ChocolateBrown, // Dark slate text
                                        fontSize = 24.sp
                                    ),
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "לוח הבקרה והניווט הכיתתי החכם שלך",
                                style = MaterialTheme.typography.bodyMedium.copy(color = com.example.ui.theme.MochaTaupe),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            // Quick Stats Grid Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total Students Card
                    Card(
                        modifier = Modifier.weight(1f).shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = com.example.ui.theme.GoldGingerEnd)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("תלמידים רשומים", style = MaterialTheme.typography.bodySmall.copy(color = com.example.ui.theme.MochaTaupe))
                            Text("$totalStudents", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown))
                        }
                    }

                    // Attendance Rate Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = com.example.ui.theme.PositiveGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("נוכחות היום", style = MaterialTheme.typography.bodySmall.copy(color = com.example.ui.theme.MochaTaupe))
                            Text("$attendanceRate%", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = com.example.ui.theme.PositiveGreen))
                        }
                    }
                }
            }

            // Profiles Algorithm Row
            item {
                val classProfile by viewModel.classProfileFlow.collectAsState(ClassViewModel.ClassProfile())
                val teacherProfile by viewModel.teacherProfileFlow.collectAsState(ClassViewModel.TeacherProfile())

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Class Profile Card
                    Card(
                        modifier = Modifier.weight(1f).shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
                            ) {
                                Text("פרופיל הכיתה", fontWeight = FontWeight.Bold, color = com.example.ui.theme.GoldGingerEnd, fontSize = 13.sp)
                                Icon(Icons.Default.Info, contentDescription = null, tint = com.example.ui.theme.GoldGingerEnd, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("ממוצע פעילות: ${classProfile.avgPoints} נק'", color = com.example.ui.theme.ChocolateBrown, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("שיעור נוכחות: ${classProfile.attendanceRate}%", color = com.example.ui.theme.ChocolateBrown, fontSize = 11.sp)
                            Text("איזון גובה: ${classProfile.heightBalanceStr}", color = com.example.ui.theme.MochaTaupe, fontSize = 10.sp)
                            Text("העדפה: ${classProfile.prefBalanceStr}", color = com.example.ui.theme.MochaTaupe, fontSize = 10.sp)
                        }
                    }

                    // Teacher Profile Card
                    Card(
                        modifier = Modifier.weight(1f).shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
                            ) {
                                Text("פרופיל המורה", fontWeight = FontWeight.Bold, color = com.example.ui.theme.PositiveGreen, fontSize = 13.sp)
                                Icon(Icons.Default.Star, contentDescription = null, tint = com.example.ui.theme.PositiveGreen, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("נושאים מוכנים: ${teacherProfile.materialsQuantity}", color = com.example.ui.theme.ChocolateBrown, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("רישומי יומן כיתה: ${teacherProfile.totalAttendanceMarks}", color = com.example.ui.theme.ChocolateBrown, fontSize = 11.sp)
                            Text("שיעורים מתוכננים: ${teacherProfile.lessonsPrepped}", color = com.example.ui.theme.MochaTaupe, fontSize = 10.sp)
                            Text(teacherProfile.syncStateDesc, color = com.example.ui.theme.PositiveGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Attendance counters detail
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "מצב נוכחות עדכני",
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.ChocolateBrown,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatPill("נוכחים", "$presentCount", com.example.ui.theme.PositiveGreen)
                            StatPill("מאחרים", "$lateCount", com.example.ui.theme.GoldGingerStart)
                            StatPill("נעדרים", "$absentCount", Color(0xFFC0392B))
                        }
                    }
                }
            }

            // Sync Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { com.example.ui.SoundManager.playClick();  viewModel.forceSyncNow() },
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.GoldGingerEnd),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("force_sync_button")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = com.example.ui.theme.ChocolateBrown, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = com.example.ui.theme.ChocolateBrown)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("סנכרן כעת", color = com.example.ui.theme.ChocolateBrown)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "סנכרון ענן (Firestore Backup)",
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.ChocolateBrown,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    "מנוע סנכרון היברידי ברקע",
                                    color = com.example.ui.theme.MochaTaupe,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (syncMsg.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                syncMsg,
                                color = com.example.ui.theme.MochaTaupe,
                                fontSize = 13.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Recent activity Feed/ticker title
            item {
                Text(
                    text = "רישומי נוכחות אחרונים",
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.ChocolateBrown,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            // Last activity logs
            if (studentList.isEmpty()) {
                item {
                    Text(
                        "אין עדיין פעילויות רשומות.",
                        color = com.example.ui.theme.MochaTaupe,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            } else {
                items(studentList.take(5)) { student ->
                    val log = attendanceLogs.find { it.studentId == student.id }
                    val statusText = when (log?.status) {
                        "PRESENT" -> "נוכח/ת"
                        "ABSENT" -> "נעדר/ת"
                        "LATE" -> "איחר/ה"
                        else -> "טרם עודכן"
                    }
                    val statusColor = when (log?.status) {
                        "PRESENT" -> com.example.ui.theme.PositiveGreen
                        "ABSENT" -> Color(0xFFC0392B)
                        "LATE" -> com.example.ui.theme.GoldGingerStart
                        else -> Color.Gray
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A64748B)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                statusText,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        student.name,
                                        color = com.example.ui.theme.ChocolateBrown,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { com.example.ui.SoundManager.playClick();  showStudentModal = student }, modifier = Modifier.size(24.dp)) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "Profile",
                                            tint = com.example.ui.theme.GoldGingerEnd,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
    // 4. PROGRESS REPORT DIALOG FROM STUDENT MODAL
    showStudentModal?.let { st ->
        val pts = viewModel.getStudentPoints(st)
        
        AlertDialog(
            onDismissRequest = { showStudentModal = null },
            confirmButton = {
                Button(
                    onClick = { com.example.ui.SoundManager.playClick();  /* Export */ },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("יצא פרופיל (PDF)", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { com.example.ui.SoundManager.playClick();  showStudentModal = null }) { Text("סגור", color = com.example.ui.theme.ChocolateBrown) }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "פרופיל אישי ודוח התקדמות",
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.ChocolateBrown,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = primaryColor)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(st.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Gamification summary
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("$pts", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PositiveGreen)
                                Icon(Icons.Default.Star, contentDescription = null, tint = com.example.ui.theme.GoldGingerStart)
                                Text("נקודות התנהגות או הישגים", color = com.example.ui.theme.MochaTaupe, fontSize = 12.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("סטטוס משימות ושיעורי בית", fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Mocked Homework List
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = com.example.ui.theme.PositiveGreen, modifier = Modifier.size(16.dp))
                                Text("דף עבודה בספר בראשית - הוגש", color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC0392B), modifier = Modifier.size(16.dp))
                                Text("מטלת סיכום משנה - חסר", color = com.example.ui.theme.MochaTaupe, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Button(
                                onClick = { com.example.ui.SoundManager.playClick();  /* Simulated WhatsApp Trigger */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF128C7E)), // WhatsApp Green
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.align(Alignment.Start).height(32.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = com.example.ui.theme.ChocolateBrown, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("שלח תזכורת להורה (WhatsApp)", color = com.example.ui.theme.ChocolateBrown, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("הערות ומשוב מורה", fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var teacherNotes by remember(st.id) { mutableStateOf(st.notes) }
                    
                    OutlinedTextField(
                        value = teacherNotes,
                        onValueChange = { newValue ->
                            teacherNotes = newValue 
                            viewModel.updateStudentNotes(st.id, newValue)
                        },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End),
                        placeholder = { Text("הזן הערות יומיומיות...", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
                    )
                }
            },
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    }
}

@Composable
fun StatPill(label: String, valStr: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent) // bg-slate-50
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valStr, style = MaterialTheme.typography.titleLarge.copy(color = color, fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = com.example.ui.theme.MochaTaupe))
    }
}
