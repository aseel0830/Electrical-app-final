package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    fun copyToClipboard(context: Context, text: String, label: String = "كشف مواد الكهرباء") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ كشف الحصر بنجاح 📋", Toast.LENGTH_SHORT).show()
    }

    fun shareToWhatsApp(context: Context, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // If WhatsApp is not installed, open standard Android share sheet
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "إرسال كشف المواد عبر..."))
        }
    }

    fun sharePdf(context: Context, pdfUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة تقرير PDF مع العميل..."))
    }

    fun exportBackupJson(context: Context, jsonString: String): Uri? {
        return try {
            val fileDir = File(context.cacheDir, "backups")
            fileDir.mkdirs()
            val file = File(fileDir, "aseel_backup_${System.currentTimeMillis()}.json")
            FileOutputStream(file).use { it.write(jsonString.toByteArray()) }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareBackup(context: Context, backupUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, backupUri)
            putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية - أصيل المليكي")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "حفظ النسخة الاحتياطية سحابياً أو مشاركتها..."))
    }
}
