package com.example.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ClassViewModel
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(viewModel: ClassViewModel) {
    var timeLeft by remember { mutableStateOf(5 * 60) } // 5 minutes by default
    var isRunning by remember { mutableStateOf(false) }
    var totalTime by remember { mutableStateOf(5 * 60) }
    var hasFinished by remember { mutableStateOf(false) }
    var showSoundSettings by remember { mutableStateOf(false) }
    val currentContext = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
            hasFinished = false
            if (timeLeft <= 3 && timeLeft > 0) {
                com.example.ui.SoundManager.playPop() // Tick sound for last 3 seconds
            }
        } else if (timeLeft == 0 && isRunning && !hasFinished) {
            isRunning = false
            hasFinished = true
            com.example.ui.SoundManager.playTaskComplete()
        }
    }

    val progressFraction = if (totalTime > 0) timeLeft.toFloat() / totalTime.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "progress"
    )

    // Dynamic Color based on percentage
    val timerColor = when {
        progressFraction > 0.5f -> com.example.ui.theme.PositiveGreen // Safe Green
        progressFraction >= 0.2f -> com.example.ui.theme.GoldGingerStart // Warm Yellow
        else -> Color(0xFFC0392B) // Danger Red
    }

    // Soft Pulsing Red Effect for Danger state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (progressFraction < 0.2f && isRunning) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent) // bg-slate-50
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val minutes = timeLeft / 60
            val seconds = timeLeft % 60
            val timeString = String.format("%02d:%02d", minutes, seconds)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(320.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
            ) {
                // Background shadow/glow ring
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    drawCircle(
                        color = timerColor.copy(alpha = 0.1f),
                        style = Stroke(width = 36.dp.toPx())
                    )
                }
                
                // Background track
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    drawCircle(
                        color = Color(0xFFE2E8F0), // Slate 200
                        style = Stroke(width = 24.dp.toPx())
                    )
                }
                
                // Progress ring
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val sweepAngle = 360f * animatedProgress
                    drawArc(
                        color = timerColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                // Time Text Center
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = com.example.ui.theme.ChocolateBrown // Slate 800
                    )
                    Text(
                        text = if (isRunning) "זמן נותר" else "מושהה",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = com.example.ui.theme.MochaTaupe // Slate 500
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Circular Playback Controls
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                // Reset
                HoverScaleIconButton(
                    onClick = { com.example.ui.SoundManager.playClick();  
                        isRunning = false
                        timeLeft = totalTime
                        hasFinished = false
                    },
                    icon = Icons.Default.Refresh,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = com.example.ui.theme.MochaTaupe,
                    size = 56.dp
                )
                
                // Play/Pause
                HoverScaleIconButton(
                    onClick = { com.example.ui.SoundManager.playClick();  isRunning = !isRunning },
                    icon = if (isRunning) Icons.Default.Clear else Icons.Default.PlayArrow,
                    containerColor = if (isRunning) com.example.ui.theme.GoldGingerStart else com.example.ui.theme.PositiveGreen,
                    contentColor = Color.White,
                    size = 72.dp,
                    iconSize = 36.dp
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Quick setters (Pill shaped + animation)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val times = listOf(1 to "+1 Min", 5 to "+5 Min", 10 to "+10 Min")
                times.forEach { (mins, label) ->
                    HoverScalePillButton(
                        label = label,
                        onClick = { com.example.ui.SoundManager.playClick(); 
                            val newTotal = totalTime + (mins * 60)
                            val newTimeLeft = timeLeft + (mins * 60)
                            totalTime = newTotal
                            timeLeft = newTimeLeft
                        }
                    )
                }
            }
        }
        
        // Settings corner
        IconButton(
            onClick = { com.example.ui.SoundManager.playClick(); showSoundSettings = true },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "Sound Settings", tint = com.example.ui.theme.ChocolateBrown)
        }
    }

    if (showSoundSettings) {
        AlertDialog(
            onDismissRequest = { showSoundSettings = false },
            confirmButton = {
                TextButton(onClick = { com.example.ui.SoundManager.playClick(); showSoundSettings = false }) {
                    Text("סגור", color = com.example.ui.theme.ChocolateBrown)
                }
            },
            title = {
                Text("הגדרות צליל שעון", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Column {
                    com.example.ui.SoundTheme.values().forEach { theme ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    com.example.ui.SoundManager.updateTheme(currentContext, theme)
                                    // Give a demo sample
                                    com.example.ui.SoundManager.playTaskComplete()
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(theme.displayName, modifier = Modifier.padding(end = 12.dp))
                            RadioButton(
                                selected = com.example.ui.SoundManager.currentTheme.value == theme,
                                onClick = {
                                    com.example.ui.SoundManager.updateTheme(currentContext, theme)
                                    com.example.ui.SoundManager.playTaskComplete()
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun HoverScaleIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
    visible: Boolean = true
) {
    if (!visible) {
        Spacer(modifier = Modifier.size(size))
        return
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1.05f, label = "scale")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (isPressed) 2.dp else 8.dp, CircleShape, spotColor = Color(0x3364748B))
            .clip(CircleShape)
            .background(containerColor)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun HoverScalePillButton(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1.05f, label = "scalePill")

    Box(
        modifier = Modifier
            .width(96.dp)
            .height(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (isPressed) 1.dp else 4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x1F6366F1))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = com.example.ui.theme.GoldGingerEnd, fontSize = 14.sp)
    }
}

