package com.example.ui.screen

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.ui.viewmodel.ClassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(viewModel: ClassViewModel) {
    val studentsList by viewModel.students.collectAsState()
    val wheelName by viewModel.selectedStudentWheelName.collectAsState()
    val isWheelSpinning by viewModel.isWheelSpinning.collectAsState()

    val countdownSec by viewModel.countdownSeconds.collectAsState()
    val isTimerActive by viewModel.isTimerActive.collectAsState()

    val generatedGroups by viewModel.generatedGroups.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var reportOutputText by remember { mutableStateOf("") }

    val primaryColor = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        Color(0xFFA5B4FC)
    } else {
        Color(0xFFFCD34D)
    }

    val darkBg = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        Color(0xFF1E1B4B)
    } else {
        Color(0xFF2D2319)
    }

    // Top points leaderlist
    val leaderList = remember(studentsList) {
        studentsList.map { it to viewModel.getStudentPoints(it) }.sortedByDescending { it.second }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(darkBg, darkBg.copy(alpha = 0.9f))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    "עזרי הוראה דיגיטליים ודוחות",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 1. RANDOM NAME SELECTOR WHEEL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "גלגל בחירת תלמיד אקראי",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (wheelName.isEmpty()) "לחץ על הכפתור לבחירה" else wheelName,
                                color = if (isWheelSpinning) primaryColor else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.spinWheel() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = !isWheelSpinning && studentsList.isNotEmpty(),
                            modifier = Modifier.testTag("spin_wheel_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("סובב גלגל עכשיו", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. TIMERS & CHRONOMETERS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "טיימר משימות כיתתי ספירלי",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val displayMin = countdownSec / 60
                        val displaySec = countdownSec % 60
                        val timeStr = String.format("%02d:%02d", displayMin, displaySec)

                        Text(
                            text = timeStr,
                            color = if (countdownSec < 10 && isTimerActive) Color.Red else Color.White,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 36.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Quick inputs
                            Button(onClick = { viewModel.startTimerCount(1) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("1 דק'") }
                            Button(onClick = { viewModel.startTimerCount(5) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("5 דק'") }
                            Button(onClick = { viewModel.startTimerCount(10) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("10 דק'") }
                            
                            if (isTimerActive) {
                                Button(onClick = { viewModel.stopTimerCount() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                                    Text("עצור", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // 3. TEAM GROUP BUILDER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "מחולל קבוצות למידה פדגוגי",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(onClick = { viewModel.generateGroupsOfSize(2) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)) { Text("זוגות (2)") }
                                Button(onClick = { viewModel.generateGroupsOfSize(3) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)) { Text("שלשות (3)") }
                                Button(onClick = { viewModel.generateGroupsOfSize(4) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)) { Text("קבוצות של 4") }
                            }

                            Text("מספר חברים:", color = Color.LightGray, fontSize = 12.sp)
                        }

                        if (generatedGroups.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            generatedGroups.forEachIndexed { idx, group ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.End) {
                                        Text("קבוצה ${idx + 1}", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 11.sp)
                                        Text(
                                            group.joinToString(", ") { it.name },
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. LEADERBOARD POINTS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "לוח הישגי מצטיינים כיתתי",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        leaderList.take(4).forEachIndexed { idx, (st, points) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$points נקודות", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(st.name, color = Color.White, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when(idx) {
                                            0 -> "🥇"
                                            1 -> "🥈"
                                            2 -> "🥉"
                                            else -> "⭐"
                                        },
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. REPORT BUILDER (WEEKLY DOCUMENT TRANSCRIPTS)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "מפיק דוחות הודעות שבועיים",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "בונה הודעה שבועית מותאמת להורים המפרטת את מצטייני הלמידה והישגי המשמעת השבועיים של הכיתה.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Button(
                            onClick = {
                                val topSts = leaderList.take(3).map { it.first.name }
                                val transcript = """
                                    שלום רב להורי כיתת ClassPro היקרים,
                                    להלן סיכום הישגים שבועי פדגוגי של כיתתנו:
                                    
                                    🌟 מצטייני השבוע שבלטו בלמידה ובצבירת הישגים:
                                    ${topSts.mapIndexed { i, n -> "${i + 1}. $n" }.joinToString("\n")}
                                    
                                    נמשיך לעקוב ולתמוך בהתפתחות הפדגוגית של כל תלמיד ותלמיד, בסיוע מנוע ה-AI למפת ישיבה אופטימלית וכלים דידקטיים תומכים.
                                    
                                    בברכת שבת שלום,
                                    הנהלת המוסד החינוכי
                                """.trimIndent()
                                reportOutputText = transcript
                                showReportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.testTag("report_builder_button")
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("הפק דוח שבועי", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                confirmButton = {
                    Button(
                        onClick = { showReportDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("הבנתי, סגור", color = Color.Black)
                    }
                },
                title = {
                    Text(
                        "הודעת דיווח שבועית מוכנה",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(12.dp)
                    ) {
                        Text(
                            reportOutputText,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                containerColor = darkBg
            )
        }
    }
}
