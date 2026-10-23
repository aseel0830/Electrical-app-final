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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.util.WireColor
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddItemDialog(
    stageName: String,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        imageUri: String?,
        quantityMode: String,
        quantity: Int,
        q1: Int,
        q2: Int,
        q3: Int,
        wireColor: String?,
        c1: String?,
        c2: String?,
        c3: String?,
        unit: String
    ) -> Unit
) {
    val context = LocalContext.current

    var itemName by remember { mutableStateOf("") }
    // Units specified by user: [حبة-لفة-AWG-كرتون-مم٢-متر-]
    var itemUnit by remember { mutableStateOf("حبة") }
    val standardUnits = listOf("حبة", "لفة", "AWG", "كرتون", "مم٢", "متر")

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Mode: 0 for Option 1 (Single Counter), 1 for Option 2 (3 Counters + Color for each box)
    var selectedModeIndex by remember {
        mutableIntStateOf(if (stageName.contains("سلك") || stageName.contains("أسلاك")) 1 else 0)
    }

    // Option 1 quantity
    var singleQty by remember { mutableIntStateOf(0) }

    // Option 2: 3 boxes each with its own quantity and its own color
    var q1 by remember { mutableIntStateOf(0) }
    var q2 by remember { mutableIntStateOf(0) }
    var q3 by remember { mutableIntStateOf(0) }

    var color1 by remember { mutableStateOf(WireColor.RED) }
    var color2 by remember { mutableStateOf(WireColor.BLUE) }
    var color3 by remember { mutableStateOf(WireColor.GREEN_YELLOW) }

    // Exact input dialog
    var showExactInputDialog by remember { mutableStateOf(false) }
    var editingTarget by remember { mutableIntStateOf(0) } // 0 for single, 1/2/3 for triple

    // Gallery Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val persistentPath = com.example.util.ImageStorageHelper.saveImageToInternalStorage(context, uri)
            selectedImageUri = Uri.parse(persistentPath)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            val persistentPath = com.example.util.ImageStorageHelper.saveImageToInternalStorage(context, tempCameraUri!!)
            selectedImageUri = Uri.parse(persistentPath)
        }
    }

    fun launchCamera(context: Context) {
        try {
            val photoFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
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
                .testTag("add_item_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إضافة عنصر جديد ⚡",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "النافذة: $stageName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Photo selector section (Gallery or Camera)
                Text(
                    text = "صورة الصنف المطلوب:",
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
                    // Preview box
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

                    // Camera & Gallery action buttons
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
                                .testTag("camera_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح الكاميرا للتصوير", fontSize = 12.sp)
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
                                .testTag("gallery_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اختيار من الاستوديو", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item Name field
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("إسم العنصر *") },
                    placeholder = { Text("مثال: خراطيم سقف، سلك 2.5 مم، مفتاح...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Unit selection section: [حبة-لفة-AWG-كرتون-مم٢-متر-] + مربع لادخال النص للوحدة يدويا
                Text(
                    text = "الوحدة (اختر من القائمة أو اكتب يدوياً):",
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

                // Manual custom unit text box
                OutlinedTextField(
                    value = itemUnit,
                    onValueChange = { itemUnit = it },
                    label = { Text("مربع كتابة الوحدة يدوياً") },
                    placeholder = { Text("اكتب الوحدة هنا (مثلاً: لفة، حبة، بايب، رول...)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_unit_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Mode Selection Tabs (Option 1 vs Option 2)
                Text(
                    text = "طريقة إضافة الكمية:",
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
                    // Option 1: Single counter (+ 0 -)
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
                                // (-)
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

                                // (0) Tappable box
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

                                // (+)
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
                    // Option 2: 3 boxes (+ 0 -), EACH with its own Wire Color selector:
                    // (أحمر - أسود - أصفر - أخضر - أزرق - أخضر مخطط بأصفر)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "٣ خانات قياس، مع تحديد اللون الخاص بكل خانة:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Box 1 Section
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

                        // Box 2 Section
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

                        // Box 3 Section
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

                Spacer(modifier = Modifier.height(28.dp))

                // Submit / Add Button - elevated with extra bottom clearance
                Button(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            onConfirm(
                                itemName.trim(),
                                selectedImageUri?.toString(),
                                if (selectedModeIndex == 0) "SINGLE" else "TRIPLE_COLOR",
                                singleQty,
                                q1,
                                q2,
                                q3,
                                if (selectedModeIndex == 1) color1.labelAr else null,
                                if (selectedModeIndex == 1) color1.labelAr else null,
                                if (selectedModeIndex == 1) color2.labelAr else null,
                                if (selectedModeIndex == 1) color3.labelAr else null,
                                itemUnit.trim().ifBlank { "حبة" }
                            )
                        }
                    },
                    enabled = itemName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_add_item_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إضافة وتثبيت الصنف في النافذة",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Generous bottom clearance to stay comfortably above soft keys / navigation bars
                Spacer(modifier = Modifier.height(48.dp))
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
            itemName = itemName.ifBlank { "الصنف الجديد" },
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BoxColorCounterRow(
    boxNumber: Int,
    title: String,
    selectedColor: WireColor,
    onColorSelected: (WireColor) -> Unit,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onNumberClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header with title and active color badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Active color indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ColorCircleIndicator(color = selectedColor, size = 10)
                        Text(
                            text = selectedColor.labelAr,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6 Wire color options: أحمر - أسود - أصفر - أخضر - أزرق - أخضر مخطط بأصفر
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                WireColor.entries.forEach { wc ->
                    val isSelected = selectedColor == wc
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) {
                            BorderStrokeModifier(MaterialTheme.colorScheme.primary)
                        } else CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onColorSelected(wc) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ColorCircleIndicator(color = wc, size = 12)
                            Text(
                                text = wc.labelAr,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Counter (+ 0 -)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الكمية:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // (-)
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDecrement() },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Remove, contentDescription = "تنقيص", modifier = Modifier.size(16.dp))
                        }
                    }

                    // Number (0)
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (quantity > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                1.dp,
                                if (quantity > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onNumberClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$quantity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (quantity > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // (+)
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onIncrement() },
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "زيادة",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorCircleIndicator(color: WireColor, size: Int = 12) {
    if (color.secondaryColor != null) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(color.primaryColor, color.secondaryColor)))
                .border(0.5.dp, Color.Gray, CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(color.primaryColor)
                .border(0.5.dp, Color.Gray, CircleShape)
        )
    }
}

@Composable
private fun BorderStrokeModifier(color: Color) = CardDefaults.outlinedCardBorder().copy(
    brush = Brush.linearGradient(listOf(color, color))
)
