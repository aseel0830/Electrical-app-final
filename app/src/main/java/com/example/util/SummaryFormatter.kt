package com.example.util

import com.example.data.local.ItemEntity
import com.example.data.local.ProjectEntity

object SummaryFormatter {

    fun generateFormattedSummary(
        project: ProjectEntity?,
        technicianName: String = "أصيل المليكي",
        technicianPhone: String = "",
        itemsByStage: Map<String, List<ItemEntity>>
    ): String {
        val sb = StringBuilder()

        val client = project?.clientName?.trim()?.ifBlank { "عام" } ?: "عام"
        val tech = technicianName.trim().ifBlank { "أصيل المليكي" }
        val phoneSuffix = if (technicianPhone.isNotBlank()) " - $technicianPhone" else ""

        sb.append("السلام عليكم ورحمة الله وبركاته.\n")
        sb.append("⚡ كشف حصر مواد الكهرباء\n")
        sb.append("العميل: $client\n")
        sb.append("══════════════════════\n\n")

        // Flatten all items from all windows sequentially without mentioning windows
        val allActiveItems = itemsByStage.values.flatten().filter { it.hasQuantity() }

        if (allActiveItems.isEmpty()) {
            sb.append("(لا توجد عناصر تم تحديد كمياتها حتى الآن)\n\n")
        } else {
            allActiveItems.forEach { item ->
                val u = item.unit.ifBlank { "حبة" }
                if (item.quantityMode == "TRIPLE_COLOR") {
                    if (item.q1 > 0) {
                        val color = item.c1 ?: item.wireColor ?: "أحمر"
                        sb.append("- ${item.q1} $u = ${item.name} ($color)\n")
                    }
                    if (item.q2 > 0) {
                        val color = item.c2 ?: item.wireColor ?: "أزرق"
                        sb.append("- ${item.q2} $u = ${item.name} ($color)\n")
                    }
                    if (item.q3 > 0) {
                        val color = item.c3 ?: item.wireColor ?: "أخضر مخطط بأصفر"
                        sb.append("- ${item.q3} $u = ${item.name} ($color)\n")
                    }
                } else {
                    if (item.quantity > 0) {
                        val colorText = if (!item.wireColor.isNullOrBlank()) " (${item.wireColor})" else ""
                        sb.append("- ${item.quantity} $u = ${item.name}$colorText\n")
                    }
                }
            }
            sb.append("\n")
        }

        sb.append("══════════════════════\n")
        sb.append("تحياتي، [$tech$phoneSuffix]\n")

        return sb.toString()
    }
}
