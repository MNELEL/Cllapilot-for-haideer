package com.example.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.ClassViewModel

@Composable
fun GamificationScreen(viewModel: ClassViewModel) {
    val students by viewModel.students.collectAsState()
    
    // Auto-sorting
    val sortedStudents = remember(students) {
        students.sortedByDescending { viewModel.getStudentPoints(it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("לוח מובילים", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(sortedStudents) { student ->
                val points = viewModel.getStudentPoints(student)
                
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(student.name, modifier = Modifier.weight(1f))
                    Text("$points", modifier = Modifier.padding(horizontal = 8.dp))
                    
                    IconButton(onClick = { 
                        // Update points (assuming updateStudentPoints will be implemented in ViewModel)
                        // viewModel.updateStudentPoints(student.id, points + 1)
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "הוסף")
                    }
                    IconButton(onClick = { 
                        // viewModel.updateStudentPoints(student.id, points - 1)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "הפחת")
                    }
                }
            }
        }
    }
}
