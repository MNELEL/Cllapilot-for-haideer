package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.draw.shadow
import com.example.data.model.DeskEntity
import com.example.data.model.StudentEntity
import com.example.ui.viewmodel.ClassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatingMapScreen(viewModel: ClassViewModel) {
    val desks by viewModel.desks.collectAsState()
    val students by viewModel.students.collectAsState()
    val rows by viewModel.layoutRows.collectAsState()
    val cols by viewModel.layoutCols.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val selectedUnassignedStudent by viewModel.selectedUnassignedStudent.collectAsState()
    val selectedStudentHighlight by viewModel.selectedStudentForHighlight.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()

    val isSmartboardView by viewModel.isSmartboardView.collectAsState()
    var isRouletteModalOpen by remember { mutableStateOf(false) }

    // Gamification state
    val isWheelSpinning by viewModel.isWheelSpinning.collectAsState()
    val wheelName by viewModel.selectedStudentWheelName.collectAsState()

    // Advanced Map States
    var is3DMode by remember { mutableStateOf(false) }
    var dragSourceCoords by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var tooltipDesk by remember { mutableStateOf<DeskEntity?>(null) }
    
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedDesksForMulti by viewModel.selectedDesksForMulti.collectAsState()
    val seatHistoryMap by viewModel.seatHistoryMap.collectAsState()

    // Note dialog state
    var editingStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var noteText by remember { mutableStateOf("") }
    
    val unassignedStudents = students.filter { s ->
        desks.none { it.studentId == s.id }
    }

    val primaryColor = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        com.example.ui.theme.GoldGingerStart // Indigo Accent
    } else {
        com.example.ui.theme.GoldGingerStart // Amber Accent
    }

    val isLightMode = viewModel.selectedTheme.collectAsState().value == "MODERN"

    val darkBg = if (isLightMode) {
        com.example.ui.theme.CreamBeige // Slate-50 soft background
    } else {
        com.example.ui.theme.ChocolateBrown // Warm Dark
    }
    
    val baseTextColor = if (isLightMode) com.example.ui.theme.ChocolateBrown else Color.White
    val mutedTextColor = if (isLightMode) com.example.ui.theme.MochaTaupe else Color.LightGray

    val context = LocalContext.current

    // Keyboard support focused grid cells
    var focusedRow by remember { mutableStateOf<Int?>(0) }
    var focusedCol by remember { mutableStateOf<Int?>(0) }
    val focusRequester = remember { FocusRequester() }

    // Color coding performance
    var showPerformanceColors by remember { mutableStateOf(false) }

    val attendanceLogs by viewModel.attendanceLogs.collectAsState()
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

    // Grid sizing inputs
    var rowInput by remember { mutableStateOf(rows.toString()) }
    var colInput by remember { mutableStateOf(cols.toString()) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(com.example.ui.theme.CreamBeige, com.example.ui.theme.WhiteWarm))
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionUp, Key.W -> {
                            focusedRow = ((focusedRow ?: 0) - 1).coerceIn(0, rows - 1)
                            true
                        }
                        Key.DirectionDown, Key.S -> {
                            focusedRow = ((focusedRow ?: 0) + 1).coerceIn(0, rows - 1)
                            true
                        }
                        Key.DirectionLeft, Key.A -> {
                            focusedCol = ((focusedCol ?: 0) - 1).coerceIn(0, cols - 1)
                            true
                        }
                        Key.DirectionRight, Key.D -> {
                            focusedCol = ((focusedCol ?: 0) + 1).coerceIn(0, cols - 1)
                            true
                        }
                        Key.Z -> {
                            if (keyEvent.isCtrlPressed) {
                                viewModel.undoPlacement()
                                true
                            } else false
                        }
                        Key.Y -> {
                            if (keyEvent.isCtrlPressed) {
                                viewModel.redoPlacement()
                                true
                            } else false
                        }
                        Key.L -> {
                            focusedRow?.let { r ->
                                focusedCol?.let { c ->
                                    viewModel.toggleDeskLock(r, c)
                                }
                            }
                            true
                        }
                        Key.Backspace, Key.Delete -> {
                            focusedRow?.let { r ->
                                focusedCol?.let { c ->
                                    val cell = desks.find { it.row == r && it.col == c }
                                    cell?.studentId?.let { id ->
                                        viewModel.removeStudentFromLayout(id)
                                    }
                                }
                            }
                            true
                        }
                        Key.Enter, Key.Spacebar -> {
                            focusedRow?.let { r ->
                                focusedCol?.let { c ->
                                    val desk = desks.find { it.row == r && it.col == c }
                                    if (desk != null) {
                                        if (selectedMode == "STRUCTURE") {
                                            viewModel.toggleCellType(r, c)
                                        } else {
                                            val assignedStudent = students.find { it.id == desk.studentId }
                                            if (assignedStudent != null) {
                                                if (viewModel.selectedStudentForHighlight.value == assignedStudent) {
                                                    viewModel.selectedStudentForHighlight.value = null
                                                } else {
                                                    viewModel.selectedStudentForHighlight.value = assignedStudent
                                                }
                                            } else {
                                                viewModel.placeStudentAt(r, c)
                                            }
                                        }
                                    }
                                }
                            }
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Screen Header & AI Solver Execution Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Optimizer launch bubble
                Button(
                    onClick = { com.example.ui.SoundManager.playClick();  viewModel.runIntelligentAIPlacement() },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("ai_optimize_button")
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("סידור מקומות אוטומטי", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                IconButton(onClick = { com.example.ui.SoundManager.playClick();  viewModel.isSmartboardView.value = !isSmartboardView }) {
                    Icon(if (isSmartboardView) Icons.Default.Close else Icons.Default.Menu, contentDescription = "Smartboard", tint = com.example.ui.theme.ChocolateBrown)
                }

                Text(
                    "עיצוב ויצירת מפת ישיבה כיתתית",
                    style = MaterialTheme.typography.titleMedium.copy(color = com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Bold)
                )
            }

            // Conditionally show full screen smartboard or normal control panel
            if (isSmartboardView) {
                // Full Screen Smartboard View
                Box(modifier = Modifier.fillMaxSize().padding(16.dp).background(Color.Black)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(rows) { r ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (c in 0 until cols) {
                                        val desk = desks.find { it.row == r && it.col == c }
                                        if (desk != null) {
                                            val studentAssigned = students.find { it.id == desk.studentId }
                                            
                                            Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(if (studentAssigned != null) primaryColor else Color.White.copy(alpha = 0.12f)).border(BorderStroke(1.dp, Color.White), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                                if (studentAssigned != null) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(Icons.Default.Menu, contentDescription = "Drag Handle", tint = Color.Black, modifier = Modifier.size(32.dp))
                                                        Text(studentAssigned.name, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                                    }
                                                } else {
                                                    Icon(Icons.Default.Add, contentDescription = "Place Student", tint = com.example.ui.theme.ChocolateBrown.copy(alpha=0.5f), modifier = Modifier.size(48.dp))
                                                }
                                            }
                                        } else {
                                            Box(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Original View (Grid controller inputs + Active Tools + ... )
                Column {
                    // Grid controller inputs
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { com.example.ui.SoundManager.playClick(); 
                                        val rVal = rowInput.toIntOrNull() ?: rows
                                        val cVal = colInput.toIntOrNull() ?: cols
                                        viewModel.gridResize(rVal, cVal)
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("עדכן גודל")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = colInput,
                                    onValueChange = { colInput = it },
                                    label = { Text("עמודות", fontSize = 10.sp) },
                                    modifier = Modifier
                                        .width(65.dp)
                                        .height(50.dp),
                                    textStyle = TextStyle(color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = rowInput,
                                    onValueChange = { rowInput = it },
                                    label = { Text("שורות", fontSize = 10.sp) },
                                    modifier = Modifier
                                        .width(65.dp)
                                        .height(50.dp),
                                    textStyle = TextStyle(color = com.example.ui.theme.ChocolateBrown, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray)
                                )
                            }
                            Text("מימדי הכיתה (1x1 עד 20x20):", color = com.example.ui.theme.MochaTaupe, fontSize = 12.sp)
                        }
                    }

                    // Active Pedagogical Tools & Export Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color coding triggers
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("הצג הישגים פדגוגיים:", color = com.example.ui.theme.ChocolateBrown, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Switch(
                                        checked = showPerformanceColors,
                                        onCheckedChange = { showPerformanceColors = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = primaryColor,
                                            checkedTrackColor = primaryColor.copy(alpha = 0.4f)
                                        )
                                    )
                                }

                                // Export Trigger Buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { com.example.ui.SoundManager.playClick();  viewModel.exportToPDF(context) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                                        border = BorderStroke(1.dp, Color(0xFFC0392B).copy(alpha = 0.5f)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ייצא PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { com.example.ui.SoundManager.playClick();  viewModel.exportToCSV(context) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF86EFAC)),
                                        border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ייצא CSV", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            // Display Legend if colors are active
                            if (showPerformanceColors) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.8f))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("מקרא הישגים פדגוגיים:", color = com.example.ui.theme.MochaTaupe, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(com.example.ui.theme.PositiveGreen))
                                        Text("גבוה (4+ נק')", color = com.example.ui.theme.ChocolateBrown, fontSize = 8.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(com.example.ui.theme.GoldGingerStart))
                                        Text("בינוני (1-3 נק')", color = com.example.ui.theme.ChocolateBrown, fontSize = 8.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFC0392B)))
                                        Text("נדרש תמיכה (0 נק')", color = com.example.ui.theme.ChocolateBrown, fontSize = 8.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Keyboard hints bar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⌨️ שליטה במקלדת: חיצים / WASD לניווט | רווח לסימון | L לנעילה | Delete לפינוי",
                                color = com.example.ui.theme.MochaTaupe,
                                fontSize = 9.5.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(primaryColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("מקלדת פעילה", color = primaryColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Controls & Undo Redo row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Undo / Redo buttons with reactive enable state
                        Row {
                            IconButton(
                                onClick = { com.example.ui.SoundManager.playClick();  viewModel.undoPlacement() },
                                enabled = canUndo,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (canUndo) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.8f))
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "בטל",
                                    tint = if (canUndo) Color.White else Color.Gray.copy(alpha = 0.4f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { com.example.ui.SoundManager.playClick();  viewModel.redoPlacement() },
                                enabled = canRedo,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (canRedo) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.8f))
                            ) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "בצע שוב",
                                    tint = if (canRedo) Color.White else Color.Gray.copy(alpha = 0.4f)
                                )
                            }
                        }

                        // Modes Toggles
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.8f))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedMode == "PLACEMENT") primaryColor else Color.Transparent)
                                    .clickable { com.example.ui.SoundManager.playClick();  viewModel.selectedMode.value = "PLACEMENT" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "הושבת תלמידים",
                                    color = if (selectedMode == "PLACEMENT") Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedMode == "STRUCTURE") primaryColor else Color.Transparent)
                                    .clickable { com.example.ui.SoundManager.playClick();  viewModel.selectedMode.value = "STRUCTURE" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "עריכת מבנה כיתה",
                                    color = if (selectedMode == "STRUCTURE") Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedMode == "ATTENDANCE") primaryColor else Color.Transparent)
                                    .clickable { com.example.ui.SoundManager.playClick();  viewModel.selectedMode.value = "ATTENDANCE" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "נוכחות",
                                    color = if (selectedMode == "ATTENDANCE") Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Main Map display splits if PLACEMENT mode is active (to show sidebar / shelf of unassigned students!)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Main seating layout area
                        Card(
                            modifier = Modifier.weight(if (selectedMode == "PLACEMENT" && unassignedStudents.isNotEmpty()) 0.72f else 1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                // FRONT indication top label (Smartboard is at front of room)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.8f))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("חזית הכיתה / הלוח החכם", color = com.example.ui.theme.MochaTaupe, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Dynamic layout 2D desks rendering
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(rows) { r ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            for (c in 0 until cols) {
                                                val desk = desks.find { it.row == r && it.col == c }
                                                if (desk != null) {
                                                    val studentAssigned = students.find { it.id == desk.studentId }
                                                    
                                                    // Context Highlighting Outline Color Calculation
                                                    var highlightBorderColor: Color? = null
                                                    if (r == focusedRow && c == focusedCol) {
                                                        // Highly energetic glowing white border for keyboard cell cursor
                                                        highlightBorderColor = Color.Cyan
                                                    } else if (selectedStudentHighlight != null && studentAssigned != null) {
                                                        if (studentAssigned.id == selectedStudentHighlight!!.id) {
                                                            highlightBorderColor = Color.Yellow
                                                        } else if (selectedStudentHighlight!!.loves.contains(studentAssigned.id)) {
                                                            highlightBorderColor = com.example.ui.theme.PositiveGreen // Lovess count in green
                                                        } else if (selectedStudentHighlight!!.forbids.contains(studentAssigned.id) || selectedStudentHighlight!!.separate.contains(studentAssigned.id)) {
                                                            highlightBorderColor = Color(0xFFC0392B) // conflicted in red
                                                        }
                                                    }

                                                    // Performance color calculation to pass to cell
                                                    val perfColor = if (studentAssigned != null && showPerformanceColors) {
                                                        val pts = viewModel.getStudentPoints(studentAssigned)
                                                        when {
                                                            pts >= 4 -> com.example.ui.theme.PositiveGreen // High Green
                                                            pts in 1..3 -> com.example.ui.theme.GoldGingerStart // Medium Yellow
                                                            else -> Color(0xFFC0392B) // Support Needed Red
                                                        }
                                                    } else null

                                                    DeskCell(
                                                        desk = desk,
                                                        student = studentAssigned,
                                                        mode = selectedMode,
                                                        borderColor = highlightBorderColor,
                                                        performanceColor = perfColor,
                                                        attendanceStatus = attendanceLogs.find { it.studentId == studentAssigned?.id && it.date == today }?.status,
                                                        isDraggingPoint = (dragSourceCoords == Pair(r, c)),
                                                        isHighlighted = (viewModel.selectedStudentForHighlight.value == studentAssigned),
                                                        onToggleStructure = { viewModel.toggleCellType(r, c) },
                                                        onPlace = { viewModel.placeStudentAt(r, c) },
                                                        onLockToggle = { viewModel.toggleDeskLock(r, c) },
                                                        onSelectHighlight = {
                                                            if (viewModel.selectedStudentForHighlight.value == studentAssigned) {
                                                                viewModel.selectedStudentForHighlight.value = null
                                                            } else {
                                                                viewModel.selectedStudentForHighlight.value = studentAssigned
                                                            }
                                                        },
                                                        onAttendanceToggle = {
                                                            if (studentAssigned != null) {
                                                                val currentStatus = attendanceLogs.find { it.studentId == studentAssigned.id && it.date == today }?.status
                                                                val nextStatus = when(currentStatus) {
                                                                    "PRESENT" -> "ABSENT"
                                                                    "ABSENT" -> "LATE"
                                                                    else -> "PRESENT"
                                                                }
                                                                viewModel.toggleAttendance(studentAssigned.id, nextStatus)
                                                            }
                                                        },
                                                        onEditNote = {
                                                            if (studentAssigned != null) {
                                                                editingStudent = studentAssigned
                                                                noteText = studentAssigned.notes
                                                            }
                                                        },
                                                        onEvict = { studentId -> viewModel.removeStudentFromLayout(studentId) },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                } else {
                                                    Box(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sidebar/Shelf for unassigned students (RHS/LHS depending on layout)
                        if (selectedMode == "PLACEMENT" && unassignedStudents.isNotEmpty()) {
                            Card(
                                modifier = Modifier.weight(0.28f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        "תלמידים ללא מושב (${unassignedStudents.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.ChocolateBrown,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp)
                                    )

                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        items(unassignedStudents, key = { it.id }) { student ->
                                            val isSelected = selectedUnassignedStudent?.id == student.id
                                            Box(
                                                modifier = Modifier
                                                    .animateItem()
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) primaryColor else Color(0xFFE2E8F0).copy(alpha = 0.5f))
                                                    .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                                    .clickable { com.example.ui.SoundManager.playClick();  viewModel.selectUnassignedStudent(student) }
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    student.name,
                                                    color = if (isSelected) Color.Black else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.End,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Active Pedagogical Tools & Export Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color coding triggers
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("הצג הישגים פדגוגיים:", color = com.example.ui.theme.ChocolateBrown, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Switch(
                                checked = showPerformanceColors,
                                onCheckedChange = { showPerformanceColors = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = primaryColor,
                                    checkedTrackColor = primaryColor.copy(alpha = 0.4f)
                                )
                            )
                        }

                        // Export Trigger Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { com.example.ui.SoundManager.playClick();  viewModel.exportToPDF(context) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                                border = BorderStroke(1.dp, Color(0xFFC0392B).copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ייצא PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { com.example.ui.SoundManager.playClick();  viewModel.exportToCSV(context) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF86EFAC)),
                                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ייצא CSV", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Display Legend if colors are active
                    if (showPerformanceColors) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.8f))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("מקרא הישגים פדגוגיים:", color = com.example.ui.theme.MochaTaupe, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(com.example.ui.theme.PositiveGreen))
                                Text("גבוה (4+ נק')", color = com.example.ui.theme.ChocolateBrown, fontSize = 8.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(com.example.ui.theme.GoldGingerStart))
                                Text("בינוני (1-3 נק')", color = com.example.ui.theme.ChocolateBrown, fontSize = 8.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFC0392B)))
                                Text("נדרש תמיכה (0 נק')", color = com.example.ui.theme.ChocolateBrown, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }

            // Keyboard hints bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⌨️ שליטה במקלדת: חיצים / WASD לניווט | רווח לסימון | L לנעילה | Delete לפינוי",
                        color = com.example.ui.theme.MochaTaupe,
                        fontSize = 9.5.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(primaryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("מקלדת פעילה", color = primaryColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Controls & Undo Redo row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo / Redo buttons with reactive enable state
                Row {
                    IconButton(
                        onClick = { com.example.ui.SoundManager.playClick();  viewModel.undoPlacement() },
                        enabled = canUndo,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (canUndo) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "בטל",
                            tint = if (canUndo) Color.White else Color.Gray.copy(alpha = 0.4f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { com.example.ui.SoundManager.playClick();  viewModel.redoPlacement() },
                        enabled = canRedo,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (canRedo) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "בצע שוב",
                            tint = if (canRedo) Color.White else Color.Gray.copy(alpha = 0.4f)
                        )
                    }
                }

                // Contextual Advanced Actions Toolbar
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Roulette Button
                    Button(
                        onClick = { com.example.ui.SoundManager.playClick();  isRouletteModalOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("🎡 הגרלת תלמיד", color = com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // 1. Interactive 3D View Toggle (Simulated perspective)
                    IconButton(
                        onClick = { com.example.ui.SoundManager.playClick();  is3DMode = !is3DMode },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (is3DMode) primaryColor else Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "3D View Toggle", tint = if (is3DMode) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                    }

                    if (selectedMode == "STRUCTURE") {
                        IconButton(
                            onClick = { com.example.ui.SoundManager.playClick();  viewModel.injectCustomDeskRow() },
                            modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Inject Custom Desk Matrix", tint = com.example.ui.theme.ChocolateBrown, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { com.example.ui.SoundManager.playClick();  viewModel.unhideAllDesks() },
                            modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Unhide Walkways", tint = com.example.ui.theme.ChocolateBrown, modifier = Modifier.size(16.dp))
                        }
                    } else if (selectedMode == "PLACEMENT") {
                        IconButton(
                            onClick = { com.example.ui.SoundManager.playClick();  viewModel.toggleMultiSelectMode() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isMultiSelectMode) primaryColor else Color.White.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Multi-Select", tint = if (isMultiSelectMode) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                        }
                        
                        if (isMultiSelectMode && selectedDesksForMulti.isNotEmpty()) {
                            IconButton(
                                onClick = { com.example.ui.SoundManager.playClick();  viewModel.clearMultiSelectedAssignments() },
                                modifier = Modifier.clip(CircleShape).background(Color.Red.copy(alpha = 0.8f))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Assignments", tint = com.example.ui.theme.ChocolateBrown, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Modes Toggles
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedMode == "PLACEMENT") primaryColor else Color.Transparent)
                            .clickable { com.example.ui.SoundManager.playClick();  viewModel.selectedMode.value = "PLACEMENT" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "הושבת תלמידים",
                            color = if (selectedMode == "PLACEMENT") Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedMode == "STRUCTURE") primaryColor else Color.Transparent)
                            .clickable { com.example.ui.SoundManager.playClick();  viewModel.selectedMode.value = "STRUCTURE" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "עריכת מבנה כיתה",
                            color = if (selectedMode == "STRUCTURE") Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Main Map display splits if PLACEMENT mode is active (to show sidebar / shelf of unassigned students!)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main seating layout area
                Card(
                    modifier = Modifier.weight(if (selectedMode == "PLACEMENT" && unassignedStudents.isNotEmpty()) 0.72f else 1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // FRONT indication top label (Smartboard is at front of room)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.8f))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("חזית הכיתה / הלוח החכם", color = com.example.ui.theme.MochaTaupe, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dynamic layout 2D desks rendering
                        val densityFactor = androidx.compose.ui.platform.LocalDensity.current.density
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (is3DMode) {
                                        Modifier.graphicsLayer {
                                            rotationX = 45f
                                            cameraDistance = 8f * densityFactor
                                        }
                                    } else Modifier
                                )
                        ) {
                            items(rows) { r ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (c in 0 until cols) {
                                        val desk = desks.find { it.row == r && it.col == c }
                                        if (desk != null) {
                                            val studentAssigned = students.find { it.id == desk.studentId }
                                            
                                            // Context Highlighting Outline Color Calculation
                                            var highlightBorderColor: Color? = null
                                            if (r == focusedRow && c == focusedCol) {
                                                // Highly energetic glowing white border for keyboard cell cursor
                                                highlightBorderColor = Color.Cyan
                                            } else if (isMultiSelectMode && selectedDesksForMulti.contains(Pair(r, c))) {
                                                highlightBorderColor = Color.Magenta
                                            } else if (dragSourceCoords == Pair(r, c)) {
                                                highlightBorderColor = primaryColor
                                            } else if (selectedStudentHighlight != null && studentAssigned != null) {
                                                if (studentAssigned.id == selectedStudentHighlight!!.id) {
                                                    highlightBorderColor = Color.Yellow
                                                } else if (selectedStudentHighlight!!.loves.contains(studentAssigned.id)) {
                                                    highlightBorderColor = com.example.ui.theme.PositiveGreen // Lovess count in green
                                                } else if (selectedStudentHighlight!!.forbids.contains(studentAssigned.id) || selectedStudentHighlight!!.separate.contains(studentAssigned.id)) {
                                                    highlightBorderColor = Color(0xFFC0392B) // conflicted in red
                                                }
                                            }

                                            // Performance color calculation to pass to cell
                                            val perfColor = if (studentAssigned != null && showPerformanceColors) {
                                                val pts = viewModel.getStudentPoints(studentAssigned)
                                                when {
                                                    pts >= 4 -> com.example.ui.theme.PositiveGreen // High Green
                                                    pts in 1..3 -> com.example.ui.theme.GoldGingerStart // Medium Yellow
                                                    else -> Color(0xFFC0392B) // Support Needed Red
                                                }
                                            } else null

                                            DeskCell(
                                                desk = desk,
                                                student = studentAssigned,
                                                mode = selectedMode,
                                                borderColor = highlightBorderColor,
                                                performanceColor = perfColor,
                                                attendanceStatus = attendanceLogs.find { it.studentId == studentAssigned?.id && it.date == today }?.status,
                                                isDraggingPoint = (dragSourceCoords == Pair(r, c)),
                                                isHighlighted = (viewModel.selectedStudentForHighlight.value == studentAssigned),
                                                onToggleStructure = { viewModel.toggleCellType(r, c) },
                                                onPlace = { 
                                                    if (selectedMode == "PLACEMENT" && isMultiSelectMode) {
                                                        viewModel.toggleDeskMultiSelection(r, c)
                                                    } else if (selectedMode == "PLACEMENT" && selectedUnassignedStudent != null) {
                                                        viewModel.placeStudentAt(r, c)
                                                    } else {
                                                        if (dragSourceCoords == null) {
                                                            dragSourceCoords = Pair(r, c)
                                                        } else {
                                                            viewModel.swapOrMoveStudent(dragSourceCoords!!.first, dragSourceCoords!!.second, r, c)
                                                            dragSourceCoords = null
                                                        }
                                                    }
                                                },
                                                onLockToggle = { viewModel.toggleDeskLock(r, c) },
                                                onSelectHighlight = {
                                                    if (isMultiSelectMode) {
                                                        viewModel.toggleDeskMultiSelection(r, c)
                                                    } else if (dragSourceCoords != null) {
                                                        viewModel.swapOrMoveStudent(dragSourceCoords!!.first, dragSourceCoords!!.second, r, c)
                                                        dragSourceCoords = null
                                                    } else {
                                                        tooltipDesk = desk
                                                        if (viewModel.selectedStudentForHighlight.value == studentAssigned) {
                                                            viewModel.selectedStudentForHighlight.value = null
                                                        } else {
                                                            viewModel.selectedStudentForHighlight.value = studentAssigned
                                                        }
                                                    }
                                                },
                                                onAttendanceToggle = {
                                                    if (studentAssigned != null) {
                                                        val currentStatus = attendanceLogs.find { it.studentId == studentAssigned.id && it.date == today }?.status
                                                        val nextStatus = when(currentStatus) {
                                                            "PRESENT" -> "ABSENT"
                                                            "ABSENT" -> "LATE"
                                                            else -> "PRESENT"
                                                        }
                                                        viewModel.toggleAttendance(studentAssigned.id, nextStatus)
                                                    }
                                                },
                                                onEditNote = {
                                                    if (studentAssigned != null) {
                                                        editingStudent = studentAssigned
                                                        noteText = studentAssigned.notes
                                                    }
                                                },
                                                onEvict = { studentId -> viewModel.removeStudentFromLayout(studentId) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Box(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sidebar/Shelf for unassigned students (RHS/LHS depending on layout)
                if (selectedMode == "PLACEMENT" && unassignedStudents.isNotEmpty()) {
                    Card(
                        modifier = Modifier.weight(0.28f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            Text(
                                "תלמידים ללא מושב (${unassignedStudents.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.ChocolateBrown,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            )

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(unassignedStudents, key = { it.id }) { student ->
                                    val isSelected = selectedUnassignedStudent?.id == student.id
                                    Box(
                                        modifier = Modifier
                                            .animateItem()
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) primaryColor else Color(0xFFE2E8F0).copy(alpha = 0.5f))
                                            .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { com.example.ui.SoundManager.playClick();  viewModel.selectUnassignedStudent(student) }
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            student.name,
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Seat History & Constraint Tooltip Dialog
    if (tooltipDesk != null) {
        val desk = tooltipDesk!!
        val historyList = seatHistoryMap[Pair(desk.row, desk.col)] ?: emptyList()
        val assignedStud = students.find { it.id == desk.studentId }
        
        AlertDialog(
            onDismissRequest = { tooltipDesk = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = com.example.ui.theme.GoldGingerStart)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("מידע והיסטוריית מושב", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("מיקום: שורה ${desk.row + 1}, עמודה ${desk.col + 1}", fontWeight = FontWeight.Bold, color = com.example.ui.theme.ChocolateBrown)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("היסטוריית יושבים:", color = com.example.ui.theme.MochaTaupe, fontSize = 12.sp)
                    if (historyList.isEmpty()) {
                        Text("אין היסטוריה קודמת למושב זה.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        historyList.forEachIndexed { idx, name ->
                            Text("${idx + 1}. $name", fontSize = 12.sp, color = com.example.ui.theme.ChocolateBrown)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("ניתוח חכם (AI Constraints):", color = com.example.ui.theme.GoldGingerStart, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (assignedStud != null) {
                        Text(
                            "מערכת האופטימיזציה הבטיחה שתלמיד זה יושב ללא תלמידים סותרים מסביבו, וזאת בהתאם לרדיוס של 4 עמדות (למעלה, למטה, ימין ושמאל). אין חסימות גובה מזוהות בתצורה זו.",
                            fontSize = 11.sp,
                            color = com.example.ui.theme.ChocolateBrown,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text("המושב כרגע פנוי. הצב תלמיד כאן כדי לאמת אילוצים פדגוגיים.", fontSize = 11.sp, color = com.example.ui.theme.MochaTaupe)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { com.example.ui.SoundManager.playClick();  tooltipDesk = null }) {
                    Text("סגור", color = com.example.ui.theme.GoldGingerStart)
                }
            },
            containerColor = Color(0xFF1E1E2E)
        )
    }

    // Notes Dialog
    if (editingStudent != null) {
        AlertDialog(
            onDismissRequest = { editingStudent = null },
            title = { Text("עריכת הערות עבור ${editingStudent?.name}") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("הערות") }
                )
            },
            confirmButton = {
                TextButton(onClick = { com.example.ui.SoundManager.playClick(); 
                    editingStudent?.let {
                        viewModel.updateStudentNotes(it.id, noteText)
                    }
                    editingStudent = null
                }) {
                    Text("שמור")
                }
            },
            dismissButton = {
                TextButton(onClick = { com.example.ui.SoundManager.playClick();  editingStudent = null }) {
                    Text("ביטול")
                }
            }
        )
    }

    if (isRouletteModalOpen) {
        AlertDialog(
            onDismissRequest = { if (!isWheelSpinning) isRouletteModalOpen = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "🎉 הגרלת תלמיד ברולטה",
                    fontWeight = FontWeight.Black,
                    color = com.example.ui.theme.ChocolateBrown,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("מי יעלה הבא בתור ללוח?", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))

                    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
                    val spinAngle by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isWheelSpinning) 1800f else 0f,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 4000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        label = "spin"
                    )

                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (students.isEmpty()) {
                            Text("אין תלמידים להגרלה", color = Color(0xFF94A3B8))
                        } else {
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp).graphicsLayer { rotationZ = spinAngle }) {
                                val sweep = 360f / students.size
                                val colorsList = listOf(Color(0xFFFFADAD), Color(0xFFFFD6A5), Color(0xFFFDFFB6), Color(0xFFCAFFBF), Color(0xFF9BF6FF), Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFC6FF))
                                students.forEachIndexed { index, student ->
                                    drawArc(
                                        color = colorsList[index % colorsList.size],
                                        startAngle = index * sweep,
                                        sweepAngle = sweep,
                                        useCenter = true
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier.size(50.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.size(16.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF94A3B8)))
                            }
                        }
                        
                        // Pointer
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFC0392B), modifier = Modifier.size(48.dp).align(Alignment.TopCenter).offset(y = (-8).dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    androidx.compose.animation.AnimatedVisibility(visible = !isWheelSpinning && wheelName.isNotEmpty()) {
                        Box(modifier = Modifier.background(com.example.ui.theme.CreamBeige, RoundedCornerShape(16.dp)).padding(horizontal = 24.dp, vertical = 12.dp).border(1.dp, Color(0xFFE0E7FF), RoundedCornerShape(16.dp))) {
                            Text(
                                text = "⭐ $wheelName ⭐",
                                color = Color(0xFF4F46E5),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { com.example.ui.SoundManager.playClick();  viewModel.spinWheel() },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = !isWheelSpinning && students.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (isWheelSpinning) "מסתובב..." else "סובב את הגלגל! 🎡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            dismissButton = {
                if (!isWheelSpinning) {
                    TextButton(onClick = { com.example.ui.SoundManager.playClick();  isRouletteModalOpen = false }) {
                        Text("סגור", color = Color.Gray)
                    }
                }
            }
        )
    }

    if (isSyncing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) {}, // Intercept clicks
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = syncMessage,
                        color = com.example.ui.theme.ChocolateBrown,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DeskCell(
    desk: DeskEntity,
    student: StudentEntity?,
    mode: String,
    borderColor: Color?,
    performanceColor: Color? = null,
    attendanceStatus: String?,
    isDraggingPoint: Boolean = false,
    isHighlighted: Boolean = false,
    onToggleStructure: () -> Unit,
    onPlace: () -> Unit,
    onLockToggle: () -> Unit,
    onSelectHighlight: () -> Unit,
    onAttendanceToggle: () -> Unit,
    onEditNote: () -> Unit,
    onEvict: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Engaging UI: Crisp White, Slate Backgrounds
    val cellColor = when (desk.type) {
        "WALKWAY" -> Color.Transparent
        "BLOCK" -> Color(0xFFE2E8F0) // Slate-200 obstruction
        else -> if (student != null) {
            Color.White // Crisp white
        } else {
            Color(0xFFCBD5E1).copy(alpha = 0.5f) // Slate-300 empty space
        }
    }

    // Engaging UI: Smooth Micro-Interactions
    val rotation by animateFloatAsState(targetValue = if (isDraggingPoint) -2f else 0f, label = "rot")
    val scale by animateFloatAsState(targetValue = if (isDraggingPoint || isHighlighted) 1.05f else 1f, label = "sca")
    val shadow by animateDpAsState(targetValue = if (isDraggingPoint || isHighlighted) 8.dp else 1.dp, label = "shad")

    val borderStroke = if (borderColor != null) {
        BorderStroke(2.dp, borderColor) // Active highlighting outline
    } else if (desk.type == "DESK" && desk.isLocked) {
        BorderStroke(1.5.dp, com.example.ui.theme.GoldGingerStart) // Amber lock outline
    } else if (desk.type == "DESK" && student != null) {
        BorderStroke(0.dp, Color.Transparent) // Use shadow instead
    } else {
        BorderStroke(1.dp, Color(0xFF94A3B8).copy(alpha = 0.4f)) // Slate-400 empty borders
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            }
            .shadow(shadow, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(cellColor)
            .border(borderStroke, RoundedCornerShape(12.dp))
            .clickable { com.example.ui.SoundManager.playClick(); 
                when (mode) {
                    "ATTENDANCE" -> onAttendanceToggle()
                    "STRUCTURE" -> onToggleStructure()
                    "PLACEMENT" -> {
                        if (student != null) onSelectHighlight() else onPlace()
                    }
                    else -> onSelectHighlight()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (desk.type == "DESK") {
            if (student != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Smart Hover Controls: Only reveal management actions if highlighted
                    Row(
                        modifier = Modifier.fillMaxWidth().height(14.dp), // keep height fixed to avoid layout jumping
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(visible = isHighlighted || desk.isLocked || !student.notes.isNullOrBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                if (desk.isLocked || isHighlighted) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "נעול",
                                        tint = if (desk.isLocked) com.example.ui.theme.GoldGingerStart else Color(0xFFCBD5E1),
                                        modifier = Modifier.size(10.dp).clickable { com.example.ui.SoundManager.playClick();  onLockToggle() }
                                    )
                                }
                                if (!student.notes.isNullOrBlank() || isHighlighted) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "הערה",
                                        tint = if (!student.notes.isNullOrBlank()) com.example.ui.theme.GoldGingerEnd else Color(0xFFCBD5E1),
                                        modifier = Modifier.size(10.dp).clickable { com.example.ui.SoundManager.playClick();  onEditNote() }
                                    )
                                }
                            }
                        }

                        // Status indicator or interactive evict icon
                        val statusColor = when (attendanceStatus) {
                            "PRESENT" -> com.example.ui.theme.PositiveGreen
                            "ABSENT" -> Color(0xFFC0392B)
                            "LATE" -> com.example.ui.theme.GoldGingerStart
                            else -> Color.Transparent
                        }
                        if (statusColor != Color.Transparent) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(statusColor)
                            )
                        } else {
                            androidx.compose.animation.AnimatedVisibility(visible = isHighlighted) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "הסר",
                                    tint = Color(0xFF94A3B8), // slate-400
                                    modifier = Modifier.size(12.dp).clickable { com.example.ui.SoundManager.playClick();  onEvict(student.id) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Big, clear, bold student name (Crisp Dark text on Light Card)
                    Text(
                        text = student.name,
                        color = com.example.ui.theme.ChocolateBrown, // slate-800
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Modern bottom strip indicator (acts as visual floor color indicator)
                    val stripColor = performanceColor ?: Color(0xFF3B82F6)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(stripColor)
                    )
                }
            } else {
                // Highly helpful empty desk layout showing grid positions (e.g. 1,1)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "פנוי",
                        tint = Color(0xFF94A3B8).copy(alpha = 0.5f), // slate-400
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "${desk.row + 1},${desk.col + 1}",
                        color = Color(0xFF94A3B8).copy(alpha = 0.5f), // slate-400
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        } else if (desk.type == "BLOCK") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFCBD5E1).copy(alpha = 0.2f)), // slate-300
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "קיר קובץ",
                    tint = Color(0xFF94A3B8).copy(alpha = 0.4f), // slate-400
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
