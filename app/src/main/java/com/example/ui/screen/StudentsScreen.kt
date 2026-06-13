package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.ui.viewmodel.ClassViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(viewModel: ClassViewModel) {
    val studentsList by viewModel.students.collectAsState()
    val attendanceLogs by viewModel.attendanceLogs.collectAsState()
    val todayDate = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) }
    var selectedStudentForEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var showProgressReportFor by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }
    var showIntakeDialog by remember { mutableStateOf(false) }
    var bulkInputText by remember { mutableStateOf("") }

    // State trackers for adding/editing students
    var showAddDialog by remember { mutableStateOf(false) }
    var nameField by remember { mutableStateOf("") }
    var heightField by remember { mutableStateOf("Medium") }
    var rowPrefField by remember { mutableStateOf("Middle") }
    var lovesField by remember { mutableStateOf<List<String>>(emptyList()) }
    var forbidsField by remember { mutableStateOf<List<String>>(emptyList()) }
    var separateField by remember { mutableStateOf<List<String>>(emptyList()) }
    var notesField by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var sortFilter by remember { mutableStateOf("ALPHABETICAL") }

    val context = androidx.compose.ui.platform.LocalContext.current
    var importStatusMsg by remember { mutableStateOf("") }

    val studentFileLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val text = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (text.isNotEmpty()) {
                    val ok = viewModel.importStudentsFromFileContent(text)
                    importStatusMsg = if (ok) {
                        "ייבוא הושלם! כל התלמידים החדשים נקלטו בהצלחה!"
                    } else {
                        "מבנה הקובץ אינו מזוהה, אנא השתמש בפורמט פשוט של שורות טקסט."
                    }
                }
            } catch (e: Exception) {
                importStatusMsg = "שגיאה בטעינת הקובץ."
            }
        }
    }

    val isLightMode = viewModel.selectedTheme.collectAsState().value == "MODERN"
    
    val primaryColor = if (isLightMode) {
        com.example.ui.theme.GoldGingerStart
    } else {
        com.example.ui.theme.GoldGingerStart
    }

    val darkBg = if (isLightMode) {
        com.example.ui.theme.CreamBeige // Slate-50 soft background
    } else {
        com.example.ui.theme.ChocolateBrown // Warm dark background
    }
    
    val baseTextColor = if (isLightMode) com.example.ui.theme.ChocolateBrown else Color.White

    // Prepare processed student list
    val processedStudents = remember(studentsList, searchQuery, sortFilter) {
        val filtered = studentsList.filter { it.name.contains(searchQuery, ignoreCase = true) }
        when (sortFilter) {
            "POINTS" -> filtered.sortedByDescending { viewModel.getStudentPoints(it) }
            else -> filtered.sortedBy { it.name }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(com.example.ui.theme.CreamBeige, com.example.ui.theme.WhiteWarm))
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header actions row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add and Import actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Raw physical file uploader
                    Button(
                        onClick = { com.example.ui.SoundManager.playClick();  studentFileLauncher.launch("*/*") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("bulk_file_upload_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("העלאת קובץ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { com.example.ui.SoundManager.playClick(); 
                            bulkInputText = ""
                            showIntakeDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("bulk_import_button")
                    ) {
                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("קלוט מנה", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { com.example.ui.SoundManager.playClick(); 
                            nameField = ""
                            heightField = "Medium"
                            rowPrefField = "Middle"
                            lovesField = emptyList()
                            forbidsField = emptyList()
                            separateField = emptyList()
                            notesField = ""
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_student_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("תלמיד חדש", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Text(
                    "רשימת התלמידים בכיתה",
                    style = MaterialTheme.typography.titleMedium.copy(color = com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Bold)
                )
            }

            // Search and Filter Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sorting toggle
                Button(
                    onClick = { com.example.ui.SoundManager.playClick();  sortFilter = if (sortFilter == "ALPHABETICAL") "POINTS" else "ALPHABETICAL" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (sortFilter == "ALPHABETICAL") Icons.Default.MoreVert else Icons.Default.Star,
                        contentDescription = "מיון",
                        tint = com.example.ui.theme.ChocolateBrown,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (sortFilter == "ALPHABETICAL") "מיון: א'-ת'" else "מיון: נקודות",
                        color = com.example.ui.theme.ChocolateBrown,
                        fontSize = 11.sp
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("חיפוש תלמיד...", fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = primaryColor, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(textAlign = TextAlign.End, fontSize = 12.sp, color = com.example.ui.theme.ChocolateBrown),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = primaryColor.copy(alpha = 0.5f)
                    )
                )
            }

            if (importStatusMsg.isNotEmpty()) {
                Surface(
                    color = primaryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { com.example.ui.SoundManager.playClick();  importStatusMsg = "" }) {
                            Text("הבנתי", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(importStatusMsg, color = com.example.ui.theme.ChocolateBrown, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
                    }
                }
            }

            // Scrollable roster lists
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(processedStudents) { student ->
                    val pts = viewModel.getStudentPoints(student)
                    val todayLog = attendanceLogs.find { it.studentId == student.id && it.date == todayDate }
                    val isPresent = todayLog?.status == "PRESENT"
                    val isAbsent = todayLog?.status == "ABSENT"
                    val isLate = todayLog?.status == "LATE"

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Gamified scoring controls (+1 / -1) and profile action
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { com.example.ui.SoundManager.playClick();  showProgressReportFor = student },
                                        modifier = Modifier.size(31.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF2B579A).copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "פרופיל אישי דוח התקדמות",
                                            tint = com.example.ui.theme.GoldGingerStart,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { com.example.ui.SoundManager.playClick();  viewModel.incrementScore(student.id, -1) },
                                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).background(Color.Red.copy(alpha = 0.2f))
                                    ) {
                                        Text("-1", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        "$pts נק'",
                                        color = primaryColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    IconButton(
                                        onClick = { com.example.ui.SoundManager.playClick();  viewModel.incrementScore(student.id, 1) },
                                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).background(Color.Green.copy(alpha = 0.2f))
                                    ) {
                                        Text("+1", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Clickable Info details + Edit icon to trigger the edit popup dialog
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { com.example.ui.SoundManager.playClick(); 
                                            selectedStudentForEdit = student
                                            nameField = student.name
                                            heightField = student.height
                                            rowPrefField = student.rowPreference
                                            lovesField = student.loves
                                            forbidsField = student.forbids
                                            separateField = student.separate
                                            notesField = student.notes
                                        }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "ערוך תלמיד",
                                        tint = com.example.ui.theme.MochaTaupe.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            student.name,
                                            fontWeight = FontWeight.Bold,
                                            color = com.example.ui.theme.ChocolateBrown,
                                            fontSize = 15.sp,
                                            textAlign = TextAlign.End
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "גובה: ${when(student.height) {
                                                    "Low" -> "נמוך"
                                                    "Tall" -> "גבוה"
                                                    else -> "בינוני"
                                                }}",
                                                color = com.example.ui.theme.MochaTaupe,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                "שורה מועדפת: ${when(student.rowPreference) {
                                                    "Front" -> "קדמית"
                                                    "Back" -> "אחורית"
                                                    else -> "אמצעית"
                                                }}",
                                                color = com.example.ui.theme.MochaTaupe,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Custom subtle divider
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.8f))
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Today's Attendance controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status interactive selectors (3 buttons for Present, Late, Absent)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AttendanceIndicatorButton(
                                        label = "נוכח",
                                        icon = Icons.Default.Check,
                                        isSelected = isPresent,
                                        selectedColor = com.example.ui.theme.PositiveGreen, // emerald green
                                        onClick = { com.example.ui.SoundManager.playClick();  viewModel.toggleAttendance(student.id, "PRESENT") }
                                    )

                                    AttendanceIndicatorButton(
                                        label = "איחר",
                                        icon = Icons.Default.Refresh,
                                        isSelected = isLate,
                                        selectedColor = com.example.ui.theme.GoldGingerStart, // amber
                                        onClick = { com.example.ui.SoundManager.playClick();  viewModel.toggleAttendance(student.id, "LATE") }
                                    )

                                    AttendanceIndicatorButton(
                                        label = "חיסור",
                                        icon = Icons.Default.Close,
                                        isSelected = isAbsent,
                                        selectedColor = Color(0xFFC0392B), // red
                                        onClick = { com.example.ui.SoundManager.playClick();  viewModel.toggleAttendance(student.id, "ABSENT") }
                                    )
                                }

                                Text(
                                    text = "נוכחות להיום:",
                                    color = com.example.ui.theme.MochaTaupe.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1. ADD / EDIT STUDENT MODAL DIALOG
        if (showAddDialog || selectedStudentForEdit != null) {
            val isEditMode = selectedStudentForEdit != null
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    selectedStudentForEdit = null
                },
                confirmButton = {
                    Button(
                        onClick = { com.example.ui.SoundManager.playClick(); 
                            val idVal = if (isEditMode) selectedStudentForEdit!!.id else ""
                            viewModel.addOrUpdateStudent(
                                id = idVal,
                                name = nameField,
                                height = heightField,
                                rowPreference = rowPrefField,
                                loves = lovesField,
                                forbids = forbidsField,
                                separate = separateField,
                                notes = notesField
                            )
                            showAddDialog = false
                            selectedStudentForEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text(if (isEditMode) "שמור שינויים" else "הוסף תלמיד", color = Color.Black)
                    }
                },
                dismissButton = {
                    if (isEditMode) {
                        TextButton(
                            onClick = { com.example.ui.SoundManager.playClick(); 
                                viewModel.deleteStudent(selectedStudentForEdit!!.id)
                                selectedStudentForEdit = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text("מחק תלמיד")
                        }
                    } else {
                        TextButton(onClick = { com.example.ui.SoundManager.playClick();  showAddDialog = false }) { Text("ביטול", color = com.example.ui.theme.ChocolateBrown) }
                    }
                },
                title = {
                    Text(
                        if (isEditMode) "עריכת פרטי תלמיד" else "הוספת תלמיד חדש",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = com.example.ui.theme.ChocolateBrown,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Roster fields
                        item {
                            OutlinedTextField(
                                value = nameField,
                                onValueChange = { nameField = it },
                                label = { Text("שם מלא") },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(textAlign = TextAlign.End),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, focusedLabelColor = primaryColor)
                            )
                        }

                        // Height tier selecting dropdown simulation (using chips)
                        item {
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                Text("טווח גובה:", color = com.example.ui.theme.MochaTaupe, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                    listOf("Tall" to "גבוה", "Medium" to "בינוני", "Low" to "נמוך").forEach { (v, l) ->
                                        val isChecked = heightField == v
                                        FilterChip(
                                            selected = isChecked,
                                            onClick = { com.example.ui.SoundManager.playClick();  heightField = v },
                                            label = { Text(l) },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = primaryColor,
                                                selectedLabelColor = Color.Black
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Row Preference
                        item {
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                Text("העדפת שורת ישיבה:", color = com.example.ui.theme.MochaTaupe, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                    listOf("Back" to "אחורית", "Middle" to "אמצעית", "Front" to "קדמית").forEach { (v, l) ->
                                        val isChecked = rowPrefField == v
                                        FilterChip(
                                            selected = isChecked,
                                            onClick = { com.example.ui.SoundManager.playClick();  rowPrefField = v },
                                            label = { Text(l) },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = primaryColor,
                                                selectedLabelColor = Color.Black
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Social mappings mapping (Multiselection of peer IDs)
                        item {
                            SocialListSelector(
                                label = "מתחבר היטב עם (העדפת קרבה - Green):",
                                selectedIds = lovesField,
                                students = studentsList.filter { it.id != selectedStudentForEdit?.id },
                                primaryColor = primaryColor,
                                onSelectionChanged = { lovesField = it }
                            )
                        }

                        item {
                            SocialListSelector(
                                label = "הפרד מ- (מניעת הפרעות בכיתה - Red):",
                                selectedIds = forbidsField,
                                students = studentsList.filter { it.id != selectedStudentForEdit?.id },
                                primaryColor = primaryColor,
                                onSelectionChanged = { forbidsField = it }
                            )
                        }

                        item {
                            SocialListSelector(
                                label = "הפרדה מבוקשת (למנוע הושבה סמוכה):",
                                selectedIds = separateField,
                                students = studentsList.filter { it.id != selectedStudentForEdit?.id },
                                primaryColor = primaryColor,
                                onSelectionChanged = { separateField = it }
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = notesField,
                                onValueChange = { notesField = it },
                                label = { Text("הערות פדגוגיות / מידע נוסף") },
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                textStyle = TextStyle(textAlign = TextAlign.End),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, focusedLabelColor = primaryColor)
                            )
                        }
                    }
                },
                containerColor = darkBg
            )
        }

        // 2. BULK INTAKE EXCEL/JSON PAYLOAD DIALOG
        if (showIntakeDialog) {
            AlertDialog(
                onDismissRequest = { showIntakeDialog = false },
                confirmButton = {
                    Button(
                        onClick = { com.example.ui.SoundManager.playClick(); 
                            viewModel.processBulkIntake(bulkInputText)
                            showIntakeDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("ייבא נתונים", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { com.example.ui.SoundManager.playClick();  showIntakeDialog = false }) { Text("ביטול", color = com.example.ui.theme.ChocolateBrown) }
                },
                title = {
                    Text(
                        "ייבוא מהיר של מאגר תלמידים",
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.ChocolateBrown,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "הזן נתונים בפורמט JSON או רשימת שמות מופרדת בפסיקים לקליטה מהירה:",
                            color = com.example.ui.theme.MochaTaupe,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = bulkInputText,
                            onValueChange = { bulkInputText = it },
                            placeholder = { Text("דוגמה: אהרון כהן, Medium, Front\nישראל ישראלי, Tall, Back", fontSize = 11.sp, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = com.example.ui.theme.ChocolateBrown, textAlign = TextAlign.End),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                        )
                    }
                },
                containerColor = darkBg
            )
        }

        // 3. CONFIRM DELETE DIALOG
        if (studentToDelete != null) {
            AlertDialog(
                onDismissRequest = { studentToDelete = null },
                confirmButton = {
                    Button(
                        onClick = { com.example.ui.SoundManager.playClick(); 
                            viewModel.deleteStudent(studentToDelete!!.id)
                            studentToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("מחק לצמיתות", color = com.example.ui.theme.ChocolateBrown)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { com.example.ui.SoundManager.playClick();  studentToDelete = null }) {
                        Text("ביטול", color = com.example.ui.theme.ChocolateBrown)
                    }
                },
                title = {
                    Text(
                        "מחיקת תלמיד מהרשימה",
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.ChocolateBrown,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                text = {
                    Text(
                        "האם אתה בטוח שברצונך למחוק את ${studentToDelete!!.name} ממאגר הכיתה באופן סופי?",
                        color = com.example.ui.theme.MochaTaupe,
                        fontSize = 14.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                containerColor = darkBg
            )
        }

        // 4. PROGRESS REPORT DIALOG
        if (showProgressReportFor != null) {
            val st = showProgressReportFor!!
            val pts = viewModel.getStudentPoints(st)
            
            AlertDialog(
                onDismissRequest = { showProgressReportFor = null },
                confirmButton = {
                    Button(
                        onClick = { com.example.ui.SoundManager.playClick();  viewModel.exportToPDF(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("יצא פרופיל (PDF)", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { com.example.ui.SoundManager.playClick();  showProgressReportFor = null }) { Text("סגור", color = com.example.ui.theme.ChocolateBrown) }
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
                            onValueChange = { 
                                teacherNotes = it 
                                viewModel.updateStudentNotes(st.id, it)
                            },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            textStyle = TextStyle(textAlign = TextAlign.End),
                            placeholder = { Text("הזן הערות יומיומיות...", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
                        )
                    }
                },
                containerColor = darkBg
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialListSelector(
    label: String,
    selectedIds: List<String>,
    students: List<StudentEntity>,
    primaryColor: Color,
    onSelectionChanged: (List<String>) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Text(label, color = com.example.ui.theme.MochaTaupe, fontSize = 12.sp)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            students.forEach { st ->
                val isSelected = selectedIds.contains(st.id)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) primaryColor else Color.White.copy(alpha = 0.8f))
                        .clickable { com.example.ui.SoundManager.playClick(); 
                            val newList = if (isSelected) {
                                selectedIds.filter { it != st.id }
                            } else {
                                selectedIds + st.id
                            }
                            onSelectionChanged(newList)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        st.name,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceIndicatorButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) selectedColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.8f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("attendance_${label}_btn")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) selectedColor else com.example.ui.theme.MochaTaupe.copy(alpha = 0.4f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                color = if (isSelected) selectedColor else com.example.ui.theme.MochaTaupe.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
