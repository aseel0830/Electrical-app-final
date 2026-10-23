package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.local.ItemEntity
import com.example.data.local.StageEntity
import com.example.ui.components.AddItemDialog
import com.example.ui.components.AddStageDialog
import com.example.ui.components.CloudSyncDialog
import com.example.ui.components.EditItemDialog
import com.example.ui.components.EditStageDialog
import com.example.ui.components.ItemCard
import com.example.ui.components.ProjectsDialog
import com.example.ui.components.SummaryBottomSheet
import com.example.ui.components.ViewImageDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stages by viewModel.stages.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val selectedStageName by viewModel.selectedStageName.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterMode by viewModel.filterMode.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val displayedItems by viewModel.displayedItems.collectAsStateWithLifecycle()
    val totalSelectedCount by viewModel.totalSelectedCount.collectAsStateWithLifecycle()
    val technicianName by viewModel.technicianName.collectAsStateWithLifecycle()
    val technicianPhone by viewModel.technicianPhone.collectAsStateWithLifecycle()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddStageDialog by remember { mutableStateOf(false) }
    var showSummarySheet by remember { mutableStateOf(false) }
    var showProjectsDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }

    var itemToEdit by remember { mutableStateOf<ItemEntity?>(null) }
    var itemForImageView by remember { mutableStateOf<ItemEntity?>(null) }
    var stageToEdit by remember { mutableStateOf<StageEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = technicianName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "فني تمديدات كهرباء منزلية (تأسيس وتشطيب)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Dark/Light Mode toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تبديل المظهر النهاري/الليلي",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Google Cloud Storage & Sign-In action
                    IconButton(
                        onClick = { showCloudSyncDialog = true },
                        modifier = Modifier.testTag("top_cloud_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "جوجل كلاود والنسخ الاحتياطي",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            // Button to open Material Inventory Tally Sheet
            FloatingActionButton(
                onClick = { showSummarySheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("open_summary_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ListAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "كشف الحصر ($totalSelectedCount)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Active Project Banner with quick switch/save
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showProjectsDialog = true }
                    .testTag("project_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الملف الحالي: ${currentProject?.name ?: "مشروع جديد"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "إدارة الملفات 📂",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Quick Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("بحث سريع عن صنف أو مادة كهربائية...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fast_search_bar")
                )
            }

            // Filter Chips Bar (الكل, المحدد فقط, غير المحدد + تصفير)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = filterMode == FilterMode.ALL,
                        onClick = { viewModel.setFilterMode(FilterMode.ALL) },
                        label = { Text(FilterMode.ALL.label, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_all")
                    )
                }

                item {
                    FilterChip(
                        selected = filterMode == FilterMode.SELECTED_ONLY,
                        onClick = { viewModel.setFilterMode(FilterMode.SELECTED_ONLY) },
                        label = {
                            Text(
                                "${FilterMode.SELECTED_ONLY.label} ($totalSelectedCount)",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("filter_selected_only")
                    )
                }

                item {
                    FilterChip(
                        selected = filterMode == FilterMode.UNSELECTED_ONLY,
                        onClick = { viewModel.setFilterMode(FilterMode.UNSELECTED_ONLY) },
                        label = { Text(FilterMode.UNSELECTED_ONLY.label, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("filter_unselected_only")
                    )
                }

                if (totalSelectedCount > 0) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showResetConfirmDialog = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("reset_quantities_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تصفير",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Stage Navigation Tabs ("النوافذ")
            if (searchQuery.isBlank()) {
                val selectedIndex = stages.indexOfFirst { it.name == selectedStageName }.coerceAtLeast(0)
                ScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    indicator = { tabPositions ->
                        if (selectedIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    stages.forEach { stage ->
                        val isSelected = stage.name == selectedStageName
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedStage(stage.name) },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (!stage.imageUri.isNullOrBlank()) {
                                        AsyncImage(
                                            model = stage.imageUri,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Text(
                                        text = stage.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier.testTag("stage_tab_${stage.name}")
                        )
                    }

                    // Button to add another stage/window ("إضافة نافذة جديدة")
                    Tab(
                        selected = false,
                        onClick = { showAddStageDialog = true },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "إضافة نافذة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.testTag("add_stage_tab_button")
                    )
                }

                // Stage actions bar: Edit or delete current window (Requirement 5)
                val currentStageEntity = stages.firstOrNull { it.name == selectedStageName }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!currentStageEntity?.imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentStageEntity!!.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = "نافذة: $selectedStageName",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${displayedItems.size} صنف)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (currentStageEntity != null) {
                            TextButton(
                                onClick = { stageToEdit = currentStageEntity },
                                modifier = Modifier.testTag("open_stage_edit_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تعديل / حذف النافذة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Search Mode Header
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "نتائج البحث في جميع النوافذ عن: \"$searchQuery\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (displayedItems.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "لم يتم العثور على عناصر تطابق البحث" else "لا توجد عناصر مضافة في هذه النافذة بعد",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "اضغط على زر (+ إضافة عنصر) بالأسفل لتسجيل صنف جديد مع صورته وكميته",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(displayedItems, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            onIncrementSingle = { viewModel.incrementSingleQuantity(item) },
                            onDecrementSingle = { viewModel.decrementSingleQuantity(item) },
                            onSetExactSingle = { newQty -> viewModel.setExactSingleQuantity(item, newQty) },
                            onIncrementTriple = { idx -> viewModel.incrementTripleQuantity(item, idx) },
                            onDecrementTriple = { idx -> viewModel.decrementTripleQuantity(item, idx) },
                            onSetExactTriple = { idx, newQty -> viewModel.setExactTripleQuantity(item, idx, newQty) },
                            onUpdateBoxColor = { boxIdx, newColor -> viewModel.updateBoxColor(item, boxIdx, newColor) },
                            onEdit = { itemToEdit = item },
                            onOpenImage = { itemForImageView = it },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }

                // Add Item Section at the bottom of the window
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { showAddItemDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_item_bottom_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+ إضافة عنصر إلى نافذة ($selectedStageName)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddItemDialog(
            stageName = selectedStageName,
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, imageUri, quantityMode, quantity, q1, q2, q3, wireColor, c1, c2, c3, unit ->
                viewModel.addNewItem(
                    name = name,
                    stageName = selectedStageName,
                    imageUri = imageUri,
                    quantityMode = quantityMode,
                    quantity = quantity,
                    q1 = q1,
                    q2 = q2,
                    q3 = q3,
                    wireColor = wireColor,
                    c1 = c1,
                    c2 = c2,
                    c3 = c3,
                    unit = unit
                )
                showAddItemDialog = false
            }
        )
    }

    // Edit Item Dialog (Requirement 4)
    if (itemToEdit != null) {
        EditItemDialog(
            item = itemToEdit!!,
            onDismiss = { itemToEdit = null },
            onConfirm = { updated ->
                viewModel.updateItem(updated)
                itemToEdit = null
            }
        )
    }

    // View Large Image Dialog (Requirement 6)
    if (itemForImageView != null) {
        ViewImageDialog(
            itemName = itemForImageView!!.name,
            imageUri = itemForImageView!!.imageUri,
            stageName = itemForImageView!!.stageName,
            onDismiss = { itemForImageView = null }
        )
    }

    // Edit Stage Dialog (Requirement 5)
    if (stageToEdit != null) {
        EditStageDialog(
            stage = stageToEdit!!,
            itemCount = displayedItems.size,
            onDismiss = { stageToEdit = null },
            onUpdate = { newName, newImageUri ->
                viewModel.updateStage(stageToEdit!!, newName, newImageUri)
                stageToEdit = null
            },
            onDelete = {
                viewModel.deleteStage(stageToEdit!!)
                stageToEdit = null
            }
        )
    }

    // Add Stage Dialog
    if (showAddStageDialog) {
        AddStageDialog(
            onDismiss = { showAddStageDialog = false },
            onConfirm = { stageName, imageUri ->
                viewModel.addNewStage(stageName, imageUri)
                showAddStageDialog = false
            }
        )
    }

    // Summary Bottom Sheet
    if (showSummarySheet) {
        SummaryBottomSheet(
            project = currentProject,
            technicianName = technicianName,
            technicianPhone = technicianPhone,
            onUpdateTechnicianName = { viewModel.setTechnicianName(it) },
            onUpdateTechnicianPhone = { viewModel.setTechnicianPhone(it) },
            onUpdateClientName = { viewModel.updateClientName(it) },
            itemsByStage = viewModel.getItemsByStageMap(),
            onDismiss = { showSummarySheet = false }
        )
    }

    // Projects Dialog (Requirement 9: Transfer all modified windows and items)
    if (showProjectsDialog) {
        ProjectsDialog(
            projects = projects,
            currentProject = currentProject,
            onDismiss = { showProjectsDialog = false },
            onSaveNewProject = { name, client, notes, preserveQuantities ->
                viewModel.saveAsNewProject(name, client, notes, preserveQuantities)
                showProjectsDialog = false
            },
            onSwitchProject = { id ->
                viewModel.switchProject(id)
                showProjectsDialog = false
            },
            onDeleteProject = { proj ->
                viewModel.deleteProject(proj)
            },
            onExportBackup = {
                showCloudSyncDialog = true
            }
        )
    }

    // Google Cloud Sync Dialog (Requirement 2)
    if (showCloudSyncDialog) {
        CloudSyncDialog(
            cloudSyncService = viewModel.cloudSyncService,
            onDismiss = { showCloudSyncDialog = false }
        )
    }

    // Reset Quantities Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("تصفير الكميات", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من تصفير كافة الكميات المحددة لهذا المشروع؟ لن يتم حذف الأصناف أو النوافذ، فقط سيتم إعادة الكمية إلى 0.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetCurrentQuantities()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، تصفير", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
