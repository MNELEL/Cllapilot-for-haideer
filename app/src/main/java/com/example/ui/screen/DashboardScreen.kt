package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StudentEntity
import com.example.ui.viewmodel.ClassViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: ClassViewModel, onNavigate: (String) -> Unit = {}) {
    var showStudentModal by remember { mutableStateOf<StudentEntity?>(null) }
    val studentList by viewModel.students.collectAsState()
    val attendanceLogs by viewModel.attendanceLogs.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMsg by viewModel.syncMessage.collectAsState()
    val activeMaterial by viewModel.activeMaterial.collectAsState()

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

    val lastSevenDays = remember(attendanceLogs) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val displayFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale.US)
        (0..6).map { offset ->
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -offset)
            val dateStr = sdf.format(cal.time)
            val shortLabel = displayFormat.format(cal.time)
            
            val logsForDay = attendanceLogs.filter { it.date == dateStr }
            val present = logsForDay.count { it.status == "PRESENT" }
            val late = logsForDay.count { it.status == "LATE" }
            val absent = logsForDay.count { it.status == "ABSENT" }
            
            AttendanceDayData(
                dateLabel = shortLabel,
                present = present,
                late = late,
                absent = absent,
                total = present + late + absent
            )
        }.reversed()
    }

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

            activeMaterial?.let { mat ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B))
                            .clickable {
                                com.example.ui.SoundManager.playClick()
                                onNavigate("LIBRARY")
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "פעיל",
                                        tint = com.example.ui.theme.PositiveGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "שיעור פעיל כעת",
                                        color = com.example.ui.theme.PositiveGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "נושא השיעור הפעיל",
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.ChocolateBrown,
                                        fontSize = 14.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = mat.title,
                                color = com.example.ui.theme.ChocolateBrown,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = mat.summaryNotes.take(160) + if (mat.summaryNotes.length > 160) "..." else "",
                                color = com.example.ui.theme.MochaTaupe,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
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
                        modifier = Modifier
                            .weight(1f)
                            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B))
                            .clickable {
                                com.example.ui.SoundManager.playClick()
                                onNavigate("STUDENTS")
                            },
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
                        modifier = Modifier
                            .weight(1f)
                            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1F64748B))
                            .clickable {
                                com.example.ui.SoundManager.playClick()
                                onNavigate("ATTENDANCE")
                            },
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

            // Chuck Norris Classroom Humor & Discipline Card
            item {
                val jokeText by viewModel.chuckNorrisJoke.collectAsState()
                val isLoading by viewModel.chuckNorrisLoading.collectAsState()
                val hasError by viewModel.chuckNorrisError.collectAsState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B))
                        .testTag("chuck_norris_joke_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CreamBeige),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.MochaTaupe.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left side - Refresh button (minimum 48dp touch target)
                            IconButton(
                                onClick = {
                                    com.example.ui.SoundManager.playClick()
                                    viewModel.fetchChuckNorrisJoke()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("refresh_joke_button"),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = com.example.ui.theme.GoldGingerEnd,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "בדיחה חדשה",
                                        tint = com.example.ui.theme.GoldGingerEnd,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Right side - Title and Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "קורטוב משמעת והומור כיתתי",
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.ChocolateBrown,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.GoldGingerEnd,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Speech bubble for Chuck Norris
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            if (isLoading) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = com.example.ui.theme.GoldGingerEnd,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("joke_loading_indicator"),
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "צ'אק נוריס מנסח חוק משמעת חדש...",
                                        color = com.example.ui.theme.MochaTaupe,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    hasError?.let { errorMsg ->
                                        Text(
                                            text = errorMsg,
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } ?: run {
                                        Text(
                                            text = jokeText,
                                            color = com.example.ui.theme.ChocolateBrown,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = "— חוקי המשמעת של צ'אק נוריס",
                                        color = com.example.ui.theme.MochaTaupe,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
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
                        modifier = Modifier
                            .weight(1f)
                            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B))
                            .clickable {
                                com.example.ui.SoundManager.playClick()
                                onNavigate("SEATING")
                            },
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
                        modifier = Modifier
                            .weight(1f)
                            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B))
                            .clickable {
                                com.example.ui.SoundManager.playClick()
                                onNavigate("MORE")
                            },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B))
                        .clickable {
                            com.example.ui.SoundManager.playClick()
                            onNavigate("ATTENDANCE")
                        },
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

            // Weekly attendance pattern chart
            item {
                WeeklyAttendanceChart(lastSevenDays)
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

            // Real-time interactive attendance control deck
            item {
                Text(
                    text = "מעקב נוכחות בזמן אמת וניהול מהיר",
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.ChocolateBrown,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            if (studentList.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .shadow(4.dp, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(com.example.ui.theme.GoldGingerStart.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.GoldGingerEnd,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "טרם נרשמו תלמידים בכיתה",
                                color = com.example.ui.theme.ChocolateBrown,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "כדי לקבל את דוחות ה-PDF, מדדי התקדמות החומר, נוכחות בזמן אמת ומפת הושבה כיתתית אינטראקטיבית, אנא הוסף את התלמידים הראשונים שלך במסך תלמידים.",
                                color = com.example.ui.theme.MochaTaupe,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            } else {
                items(studentList) { student ->
                    val log = attendanceLogs.find { it.studentId == student.id }
                    val currentStatus = log?.status ?: "NONE"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0C64748B)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Student Name and Profile Icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Profile Launcher
                                IconButton(
                                    onClick = {
                                        com.example.ui.SoundManager.playClick()
                                        showStudentModal = student
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "צפייה בפרופיל תלמיד",
                                        tint = com.example.ui.theme.GoldGingerEnd,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Student Name & Description
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        student.name,
                                        color = com.example.ui.theme.ChocolateBrown,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.End
                                    )
                                    val pts = viewModel.getStudentPoints(student)
                                    Text(
                                        "ניקוד פנימי: $pts נק'",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Toggle Buttons for Attendance
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. PRESENT button
                                val presentSelected = currentStatus == "PRESENT"
                                Button(
                                    onClick = {
                                        com.example.ui.SoundManager.playClick()
                                        viewModel.markAttendance(student.id, "PRESENT")
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (presentSelected) com.example.ui.theme.PositiveGreen else Color.White,
                                        contentColor = if (presentSelected) Color.White else com.example.ui.theme.PositiveGreen
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PositiveGreen.copy(alpha = 0.7f)),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (presentSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text("נוכח", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // 2. LATE button
                                val lateSelected = currentStatus == "LATE"
                                Button(
                                    onClick = {
                                        com.example.ui.SoundManager.playClick()
                                        viewModel.markAttendance(student.id, "LATE")
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (lateSelected) com.example.ui.theme.GoldGingerStart else Color.White,
                                        contentColor = if (lateSelected) Color.White else com.example.ui.theme.GoldGingerEnd
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.GoldGingerStart.copy(alpha = 0.7f)),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (lateSelected) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text("איחור", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // 3. ABSENT button
                                val absentSelected = currentStatus == "ABSENT"
                                Button(
                                    onClick = {
                                        com.example.ui.SoundManager.playClick()
                                        viewModel.markAttendance(student.id, "ABSENT")
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (absentSelected) Color(0xFFC0392B) else Color.White,
                                        contentColor = if (absentSelected) Color.White else Color(0xFFC0392B)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0392B).copy(alpha = 0.7f)),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (absentSelected) {
                                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text("חיסור", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // 4. PROGRESS REPORT DIALOG FROM STUDENT MODAL (VIEW & EDIT PROFILE)
    showStudentModal?.let { st ->
        val pts = viewModel.getStudentPoints(st)
        
        // Parse out points and clean comments for initial states
        val ptsPrefix = "ניקוד: "
        val initialCleanNotes = if (st.notes.startsWith(ptsPrefix)) {
            val parts = st.notes.split(" | ", limit = 2)
            parts.getOrNull(1) ?: ""
        } else {
            st.notes
        }

        var editedName by remember(st.id) { mutableStateOf(st.name) }
        var pointsVal by remember(st.id) { mutableIntStateOf(pts) }
        var heightVal by remember(st.id) { mutableStateOf(st.height) }
        var rowPrefVal by remember(st.id) { mutableStateOf(st.rowPreference) }
        var lovesVal by remember(st.id) { mutableStateOf(st.loves.joinToString(", ")) }
        var forbidsVal by remember(st.id) { mutableStateOf(st.forbids.joinToString(", ")) }
        var separateVal by remember(st.id) { mutableStateOf(st.separate.joinToString(", ")) }
        var commentsVal by remember(st.id) { mutableStateOf(initialCleanNotes) }
        var homeworkProgress by remember(st.id) { mutableFloatStateOf(85f) }

        AlertDialog(
            onDismissRequest = { showStudentModal = null },
            confirmButton = {
                Button(
                    onClick = {
                        com.example.ui.SoundManager.playClick()
                        val lovesList = lovesVal.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val forbidsList = forbidsVal.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val separateList = separateVal.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val finalNotes = "$ptsPrefix$pointsVal | $commentsVal"
                        
                        viewModel.addOrUpdateStudent(
                            id = st.id,
                            name = editedName,
                            height = heightVal,
                            rowPreference = rowPrefVal,
                            loves = lovesList,
                            forbids = forbidsList,
                            separate = separateList,
                            notes = finalNotes
                        )
                        showStudentModal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("שמור שינויים", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        com.example.ui.SoundManager.playClick()
                        showStudentModal = null 
                    }
                ) { 
                    Text("ביטול", color = com.example.ui.theme.ChocolateBrown) 
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "עריכת פרופיל והעדפות ישיבה",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = com.example.ui.theme.ChocolateBrown,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    // Name editor
                    Text("שם התלמיד:", fontSize = 12.sp, color = com.example.ui.theme.MochaTaupe, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Behavior Points (Performance Metric 1)
                    Text("מדדי התנהגות וניקוד:", fontSize = 13.sp, color = com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { com.example.ui.SoundManager.playClick(); pointsVal += 5 },
                                        modifier = Modifier.size(36.dp).background(com.example.ui.theme.PositiveGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    ) {
                                        Text("+5", fontSize = 11.sp, color = com.example.ui.theme.PositiveGreen, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(
                                        onClick = { com.example.ui.SoundManager.playClick(); pointsVal += 1 },
                                        modifier = Modifier.size(36.dp).background(com.example.ui.theme.PositiveGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = com.example.ui.theme.PositiveGreen, modifier = Modifier.size(16.dp))
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(36.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$pointsVal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown)
                                    }

                                    IconButton(
                                        onClick = { com.example.ui.SoundManager.playClick(); pointsVal -= 1 },
                                        modifier = Modifier.size(36.dp).background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { com.example.ui.SoundManager.playClick(); pointsVal -= 5 },
                                        modifier = Modifier.size(36.dp).background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    ) {
                                        Text("-5", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("ניקוד נוכחי", fontSize = 12.sp, color = com.example.ui.theme.MochaTaupe)
                                    Icon(Icons.Default.Star, contentDescription = null, tint = com.example.ui.theme.GoldGingerStart, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Homework Completion Rate (Performance Metric 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${homeworkProgress.toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PositiveGreen)
                        Text("קצב הכנת שיעורי בית ומשימות:", fontSize = 13.sp, color = com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = homeworkProgress,
                        onValueChange = { homeworkProgress = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = Color.LightGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray.copy(alpha=0.4f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Seating Preferences Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("העדפות הושבה בכיתה", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Height Selection (גובה התלמיד)
                    Text("גובה (נחוץ למניעת הסתרה):", fontSize = 12.sp, color = com.example.ui.theme.MochaTaupe, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val heights = listOf(
                            "Tall" to "גבוה (Tall)",
                            "Medium" to "בינוני (Medium)",
                            "Low" to "נמוך (Low)"
                        )
                        heights.forEach { (key, label) ->
                            val isSelected = heightVal == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) primaryColor else Color.White)
                                    .border(1.dp, if (isSelected) primaryColor else Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable { com.example.ui.SoundManager.playClick(); heightVal = key }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else com.example.ui.theme.ChocolateBrown
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row Preference Selection (העדפת שורה)
                    Text("העדפת שורה בכיתה:", fontSize = 12.sp, color = com.example.ui.theme.MochaTaupe, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val rowPrefs = listOf(
                            "Back" to "אחורית (Back)",
                            "Middle" to "אמצעית (Middle)",
                            "Front" to "קדמית (Front)"
                        )
                        rowPrefs.forEach { (key, label) ->
                            val isSelected = rowPrefVal == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) primaryColor else Color.White)
                                    .border(1.dp, if (isSelected) primaryColor else Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable { com.example.ui.SoundManager.playClick(); rowPrefVal = key }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else com.example.ui.theme.ChocolateBrown
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Loves Seating Preference (אוהב לשבת ליד)
                    Text("אוהב לשבת ליד (שמות מופרדים בפסיק):", fontSize = 12.sp, color = com.example.ui.theme.MochaTaupe, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = lovesVal,
                        onValueChange = { lovesVal = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("יוסף, דניאל, איתן...", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Forbids Seating Preference (לא מוכן לשבת ליד)
                    Text("לא מוכן לשבת ליד (להפריד בפסיק):", fontSize = 12.sp, color = com.example.ui.theme.MochaTaupe, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = forbidsVal,
                        onValueChange = { forbidsVal = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("להפריד מיוסף, ערן...", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Separate Seating Preference (להפריד בכל מקרה מ-)
                    Text("שמור מרחק / הפרדה מוחלטת מ-:", fontSize = 12.sp, color = com.example.ui.theme.MochaTaupe, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = separateVal,
                        onValueChange = { separateVal = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ישראל...", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray.copy(alpha=0.4f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Comments or Notes
                    Text("הערות ומשוב מורה כללי:", fontSize = 12.sp, color = com.example.ui.theme.MochaTaupe, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = commentsVal,
                        onValueChange = { commentsVal = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End),
                        placeholder = { Text("הזן הערות יומיומיות, המלצות ללמידה ומשוב...", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            },
            containerColor = Color.White.copy(alpha = 0.98f)
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

data class AttendanceDayData(
    val dateLabel: String,
    val present: Int,
    val late: Int,
    val absent: Int,
    val total: Int
)

@Composable
fun WeeklyAttendanceChart(dayDataList: List<AttendanceDayData>) {
    val darkBg = com.example.ui.theme.ChocolateBrown
    val primaryColor = com.example.ui.theme.GoldGingerStart
    val greenColor = com.example.ui.theme.PositiveGreen
    val redColor = Color(0xFFC0392B)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B)),
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
                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem("נוכח", greenColor)
                    LegendItem("איחור", primaryColor)
                    LegendItem("חיסור", redColor)
                }
                
                Text(
                    "מגמת נוכחות שבועית (הספק 7 ימים ברצף)",
                    fontWeight = FontWeight.Bold,
                    color = darkBg,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Draw Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 4.dp)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // Grid lines
                    val gridLinesCount = 4
                    val stepY = height / gridLinesCount
                    for (i in 0..gridLinesCount) {
                        val y = i * stepY
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1f
                        )
                    }
                    
                    // Determine max count for scale
                    val maxAttendanceRecorded = dayDataList.maxOfOrNull { it.present + it.late + it.absent } ?: 0
                    val maxScale = maxOf(maxAttendanceRecorded + 1, 5).toFloat()
                    
                    val barGroupCount = dayDataList.size
                    val spacingBetweenGroups = 24.dp.toPx()
                    val totalSpacing = spacingBetweenGroups * (barGroupCount - 1)
                    val availableWidthForBars = width - totalSpacing
                    val groupWidth = availableWidthForBars / barGroupCount
                    
                    // We will draw stacked bars to represent: Present, Late, Absent
                    dayDataList.forEachIndexed { index, dayData ->
                        val groupX = index * (groupWidth + spacingBetweenGroups)
                        val centerX = groupX + groupWidth / 2f
                        
                        // Stacked portion calculations
                        val total = dayData.total.toFloat()
                        if (total > 0) {
                            val presentHeightPx = (dayData.present.toFloat() / maxScale) * (height - 30.dp.toPx())
                            val lateHeightPx = (dayData.late.toFloat() / maxScale) * (height - 30.dp.toPx())
                            val absentHeightPx = (dayData.absent.toFloat() / maxScale) * (height - 30.dp.toPx())
                            
                            val barWidth = (groupWidth * 0.7f).coerceIn(12.dp.toPx(), 40.dp.toPx())
                            val barLeft = centerX - barWidth / 2f
                            
                            var currentBottomY = height - 20.dp.toPx()
                            
                            // Absent chunk
                            if (dayData.absent > 0) {
                                drawRect(
                                    color = redColor.copy(alpha = 0.85f),
                                    topLeft = androidx.compose.ui.geometry.Offset(barLeft, currentBottomY - absentHeightPx),
                                    size = androidx.compose.ui.geometry.Size(barWidth, absentHeightPx)
                                )
                                currentBottomY -= absentHeightPx
                            }
                            
                            // Late chunk
                            if (dayData.late > 0) {
                                drawRect(
                                    color = primaryColor.copy(alpha = 0.85f),
                                    topLeft = androidx.compose.ui.geometry.Offset(barLeft, currentBottomY - lateHeightPx),
                                    size = androidx.compose.ui.geometry.Size(barWidth, lateHeightPx)
                                )
                                currentBottomY -= lateHeightPx
                            }
                            
                            // Present chunk
                            if (dayData.present > 0) {
                                drawRect(
                                    color = greenColor.copy(alpha = 0.85f),
                                    topLeft = androidx.compose.ui.geometry.Offset(barLeft, currentBottomY - presentHeightPx),
                                    size = androidx.compose.ui.geometry.Size(barWidth, presentHeightPx)
                                )
                            }
                        } else {
                            // Empty day placeholder
                            val barWidth = (groupWidth * 0.7f).coerceIn(12.dp.toPx(), 40.dp.toPx())
                            val barLeft = centerX - barWidth / 2f
                            drawRect(
                                color = Color.LightGray.copy(alpha = 0.2f),
                                topLeft = androidx.compose.ui.geometry.Offset(barLeft, 10f),
                                size = androidx.compose.ui.geometry.Size(barWidth, height - 30.dp.toPx())
                            )
                        }
                    }
                }
                
                // Overlay text labels under bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 160.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayDataList.forEach { dayData ->
                        Text(
                            text = dayData.dateLabel,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
    }
}
