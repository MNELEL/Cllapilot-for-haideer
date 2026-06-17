package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.ui.viewmodel.ClassViewModel

@Composable
fun StudentProfileComponent(
    student: StudentEntity,
    viewModel: ClassViewModel,
    onDismiss: () -> Unit
) {
    var notes by remember(student.id) { mutableStateOf(student.notes.split(" | ").lastOrNull() ?: "") }
    var points by remember(student.id) { mutableStateOf(viewModel.getStudentPoints(student)) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("ניקוד התנהגותי: $points", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            Button(onClick = { points--; viewModel.incrementScore(student.id, -1) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B))) { Text("-1") }
            Button(onClick = { points++; viewModel.incrementScore(student.id, 1) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))) { Text("+1") }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("הערות פדגוגיות") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                viewModel.saveStudentNotesAndPoints(student.id, points, notes)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("שמור הערות וניקוד", fontWeight = FontWeight.Bold)
        }
    }
}
