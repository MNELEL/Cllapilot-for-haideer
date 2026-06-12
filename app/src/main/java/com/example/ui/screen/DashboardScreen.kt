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
    val studentList by viewModel.students.collectAsState()
    val attendanceLogs by viewModel.attendanceLogs.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMsg by viewModel.syncMessage.collectAsState()

    // Compute metrics
    val totalStudents = studentList.size
    val presentCount = attendanceLogs.count { it.status == "PRESENT" }
    val absentCount = attendanceLogs.count { it.status == "ABSENT" }
    val lateCount = attendanceLogs.count { it.status == "LATE" }

    val attendanceRate = if (totalStudents > 0) {
        ((presentCount + lateCount).toDouble() / totalStudents * 100).toInt().coerceAtMost(100)
    } else 0

    val primaryColor = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        Color(0xFFA5B4FC) // modern soft violet
    } else {
        Color(0xFFFCD34D) // conservative soft amber
    }

    val darkBg = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        Color(0xFF1E1B4B) // modern classic dark indigo background depth
    } else {
        Color(0xFF2D2319) // traditional Torah academy warm background
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                .background(Color(0xFFEEF2FF)) // Indigo-50
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFC7D2FE),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(36.dp)) {
                                drawRoundRect(
                                    color = Color(0xFF6366F1), // Soft Indigo
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                                )
                                drawArc(
                                    color = Color.White,
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
                                        .background(Color(0xFFEEF2FF))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "V2.0",
                                        color = Color(0xFF6366F1),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "ClassPro",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1E293B), // Dark slate text
                                        fontSize = 24.sp
                                    ),
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "לוח הבקרה והניווט הכיתתי החכם שלך",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B)),
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
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6366F1))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("תלמידים רשומים", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
                            Text("$totalStudents", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)))
                        }
                    }

                    // Attendance Rate Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("נוכחות היום", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
                            Text("$attendanceRate%", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981)))
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                Text("פרופיל הכיתה", fontWeight = FontWeight.Bold, color = Color(0xFF6366F1), fontSize = 13.sp)
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("ממוצע פעילות: ${classProfile.avgPoints} נק'", color = Color(0xFF1E293B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("שיעור נוכחות: ${classProfile.attendanceRate}%", color = Color(0xFF1E293B), fontSize = 11.sp)
                            Text("איזון גובה: ${classProfile.heightBalanceStr}", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text("העדפה: ${classProfile.prefBalanceStr}", color = Color(0xFF64748B), fontSize = 10.sp)
                        }
                    }

                    // Teacher Profile Card
                    Card(
                        modifier = Modifier.weight(1f).shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                Text("פרופיל המורה", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 13.sp)
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("נושאים מוכנים: ${teacherProfile.materialsQuantity}", color = Color(0xFF1E293B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("רישומי יומן כיתה: ${teacherProfile.totalAttendanceMarks}", color = Color(0xFF1E293B), fontSize = 11.sp)
                            Text("שיעורים מתוכננים: ${teacherProfile.lessonsPrepped}", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text(teacherProfile.syncStateDesc, color = Color(0xFF10B981), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Attendance counters detail
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "מצב נוכחות עדכני",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatPill("נוכחים", "$presentCount", Color(0xFF10B981))
                            StatPill("מאחרים", "$lateCount", Color(0xFFF59E0B))
                            StatPill("נעדרים", "$absentCount", Color(0xFFEF4444))
                        }
                    }
                }
            }

            // Sync Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F64748B)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                onClick = { viewModel.forceSyncNow() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("force_sync_button")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("סנכרן כעת", color = Color.White)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "סנכרון ענן (Firestore Backup)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    "מנוע סנכרון היברידי ברקע",
                                    color = Color(0xFF64748B),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (syncMsg.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                syncMsg,
                                color = Color(0xFF64748B),
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
                    color = Color(0xFF1E293B),
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
                        color = Color(0xFF64748B),
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
                        "PRESENT" -> Color(0xFF10B981)
                        "ABSENT" -> Color(0xFFEF4444)
                        "LATE" -> Color(0xFFF59E0B)
                        else -> Color.Gray
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A64748B)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1),
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

@Composable
fun StatPill(label: String, valStr: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC)) // bg-slate-50
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valStr, style = MaterialTheme.typography.titleLarge.copy(color = color, fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
    }
}
