package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicMaterialEntity
import com.example.ui.viewmodel.ClassViewModel
import com.example.util.QuizQuestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: ClassViewModel) {
    val materials by viewModel.materials.collectAsState()
    val isParsingFile by viewModel.isParsingFile.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()

    var showDocAddDialog by remember { mutableStateOf(false) }
    var docTitle by remember { mutableStateOf("") }
    var docContent by remember { mutableStateOf("") }

    var selectedMaterialForDetail by remember { mutableStateOf<AcademicMaterialEntity?>(null) }

    val primaryColor = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        Color(0xFFA5B4FC)
    } else {
        Color(0xFFFCD34D)
    }

    val darkBg = if (viewModel.selectedTheme.collectAsState().value == "MODERN") {
        Color(0xFF1E1B4B)
    } else {
        Color(0xFF2D2319)
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        docTitle = ""
                        docContent = ""
                        showDocAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_document_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("קלוט מקור דידקטי", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Text(
                    "ספריית חומרי לימוד ו-AI",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }

            // Central content splitter
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Documents roster list
                Card(
                    modifier = Modifier.weight(0.42f),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                        Text(
                            "מערכי שיעור קיימים",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        if (materials.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "ספריית המערכים ריקה.\nקלוט מסמך ראשון פה מעל!",
                                    color = Color.LightGray.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(materials) { mat ->
                                    val isSelected = selectedMaterialForDetail?.id == mat.id
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) primaryColor else Color.White.copy(alpha = 0.05f))
                                            .clickable { selectedMaterialForDetail = mat }
                                            .padding(10.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "מחק",
                                                    tint = if (isSelected) Color.Black else Color.Red,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clickable {
                                                            viewModel.deleteMaterial(mat.id)
                                                            if (selectedMaterialForDetail?.id == mat.id) {
                                                                selectedMaterialForDetail = null
                                                            }
                                                        }
                                                )

                                                Text(
                                                    mat.title,
                                                    color = if (isSelected) Color.Black else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "כיסוי: ${mat.coveragePercentage}%",
                                                color = if (isSelected) Color.DarkGray else Color.LightGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Detail parsing panel
                Card(
                    modifier = Modifier.weight(0.58f),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isParsingFile) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = primaryColor)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("מנוע ה-AI של Gemini מעבד ומסכם את מסמך הלימוד הנוכחי כעת...", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                    } else if (selectedMaterialForDetail != null) {
                        val mat = selectedMaterialForDetail!!
                        
                        // Parse quiz questions cleanly inside this remember block (safe computation)
                        val quizQuestions = remember(mat.quizJson) {
                            try {
                                val list = mutableListOf<QuizQuestion>()
                                val quizArr = JSONArray(mat.quizJson)
                                for (i in 0 until quizArr.length()) {
                                    val qObj = quizArr.getJSONObject(i)
                                    val qText = qObj.getString("question")
                                    val optsArr = qObj.getJSONArray("options")
                                    val options = List(optsArr.length()) { optsArr.getString(it) }
                                    val correct = qObj.getInt("correctAnswerIndex")
                                    list.add(QuizQuestion(qText, options, correct))
                                }
                                list
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    mat.title,
                                    style = MaterialTheme.typography.titleLarge.copy(color = primaryColor, fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            item {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            }

                            // Summary
                            item {
                                DetailSection("סיכום מונחה ועקרונות יסוד", mat.summaryNotes)
                            }

                            // Lesson Timeline
                            item {
                                DetailSection("מערך שיעור וציר זמן דידקטי", mat.lessonTimeline)
                            }

                            // MC Questions Quiz
                            item {
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                    Text("שאלון הערכה דיגיטלי (Multiple Choice):", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (quizQuestions.isEmpty()) {
                                        Text("אין שאלון דיגיטלי זמין עבור מערך שיעור זה.", color = Color.LightGray, fontSize = 11.sp)
                                    } else {
                                        quizQuestions.forEachIndexed { i, q ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
                                                    Text("${i + 1}. ${q.question}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    q.options.forEachIndexed { o, optText ->
                                                        val isCorrect = o == q.correctAnswerIndex
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                            horizontalArrangement = Arrangement.End,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                optText,
                                                                color = if (isCorrect) Color(0xFF10B981) else Color.LightGray,
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                                                textAlign = TextAlign.End,
                                                                modifier = Modifier.padding(end = 4.dp)
                                                            )
                                                            Icon(
                                                                imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = if (isCorrect) Color(0xFF10B981) else Color.Gray,
                                                                modifier = Modifier.size(12.dp)
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
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "אנא בחר מערך לימודי מהרשימה\nכדי להציג את סיכום ה-AI של Gemini",
                                color = Color.LightGray.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Intake Document dialog
        if (showDocAddDialog) {
            AlertDialog(
                onDismissRequest = { showDocAddDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.parseLibraryDocument(docTitle, docContent)
                            showDocAddDialog = false
                            coroutineScope.launch {
                                delay(1200)
                                selectedMaterialForDetail = viewModel.materials.value.firstOrNull()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("נתח וקלוט עם Gemini JSON", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDocAddDialog = false }) { Text("ביטול", color = Color.White) }
                },
                title = {
                    Text(
                        "קליטת חומרי לימוד (מערכי שיעור ומקורות)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.End) {
                        OutlinedTextField(
                            value = docTitle,
                            onValueChange = { docTitle = it },
                            label = { Text("כותרת מערך השיעור / נושא הלימוד") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            textStyle = TextStyle(textAlign = TextAlign.End),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, focusedLabelColor = primaryColor)
                        )

                        OutlinedTextField(
                            value = docContent,
                            onValueChange = { docContent = it },
                            label = { Text("טקסט מקור המידע פדגוגי") },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            textStyle = TextStyle(textAlign = TextAlign.End),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, focusedLabelColor = primaryColor)
                        )
                    }
                },
                containerColor = darkBg
            )
        }
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(title, color = Color(0xFFA5B4FC), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            content,
            color = Color.White,
            fontSize = 11.5.sp,
            lineHeight = 17.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
