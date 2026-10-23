package com.example.util

import androidx.compose.ui.graphics.Color

enum class WireColor(
    val labelAr: String,
    val primaryColor: Color,
    val secondaryColor: Color? = null,
    val code: String
) {
    RED("أحمر", Color(0xFFDC2626), null, "red"),
    BLACK("أسود", Color(0xFF1E293B), null, "black"),
    YELLOW("أصفر", Color(0xFFEAB308), null, "yellow"),
    GREEN("أخضر", Color(0xFF16A34A), null, "green"),
    BLUE("أزرق", Color(0xFF2563EB), null, "blue"),
    GREEN_YELLOW("أخضر مخطط بأصفر", Color(0xFF16A34A), Color(0xFFEAB308), "green_yellow");

    companion object {
        fun fromCode(code: String?): WireColor? {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) }
        }

        fun fromLabel(label: String?): WireColor? {
            return entries.firstOrNull { it.labelAr == label }
        }
    }
}
