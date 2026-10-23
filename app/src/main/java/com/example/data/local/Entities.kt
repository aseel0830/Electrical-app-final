package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val clientName: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isCurrent: Boolean = false,
    val isBase: Boolean = false
)

@Entity(tableName = "stages")
data class StageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 1L,
    val name: String,
    val imageUri: String? = null,
    val isDefault: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 1L,
    val stageName: String,
    val name: String,
    val imageUri: String? = null,
    val quantityMode: String = "SINGLE", // "SINGLE" or "TRIPLE_COLOR"
    val quantity: Int = 0,
    val q1: Int = 0,
    val q2: Int = 0,
    val q3: Int = 0,
    val wireColor: String? = null, // fallback or single mode
    val c1: String? = null, // color for box 1
    val c2: String? = null, // color for box 2
    val c3: String? = null, // color for box 3
    val unit: String = "حبة",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun hasQuantity(): Boolean {
        return if (quantityMode == "TRIPLE_COLOR") {
            q1 > 0 || q2 > 0 || q3 > 0
        } else {
            quantity > 0
        }
    }

    fun totalQuantity(): Int {
        return if (quantityMode == "TRIPLE_COLOR") {
            q1 + q2 + q3
        } else {
            quantity
        }
    }

    fun formattedQuantityText(): String {
        return if (quantityMode == "TRIPLE_COLOR") {
            val parts = mutableListOf<String>()
            if (q1 > 0) parts.add("$q1")
            if (q2 > 0) parts.add("$q2")
            if (q3 > 0) parts.add("$q3")
            if (parts.isEmpty()) "0" else parts.joinToString(" + ")
        } else {
            "$quantity"
        }
    }

    fun formattedDetailedQuantityText(): String {
        if (quantityMode == "TRIPLE_COLOR") {
            val parts = mutableListOf<String>()
            val u = if (unit.isNotBlank()) " $unit" else ""
            if (q1 > 0) {
                val color = c1 ?: wireColor
                val colorText = if (!color.isNullOrBlank()) " ($color)" else ""
                parts.add("$q1$u$colorText")
            }
            if (q2 > 0) {
                val color = c2 ?: wireColor
                val colorText = if (!color.isNullOrBlank()) " ($color)" else ""
                parts.add("$q2$u$colorText")
            }
            if (q3 > 0) {
                val color = c3 ?: wireColor
                val colorText = if (!color.isNullOrBlank()) " ($color)" else ""
                parts.add("$q3$u$colorText")
            }
            return if (parts.isEmpty()) "0$u" else parts.joinToString(" + ")
        } else {
            val u = if (unit.isNotBlank()) " $unit" else ""
            val colorText = if (!wireColor.isNullOrBlank()) " ($wireColor)" else ""
            return "$quantity$u$colorText"
        }
    }

    fun formattedCustomColorLines(): List<String> {
        val lines = mutableListOf<String>()
        val u = unit.ifBlank { "حبة" }
        if (quantityMode == "TRIPLE_COLOR") {
            if (q1 > 0) {
                val color = c1 ?: wireColor ?: "أحمر"
                lines.add("-$q1-$u-$color")
            }
            if (q2 > 0) {
                val color = c2 ?: wireColor ?: "أزرق"
                lines.add("-$q2-$u-$color")
            }
            if (q3 > 0) {
                val color = c3 ?: wireColor ?: "أخضر مخطط بأصفر"
                lines.add("-$q3-$u-$color")
            }
        } else {
            if (quantity > 0) {
                if (!wireColor.isNullOrBlank()) {
                    lines.add("-$quantity-$u-$wireColor")
                } else {
                    lines.add("-$quantity-$u")
                }
            }
        }
        return lines
    }
}
