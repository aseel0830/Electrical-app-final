package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ItemEntity
import com.example.util.WireColor

@Composable
fun ItemCard(
    item: ItemEntity,
    onIncrementSingle: () -> Unit,
    onDecrementSingle: () -> Unit,
    onSetExactSingle: (Int) -> Unit,
    onIncrementTriple: (Int) -> Unit,
    onDecrementTriple: (Int) -> Unit,
    onSetExactTriple: (Int, Int) -> Unit,
    onUpdateBoxColor: ((boxIndex: Int, newColor: String) -> Unit)? = null,
    onEdit: () -> Unit,
    onOpenImage: (ItemEntity) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExactInputDialog by remember { mutableStateOf(false) }
    var selectedTripleIndex by remember { mutableIntStateOf(1) }
    var currentEditingInitialValue by remember { mutableIntStateOf(0) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var showColorPickerForBox by remember { mutableStateOf<Int?>(null) }

    val hasQty = item.hasQuantity()
    val borderColor = if (hasQty) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("item_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasQty) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(borderColor, borderColor))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hasQty) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Image, Title, Unit badge, Delete action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item Image (clickable to view large image)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { onOpenImage(item) }
                        .testTag("open_item_image_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = item.imageUri,
                            contentDescription = "عرض صورة ${item.name}",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Name and details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        // Unit badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = item.unit.ifBlank { "حبة" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Single Mode Wire Color badge if present
                        if (item.quantityMode != "TRIPLE_COLOR" && !item.wireColor.isNullOrBlank()) {
                            val wireEnum = WireColor.fromLabel(item.wireColor)
                            WireColorBadge(wireEnum = wireEnum, label = item.wireColor)
                        }
                    }
                }

                // Action buttons: Edit & Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل العنصر",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف العنصر",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quantity Control Rows
            if (item.quantityMode == "TRIPLE_COLOR") {
                // Second Option: 3 counter boxes (+ 0 -) with color selector for EACH box
                Text(
                    text = "٣ خانات (+ 0 -) مع لون مخصص لكل خانة (اضغط لتغيير اللون):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                val box1Color = item.c1 ?: item.wireColor ?: "أحمر"
                val box2Color = item.c2 ?: item.wireColor ?: "أزرق"
                val box3Color = item.c3 ?: item.wireColor ?: "أخضر مخطط بأصفر"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CounterBox(
                        title = "خانة 1",
                        colorName = box1Color,
                        value = item.q1,
                        onIncrement = { onIncrementTriple(1) },
                        onDecrement = { onDecrementTriple(1) },
                        onNumberClick = {
                            selectedTripleIndex = 1
                            currentEditingInitialValue = item.q1
                            showExactInputDialog = true
                        },
                        onColorClick = { showColorPickerForBox = 1 },
                        modifier = Modifier.weight(1f)
                    )

                    CounterBox(
                        title = "خانة 2",
                        colorName = box2Color,
                        value = item.q2,
                        onIncrement = { onIncrementTriple(2) },
                        onDecrement = { onDecrementTriple(2) },
                        onNumberClick = {
                            selectedTripleIndex = 2
                            currentEditingInitialValue = item.q2
                            showExactInputDialog = true
                        },
                        onColorClick = { showColorPickerForBox = 2 },
                        modifier = Modifier.weight(1f)
                    )

                    CounterBox(
                        title = "خانة 3",
                        colorName = box3Color,
                        value = item.q3,
                        onIncrement = { onIncrementTriple(3) },
                        onDecrement = { onDecrementTriple(3) },
                        onNumberClick = {
                            selectedTripleIndex = 3
                            currentEditingInitialValue = item.q3
                            showExactInputDialog = true
                        },
                        onColorClick = { showColorPickerForBox = 3 },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // First Option: Single counter (+ 0 -) with direct keypad on 0 click
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الكمية المطلوبة:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Decrement Button (-)
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onDecrementSingle() }
                                .testTag("decrement_${item.id}"),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "تنقيص",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Number display box (Tappable "0" or number to enter exact quantity via keypad)
                        Box(
                            modifier = Modifier
                                .size(width = 74.dp, height = 44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (item.quantity > 0) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (item.quantity > 0) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    currentEditingInitialValue = item.quantity
                                    showExactInputDialog = true
                                }
                                .testTag("quantity_box_${item.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${item.quantity}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.quantity > 0) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center
                            )
                        }

                        // Increment Button (+)
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onIncrementSingle() }
                                .testTag("increment_${item.id}"),
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "زيادة",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExactInputDialog) {
        QuantityInputDialog(
            itemName = item.name,
            initialQuantity = currentEditingInitialValue,
            label = if (item.quantityMode == "TRIPLE_COLOR") "الكمية للخانة رقم $selectedTripleIndex" else "الكمية المطلوبة",
            onDismiss = { showExactInputDialog = false },
            onConfirm = { newQty ->
                if (item.quantityMode == "TRIPLE_COLOR") {
                    onSetExactTriple(selectedTripleIndex, newQty)
                } else {
                    onSetExactSingle(newQty)
                }
                showExactInputDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "تأكيد حذف العنصر",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف \"${item.name}\" من هذه النافذة؟",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "حذف",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Color picker dialog for specific box
    showColorPickerForBox?.let { boxIdx ->
        BoxColorPickerDialog(
            boxNumber = boxIdx,
            onDismiss = { showColorPickerForBox = null },
            onColorChosen = { chosenColor ->
                onUpdateBoxColor?.invoke(boxIdx, chosenColor.labelAr)
                showColorPickerForBox = null
            }
        )
    }
}

@Composable
fun CounterBox(
    title: String,
    colorName: String? = null,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onNumberClick: () -> Unit,
    onColorClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val wireEnum = WireColor.fromLabel(colorName)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Box title
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Color badge for this box (Clickable to change color)
        if (!colorName.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .then(
                        if (onColorClick != null) Modifier.clickable { onColorClick() } else Modifier
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    if (wireEnum != null) {
                        ColorCircleIndicator(color = wireEnum, size = 9)
                    }
                    Text(
                        text = colorName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // (-)
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onDecrement() },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "تنقيص",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(3.dp))

            // Number
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (value > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = 1.dp,
                        color = if (value > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onNumberClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$value",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (value > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(3.dp))

            // (+)
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onIncrement() },
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "زيادة",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BoxColorPickerDialog(
    boxNumber: Int,
    onDismiss: () -> Unit,
    onColorChosen: (WireColor) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اختيار لون السلك للخانة ($boxNumber):",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    text = "اختر اللون المطلوب من الألوان الستة المعتمدة:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WireColor.entries.forEach { wc ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onColorChosen(wc) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                ColorCircleIndicator(color = wc, size = 14)
                                Text(
                                    text = wc.labelAr,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun WireColorBadge(
    wireEnum: WireColor?,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (wireEnum != null) {
                ColorCircleIndicator(color = wireEnum, size = 12)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
