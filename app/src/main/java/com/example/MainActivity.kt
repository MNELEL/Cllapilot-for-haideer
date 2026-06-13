package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screen.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ClassViewModel
import com.example.ui.SoundManager

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    private val viewModel: ClassViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundManager.init(this)
        
        enableEdgeToEdge()

        setContent {
            val themeState by viewModel.selectedTheme.collectAsState()
            
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var currentDestination by remember { mutableStateOf("DASHBOARD") }

                    val primaryColor = if (themeState == "MODERN") {
                        com.example.ui.theme.GoldGingerStart
                    } else {
                        com.example.ui.theme.GoldGingerStart
                    }

                    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
                    val offsetAnim by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 2000f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(20000, easing = androidx.compose.animation.core.LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bg_offset"
                    )

                    val mainBgBrush = Brush.linearGradient(
                        colors = listOf(com.example.ui.theme.CreamBeige, Color(0xFFFFFFFF), Color(0xFFECFEFF)),
                        start = androidx.compose.ui.geometry.Offset(0f, offsetAnim),
                        end = androidx.compose.ui.geometry.Offset(2000f, offsetAnim + 1000f)
                    )

                    val isUnlocked by viewModel.isAppUnlocked.collectAsState()

                    if (!isUnlocked) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(brush = mainBgBrush)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Secure Lock",
                                    tint = com.example.ui.theme.GoldGingerEnd, // Soft Indigo
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "ClassPro - כניסה לפרופיל כיתה",
                                    color = com.example.ui.theme.ChocolateBrown,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "הזן קוד מוסדי סודי כדי לפתוח את לוח הבקרה הכיתתי",
                                    color = com.example.ui.theme.MochaTaupe,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                var enteredCode by remember { mutableStateOf("") }
                                var errorMsg by remember { mutableStateOf("") }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 1..4) {
                                        val active = enteredCode.length >= i
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(if (active) com.example.ui.theme.GoldGingerEnd else Color(0xFF94A3B8).copy(alpha = 0.3f))
                                        )
                                    }
                                }

                                if (errorMsg.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(errorMsg, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                val buttons = listOf(
                                    listOf("1", "2", "3"),
                                    listOf("4", "5", "6"),
                                    listOf("7", "8", "9"),
                                    listOf("CLR", "0", "OK")
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    buttons.forEach { row ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            row.forEach { btn ->
                                                Button(
                                                    onClick = { com.example.ui.SoundManager.playClick(); 
                                                        when (btn) {
                                                            "CLR" -> {
                                                                enteredCode = ""
                                                                errorMsg = ""
                                                            }
                                                            "OK" -> {
                                                                if (viewModel.attemptUnlock(enteredCode)) {
                                                                    errorMsg = ""
                                                                } else {
                                                                    errorMsg = "קוד שגוי, אנא נסה שוב!"
                                                                    enteredCode = ""
                                                                }
                                                            }
                                                            else -> {
                                                                if (enteredCode.length < 4) {
                                                                    enteredCode += btn
                                                                }
                                                                if (enteredCode.length == 4) {
                                                                    if (viewModel.attemptUnlock(enteredCode)) {
                                                                        errorMsg = ""
                                                                    } else {
                                                                        errorMsg = "קוד שגוי, אנא נסה שוב!"
                                                                        enteredCode = ""
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(64.dp),
                                                    shape = androidx.compose.foundation.shape.CircleShape,
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                                ) {
                                                    Text(btn, color = com.example.ui.theme.ChocolateBrown, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Text("קוד מוסדי ברירת מחדל: 1234", color = com.example.ui.theme.MochaTaupe, fontSize = 11.sp)
                            }
                        }
                    } else {
                        // Floating Header Nav
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(brush = mainBgBrush)
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                        ) {
                            // Main content area
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 90.dp, bottom = 100.dp) // generous top and bottom padding
                            ) {
                                when (currentDestination) {
                                    "DASHBOARD" -> DashboardScreen(viewModel)
                                    "SEATING" -> SeatingMapScreen(viewModel)
                                    "TIMER" -> TimerScreen(viewModel)
                                    "STUDENTS" -> StudentsScreen(viewModel)
                                    "LIBRARY" -> LibraryScreen(viewModel)
                                    "PORTAL" -> ParentPortalScreen(viewModel)
                                    "MORE" -> MoreScreen(viewModel)
                                    "ATTENDANCE" -> AttendanceScreen(viewModel)
                                }
                            }
                            
                            // Floating Top Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x3364748B))
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(alpha = 0.85f))
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Minimalist Blue Geometric Logo
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        androidx.compose.foundation.Canvas(modifier = Modifier.size(32.dp)) {
                                            drawRoundRect(
                                                color = com.example.ui.theme.GoldGingerEnd, // Soft Indigo
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                                            )
                                            drawArc(
                                                color = Color.White,
                                                startAngle = 0f,
                                                sweepAngle = -180f,
                                                useCenter = false,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                                                size = androidx.compose.ui.geometry.Size(16.dp.toPx(), 16.dp.toPx()),
                                                topLeft = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 10.dp.toPx())
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "ClassPro",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = com.example.ui.theme.ChocolateBrown,
                                            letterSpacing = (-0.5).sp,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                    
                                    // User Profile & Notifications
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        val isSyncEngineActive by viewModel.isSyncing.collectAsState()
                                        
                                        // Offline Sync Indicator
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSyncEngineActive) com.example.ui.theme.GoldGingerStart.copy(alpha = 0.15f) else com.example.ui.theme.PositiveGreen.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSyncEngineActive) com.example.ui.theme.GoldGingerStart else com.example.ui.theme.PositiveGreen)
                                            )
                                            Text(
                                                text = if (isSyncEngineActive) "Pending" else "Synced",
                                                fontSize = 12.sp,
                                                color = if (isSyncEngineActive) com.example.ui.theme.GoldGingerStart else com.example.ui.theme.PositiveGreen,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        IconButton(
                                            onClick = { com.example.ui.SoundManager.playClick();  },
                                            modifier = Modifier.clip(CircleShape).background(com.example.ui.theme.CreamBeige).size(40.dp)
                                        ) {
                                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = com.example.ui.theme.GoldGingerEnd, modifier = Modifier.size(20.dp))
                                        }
                                        Box(
                                            modifier = Modifier.size(40.dp).clip(CircleShape).background(com.example.ui.theme.MochaTaupe),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                            }

                            // Floating Bottom Navigation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 24.dp, vertical = 24.dp)
                                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0x3364748B))
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    navigationItems.forEach { nav ->
                                        val isSelected = currentDestination == nav.route
                                        val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1f, label = "scaleAnim", animationSpec = tween(300))
                                        // soft pastel fill: indigo-50
                                        val backgroundColor = if (isSelected) com.example.ui.theme.CreamBeige else Color.Transparent
                                        val contentColor = if (isSelected) Color(0xFF4F46E5) else Color(0xFF94A3B8)
                                        
                                        Row(
                                            modifier = Modifier
                                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(backgroundColor)
                                                .clickable(
                                                     interactionSource = remember { MutableInteractionSource() },
                                                     indication = LocalIndication.current
                                                ) { com.example.ui.SoundManager.playClick(); currentDestination = nav.route }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                             Icon(nav.icon, contentDescription = nav.label, tint = contentColor, modifier = Modifier.size(20.dp))
                                             if (isSelected) {
                                                 Text(nav.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
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
    }

    private val navigationItems = listOf(
        NavItem("SEATING", "מפת כיתה", Icons.Default.Place),
        NavItem("ATTENDANCE", "נוכחות", Icons.Default.CheckCircle),
        NavItem("TIMER", "טיימר", Icons.Default.PlayArrow),
        NavItem("MORE", "כלים וקמפיינים", Icons.Default.Star),
        NavItem("STUDENTS", "רשימת תלמידים", Icons.Default.Person),
        NavItem("LIBRARY", "ספרייה", Icons.Default.List)
    )
}
