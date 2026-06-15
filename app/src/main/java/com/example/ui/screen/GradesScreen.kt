package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ClassViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.example.data.model.StudentEntity
import com.example.data.model.StudentGradeEntity

@Composable
fun GradesScreen(viewModel: ClassViewModel) {
    val students by viewModel.students.collectAsState()
    val gradesList by viewModel.grades.collectAsState()
    
    val assignments = listOf("מטלה 1", "מטלה 2", "מבחן", "ציוד")
    
    var grades by remember(gradesList) { 
        val map = mutableMapOf<String, MutableMap<String, String>>()
        gradesList.forEach { grade ->
            if (!map.containsKey(grade.studentId)) map[grade.studentId] = mutableMapOf()
            map[grade.studentId]!![grade.assignmentId] = grade.gradeValue
        }
        mutableStateOf(map) 
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { 
                    com.example.ui.SoundManager.playClick()
                    viewModel.clearAllGrades() 
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B))
            ) {
                Text("מחק הכל (ציונים וציוד)", color = Color.White)
            }
            Text("ניהול ציונים וציוד", style = MaterialTheme.typography.headlineMedium, color = com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().background(com.example.ui.theme.CreamBeige).padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("ממוצע", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, color = com.example.ui.theme.MochaTaupe)
                    assignments.reversed().forEach { ass ->
                        Text(ass, modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, color = com.example.ui.theme.MochaTaupe)
                    }
                    Text("תלמיד", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, color = com.example.ui.theme.MochaTaupe)
                }
            }
            items(students) { student ->
                val studentGrades = grades[student.id] ?: mutableMapOf()
                
                Card(
                     colors = CardDefaults.cardColors(containerColor = Color.White),
                     elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp).fillMaxWidth()
                    ) {
                        val avg = studentGrades.values.mapNotNull { it.toIntOrNull() }.let { if (it.isEmpty()) 0.0 else it.average() }
                        Text(String.format("%.1f", avg), modifier = Modifier.weight(0.5f), color = if(avg > 80) com.example.ui.theme.PositiveGreen else com.example.ui.theme.ChocolateBrown, fontWeight = FontWeight.Bold)

                        assignments.reversed().forEach { ass ->
                            OutlinedTextField(
                                value = studentGrades[ass] ?: "",
                                onValueChange = { newValue ->
                                  viewModel.addOrUpdateGrade(student.id, ass, newValue)
                                },
                                modifier = Modifier.weight(0.5f).padding(horizontal = 2.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                            )
                        }
                        
                        Text(student.name, modifier = Modifier.weight(0.8f), fontWeight = FontWeight.SemiBold, color = com.example.ui.theme.ChocolateBrown)
                    }
                }
            }
        }
    }
}
