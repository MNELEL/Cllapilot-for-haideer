package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.local.ClassProDatabase
import com.example.data.model.DeskEntity
import com.example.data.model.StudentEntity
import com.example.ui.theme.MyApplicationTheme

class KioskActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Strict Landscape Orientation mapping
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // Immersive full-screen window management configurations
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val db = ClassProDatabase.getDatabase(this)
        val deskFlow = db.deskDao().getDesksFlow()
        val studentFlow = db.studentDao().getStudentsFlow()

        setContent {
            MyApplicationTheme {
                val desksList by deskFlow.collectAsState(initial = emptyList())
                val studentsList by studentFlow.collectAsState(initial = emptyList())

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    KioskImmersiveGrid(desksList, studentsList, onBack = { finish() })
                }
            }
        }
    }
}

@Composable
fun KioskImmersiveGrid(desks: List<DeskEntity>, students: List<StudentEntity>, onBack: () -> Unit) {
    // Determine maximum rows and columns
    val maxRow = (desks.maxOfOrNull { it.row } ?: 5) + 1
    val maxCol = (desks.maxOfOrNull { it.col } ?: 5) + 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Midnight deep contrast background
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Uncluttered, flat header optimized for high-lumens smartboards
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "לחיצה לשינוי / יציאה מקיוסק ❌",
                    color = Color.LightGray.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.clip(RoundedCornerShape(6.dp))
                        .clickable { onBack() }
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )

                Text(
                    text = "לוח מפת הישיבה הכיתתית שלכם 🖥️",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            // FRONT INDICATOR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "לוח הכיתה / שולחן המורה",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Grid mapping
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(maxRow) { r ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (c in 0 until maxCol) {
                            val desk = desks.find { it.row == r && it.col == c }
                            if (desk != null) {
                                val student = students.find { it.id == desk.studentId }
                                KioskDeskCell(desk, student, modifier = Modifier.weight(1f))
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KioskDeskCell(desk: DeskEntity, student: StudentEntity?, modifier: Modifier = Modifier) {
    val isWalkway = desk.type == "WALKWAY"
    val isWall = desk.type == "BLOCK"

    val containerColor = when {
        isWalkway -> Color.Transparent
        isWall -> Color.DarkGray.copy(alpha = 0.4f)
        student != null -> Color(0xFF1E3A8A) // deep flat contrast blue
        else -> Color.White.copy(alpha = 0.05f) // empty desk outline
    }

    Box(
        modifier = modifier
            .aspectRatio(2.2f) // wider for landscape kiosk screens!
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (!isWalkway && !isWall) {
            if (student != null) {
                Text(
                    text = student.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp, // Bold, high readability for projector screens!
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                Text(
                    text = "", // Hides all administrative details in Kiosk Smartboard projection
                    color = Color.Transparent
                )
            }
        }
    }
}
