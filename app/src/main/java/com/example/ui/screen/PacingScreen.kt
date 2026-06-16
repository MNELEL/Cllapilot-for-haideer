package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PacingEntity
import com.example.ui.viewmodel.ClassViewModel
import java.util.UUID

@Composable
fun PacingScreen(viewModel: ClassViewModel) {
    val pacingList by viewModel.pacingList.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var moduleName by remember { mutableStateOf("") }
    var startBoundary by remember { mutableStateOf("") }
    var endBoundary by remember { mutableStateOf("") }
    
    val primaryColor = com.example.ui.theme.GoldGingerStart
    val darkBg = com.example.ui.theme.ChocolateBrown
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("classpro_prefs", android.content.Context.MODE_PRIVATE) }
    val learnedTone = remember { sharedPref.getString("pedagogical_tone", "עיוני ומעמיק, מותאם לצורכי החינוך וההוראה") ?: "" }
    val learnedFormat = remember { sharedPref.getString("pedagogical_format", "סיכום מובנה, ציר זמן פדגוגי ברור ושאלות חזרה בהבנה מעמיקה") ?: "" }

    Column(modifier = Modifier.fillMaxSize().background(com.example.ui.theme.CreamBeige).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { com.example.ui.SoundManager.playClick(); showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("הוסף הספק חדש", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text("מעקב קצב והספקים", style = MaterialTheme.typography.titleMedium.copy(color = darkBg, fontWeight = FontWeight.Bold))
        }

        // Pedagogical Style Profile banner
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                Text(
                    "פרופיל סגנון פדגוגי מותאם אישית (בינה מלאכותית לומדת):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = darkBg
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "טון פדגוגי: $learnedTone",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "העדפת עימוד: $learnedFormat",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
        ) {
            if (pacingList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.GoldGingerStart.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = com.example.ui.theme.GoldGingerEnd,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "אין יעדי הספק פעילים בכיתה",
                        color = com.example.ui.theme.ChocolateBrown,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "הגדר יעדים וטווחים שבועיים עבור חומרי הלימוד באמצעות הטופס שלמטה כדי לעקוב אחר התקדמות הלימוד הכיתתית שלכם.",
                        color = com.example.ui.theme.MochaTaupe,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pacingList) { pacing ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (pacing.completionStatus) com.example.ui.theme.PositiveGreen.copy(alpha=0.2f) else com.example.ui.theme.CreamBeige),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { viewModel.deletePacing(pacing.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(pacing.moduleName, fontWeight = FontWeight.Bold, color = darkBg, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("מתחיל: ${pacing.rangeStart} | מסתיים: ${pacing.rangeEnd}", color = Color.DarkGray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val newPacing = PacingEntity(
                            id = UUID.randomUUID().toString(),
                            moduleName = moduleName,
                            rangeStart = startBoundary,
                            rangeEnd = endBoundary,
                            associatedMaterialId = "",
                            completionStatus = false
                        )
                        viewModel.insertPacing(newPacing)
                        showAddDialog = false
                        moduleName = ""
                        startBoundary = ""
                        endBoundary = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("שמור", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("ביטול", color = darkBg) }
            },
            title = { Text("הזנת יחידת הספק חדשה", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(horizontalAlignment = Alignment.End) {
                    OutlinedTextField(value = moduleName, onValueChange = { moduleName = it }, label = { Text("שם הספר/הנושא (למשל מסכת בבא קמא)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = endBoundary, onValueChange = { endBoundary = it }, label = { Text("עד למילה/פסוק") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = startBoundary, onValueChange = { startBoundary = it }, label = { Text("מהמילה/פסוק") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Text("אנג'ין הבינה המלאכותית שלנו ימפה באופן רציף את הישגי התלמידים לפי ההספק שסומן.", color = Color.DarkGray, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        )
    }
}
