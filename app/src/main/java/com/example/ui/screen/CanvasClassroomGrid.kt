package com.example.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.data.model.DeskEntity
import com.example.data.model.StudentEntity
import com.example.ui.SoundManager
import com.example.ui.theme.*

@Composable
fun CanvasClassroomGrid(
    rows: Int,
    cols: Int,
    desks: List<DeskEntity>,
    students: List<StudentEntity>,
    selectedMode: String,
    onDeskClick: (r: Int, c: Int) -> Unit,
    onStudentDragEnd: (sourceR: Int, sourceC: Int, targetR: Int, targetC: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    
    var draggingDesk by remember { mutableStateOf<DeskEntity?>(null) }
    var draggingPos by remember { mutableStateOf(Offset.Zero) }

    Canvas(modifier = modifier
        .fillMaxSize()
        .pointerInput(selectedMode) {
            if (selectedMode == "PLACEMENT") {
                detectDragGestures(
                    onDragStart = { offset ->
                        // find desk
                        // cell size calculation
                        val cellWidth = size.width / cols
                        val cellHeight = size.height / rows
                        val c = (offset.x / cellWidth).toInt()
                        val r = (offset.y / cellHeight).toInt()
                        
                        val desk = desks.find { it.row == r && it.col == c && it.studentId != null }
                        if (desk != null) {
                            SoundManager.playClick()
                            draggingDesk = desk
                            draggingPos = offset
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        draggingPos = change.position
                    },
                    onDragEnd = {
                        val cellWidth = size.width / cols
                        val cellHeight = size.height / rows
                        val c = (draggingPos.x / cellWidth).toInt().coerceIn(0, cols - 1)
                        val r = (draggingPos.y / cellHeight).toInt().coerceIn(0, rows - 1)
                        if (draggingDesk != null) {
                            SoundManager.playClick()
                            onStudentDragEnd(draggingDesk!!.row, draggingDesk!!.col, r, c)
                        }
                        draggingDesk = null
                    },
                    onDragCancel = {
                        draggingDesk = null
                    }
                )
            }
        }
        .pointerInput(selectedMode) {
            detectTapGestures { offset ->
                val cellWidth = size.width / cols
                val cellHeight = size.height / rows
                val c = (offset.x / cellWidth).toInt().coerceIn(0, cols - 1)
                val r = (offset.y / cellHeight).toInt().coerceIn(0, rows - 1)
                onDeskClick(r, c)
            }
        }
    ) {
        val cellWidth = size.width / cols
        val paddingX = cellWidth * 0.1f
        val cellHeight = (size.height / rows).coerceAtMost(cellWidth) // keep squares if possible
        val paddingY = cellHeight * 0.1f
        
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val desk = desks.find { it.row == r && it.col == c }
                val startX = c * cellWidth + paddingX
                val startY = r * cellHeight + paddingY
                val w = cellWidth - paddingX * 2
                val h = cellHeight - paddingY * 2
                
                if (desk != null && desk.type != "WALKWAY") {
                    val isBlock = desk.type == "BLOCK"
                    val fillColor = if (isBlock) Color(0xFFE2E8F0) else Color.White
                    val borderColor = if (isBlock) Color.Transparent else Color(0xFF94A3B8)
                    
                    if (draggingDesk != desk) {
                        drawRoundRect(
                            color = fillColor,
                            topLeft = Offset(startX, startY),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        if (!isBlock) {
                            drawRoundRect(
                                color = borderColor,
                                topLeft = Offset(startX, startY),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(12f, 12f),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                    
                    if (desk.type == "DESK") {
                        val student = students.find { it.id == desk.studentId }
                        if (student != null && draggingDesk != desk) {
                            drawCircle(
                                color = ChocolateBrown,
                                radius = w * 0.25f,
                                center = Offset(startX + w / 2, startY + h / 2 - 10f)
                            )
                            val nameRessult = textMeasurer.measure(
                                text = student.name.split(" ").firstOrNull() ?: "",
                                style = TextStyle(fontSize = 11.sp, color = ChocolateBrown, fontWeight = FontWeight.Bold)
                            )
                            drawText(
                                textLayoutResult = nameRessult,
                                topLeft = Offset(startX + w / 2 - nameRessult.size.width / 2, startY + h - nameRessult.size.height - 5f)
                            )
                        }
                    }
                } else if (desk == null) {
                    drawRoundRect(
                        color = Color(0xFFCBD5E1).copy(alpha = 0.5f),
                        topLeft = Offset(startX, startY),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
            }
        }
        
        // Draw dragging avatar floating overlay
        if (draggingDesk != null) {
            val student = students.find { it.id == draggingDesk!!.studentId }
            if (student != null) {
                drawCircle(
                    color = PositiveGreen,
                    radius = cellWidth * 0.3f,
                    center = draggingPos
                )
                val nm = textMeasurer.measure(
                    text = student.name.split(" ").firstOrNull() ?: "",
                    style = TextStyle(fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = nm,
                    topLeft = Offset(draggingPos.x - nm.size.width / 2, draggingPos.y + (cellWidth * 0.3f) + 5f)
                )
            }
        }
    }
}
