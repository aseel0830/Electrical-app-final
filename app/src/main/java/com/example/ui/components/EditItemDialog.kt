package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.local.ItemEntity
import com.example.util.ImageStorageHelper
import com.example.util.WireColor
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditItemDialog(
    item: ItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (ItemEntity) -> Unit
) {
    val context = LocalContext.current

    var itemName by remember { mutableStateOf(item.name) }
    var itemUnit by remember { mutableStateOf(item.unit.ifBlank { "حبة" }) }
    val standardUnits = listOf("حبة", "لفة", "AWG", "كرتون", "مم٢", "متر")

    var selectedImageUri by remember { mutableStateOf<Uri?>(item.imageUri?.let { Uri.parse(it) }) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var selectedModeIndex by remember {
        mutableIntStateOf(if (item.quantityMode == "TRIPLE_COLOR") 1 else 0)
    }

    var singleQty by remember { mutableIntStateOf(item.quantity) }

    var q1 by remember { mutableIntStateOf(item.q1) }
    var q2 by remember { mutableIntStateOf(item.q2) }
    var q3 by remember { mutableIntStateOf(item.q3) }

    var color1 by remember {
        mutableStateOf(WireColor.entries.firstOrNull { it.labelAr == item.c1 } ?: WireColor.RED)
    }
    var color2 by remember {
        mutableStateOf(WireColor.entries.firstOrNull { it.labelAr == item.c2 } ?: WireColor.BLUE)
    }
    var color3 by remember {
        mutableStateOf(WireColor.entries.firstOrNull { it.labelAr == item.c3 } ?: WireColor.GREEN_YELLOW)
    }

    var showExactInputDialog by remember { mutableStateOf(false) }
    var editingTarget by remember { mutableIntStateOf(0) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val persistentPath = ImageStorageHelper.saveImageToInternalStorage(context, uri)
            selectedImageUri = Uri.parse(persistentPath)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            val persistentPath = ImageStorageHelper.saveImageToInternalStorage(context, tempCameraUri!!)
            selectedImageUri = Uri.parse(persistentPath)
        }
    }

    fun launchCamera(context: Context) {
        try {
            val photoFile = File(context.cacheDir, "camera_edit_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("edit_item_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
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
                            text = "تعديل بيانات العنصر ✏️",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "النافذة: ${item.stageName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image Section
                Text(
                    text = "صورة الصنف:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "صورة العنصر",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = "بدون\nصورة",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { launchCamera(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("التقاط صورة جديدة", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تغيير من الاستوديو", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item Name
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("إسم العنصر *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_item_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Units selection
                Text(
                    text = "الوحدة:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    standardUnits.forEach { u ->
                        FilterChip(
                            selected = itemUnit == u,
                            onClick = { itemUnit = u },
                            label = { Text(u, fontSize = 12.sp, fontWeight = if (itemUnit == u) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = itemUnit,
                    onValueChange = { itemUnit = it },
                    label = { Text("مربع كتابة الوحدة يدوياً") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Mode Selection Tabs
                Text(
                    text = "طريقة تحديد الكمية:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                TabRow(
                    selectedTabIndex = selectedModeIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedModeIndex == 0,
                        onClick = { selectedModeIndex = 0 },
                        text = {
                            Text(
                                "الخيار الأول (+ 0 -)",
                                fontWeight = if (selectedModeIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedModeIndex == 1,
                        onClick = { selectedModeIndex = 1 },
                        text = {
                            Text(
                                "الخيار الثاني (٣ خانات + ألوان)",
                                fontWeight = if (selectedModeIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedModeIndex == 0) {
                    // Option 1
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "تحديد الكمية (اضغط على المربع للكتابة بالأرقام):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { if (singleQty > 0) singleQty-- },
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Remove, contentDescription = "تنقيص")
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                        .clickable {
                                            editingTarget = 0
                                            showExactInputDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$singleQty",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { singleQty++ },
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Add, contentDescription = "زيادة", tint = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Option 2
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        BoxColorCounterRow(
                            boxNumber = 1,
                            title = "الخانة الأولى (1)",
                            selectedColor = color1,
                            onColorSelected = { color1 = it },
                            quantity = q1,
                            onIncrement = { q1++ },
                            onDecrement = { if (q1 > 0) q1-- },
                            onNumberClick = {
                                editingTarget = 1
                                showExactInputDialog = true
                            }
                        )

                        BoxColorCounterRow(
                            boxNumber = 2,
                            title = "الخانة الثانية (2)",
                            selectedColor = color2,
                            onColorSelected = { color2 = it },
                            quantity = q2,
                            onIncrement = { q2++ },
                            onDecrement = { if (q2 > 0) q2-- },
                            onNumberClick = {
                                editingTarget = 2
                                showExactInputDialog = true
                            }
                        )

                        BoxColorCounterRow(
                            boxNumber = 3,
                            title = "الخانة الثالثة (3)",
                            selectedColor = color3,
                            onColorSelected = { color3 = it },
                            quantity = q3,
                            onIncrement = { q3++ },
                            onDecrement = { if (q3 > 0) q3-- },
                            onNumberClick = {
                                editingTarget = 3
                                showExactInputDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save button
                Button(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            val updatedItem = item.copy(
                                name = itemName.trim(),
                                imageUri = selectedImageUri?.toString(),
                                quantityMode = if (selectedModeIndex == 0) "SINGLE" else "TRIPLE_COLOR",
                                quantity = singleQty,
                                q1 = q1,
                                q2 = q2,
                                q3 = q3,
                                wireColor = if (selectedModeIndex == 1) color1.labelAr else item.wireColor,
                                c1 = if (selectedModeIndex == 1) color1.labelAr else null,
                                c2 = if (selectedModeIndex == 1) color2.labelAr else null,
                                c3 = if (selectedModeIndex == 1) color3.labelAr else null,
                                unit = itemUnit.trim().ifBlank { "حبة" }
                            )
                            onConfirm(updatedItem)
                        }
                    },
                    enabled = itemName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_edit_item_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "حفظ التعديلات على العنصر",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    if (showExactInputDialog) {
        val currentVal = when (editingTarget) {
            0 -> singleQty
            1 -> q1
            2 -> q2
            3 -> q3
            else -> 0
        }
        QuantityInputDialog(
            itemName = itemName.ifBlank { "العنصر" },
            initialQuantity = currentVal,
            label = "اكتب الكمية بالأرقام",
            onDismiss = { showExactInputDialog = false },
            onConfirm = { typedVal ->
                when (editingTarget) {
                    0 -> singleQty = typedVal
                    1 -> q1 = typedVal
                    2 -> q2 = typedVal
                    3 -> q3 = typedVal
                }
                showExactInputDialog = false
            }
        )
    }
}
