package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ItemEntity
import com.example.data.local.ProjectEntity
import com.example.util.PdfGenerator
import com.example.util.ShareHelper
import com.example.util.SummaryFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryBottomSheet(
    project: ProjectEntity?,
    technicianName: String,
    technicianPhone: String,
    onUpdateTechnicianName: (String) -> Unit,
    onUpdateTechnicianPhone: (String) -> Unit,
    onUpdateClientName: (String) -> Unit,
    itemsByStage: Map<String, List<ItemEntity>>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentTechName by remember { mutableStateOf(technicianName) }
    var currentTechPhone by remember { mutableStateOf(technicianPhone) }
    var currentClientName by remember { mutableStateOf(project?.clientName ?: "") }

    val formattedSummaryText = remember(project, currentClientName, currentTechName, currentTechPhone, itemsByStage) {
        val tempProject = project?.copy(clientName = currentClientName) ?: ProjectEntity(name = "", clientName = currentClientName)
        SummaryFormatter.generateFormattedSummary(
            project = tempProject,
            technicianName = currentTechName,
            technicianPhone = currentTechPhone,
            itemsByStage = itemsByStage
        )
    }

    val totalSelectedItems = itemsByStage.values.flatten().count { it.hasQuantity() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("summary_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "كشف حصر وتوريد المواد 📋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "إجمالي المواد المحددة: $totalSelectedItems صنف",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_summary_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Configuration Card for Technician Name, Phone & Client Name
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Technician Name Input
                    OutlinedTextField(
                        value = currentTechName,
                        onValueChange = {
                            currentTechName = it
                            onUpdateTechnicianName(it)
                        },
                        label = { Text("اسم فني التمديدات") },
                        leadingIcon = {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Technician Phone Input (As requested: اضف خيار رقم الهاتف بعد اسم الفني بحيث استطيع كتابة الرقم)
                    OutlinedTextField(
                        value = currentTechPhone,
                        onValueChange = {
                            currentTechPhone = it
                            onUpdateTechnicianPhone(it)
                        },
                        label = { Text("رقم هاتف الفني (اختياري)") },
                        placeholder = { Text("مثال: 770000000") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_phone_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Client Name Input (Only client name appears in tally, no project name)
                    OutlinedTextField(
                        value = currentClientName,
                        onValueChange = {
                            currentClientName = it
                            onUpdateClientName(it)
                        },
                        label = { Text("اسم العميل (يوضع في كشف الحصر)") },
                        placeholder = { Text("مثال: أبو فهد، المهندس أحمد...") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_name_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row (Copy, WhatsApp, PDF)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy Button
                Button(
                    onClick = {
                        ShareHelper.copyToClipboard(context, formattedSummaryText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_summary_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نسخ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // WhatsApp Button
                Button(
                    onClick = {
                        ShareHelper.shareToWhatsApp(context, formattedSummaryText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("whatsapp_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("واتساب", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }

                // PDF Download Button
                OutlinedButton(
                    onClick = {
                        val tempProject = project?.copy(clientName = currentClientName) ?: ProjectEntity(name = "", clientName = currentClientName)
                        val pdfUri = PdfGenerator.generateMaterialsPdf(
                            context = context,
                            project = tempProject,
                            technicianName = currentTechName,
                            technicianPhone = currentTechPhone,
                            itemsByStage = itemsByStage
                        )
                        if (pdfUri != null) {
                            ShareHelper.sharePdf(context, pdfUri)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("pdf_download_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ملف PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display text box showing the exact formatted text
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = formattedSummaryText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("summary_text_content")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footnote
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 التنسيق يبدأ بالتحية، يليه اسم العميل، ثم حصر المواد متتالية بصيغة (العدد والوحدة = اسم العنصر واللون)، وتختم بالتوقيع ورقم الهاتف.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
