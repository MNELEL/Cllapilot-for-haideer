#!/usr/bin/env python3
import os
import re

directory = "app/src/main/java/com/example/ui/screen"
replacements = {
    "Color(0xFF1E293B)": "com.example.ui.theme.ChocolateBrown",
    "Color(0xFF64748B)": "com.example.ui.theme.MochaTaupe",
    "Color(0xFF6366F1)": "com.example.ui.theme.GoldGingerEnd",
    "Color(0xFF10B981)": "com.example.ui.theme.PositiveGreen",
    "Color(0xFFF59E0B)": "com.example.ui.theme.GoldGingerStart",
    "Color(0xFFEF4444)": "Color(0xFFC0392B)", # Leave red
    "Color(0xFFEEF2FF)": "com.example.ui.theme.CreamBeige",
    "Color(0xFFC7D2FE)": "com.example.ui.theme.MochaTaupe",
    "Color(0xFF2D2319)": "com.example.ui.theme.ChocolateBrown",
    "Color(0xFF1E1B4B)": "com.example.ui.theme.ChocolateBrown.copy(alpha=0.9f)",
    "Color(0xFFA5B4FC)": "com.example.ui.theme.GoldGingerStart",
    "Color(0xFFF8FAFC)": "com.example.ui.theme.CreamBeige",
    "Color(0xFFFCD34D)": "com.example.ui.theme.GoldGingerStart",
}

for root, _, files in os.walk(directory):
    for f in files:
        if f.endswith(".kt"):
            filepath = os.path.join(root, f)
            with open(filepath, "r") as file:
                content = file.read()
            
            for key, val in replacements.items():
                content = content.replace(key, val)
            
            # Additional replacement for gradient background on screens
            content = re.sub(r'Brush\.verticalGradient\(\s*listOf\(darkBg, darkBg\.copy\(alpha = 0\.9f\)\)\s*\)',
                             r'Brush.verticalGradient(listOf(com.example.ui.theme.CreamBeige, com.example.ui.theme.WhiteWarm))', content)

            with open(filepath, "w") as file:
                file.write(content)
print("Color replacement done!")
