package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.ItemEntity
import com.example.data.local.ProjectEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    data class FlatPdfItem(
        val indexNumber: Int,
        val itemName: String,
        val imageUri: String?,
        val quantityText: String,
        val colorName: String?,
        val colorInt: Int?,
        val unitText: String
    )

    private fun toArabicIndic(number: Int): String {
        val arabicDigits = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
        return number.toString().map { char ->
            if (char in '0'..'9') arabicDigits[char - '0'] else char.toString()
        }.joinToString("")
    }

    fun generateMaterialsPdf(
        context: Context,
        project: ProjectEntity?,
        technicianName: String = "أصيل المليكي",
        technicianPhone: String = "",
        itemsByStage: Map<String, List<ItemEntity>>
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width (points)
        val pageHeight = 842 // A4 standard height (points)

        val tech = technicianName.trim().ifBlank { "أصيل المليكي" }
        val phoneSuffix = if (technicianPhone.isNotBlank()) " - $technicianPhone" else ""
        val client = project?.clientName?.trim()?.ifBlank { "عام" } ?: "عام"

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        var y = 35f

        fun drawHeader() {
            // Header background banner
            val bgRect = RectF(20f, y - 15f, (pageWidth - 20).toFloat(), y + 55f)
            val bgPaint = Paint().apply { color = Color.rgb(15, 23, 42) } // Dark slate
            canvas.drawRoundRect(bgRect, 8f, 8f, bgPaint)

            // Technician title
            val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(245, 158, 11)
                textSize = 15f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("فني تمديدات الكهرباء المنزلية [$tech$phoneSuffix]", (pageWidth - 35).toFloat(), y + 12f, goldPaint)

            val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 11f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("⚡ كشف حصر وتوريد المواد الكهربائية", (pageWidth - 35).toFloat(), y + 34f, whitePaint)

            val leftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.LTGRAY
                textSize = 10f
                textAlign = Paint.Align.LEFT
            }
            val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
            canvas.drawText("التاريخ: $dateStr", 35f, y + 14f, leftPaint)
            // As specifically required: client name
            canvas.drawText("العميل: $client", 35f, y + 34f, leftPaint)

            y += 85f
        }

        fun drawTableHeader() {
            val thBox = RectF(20f, y - 10f, (pageWidth - 20).toFloat(), y + 16f)
            val thBg = Paint().apply { color = Color.rgb(241, 245, 249) }
            canvas.drawRoundRect(thBox, 4f, 4f, thBg)

            val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(51, 65, 85)
                textSize = 11f
                isFakeBoldText = true
            }

            // Columns (RTL):
            // 1. الرقم
            thPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("الرقم", 555f, y + 6f, thPaint)

            // 2. الصورة
            canvas.drawText("الصورة", 500f, y + 6f, thPaint)

            // 3. اسم العنصر المطلوب
            thPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("اسم العنصر المطلوب", 460f, y + 6f, thPaint)

            // 4. اللون المطلوب
            thPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("اللون المطلوب", 215f, y + 6f, thPaint)

            // 5. الكمية والوحدة
            canvas.drawText("الكمية والوحدة", 85f, y + 6f, thPaint)

            y += 24f
        }

        drawHeader()
        drawTableHeader()

        // Unpack all items sequentially without stage grouping!
        val flatRows = mutableListOf<FlatPdfItem>()
        var counter = 1

        for ((_, items) in itemsByStage) {
            for (item in items) {
                if (!item.hasQuantity()) continue

                if (item.quantityMode == "TRIPLE_COLOR") {
                    var addedAny = false
                    if (item.q1 > 0) {
                        flatRows.add(
                            FlatPdfItem(
                                indexNumber = counter++,
                                itemName = item.name,
                                imageUri = item.imageUri,
                                quantityText = "${item.q1}",
                                colorName = item.c1 ?: "أحمر",
                                colorInt = getColorInt(item.c1 ?: "أحمر"),
                                unitText = item.unit.ifBlank { "لفة" }
                            )
                        )
                        addedAny = true
                    }
                    if (item.q2 > 0) {
                        flatRows.add(
                            FlatPdfItem(
                                indexNumber = counter++,
                                itemName = item.name,
                                imageUri = item.imageUri,
                                quantityText = "${item.q2}",
                                colorName = item.c2 ?: "أزرق",
                                colorInt = getColorInt(item.c2 ?: "أزرق"),
                                unitText = item.unit.ifBlank { "لفة" }
                            )
                        )
                        addedAny = true
                    }
                    if (item.q3 > 0) {
                        flatRows.add(
                            FlatPdfItem(
                                indexNumber = counter++,
                                itemName = item.name,
                                imageUri = item.imageUri,
                                quantityText = "${item.q3}",
                                colorName = item.c3 ?: "أخضر مخطط بأصفر",
                                colorInt = getColorInt(item.c3 ?: "أخضر مخطط بأصفر"),
                                unitText = item.unit.ifBlank { "لفة" }
                            )
                        )
                        addedAny = true
                    }
                    if (!addedAny && item.quantity > 0) {
                        flatRows.add(
                            FlatPdfItem(
                                indexNumber = counter++,
                                itemName = item.name,
                                imageUri = item.imageUri,
                                quantityText = "${item.quantity}",
                                colorName = item.wireColor,
                                colorInt = item.wireColor?.let { getColorInt(it) },
                                unitText = item.unit.ifBlank { "لفة" }
                            )
                        )
                    }
                } else {
                    flatRows.add(
                        FlatPdfItem(
                            indexNumber = counter++,
                            itemName = item.name,
                            imageUri = item.imageUri,
                            quantityText = "${item.quantity}",
                            colorName = item.wireColor,
                            colorInt = item.wireColor?.let { getColorInt(it) },
                            unitText = item.unit.ifBlank { "حبة" }
                        )
                    )
                }
            }
        }

        if (flatRows.isEmpty()) {
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 14f
            paint.color = Color.GRAY
            canvas.drawText("لا توجد عناصر محددة الكمية حالياً في هذا الكشف", (pageWidth / 2).toFloat(), y + 80f, paint)
        } else {
            val rowHeight = 62f // Generous height for large image (52pt) and readable table row
            val linePaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 1f
            }

            for (row in flatRows) {
                if (y + rowHeight > pageHeight - 50) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 35f
                    drawHeader()
                    drawTableHeader()
                }

                // Alternating row background
                if (row.indexNumber % 2 == 0) {
                    val altBox = RectF(20f, y, (pageWidth - 20).toFloat(), y + rowHeight)
                    val altBg = Paint().apply { color = Color.rgb(248, 250, 252) }
                    canvas.drawRect(altBox, altBg)
                }

                // 1. Number column (Far right: ١، ٢، ٣، ٤، ٥...)
                val numStr = toArabicIndic(row.indexNumber)
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 14f
                textPaint.isFakeBoldText = true
                textPaint.color = Color.rgb(30, 41, 59)
                canvas.drawText(numStr, 555f, y + 36f, textPaint)

                // 2. Large Image (52x52 pt)
                val imgSize = 52f
                val imgLeft = 475f
                val imgTop = y + 5f
                var drewImage = false

                if (!row.imageUri.isNullOrBlank()) {
                    try {
                        val bitmap = decodeBitmapSafely(context, row.imageUri)
                        if (bitmap != null) {
                            val scaled = Bitmap.createScaledBitmap(bitmap, imgSize.toInt(), imgSize.toInt(), true)
                            canvas.drawBitmap(scaled, imgLeft, imgTop, paint)
                            drewImage = true
                        }
                    } catch (_: Exception) {}
                }

                if (!drewImage) {
                    val iconBg = Paint().apply { color = Color.rgb(241, 245, 249) }
                    canvas.drawRoundRect(RectF(imgLeft, imgTop, imgLeft + imgSize, imgTop + imgSize), 6f, 6f, iconBg)
                    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.rgb(217, 119, 6)
                        textSize = 18f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("⚡", imgLeft + imgSize / 2f, imgTop + imgSize / 2f + 6f, iconPaint)
                }

                // Border around the photo
                val photoBorderPaint = Paint().apply {
                    color = Color.rgb(203, 213, 225)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                canvas.drawRoundRect(RectF(imgLeft, imgTop, imgLeft + imgSize, imgTop + imgSize), 6f, 6f, photoBorderPaint)

                // 3. Item Name
                textPaint.isFakeBoldText = true
                textPaint.textSize = 12f
                textPaint.color = Color.rgb(15, 23, 42)
                textPaint.textAlign = Paint.Align.RIGHT

                val maxChars = 32
                val nameText = if (row.itemName.length > maxChars) row.itemName.take(maxChars - 3) + "..." else row.itemName
                canvas.drawText(nameText, 460f, y + 35f, textPaint)

                // 4. Color column
                if (!row.colorName.isNullOrBlank()) {
                    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = row.colorInt ?: Color.BLACK
                        style = Paint.Style.FILL
                    }
                    val dotBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.GRAY
                        style = Paint.Style.STROKE
                        strokeWidth = 0.5f
                    }
                    canvas.drawCircle(250f, y + 31f, 6f, dotPaint)
                    canvas.drawCircle(250f, y + 31f, 6f, dotBorder)

                    textPaint.isFakeBoldText = true
                    textPaint.textSize = 11f
                    textPaint.color = Color.rgb(51, 65, 85)
                    textPaint.textAlign = Paint.Align.RIGHT
                    canvas.drawText(row.colorName, 240f, y + 35f, textPaint)
                } else {
                    textPaint.isFakeBoldText = false
                    textPaint.textSize = 11f
                    textPaint.color = Color.LTGRAY
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText("-", 215f, y + 35f, textPaint)
                }

                // 5. Quantity and Unit
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.isFakeBoldText = true
                textPaint.textSize = 14f
                textPaint.color = Color.rgb(217, 119, 6)
                canvas.drawText("${row.quantityText} ${row.unitText}", 85f, y + 35f, textPaint)

                // Row underline
                canvas.drawLine(20f, y + rowHeight, (pageWidth - 20).toFloat(), y + rowHeight, linePaint)
                y += rowHeight
            }
        }

        // Footer note
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "تحياتي، [$tech$phoneSuffix]",
            (pageWidth / 2).toFloat(),
            (pageHeight - 20).toFloat(),
            footerPaint
        )

        pdfDocument.finishPage(page)

        return try {
            val fileDir = File(context.cacheDir, "pdf_reports")
            fileDir.mkdirs()
            val safeClient = client.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
            // Exact file naming requirement: "مشروع [اسم العميل].pdf"
            val fileName = "مشروع $safeClient.pdf"
            val file = File(fileDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.flush()
            outputStream.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun decodeBitmapSafely(context: Context, pathOrUri: String): Bitmap? {
        val file = File(pathOrUri)
        if (file.exists() && file.isFile) {
            return BitmapFactory.decodeFile(file.absolutePath)
        }
        return try {
            val uri = Uri.parse(pathOrUri)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getColorInt(colorName: String): Int {
        return when {
            colorName.contains("أحمر") -> Color.rgb(220, 38, 38)
            colorName.contains("أسود") -> Color.rgb(15, 23, 42)
            colorName.contains("أصفر") && !colorName.contains("مخطط") -> Color.rgb(234, 179, 8)
            colorName.contains("أخضر") && !colorName.contains("أصفر") -> Color.rgb(16, 185, 129)
            colorName.contains("أزرق") -> Color.rgb(37, 99, 235)
            colorName.contains("مخطط") -> Color.rgb(101, 163, 13)
            else -> Color.GRAY
        }
    }
}
