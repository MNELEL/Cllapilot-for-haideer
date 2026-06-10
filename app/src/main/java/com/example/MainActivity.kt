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
import androidx.compose.ui.draw.clip
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
        
        enableEdgeToEdge()

        setContent {
            val themeState by viewModel.selectedTheme.collectAsState()
            
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var currentDestination by remember { mutableStateOf("DASHBOARD") }

                    val primaryColor = if (themeState == "MODERN") {
                        Color(0xFFA5B4FC)
                    } else {
                        Color(0xFFFCD34D)
                    }

                    val darkBg = if (themeState == "MODERN") {
                        Color(0xFF1E1B4B)
                    } else {
                        Color(0xFF2D2319)
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "ClassPro - ניהול כיתה חכם",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                },
                                actions = {
                                    // Projector Kiosk Mode Launcher button
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(this@MainActivity, KioskActivity::class.java)
                                            startActivity(intent)
                                        },
                                        modifier = Modifier.testTag("launch_kiosk_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Settings, // guaranteed standard icon
                                            contentDescription = "מסך מקרן קיוסק",
                                            tint = primaryColor
                                        )
                                    }

                                    // Theme Mode toggging button
                                    IconButton(
                                        onClick = {
                                            val nextTheme = if (themeState == "MODERN") "CONSERVATIVE" else "MODERN"
                                            viewModel.setTheme(nextTheme)
                                        },
                                        modifier = Modifier.testTag("toggle_theme_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Build, // guaranteed standard icon
                                            contentDescription = "החלף ערכת נושא",
                                            tint = Color.White
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = darkBg,
                                    navigationIconContentColor = Color.White,
                                    titleContentColor = Color.White
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = darkBg,
                                tonalElevation = 8.dp
                            ) {
                                navigationItems.forEach { nav ->
                                    val isSelected = currentDestination == nav.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentDestination = nav.route },
                                        icon = {
                                            Icon(
                                                imageVector = nav.icon,
                                                contentDescription = nav.label,
                                                tint = if (isSelected) Color.Black else Color.White
                                            )
                                        },
                                        label = {
                                            Text(
                                                nav.label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) primaryColor else Color.LightGray
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = primaryColor
                                        )
                                    )
                                }
                            }
                        },
                        containerColor = darkBg
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentDestination) {
                                "DASHBOARD" -> DashboardScreen(viewModel)
                                "SEATING" -> SeatingMapScreen(viewModel)
                                "STUDENTS" -> StudentsScreen(viewModel)
                                "LIBRARY" -> LibraryScreen(viewModel)
                                "MORE" -> MoreScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    private val navigationItems = listOf(
        NavItem("DASHBOARD", "לוח בקרה", Icons.Default.Home),
        NavItem("SEATING", "מפת ישיבה", Icons.Default.Edit),
        NavItem("STUDENTS", "רשימת תלמידים", Icons.Default.Person),
        NavItem("LIBRARY", "ספריית AI", Icons.Default.List),
        NavItem("MORE", "עוד / עזרים", Icons.Default.Star)
    )
}
