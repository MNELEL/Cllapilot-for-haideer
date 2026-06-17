package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val LightColorSchemeCream = lightColorScheme(
    primary = ChocolateBrown,
    onPrimary = WhiteWarm,
    secondary = MochaTaupe,
    onSecondary = WhiteWarm,
    tertiary = PositiveGreen,
    background = CreamBeige,
    surface = WhiteWarm,
    onBackground = ChocolateBrown,
    onSurface = ChocolateBrown,
)

private val LightColorSchemePink = lightColorScheme(
    primary = Color(0xFFE11D48),
    onPrimary = WhiteWarm,
    secondary = Color(0xFFFDA4AF),
    onSecondary = ChocolateBrown,
    tertiary = PositiveGreen,
    background = LightPinkStart,
    surface = LightPinkEnd,
    onBackground = ChocolateBrown,
    onSurface = ChocolateBrown,
)

private val LightColorSchemeTeal = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = WhiteWarm,
    secondary = Color(0xFF2DD4BF),
    onSecondary = ChocolateBrown,
    tertiary = PositiveGreen,
    background = LightTealStart,
    surface = LightTealEnd,
    onBackground = ChocolateBrown,
    onSurface = ChocolateBrown,
)

private val LightColorSchemeGold = lightColorScheme(
    primary = Color(0xFFB45309),
    onPrimary = WhiteWarm,
    secondary = Color(0xFFFBBF24),
    onSecondary = ChocolateBrown,
    tertiary = PositiveGreen,
    background = LightGoldStart,
    surface = LightGoldEnd,
    onBackground = ChocolateBrown,
    onSurface = ChocolateBrown,
)

private val LightColorSchemeModern = lightColorScheme(
    primary = Color(0xFF6C63FF),
    onPrimary = WhiteWarm,
    secondary = MochaTaupe,
    onSecondary = WhiteWarm,
    tertiary = PositiveGreen,
    background = CreamBeige,
    surface = WhiteWarm,
    onBackground = ChocolateBrown,
    onSurface = ChocolateBrown,
)

private val DarkColorSchemePurple = darkColorScheme(
    primary = Color(0xFFD8B4FE),
    onPrimary = Color(0xFF1E1B4B),
    secondary = Color(0xFFA78BFA),
    onSecondary = Color(0xFF1E1B4B),
    tertiary = PositiveGreen,
    background = DarkPurpleStart,
    surface = DarkPurpleEnd,
    onBackground = CreamBeige,
    onSurface = CreamBeige,
)

private val DarkColorSchemeTeal = darkColorScheme(
    primary = Color(0xFF2DD4BF),
    onPrimary = Color(0xFF07201D),
    secondary = Color(0xFF14B8A6),
    onSecondary = Color(0xFF07201D),
    tertiary = PositiveGreen,
    background = DarkTealStart,
    surface = DarkTealEnd,
    onBackground = CreamBeige,
    onSurface = CreamBeige,
)

private val DarkColorSchemeBlue = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF030712),
    secondary = Color(0xFF3B82F6),
    onSecondary = Color(0xFF030712),
    tertiary = PositiveGreen,
    background = DarkBlueStart,
    surface = DarkBlueEnd,
    onBackground = CreamBeige,
    onSurface = CreamBeige,
)

@Composable
fun MyApplicationTheme(
  themePreference: String = "WARM",
  darkTheme: Boolean = ThemeManager.isDarkTheme.collectAsState(initial = false).value,
  // Switch dynamicColor off to respect brand colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val isDark = darkTheme || themePreference == "PURPLE" || themePreference == "DARK_TEAL" || themePreference == "DARK_BLUE"
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      isDark -> {
        when (themePreference) {
          "PURPLE" -> DarkColorSchemePurple
          "DARK_TEAL" -> DarkColorSchemeTeal
          "DARK_BLUE" -> DarkColorSchemeBlue
          else -> DarkColorSchemePurple
        }
      }

      else -> {
        when (themePreference) {
          "PINK" -> LightColorSchemePink
          "TEAL" -> LightColorSchemeTeal
          "GOLD" -> LightColorSchemeGold
          "MODERN" -> LightColorSchemeModern
          else -> LightColorSchemeCream
        }
      }
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
