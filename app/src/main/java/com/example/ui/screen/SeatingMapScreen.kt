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

    val unassignedStudents = students.filter { s ->
        desks.none { it.studentId == s.id }
    }

    val primaryColor = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        Color(0xFFA5B4FC) // Indigo Accent
    } else {
        Color(0xFFFCD34D) // Amber Accent
    }

    val darkBg = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        Color(0xFF1E1B4B)
    } else {
        Color(0xFF2D2319)
    }

    val context = LocalContext.current

    // Keyboard support focused grid cells
    var focusedRow by remember { mutableStateOf<Int?>(0) }
    var focusedCol by remember { mutableStateOf<Int?>(0) }
    val focusRequester = remember { FocusRequester() }

    // Color coding performance
    var showPerformanceColors by remember { mutableStateOf(false) }

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
                Brush.verticalGradient(
                    listOf(darkBg, darkBg.copy(alpha = 0.9f))
                )
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
                    onClick = { viewModel.runIntelligentAIPlacement() },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("ai_optimize_button")
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("סידור מקומות חכם (AI)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Text(
                    "עיצוב ויצירת מפת ישיבה כיתתית",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }

            // Grid controller inputs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
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
                            onClick = {
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
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
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
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray)
                        )
                    }

                    Text("מימדי הכיתה (1x1 עד 20x20):", color = Color.LightGray, fontSize = 12.sp)
                }
            }

            // Active Pedagogical Tools & Export Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
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
                            Text("הצג הישגים פדגוגיים:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                                onClick = { viewModel.exportToPDF(context) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ייצא PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.exportToCSV(context) },
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
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("מקרא הישגים פדגוגיים:", color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Text("גבוה (4+ נק')", color = Color.White, fontSize = 8.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                Text("בינוני (1-3 נק')", color = Color.White, fontSize = 8.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                Text("נדרש תמיכה (0 נק')", color = Color.White, fontSize = 8.sp)
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
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
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
                        color = Color.LightGray,
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
                        onClick = { viewModel.undoPlacement() },
                        enabled = canUndo,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (canUndo) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f))
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "בטל",
                            tint = if (canUndo) Color.White else Color.Gray.copy(alpha = 0.4f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.redoPlacement() },
                        enabled = canRedo,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (canRedo) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f))
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
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedMode == "PLACEMENT") primaryColor else Color.Transparent)
                            .clickable { viewModel.selectedMode.value = "PLACEMENT" }
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
                            .clickable { viewModel.selectedMode.value = "STRUCTURE" }
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
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
                                .background(Color.White.copy(alpha = 0.06f))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("חזית הכיתה / הלוח החכם", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                                    highlightBorderColor = Color(0xFF10B981) // Lovess count in green
                                                } else if (selectedStudentHighlight!!.forbids.contains(studentAssigned.id) || selectedStudentHighlight!!.separate.contains(studentAssigned.id)) {
                                                    highlightBorderColor = Color(0xFFEF4444) // conflicted in red
                                                }
                                            }

                                            // Performance color calculation to pass to cell
                                            val perfColor = if (studentAssigned != null && showPerformanceColors) {
                                                val pts = viewModel.getStudentPoints(studentAssigned)
                                                when {
                                                    pts >= 4 -> Color(0xFF10B981) // High Green
                                                    pts in 1..3 -> Color(0xFFF59E0B) // Medium Yellow
                                                    else -> Color(0xFFEF4444) // Support Needed Red
                                                }
                                            } else null

                                            DeskCell(
                                                desk = desk,
                                                student = studentAssigned,
                                                mode = selectedMode,
                                                borderColor = highlightBorderColor,
                                                performanceColor = perfColor,
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
                                                onEvict = { studentId -> viewModel.removeStudentFromLayout(studentId) },
                                                modifier = Modifier.weight(1f)
                                                    .clickable {
                                                        focusedRow = r
                                                        focusedCol = c
                                                        if (selectedMode == "STRUCTURE") {
                                                            viewModel.toggleCellType(r, c)
                                                        } else {
                                                            if (studentAssigned != null) {
                                                                if (viewModel.selectedStudentForHighlight.value == studentAssigned) {
                                                                    viewModel.selectedStudentForHighlight.value = null
                                                                } else {
                                                                    viewModel.selectedStudentForHighlight.value = studentAssigned
                                                                }
                                                            } else {
                                                                viewModel.placeStudentAt(r, c)
                                                            }
                                                        }
                                                    }
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
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
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
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            )

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(unassignedStudents) { student ->
                                    val isSelected = selectedUnassignedStudent?.id == student.id
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) primaryColor else Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.selectUnassignedStudent(student) }
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
}

@Composable
fun DeskCell(
    desk: DeskEntity,
    student: StudentEntity?,
    mode: String,
    borderColor: Color?,
    performanceColor: Color? = null,
    onToggleStructure: () -> Unit,
    onPlace: () -> Unit,
    onLockToggle: () -> Unit,
    onSelectHighlight: () -> Unit,
    onEvict: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cellColor = when (desk.type) {
        "WALKWAY" -> Color.Transparent
        "BLOCK" -> Color.DarkGray.copy(alpha = 0.3f)
        else -> if (student != null) {
            performanceColor ?: Color(0xFF3B82F6)
        } else Color.White.copy(alpha = 0.12f)
    }

    val clickableModifier = when (mode) {
        "STRUCTURE" -> modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(cellColor)
        else -> {
            if (desk.type == "DESK") {
                modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cellColor)
            } else {
                modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cellColor)
            }
        }
    }

    val borderStroke = if (borderColor != null) {
        BorderStroke(2.dp, borderColor)
    } else if (desk.type == "DESK" && desk.isLocked) {
        BorderStroke(1.5.dp, Color(0xFFF59E0B)) // Locked indicator in amber outline
    } else {
        BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
    }

    Box(
        modifier = clickableModifier.border(borderStroke, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (desk.type == "DESK") {
            if (student != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Actions on top
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = if (desk.isLocked) Color(0xFFF59E0B) else Color.LightGray.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(10.dp)
                                .clickable { onLockToggle() }
                        )

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Evict",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(11.dp)
                                .clickable { onEvict(student.id) }
                        )
                    }

                    Text(
                        text = student.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    // Small height indication tag
                    Text(
                        text = when (student.height) {
                            "Low" -> "נמוך"
                            "Tall" -> "גבוה"
                            else -> "בינוני"
                        },
                        fontSize = 7.sp,
                        color = Color.LightGray
                    )
                }
            } else {
                Text(
                    text = "ריק",
                    color = Color.LightGray.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light
                )
            }
        } else if (desk.type == "BLOCK") {
            Icon(Icons.Default.Build, contentDescription = "קיר", tint = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
        }
    }
}
