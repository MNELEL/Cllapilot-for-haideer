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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Grid sizing inputs
    var rowInput by remember { mutableStateOf(rows.toString()) }
    var colInput by remember { mutableStateOf(cols.toString()) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Screen Header & AI Solver Execution Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                    Icon(Icons.Default.Build, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
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
                            modifier = Modifier.width(65.dp).height(50.dp),
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = rowInput,
                            onValueChange = { rowInput = it },
                            label = { Text("שורות", fontSize = 10.sp) },
                            modifier = Modifier.width(65.dp).height(50.dp),
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray)
                        )
                    }

                    Text("מימדי הכיתה (1x1 עד 20x20):", color = Color.LightGray, fontSize = 12.sp)
                }
            }

            // Controls & Undo Redo row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo / Redo buttons
                Row {
                    IconButton(
                        onClick = { viewModel.undoPlacement() },
                        modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.07f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "בטל", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.redoPlacement() },
                        modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.07f))
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "בצע שוב", tint = Color.White)
                    }
                }

                // Modes Toggles
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(2.dp)
                ) {
                    val modeHebrew = if (selectedMode == "STRUCTURE") "עריכת מבנה" else "הושבת תלמיד"
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
                        modifier = Modifier.fillMaxSize().padding(12.dp)
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
                                            if (selectedStudentHighlight != null && studentAssigned != null) {
                                                if (studentAssigned.id == selectedStudentHighlight!!.id) {
                                                    highlightBorderColor = Color.Yellow
                                                } else if (selectedStudentHighlight!!.loves.contains(studentAssigned.id)) {
                                                    highlightBorderColor = Color(0xFF10B981) // preferred Lovess count in green
                                                } else if (selectedStudentHighlight!!.forbids.contains(studentAssigned.id) || selectedStudentHighlight!!.separate.contains(studentAssigned.id)) {
                                                    highlightBorderColor = Color(0xFFEF4444) // separated/conflicted in red
                                                }
                                            }

                                            DeskCell(
                                                desk = desk,
                                                student = studentAssigned,
                                                mode = selectedMode,
                                                borderColor = highlightBorderColor,
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
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        ) {
                            Text(
                                "תלמידים ללא מושב (${unassignedStudents.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
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
        else -> if (student != null) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.12f)
    }

    val clickableModifier = when (mode) {
        "STRUCTURE" -> modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(cellColor)
            .clickable { onToggleStructure() }
        else -> {
            if (desk.type == "DESK") {
                modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cellColor)
                    .clickable {
                        if (student != null) {
                            onSelectHighlight()
                        } else {
                            onPlace()
                        }
                    }
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
        null
    }

    Box(
        modifier = if (borderStroke != null) {
            clickableModifier.border(borderStroke, RoundedCornerShape(8.dp))
        } else {
            clickableModifier
        },
        contentAlignment = Alignment.Center
    ) {
        if (desk.type == "DESK") {
            if (student != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(2.dp),
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
                                .size(12.dp)
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
