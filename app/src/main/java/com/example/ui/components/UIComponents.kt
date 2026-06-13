package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import com.example.ui.SoundManager
import com.example.ui.theme.*

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteWarm),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = MochaTaupe, spotColor = ChocolateBrown.copy(alpha = 0.1f)),
        content = {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            SoundManager.playClick()
            onClick()
        },
        colors = ButtonDefaults.buttonColors(containerColor = ChocolateBrown, contentColor = WhiteWarm),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun SecondaryOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = {
            SoundManager.playClick()
            onClick()
        },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChocolateBrown, containerColor = CreamBeige),
        border = BorderStroke(1.dp, MochaTaupe),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun IconButtonSquare(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CreamBeige)
            .clickable {
                SoundManager.playClick()
                onClick()
            }
            .border(1.dp, MochaTaupe.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(GoldGingerStart, GoldGingerEnd)
    )
    Text(
        text = text,
        modifier = modifier,
        style = androidx.compose.ui.text.TextStyle(
            brush = gradientBrush,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    )
}

@Composable
fun TopBar(title: String, onMenuClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButtonSquare(
            icon = { androidx.compose.material3.Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = ChocolateBrown) },
            onClick = onMenuClick
        )
        GradientText(text = title, fontSize = 20.sp)
        IconButtonSquare(
            icon = { androidx.compose.material3.Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = ChocolateBrown) },
            onClick = onSettingsClick
        )
    }
}
