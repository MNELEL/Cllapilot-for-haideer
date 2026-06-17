package com.example.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.ui.viewmodel.ClassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(viewModel: ClassViewModel, onNavigate: (String) -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val studentsList by viewModel.students.collectAsState()
    val wheelName by viewModel.selectedStudentWheelName.collectAsState()
    val isWheelSpinning by viewModel.isWheelSpinning.collectAsState()

    val countdownSec by viewModel.countdownSeconds.collectAsState()
    val maxTimerSec by viewModel.maxTimerSeconds.collectAsState()
    val isTimerActive by viewModel.isTimerActive.collectAsState()

    val generatedGroups by viewModel.generatedGroups.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var reportOutputText by remember { mutableStateOf("") }
    
    val classReportTheme by viewModel.classReportWeeklyTheme.collectAsState()
    val classReportTeacherSummary by viewModel.classReportTeacherSummary.collectAsState()
    val gradesList by viewModel.grades.collectAsState()
    val pdfPaperFormat by viewModel.pdfPaperFormat.collectAsState()
    val logoUriStr by viewModel.schoolLogoUri.collectAsState()
    val activeDark by com.example.ui.theme.ThemeManager.isDarkTheme.collectAsState()

    val isLightMode = viewModel.selectedTheme.collectAsState().value == "MODERN"
    val primaryColor = if (isLightMode) com.example.ui.theme.GoldGingerEnd else com.example.ui.theme.GoldGingerStart
    val appBg = if (isLightMode) com.example.ui.theme.CreamBeige else com.example.ui.theme.ChocolateBrown
    val baseTextColor = if (isLightMode) com.example.ui.theme.ChocolateBrown else Color.White

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

            // EXTRA TOOLS CARD GRID Shortcuts Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "כלים ומסכים נוספים",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Library Screen card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clickable { com.example.ui.SoundManager.playClick(); onNavigate("LIBRARY") },
                                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CreamBeige.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.List, contentDescription = "ספריית שיעורים", tint = primaryColor, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("ספרייה", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            // Parent Portal card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clickable { com.example.ui.SoundManager.playClick(); onNavigate("PORTAL") },
                                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CreamBeige.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = "פורטל הורים", tint = primaryColor, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("פורטל הורים", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            // Timer Screen card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clickable { com.example.ui.SoundManager.playClick(); onNavigate("TIMER") },
                                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CreamBeige.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "טיימר", tint = primaryColor, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("טיימר", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }

                        // Row for more tools
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Pacing module
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clickable { com.example.ui.SoundManager.playClick(); onNavigate("PACING") },
                                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CreamBeige.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = "הספקים", tint = primaryColor, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("הספקים", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            
                            // Grades Screen card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clickable { com.example.ui.SoundManager.playClick(); onNavigate("GRADES") },
                                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CreamBeige.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "ניהול ציונים", tint = primaryColor, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("ציונים", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            
                            // Gamification card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clickable { com.example.ui.SoundManager.playClick(); onNavigate("GAMIFICATION") },
                                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CreamBeige.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = "מובילים", tint = primaryColor, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("מובילים", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }


            // 0. ACCESSIBILITY & THEME
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
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
                                Text("מצב 'Modern' (ניגודיות גבוהה)", color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp)
                                Icon(Icons.Default.Settings, contentDescription = null, tint = com.example.ui.theme.MochaTaupe)
                            }
                        }
                    }
                }
            }

            // Customizable Pin-Code Passcode Lock Settings Card
            item {
                val pinEnabled by viewModel.pinEnabled.collectAsState()
                val currentPin by viewModel.appPinCode.collectAsState()
                var newPinText by remember(currentPin) { mutableStateOf(currentPin) }
                var pinSavedFeedback by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1264748B)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "אבטחת לוח בקרה וקוד כניסה",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "שלוט באבטחת המערכת ומנע גישה מקרית של תלמידים לחלקי הניהול.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch to disable or enable PIN lock completely
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = pinEnabled,
                                onCheckedChange = { enabled ->
                                    com.example.ui.SoundManager.playClick()
                                    viewModel.updatePinEnabled(enabled)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = primaryColor, checkedTrackColor = primaryColor.copy(alpha = 0.5f))
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("דרוש קוד סודי בכניסה לאפליקציה", color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp)
                                Icon(Icons.Default.Lock, contentDescription = "נעילת אפליקציה", tint = com.example.ui.theme.MochaTaupe)
                            }
                        }

                        if (pinEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("החלף קוד כניסה (4 ספרות בלבד):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = com.example.ui.theme.ChocolateBrown)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (newPinText.length == 4 && newPinText.all { it.isDigit() }) {
                                            com.example.ui.SoundManager.playClick()
                                            viewModel.updatePinCode(newPinText)
                                            pinSavedFeedback = true
                                        }
                                    },
                                    enabled = newPinText.length == 4 && newPinText.all { it.isDigit() } && newPinText != currentPin,
                                    modifier = Modifier.height(38.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                ) {
                                    Text("שמור קוד חדש", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedTextField(
                                    value = newPinText,
                                    onValueChange = { val clean = it.filter { c -> c.isDigit() }; if (clean.length <= 4) newPinText = clean },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        cursorColor = primaryColor,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                            }
                            
                            LaunchedEffect(pinSavedFeedback) {
                                if (pinSavedFeedback) {
                                    kotlinx.coroutines.delay(2000)
                                    pinSavedFeedback = false
                                }
                            }
                            
                            androidx.compose.animation.AnimatedVisibility(visible = pinSavedFeedback) {
                                Text(
                                    text = "הקוד עודכן בהצלחה! הקוד החדש: $currentPin",
                                    color = com.example.ui.theme.PositiveGreen,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            // Institutional Branding & PDF format settings card
            item {
                val pdfFormat by viewModel.pdfPaperFormat.collectAsState()
                val logoUriStr by viewModel.schoolLogoUri.collectAsState()
                
                val brandingLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    if (uri != null) {
                        viewModel.setSchoolLogoUri(uri.toString())
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1264748B)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "מיתוג מוסד והגדרות ייצוא",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "קבע את ממדי ייצוא ה-PDF והעלה סמל מוסד (לוגו) שיוטמע כסימן מים וכותרת עטיפה רשמית בכל מסמכי השיעור ומפות הישיבה.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // A4 vs Letter custom selector
                        Text("פורמט נייר מועדף:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = com.example.ui.theme.ChocolateBrown)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { com.example.ui.SoundManager.playClick(); viewModel.setPdfPaperFormat("Letter") },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pdfFormat == "Letter") primaryColor else com.example.ui.theme.CreamBeige,
                                    contentColor = if (pdfFormat == "Letter") Color.White else com.example.ui.theme.ChocolateBrown
                                )
                            ) {
                                Text("Letter (אופטימלי זום)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { com.example.ui.SoundManager.playClick(); viewModel.setPdfPaperFormat("A4") },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pdfFormat == "A4") primaryColor else com.example.ui.theme.CreamBeige,
                                    contentColor = if (pdfFormat == "A4") Color.White else com.example.ui.theme.ChocolateBrown
                                )
                            ) {
                                Text("A4 סנדרטי (ישראל)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // School Logo Picker Action
                        Text("סמל מוסד רשמי:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = com.example.ui.theme.ChocolateBrown)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!logoUriStr.isNullOrEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { com.example.ui.SoundManager.playClick(); viewModel.setSchoolLogoUri(null) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B), contentColor = Color.White),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("הסר סמל", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(com.example.ui.theme.CreamBeige)
                                            .border(1.dp, primaryColor, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "לוגו פעיל", tint = primaryColor, modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(com.example.ui.theme.CreamBeige.copy(alpha=0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = "לוגו ברירת מחדל", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }

                            Button(
                                onClick = { com.example.ui.SoundManager.playClick(); brandingLauncher.launch("image/*") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("העלה לוגו בית ספר", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
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
                            color = com.example.ui.theme.ChocolateBrown,
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
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = com.example.ui.theme.ChocolateBrown, modifier = Modifier.size(32.dp))
                                }
                            }
                            // Pointer
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFC0392B), modifier = Modifier.size(48.dp).align(Alignment.TopCenter).offset(y = (-8).dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        androidx.compose.animation.AnimatedVisibility(visible = !isWheelSpinning && wheelName.isNotEmpty()) {
                            Text(
                                text = "הזוכה: $wheelName 🎉",
                                color = com.example.ui.theme.PositiveGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { com.example.ui.SoundManager.playClick();  viewModel.spinWheel() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = !isWheelSpinning && studentsList.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = com.example.ui.theme.ChocolateBrown)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("סובב את הגלגל", color = com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                            color = com.example.ui.theme.ChocolateBrown,
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
                            progress > 0.7f -> com.example.ui.theme.PositiveGreen // Safe Green
                            progress > 0.2f -> com.example.ui.theme.GoldGingerStart // Warm Yellow
                            else -> Color(0xFFC0392B) // Soft Red
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
                            Button(onClick = { com.example.ui.SoundManager.playClick();  viewModel.startTimerCount(1) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.MochaTaupe), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) { Text("1 דק'") }
                            Button(onClick = { com.example.ui.SoundManager.playClick();  viewModel.startTimerCount(5) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.MochaTaupe), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) { Text("5 דק'") }
                            Button(onClick = { com.example.ui.SoundManager.playClick();  viewModel.startTimerCount(10) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.MochaTaupe), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) { Text("10 דק'") }
                            
                            if (isTimerActive) {
                                Button(onClick = { com.example.ui.SoundManager.playClick();  viewModel.stopTimerCount() }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B))) {
                                    Text("עצור", color = com.example.ui.theme.ChocolateBrown)
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
                            color = com.example.ui.theme.ChocolateBrown,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Master Sound Toggle Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val soundEnabled = com.example.ui.SoundManager.isSoundEnabled.value
                            Text(
                                text = if (soundEnabled) "הצלילים מופעלים כעת" else "הצלילים מושתקים כעת",
                                color = if (soundEnabled) com.example.ui.theme.PositiveGreen else com.example.ui.theme.MochaTaupe,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { checked ->
                                    com.example.ui.SoundManager.updateSoundEnabled(context, checked)
                                    if (checked) {
                                        com.example.ui.SoundManager.playTaskComplete()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = com.example.ui.theme.GoldGingerStart,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Audio Theme Selection
                        Text("ערכת צלילים מובנית", color = com.example.ui.theme.MochaTaupe, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        @OptIn(ExperimentalLayoutApi::class)
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            com.example.ui.SoundTheme.values().forEach { theme ->
                                val isSelected = com.example.ui.SoundManager.currentTheme.value == theme
                                androidx.compose.material3.FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        com.example.ui.SoundManager.updateTheme(context, theme)
                                        com.example.ui.SoundManager.playTaskComplete()
                                    },
                                    label = { Text(theme.displayName, fontSize = 13.sp) },
                                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = com.example.ui.theme.CreamBeige,
                                        selectedLabelColor = com.example.ui.theme.ChocolateBrown
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // EXTRA DESIGN: DYNAMIC ALIGNED CONTAINER WITH GRADIENS & ROUNDED TILES (image_0/image_1)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("theme_showcase_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.CreamBeige),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "ערכת רקעים ומכולות עקביים (UI Guidelines)",
                            fontWeight = FontWeight.Black,
                            color = com.example.ui.theme.ChocolateBrown,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "מארג רקעים של ClassAlign Studio המבוסס על מעבר צבעים אחיד ומלבני אריחים בעלי פינות מעוגלות.",
                            color = com.example.ui.theme.MochaTaupe,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Dynamic Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = activeDark,
                                onCheckedChange = { checked ->
                                    com.example.ui.SoundManager.playPop()
                                    com.example.ui.theme.ThemeManager.setDarkTheme(context, checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = com.example.ui.theme.GoldGingerStart,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "מעבר לערכת נושא כהה (Dark Theme)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.ChocolateBrown
                                )
                                Icon(
                                    imageVector = if (activeDark) Icons.Default.Star else Icons.Default.Build,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.GoldGingerEnd,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // A. ערכת נושא בהירה
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ערכת נושא בהירה (Light Mode)", fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, fontSize = 14.sp)
                            Icon(Icons.Default.Share, contentDescription = null, tint = com.example.ui.theme.GoldGingerStart, modifier = Modifier.size(16.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Screen mock container (light theme background)
                        val currentTheme by viewModel.selectedTheme.collectAsState()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(com.example.ui.theme.WhiteWarm, com.example.ui.theme.CreamBeige, Color(0xFFFFF2D9))
                                    )
                                )
                                .clickable {
                                    com.example.ui.SoundManager.playClick()
                                    viewModel.setTheme("CREAM")
                                    com.example.ui.theme.ThemeManager.setDarkTheme(context, false)
                                }
                                .border(
                                    width = if ((currentTheme == "CREAM" || currentTheme == "WARM") && !activeDark) 2.5.dp else 0.dp,
                                    color = if ((currentTheme == "CREAM" || currentTheme == "WARM") && !activeDark) com.example.ui.theme.GoldGingerStart else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Tile 1: Cream-pink style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(75.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.radialGradient(colors = listOf(com.example.ui.theme.LightPinkStart, com.example.ui.theme.LightPinkEnd)))
                                        .clickable {
                                            com.example.ui.SoundManager.playPop()
                                            viewModel.setTheme("PINK")
                                            com.example.ui.theme.ThemeManager.setDarkTheme(context, false)
                                        }
                                        .border(
                                            width = if (currentTheme == "PINK" && !activeDark) 2.5.dp else 1.dp,
                                            color = if (currentTheme == "PINK" && !activeDark) com.example.ui.theme.GoldGingerStart else Color.White.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(8.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Text("קרם-ורדרד\nעדין", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, textAlign = TextAlign.End)
                                }

                                // Tile 2: Green-blue style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(75.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.radialGradient(colors = listOf(com.example.ui.theme.LightTealStart, com.example.ui.theme.LightTealEnd)))
                                        .clickable {
                                            com.example.ui.SoundManager.playPop()
                                            viewModel.setTheme("TEAL")
                                            com.example.ui.theme.ThemeManager.setDarkTheme(context, false)
                                        }
                                        .border(
                                            width = if (currentTheme == "TEAL" && !activeDark) 2.5.dp else 1.dp,
                                            color = if (currentTheme == "TEAL" && !activeDark) com.example.ui.theme.GoldGingerStart else Color.White.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(8.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Text("ירקרק-\nתכלת עדין", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, textAlign = TextAlign.End)
                                }

                                // Tile 3: Golden style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(75.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.radialGradient(colors = listOf(com.example.ui.theme.LightGoldStart, com.example.ui.theme.LightGoldEnd)))
                                        .clickable {
                                            com.example.ui.SoundManager.playPop()
                                            viewModel.setTheme("GOLD")
                                            com.example.ui.theme.ThemeManager.setDarkTheme(context, false)
                                        }
                                        .border(
                                            width = if (currentTheme == "GOLD" && !activeDark) 2.5.dp else 1.dp,
                                            color = if (currentTheme == "GOLD" && !activeDark) com.example.ui.theme.GoldGingerStart else Color.White.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(8.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Text("זהבהב\nעדין", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, textAlign = TextAlign.End)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // B. ערכת נושא כהה
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ערכת נושא כהה (Dark Theme)", fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown, fontSize = 14.sp)
                            Icon(Icons.Default.Warning, contentDescription = null, tint = com.example.ui.theme.ChocolateBrown, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Screen mock container (dark theme background)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF1E1E22), Color(0xFF121214), Color(0xFF18151E))
                                    )
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Tile 1: Deep purple
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(75.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.radialGradient(colors = listOf(com.example.ui.theme.DarkPurpleStart, com.example.ui.theme.DarkPurpleEnd)))
                                        .clickable {
                                            com.example.ui.SoundManager.playPop()
                                            viewModel.setTheme("PURPLE")
                                            com.example.ui.theme.ThemeManager.setDarkTheme(context, true)
                                        }
                                        .border(
                                            width = if (currentTheme == "PURPLE" && activeDark) 2.5.dp else 1.dp,
                                            color = if (currentTheme == "PURPLE" && activeDark) com.example.ui.theme.GoldGingerStart else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(8.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Text("סגול\nעמוק", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.End)
                                }

                                // Tile 2: Turquoise-green deep
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(75.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.radialGradient(colors = listOf(com.example.ui.theme.DarkTealStart, com.example.ui.theme.DarkTealEnd)))
                                        .clickable {
                                            com.example.ui.SoundManager.playPop()
                                            viewModel.setTheme("DARK_TEAL")
                                            com.example.ui.theme.ThemeManager.setDarkTheme(context, true)
                                        }
                                        .border(
                                            width = if (currentTheme == "DARK_TEAL" && activeDark) 2.5.dp else 1.dp,
                                            color = if (currentTheme == "DARK_TEAL" && activeDark) com.example.ui.theme.GoldGingerStart else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(8.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Text("טורקיז-\nירוק עמוק", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.End)
                                }

                                // Tile 3: Dark blue with tiny blue loading circular icon over it
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(75.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.radialGradient(colors = listOf(com.example.ui.theme.DarkBlueStart, com.example.ui.theme.DarkBlueEnd)))
                                        .clickable {
                                            com.example.ui.SoundManager.playPop()
                                            viewModel.setTheme("DARK_BLUE")
                                            com.example.ui.theme.ThemeManager.setDarkTheme(context, true)
                                        }
                                        .border(
                                            width = if (currentTheme == "DARK_BLUE" && activeDark) 2.5.dp else 1.dp,
                                            color = if (currentTheme == "DARK_BLUE" && activeDark) com.example.ui.theme.GoldGingerStart else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFF64B5F6),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("כחול כהה", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                                    }
                                }
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
                            color = com.example.ui.theme.ChocolateBrown,
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
                            gradientColors = listOf(com.example.ui.theme.GoldGingerStart, Color(0xFFC0392B)) // Amber to Red
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { com.example.ui.SoundManager.playClick();  /* Add modern campaign */ }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.MochaTaupe), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))) {
                            Text("הוסף קמפיין חדש +", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
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

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                "מספר חברים בקבוצה:", 
                                color = com.example.ui.theme.MochaTaupe, 
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                Button(onClick = { com.example.ui.SoundManager.playClick(); viewModel.generateGroupsOfSize(4) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)) { Text("רביעיות (4)") }
                                Button(onClick = { com.example.ui.SoundManager.playClick(); viewModel.generateGroupsOfSize(3) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)) { Text("שלשות (3)") }
                                Button(onClick = { com.example.ui.SoundManager.playClick(); viewModel.generateGroupsOfSize(2) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)) { Text("זוגות (2)") }
                            }
                        }

                        if (generatedGroups.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            generatedGroups.forEachIndexed { idx, group ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.End) {
                                        Text("קבוצה ${idx + 1}", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 11.sp)
                                        Text(
                                            group.joinToString(", ") { it.name },
                                            color = com.example.ui.theme.ChocolateBrown,
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
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
                                    Text(st.name, color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp)
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "מנהל דוחות סיכום כיתתיים שבועיים (PDF)",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "מערכת מובנית המפיקה דוחות כיתתיים מקיפים המסכמים התקדמות, ציונים ממוצעים, נוכחות והתנהגות לכלל התלמידים בכיתה, כולל קומפוזר לעריכה חיה ותצוגה מקדימה מלאה של דף ה-PDF לפני ההורדה.",
                            color = com.example.ui.theme.MochaTaupe,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Button(
                            onClick = { com.example.ui.SoundManager.playClick(); 
                                showReportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.testTag("report_builder_button")
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("פתח קומפוזר ותצוגה מקדימה", color = Color.Black, fontWeight = FontWeight.Bold)
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
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
                            color = com.example.ui.theme.MochaTaupe,
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
                                Text("מחק תלמידים לצמיתות", color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
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
                                Text("איפוס והסרת מיקומי ישיבה (פינוי שולחנות)", color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
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
                                Text("מחק את יומן הנוכחות בלבד", color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                                Checkbox(
                                    checked = delAttendance,
                                    onCheckedChange = { delAttendance = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color.Red)
                                )
                            }
                        }

                        if (resetSuccessMsg.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(resetSuccessMsg, color = com.example.ui.theme.PositiveGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { com.example.ui.SoundManager.playClick(); 
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
                            Icon(Icons.Default.Delete, contentDescription = null, tint = com.example.ui.theme.ChocolateBrown)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("מחק ואפס את הרכיבים שנבחרו", color = com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Bold)
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
                        onClick = { com.example.ui.SoundManager.playClick(); showReportDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("סגור קומפוזר", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        "קומפוזר ותצוגה מקדימה ל-PDF כיתתי",
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.ChocolateBrown,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "ערוך את פרטי הדוח השבועי המקיף לכלל התלמידים. השינויים ישתקפו מיידית בתצוגה המקדימה מטה:",
                            color = com.example.ui.theme.MochaTaupe,
                            fontSize = 11.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Input 1: Weekly theme
                        OutlinedTextField(
                            value = classReportTheme,
                            onValueChange = { viewModel.setClassReportWeeklyTheme(it) },
                            label = { Text("נושא פדגוגי שבועי", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = com.example.ui.theme.MochaTaupe.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Input 2: Teacher summary
                        OutlinedTextField(
                            value = classReportTeacherSummary,
                            onValueChange = { viewModel.setClassReportTeacherSummary(it) },
                            label = { Text("משוב כללי ודגשים מהמורה", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = com.example.ui.theme.MochaTaupe.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Paper format toggle inside compiler dialog (using grid-based layout structure with min-w-0 and shrink-0 equivalent weight constraints)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1.1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { com.example.ui.SoundManager.playClick(); viewModel.setPdfPaperFormat("Letter") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (pdfPaperFormat == "Letter") primaryColor else Color.LightGray.copy(alpha = 0.4f)
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(29.dp).weight(1f)
                                ) {
                                    Text("Letter", fontSize = 11.sp, color = Color.Black, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                                Button(
                                    onClick = { com.example.ui.SoundManager.playClick(); viewModel.setPdfPaperFormat("A4") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (pdfPaperFormat == "A4") primaryColor else Color.LightGray.copy(alpha = 0.4f)
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(29.dp).weight(1f)
                                ) {
                                    Text("A4", fontSize = 11.sp, color = Color.Black, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "גודל דף הדפסה:", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = com.example.ui.theme.ChocolateBrown,
                                modifier = Modifier.weight(0.9f, fill = false),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "תצוגה מקדימה פנימית לכותרות ומבנה ה-PDF:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // HIGH FIDELITY PRINT PREVIEW
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .border(2.dp, com.example.ui.theme.GoldGingerStart, RoundedCornerShape(8.dp))
                                    .border(3.5.dp, Color.White, RoundedCornerShape(8.dp))
                                    .border(0.5.dp, com.example.ui.theme.GoldGingerStart, RoundedCornerShape(8.dp))
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                // PDF crest fallback or logo (proportional grid constraints with min-w-0 & shrink-0 behavior)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(31.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(com.example.ui.theme.CreamBeige),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = com.example.ui.theme.GoldGingerStart,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            "דוח סיכום כיתתי שבועי - ClassPro",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E1B4B),
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            textAlign = TextAlign.End
                                        )
                                        Text(
                                            "פורמט נייר: $pdfPaperFormat | נושא: $classReportTheme",
                                            fontSize = 9.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color(0xFFE5E7EB), thickness = 0.75.dp)
                                Spacer(modifier = Modifier.height(10.dp))

                                // Section 1
                                Text(
                                    "1. מדדי התנהגות כיתתית שבועיים:",
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.GoldGingerStart,
                                    fontSize = 10.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                    studentsList.sortedByDescending { viewModel.getStudentPoints(it) }.take(4).forEachIndexed { i, st ->
                                        Text(
                                            "${i + 1}. תלמיד: ${st.name}  —  צבר: ${viewModel.getStudentPoints(st)} נק'",
                                            fontSize = 9.sp,
                                            color = Color.Black,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Section 2
                                Text(
                                    "2. סיכום הישגים ממוצעים וציונים:",
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.GoldGingerStart,
                                    fontSize = 10.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                    studentsList.take(4).forEachIndexed { i, st ->
                                        val studGrades = gradesList.filter { it.studentId == st.id }
                                        val avg = studGrades.mapNotNull { it.gradeValue.toIntOrNull() }.let { if (it.isEmpty()) 0.0 else it.average() }
                                        val avgStr = if (avg > 0) "${avg.toInt()}" else "ללא דיווח"
                                        Text(
                                            "${i + 1}. תלמיד: ${st.name} | ציון ממוצע: $avgStr",
                                            fontSize = 9.sp,
                                            color = Color.Black,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                    if (studentsList.size > 4) {
                                        Text("... (ועוד ${studentsList.size - 4} תלמידים בדוח ה-PDF המלא)", fontSize = 8.sp, color = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Section 3
                                Text(
                                    "3. דגשים פדגוגיים, יעדים והערות מהמורה:",
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.GoldGingerStart,
                                    fontSize = 10.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    classReportTeacherSummary,
                                    fontSize = 9.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // EXPORT BUTTONS
                        Button(
                            onClick = { com.example.ui.SoundManager.playClick();
                                viewModel.exportClassWeeklyReportToPDF(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.fillMaxWidth().testTag("export_pdf_report_confirm_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ייצא קובץ PDF ושתף עכשיו", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
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
            Text(title, fontWeight = FontWeight.Black, color = com.example.ui.theme.ChocolateBrown, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = com.example.ui.theme.MochaTaupe, fontSize = 12.sp)

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
