package com.example.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.graphicsLayer
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
    val maxTimerSec by viewModel.maxTimerSeconds.collectAsState()
    val isTimerActive by viewModel.isTimerActive.collectAsState()

    val generatedGroups by viewModel.generatedGroups.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var reportOutputText by remember { mutableStateOf("") }

    val isLightMode = viewModel.selectedTheme.collectAsState().value == "MODERN"
    val primaryColor = if (isLightMode) Color(0xFF6366F1) else Color(0xFFFCD34D)
    val appBg = if (isLightMode) Color(0xFFF8FAFC) else Color(0xFF2D2319)
    val baseTextColor = if (isLightMode) Color(0xFF1E293B) else Color.White

    // Top points leaderlist
    val leaderList = remember(studentsList) {
        studentsList.map { it to viewModel.getStudentPoints(it) }.sortedByDescending { it.second }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Text(
                    "לוח קוקפיט פדגוגי (Teacher Dashboard)",
                    style = MaterialTheme.typography.titleLarge.copy(color = baseTextColor, fontWeight = FontWeight.Black),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }


            // 0. ACCESSIBILITY & THEME
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
                            "הגדרות תצוגה ונגישות",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = viewModel.selectedTheme.collectAsState().value == "MODERN",
                                onCheckedChange = { isModern ->
                                    viewModel.selectedTheme.value = if (isModern) "MODERN" else "WARM"
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = primaryColor, checkedTrackColor = primaryColor.copy(alpha = 0.5f))
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("מצב 'Modern' (ניגודיות גבוהה)", color = Color.White, fontSize = 12.sp)
                                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                    }
                }
            }

            // 1. RANDOM NAME SELECTOR WHEEL (Animated Canvas)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "גלגל המזל (Wheel of Names)",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        val infiniteTransition = rememberInfiniteTransition()
                        val spinAngle by animateFloatAsState(
                            targetValue = if (isWheelSpinning) 1440f else 0f,
                            animationSpec = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
                            label = "spin"
                        )

                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (studentsList.isEmpty()) {
                                Text("אין תלמידים להגרלה", color = Color(0xFF94A3B8))
                            } else {
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp).graphicsLayer { rotationZ = spinAngle }) {
                                    val sweep = 360f / studentsList.size
                                    val colorsList = listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFC084FC), Color(0xFFF472B6), Color(0xFFFACC15))
                                    studentsList.forEachIndexed { index, student ->
                                        drawArc(
                                            color = colorsList[index % colorsList.size],
                                            startAngle = index * sweep,
                                            sweepAngle = sweep,
                                            useCenter = true
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier.size(70.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.White).shadow(4.dp, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(32.dp))
                                }
                            }
                            // Pointer
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp).align(Alignment.TopCenter).offset(y = (-8).dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        androidx.compose.animation.AnimatedVisibility(visible = !isWheelSpinning && wheelName.isNotEmpty()) {
                            Text(
                                text = "הזוכה: $wheelName 🎉",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.spinWheel() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = !isWheelSpinning && studentsList.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("סובב את הגלגל", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // 2. TIMERS & CHRONOMETERS (Engaging Circular Progress)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "מדד זמן משימה",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        val displayMin = countdownSec / 60
                        val displaySec = countdownSec % 60
                        val timeStr = String.format("%02d:%02d", displayMin, displaySec)
                        
                        val rawProgress = if (maxTimerSec > 0) countdownSec.toFloat() / maxTimerSec.toFloat() else 0f
                        val progress by animateFloatAsState(targetValue = rawProgress, animationSpec = tween(durationMillis = 1000, easing = LinearEasing), label = "prog")
                        val timerColor = when {
                            progress > 0.7f -> Color(0xFF10B981) // Safe Green
                            progress > 0.2f -> Color(0xFFF59E0B) // Warm Yellow
                            else -> Color(0xFFEF4444) // Soft Red
                        }

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFFF1F5F9), // Background path
                                strokeWidth = 16.dp,
                                trackColor = Color(0xFFF1F5F9)
                            )
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxSize(),
                                color = timerColor,
                                strokeWidth = 16.dp,
                                trackColor = Color.Transparent,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            Text(
                                text = timeStr,
                                color = timerColor,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 48.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Quick inputs
                            Button(onClick = { viewModel.startTimerCount(1) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) { Text("1 דק'") }
                            Button(onClick = { viewModel.startTimerCount(5) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) { Text("5 דק'") }
                            Button(onClick = { viewModel.startTimerCount(10) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) { Text("10 דק'") }
                            
                            if (isTimerActive) {
                                Button(onClick = { viewModel.stopTimerCount() }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                                    Text("עצור", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // 3. AUDIO ENGINE & SOUND SETTINGS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "מנוע שמע וצלילים (Audio Engine)",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Audio Settings list
                        val audioOptions = listOf("הצלחה / ניצחון 🏆", "שגיאה קלה / נסה שוב", "סיום משימה ⏱️")
                        audioOptions.forEach { label ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { /* play mock sound */ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)), shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                    }
                                    Button(onClick = { /* mock upload sound */ }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                                        Icon(Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("העלה MP3", fontSize = 12.sp)
                                    }
                                }
                                Text(label, color = Color(0xFF334155), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            if (label != audioOptions.last()) {
                                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            // 4. CLASSROOM CAMPAIGNS & INCENTIVES
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "קמפיינים ותמריצים כיתתיים",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Campaign mock 1
                        GamifiedCampaignCard(
                            title = "אתגר כיתה שקטה",
                            description = "נשמור על שקט ויצירה בזמן השיעור.",
                            currentPoints = 7,
                            targetPoints = 10,
                            rewardIcon = "🤫",
                            gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)) // Violet to Blue
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Campaign mock 2
                        GamifiedCampaignCard(
                            title = "מבצע כיתה נקייה",
                            description = "נשמור על סביבה נקייה ומזמינה לכל התלמידים.",
                            currentPoints = 3,
                            targetPoints = 10,
                            rewardIcon = "🧹",
                            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFEF4444)) // Amber to Red
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { /* Add modern campaign */ }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))) {
                            Text("הוסף קמפיין חדש +", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
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
                                    להלן סיכום הישגים שבועי פדגוגק של כיתתנו:
                                    
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

            // 6. DETAILED DATA WIPER (מחיקה ואיפוס נתונים מופרד)
            item {
                var delStudents by remember { mutableStateOf(false) }
                var delDesks by remember { mutableStateOf(false) }
                var delAttendance by remember { mutableStateOf(false) }
                var resetSuccessMsg by remember { mutableStateOf("") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "מחיקה ואיפוס סלקטיבי",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "בחר אילו רכיבים ברצונך למחוק או לאפס. ניתן לבצע את האיפוס בנפרד לכל קטגוריה או ביחד לכולן.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // 3 Toggles/Checkboxes
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Students Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("מחק תלמידים לצמיתות", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                                Checkbox(
                                    checked = delStudents,
                                    onCheckedChange = { delStudents = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color.Red)
                                )
                            }

                            // Desks Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("איפוס והסרת מיקומי ישיבה (פינוי שולחנות)", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                                Checkbox(
                                    checked = delDesks,
                                    onCheckedChange = { delDesks = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color.Red)
                                )
                            }

                            // Attendance Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("מחק את יומן הנוכחות בלבד", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                                Checkbox(
                                    checked = delAttendance,
                                    onCheckedChange = { delAttendance = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color.Red)
                                )
                            }
                        }

                        if (resetSuccessMsg.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(resetSuccessMsg, color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (delStudents || delDesks || delAttendance) {
                                    viewModel.resetData(
                                        deleteStudents = delStudents,
                                        deleteDesks = delDesks,
                                        deleteAttendance = delAttendance
                                    )
                                    resetSuccessMsg = "איפוס פנימי הושלם בהצלחה!"
                                    delStudents = false
                                    delDesks = false
                                    delAttendance = false
                                } else {
                                    resetSuccessMsg = "נא לבחור קטגוריה אחת לפחות למחיקה."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            enabled = delStudents || delDesks || delAttendance,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("מחק ואפס את הרכיבים שנבחרו", color = Color.White, fontWeight = FontWeight.Bold)
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
                containerColor = appBg
            )
        }
    }
}

@Composable
fun GamifiedCampaignCard(
    title: String,
    description: String,
    currentPoints: Int,
    targetPoints: Int,
    rewardIcon: String,
    gradientColors: List<Color>
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val elevation by animateDpAsState(targetValue = if (isPressed) 2.dp else 4.dp, label = "elevation")
    val translateY by animateFloatAsState(targetValue = if (isPressed) 0f else -4f, label = "translateY")

    val rawProgress = (currentPoints.toFloat() / targetPoints.toFloat()).coerceIn(0f, 1f)
    
    var animatedProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(rawProgress) {
        animatedProgress = rawProgress
    }
    
    val fillWidth by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "fillWidth"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = translateY }
            .shadow(elevation, RoundedCornerShape(20.dp), spotColor = Color(0x3364748B))
            .clickable(interactionSource = interactionSource, indication = null) { },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Title and Description
            Text(title, fontWeight = FontWeight.Black, color = Color(0xFF1E293B), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = Color(0xFF64748B), fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFF1F5F9)) // slate 100
            ) {
                if (fillWidth > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = fillWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(gradientColors))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(rewardIcon, fontSize = 24.sp)
                Text(
                    "$currentPoints / $targetPoints נקודות",
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
