package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.ui.SoundManager
import com.example.ui.viewmodel.ClassViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(viewModel: ClassViewModel) {
    val students by viewModel.students.collectAsState()
    val attendanceLogs by viewModel.attendanceLogs.collectAsState()
    
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Statuses for each student today
    val todayAttendance = students.associateWith { st ->
        attendanceLogs.find { it.studentId == st.id && it.date == today }?.status
    }

    val presentCount = todayAttendance.values.count { it == "PRESENT" }
    val absentCount = todayAttendance.values.count { it == "ABSENT" }
    val lateCount = todayAttendance.values.count { it == "LATE" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tracker Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
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
                    Text(
                        today,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "רישום נוכחות יומי",
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.ChocolateBrown,
                            fontSize = 20.sp
                        )
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = com.example.ui.theme.GoldGingerEnd)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBadge("נוכחים", presentCount.toString(), com.example.ui.theme.PositiveGreen)
                    StatBadge("מאחרים", lateCount.toString(), com.example.ui.theme.GoldGingerStart)
                    StatBadge("נעדרים", absentCount.toString(), Color(0xFFC0392B))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of students
        LazyVerticalGrid(
            columns = GridCells.Adaptive(110.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(students, key = { it.id }) { student ->
                val currentStatus = todayAttendance[student]
                AttendanceStudentCard(
                    student = student,
                    status = currentStatus,
                    onStatusSelected = { newStatus ->
                        viewModel.toggleAttendance(student.id, newStatus)
                        if (newStatus == "PRESENT") {
                            SoundManager.playPop()
                        } else if (newStatus == "LATE") {
                            SoundManager.playNotification()
                        } else {
                            SoundManager.playDelete()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AttendanceStudentCard(
    student: StudentEntity,
    status: String?,
    onStatusSelected: (String) -> Unit
) {
    val borderColor = when (status) {
        "PRESENT" -> com.example.ui.theme.PositiveGreen
        "ABSENT" -> Color(0xFFC0392B)
        "LATE" -> com.example.ui.theme.GoldGingerStart
        else -> Color.LightGray.copy(alpha = 0.5f)
    }
    
    val bgColor = when (status) {
        "PRESENT" -> com.example.ui.theme.PositiveGreen.copy(alpha = 0.1f)
        "ABSENT" -> Color(0xFFC0392B).copy(alpha = 0.1f)
        "LATE" -> com.example.ui.theme.GoldGingerStart.copy(alpha = 0.1f)
        else -> Color.White.copy(alpha = 0.8f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(if (status != null) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Tap to cycle" logic or quick actions
            // Let's use a quick cycle: Null -> Present -> Late -> Absent -> Present...
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val nextStatus = when (status) {
                            null, "ABSENT" -> "PRESENT"
                            "PRESENT" -> "LATE"
                            "LATE" -> "ABSENT"
                            else -> "PRESENT"
                        }
                        onStatusSelected(nextStatus)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = com.example.ui.theme.MochaTaupe,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(bottom = 4.dp)
                    )
                    Text(
                        student.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.ChocolateBrown,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mini buttons row
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { onStatusSelected("PRESENT") },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (status == "PRESENT") com.example.ui.theme.PositiveGreen else Color.LightGray.copy(0.3f))
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Present", tint = if (status == "PRESENT") Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { onStatusSelected("LATE") },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (status == "LATE") com.example.ui.theme.GoldGingerStart else Color.LightGray.copy(0.3f))
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Late", tint = if (status == "LATE") Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { onStatusSelected("ABSENT") },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (status == "ABSENT") Color(0xFFC0392B) else Color.LightGray.copy(0.3f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Absent", tint = if (status == "ABSENT") Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
