package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ItemEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.StageEntity
import com.example.data.repository.ElectricalRepository
import com.example.util.SummaryFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class FilterMode(val label: String) {
    ALL("الكل"),
    SELECTED_ONLY("المحدد فقط"),
    UNSELECTED_ONLY("غير المحدد")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ElectricalRepository
    val cloudSyncService: com.example.data.cloud.CloudSyncService

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ElectricalRepository(database.electricalDao())
        cloudSyncService = com.example.data.cloud.CloudSyncService(application, database)
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    val projects: StateFlow<List<ProjectEntity>> = repository.projects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentProject: StateFlow<ProjectEntity?> = repository.currentProject.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Stages dynamically bound to current project
    val stages: StateFlow<List<StageEntity>> = currentProject.flatMapLatest { proj ->
        if (proj != null) {
            repository.getStagesForProject(proj.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedStageName = MutableStateFlow("تأسيس السقف")
    val selectedStageName: StateFlow<String> = _selectedStageName.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterMode = MutableStateFlow(FilterMode.ALL)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true) // Default to modern eye-comfort dark mode
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val prefs = application.getSharedPreferences("aseel_prefs", android.content.Context.MODE_PRIVATE)
    private val _technicianName = MutableStateFlow(prefs.getString("tech_name", "أصيل المليكي") ?: "أصيل المليكي")
    val technicianName: StateFlow<String> = _technicianName.asStateFlow()

    private val _technicianPhone = MutableStateFlow(prefs.getString("tech_phone", "") ?: "")
    val technicianPhone: StateFlow<String> = _technicianPhone.asStateFlow()

    fun setTechnicianName(name: String) {
        val cleanName = name.trim().ifBlank { "أصيل المليكي" }
        _technicianName.value = cleanName
        prefs.edit().putString("tech_name", cleanName).apply()
    }

    fun setTechnicianPhone(phone: String) {
        val cleanPhone = phone.trim()
        _technicianPhone.value = cleanPhone
        prefs.edit().putString("tech_phone", cleanPhone).apply()
    }

    fun updateClientName(newClientName: String) {
        val proj = currentProject.value ?: return
        viewModelScope.launch {
            repository.updateProject(proj.copy(clientName = newClientName.trim()))
        }
    }

    // Items for current project
    val allCurrentItems: StateFlow<List<ItemEntity>> = currentProject.flatMapLatest { project ->
        if (project != null) {
            repository.getItemsForProject(project.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered items for display in current stage or search
    val displayedItems: StateFlow<List<ItemEntity>> = combine(
        allCurrentItems,
        _selectedStageName,
        _searchQuery,
        _filterMode
    ) { items, currentStage, query, filter ->
        items.filter { item ->
            val matchesStage = if (query.isNotBlank()) true else item.stageName == currentStage
            val matchesQuery = query.isBlank() || item.name.contains(query, ignoreCase = true) || item.stageName.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                FilterMode.ALL -> true
                FilterMode.SELECTED_ONLY -> item.hasQuantity()
                FilterMode.UNSELECTED_ONLY -> !item.hasQuantity()
            }
            matchesStage && matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected count
    val totalSelectedCount: StateFlow<Int> = allCurrentItems.combine(flowOf(Unit)) { items, _ ->
        items.count { it.hasQuantity() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setSelectedStage(stageName: String) {
        _selectedStageName.value = stageName
        _searchQuery.value = "" // clear search when switching stages
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterMode(mode: FilterMode) {
        _filterMode.value = mode
    }

    fun incrementSingleQuantity(item: ItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item.copy(quantity = item.quantity + 1))
        }
    }

    fun decrementSingleQuantity(item: ItemEntity) {
        if (item.quantity > 0) {
            viewModelScope.launch {
                repository.updateItem(item.copy(quantity = item.quantity - 1))
            }
        }
    }

    fun setExactSingleQuantity(item: ItemEntity, newQuantity: Int) {
        val qty = if (newQuantity < 0) 0 else newQuantity
        viewModelScope.launch {
            repository.updateItem(item.copy(quantity = qty))
        }
    }

    fun incrementTripleQuantity(item: ItemEntity, index: Int) {
        viewModelScope.launch {
            val updated = when (index) {
                1 -> item.copy(q1 = item.q1 + 1)
                2 -> item.copy(q2 = item.q2 + 1)
                3 -> item.copy(q3 = item.q3 + 1)
                else -> item
            }
            repository.updateItem(updated)
        }
    }

    fun decrementTripleQuantity(item: ItemEntity, index: Int) {
        viewModelScope.launch {
            val updated = when (index) {
                1 -> if (item.q1 > 0) item.copy(q1 = item.q1 - 1) else item
                2 -> if (item.q2 > 0) item.copy(q2 = item.q2 - 1) else item
                3 -> if (item.q3 > 0) item.copy(q3 = item.q3 - 1) else item
                else -> item
            }
            repository.updateItem(updated)
        }
    }

    fun setExactTripleQuantity(item: ItemEntity, index: Int, newQuantity: Int) {
        val qty = if (newQuantity < 0) 0 else newQuantity
        viewModelScope.launch {
            val updated = when (index) {
                1 -> item.copy(q1 = qty)
                2 -> item.copy(q2 = qty)
                3 -> item.copy(q3 = qty)
                else -> item
            }
            repository.updateItem(updated)
        }
    }

    fun addNewItem(
        name: String,
        stageName: String,
        imageUri: String?,
        quantityMode: String,
        quantity: Int,
        q1: Int,
        q2: Int,
        q3: Int,
        wireColor: String?,
        c1: String? = null,
        c2: String? = null,
        c3: String? = null,
        unit: String
    ) {
        viewModelScope.launch {
            val project = currentProject.value ?: return@launch
            val newItem = ItemEntity(
                projectId = project.id,
                stageName = stageName,
                name = name.trim(),
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
                unit = unit.ifBlank { "حبة" }
            )
            repository.insertItem(newItem)
        }
    }

    fun updateBoxColor(item: ItemEntity, boxIndex: Int, newColor: String) {
        viewModelScope.launch {
            val updated = when (boxIndex) {
                1 -> item.copy(c1 = newColor)
                2 -> item.copy(c2 = newColor)
                3 -> item.copy(c3 = newColor)
                else -> item
            }
            repository.updateItem(updated)
        }
    }

    fun updateItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun addNewStage(name: String, imageUri: String? = null) {
        if (name.isBlank()) return
        val projId = currentProject.value?.id ?: 1L
        viewModelScope.launch {
            repository.insertStage(projectId = projId, name = name.trim(), imageUri = imageUri)
            _selectedStageName.value = name.trim()
        }
    }

    fun updateStage(stage: StageEntity, newName: String, newImageUri: String? = null) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateStage(stage, newName.trim(), newImageUri)
            if (_selectedStageName.value == stage.name) {
                _selectedStageName.value = newName.trim()
            }
        }
    }

    fun deleteStage(stage: StageEntity) {
        viewModelScope.launch {
            repository.deleteStage(stage)
            val remaining = stages.value.filter { it.id != stage.id }
            if (remaining.isNotEmpty()) {
                _selectedStageName.value = remaining.first().name
            }
        }
    }

    fun saveAsNewProject(projectName: String, clientName: String, notes: String, preserveQuantities: Boolean = true) {
        viewModelScope.launch {
            val sourceId = currentProject.value?.id
            val newId = repository.createProject(
                name = projectName.trim().ifBlank { "مشروع جديد" },
                clientName = clientName.trim(),
                notes = notes.trim(),
                sourceProjectId = sourceId,
                preserveQuantities = preserveQuantities
            )
            repository.switchProject(newId)
        }
    }

    fun switchProject(projectId: Long) {
        viewModelScope.launch {
            repository.switchProject(projectId)
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun resetCurrentQuantities() {
        val proj = currentProject.value ?: return
        viewModelScope.launch {
            repository.resetQuantitiesForProject(proj.id)
        }
    }

    fun getItemsByStageMap(): Map<String, List<ItemEntity>> {
        val items = allCurrentItems.value
        return items.groupBy { it.stageName }
    }

    fun getFormattedSummary(): String {
        return SummaryFormatter.generateFormattedSummary(
            project = currentProject.value,
            technicianName = _technicianName.value,
            technicianPhone = _technicianPhone.value,
            itemsByStage = getItemsByStageMap()
        )
    }

    suspend fun exportBackupJsonString(): String {
        val backupData = repository.getAllDataForBackup()
        val json = JSONObject()
        json.put("app", "أصيل المليكي")
        json.put("version", 3)
        json.put("timestamp", System.currentTimeMillis())

        val projectsArray = JSONArray()
        @Suppress("UNCHECKED_CAST")
        (backupData["projects"] as? List<ProjectEntity>)?.forEach { p ->
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("name", p.name)
            pObj.put("clientName", p.clientName)
            pObj.put("notes", p.notes)
            pObj.put("createdAt", p.createdAt)
            pObj.put("isCurrent", p.isCurrent)
            pObj.put("isBase", p.isBase)
            projectsArray.put(pObj)
        }
        json.put("projects", projectsArray)

        val stagesArray = JSONArray()
        @Suppress("UNCHECKED_CAST")
        (backupData["stages"] as? List<StageEntity>)?.forEach { s ->
            val sObj = JSONObject()
            sObj.put("id", s.id)
            sObj.put("projectId", s.projectId)
            sObj.put("name", s.name)
            sObj.put("imageUri", s.imageUri ?: "")
            sObj.put("isDefault", s.isDefault)
            sObj.put("orderIndex", s.orderIndex)
            stagesArray.put(sObj)
        }
        json.put("stages", stagesArray)

        val itemsArray = JSONArray()
        @Suppress("UNCHECKED_CAST")
        (backupData["items"] as? List<ItemEntity>)?.forEach { item ->
            val itemObj = JSONObject()
            itemObj.put("id", item.id)
            itemObj.put("projectId", item.projectId)
            itemObj.put("stageName", item.stageName)
            itemObj.put("name", item.name)
            itemObj.put("imageUri", item.imageUri ?: "")
            itemObj.put("quantityMode", item.quantityMode)
            itemObj.put("quantity", item.quantity)
            itemObj.put("q1", item.q1)
            itemObj.put("q2", item.q2)
            itemObj.put("q3", item.q3)
            itemObj.put("wireColor", item.wireColor ?: "")
            itemObj.put("c1", item.c1 ?: "")
            itemObj.put("c2", item.c2 ?: "")
            itemObj.put("c3", item.c3 ?: "")
            itemObj.put("unit", item.unit)
            itemsArray.put(itemObj)
        }
        json.put("items", itemsArray)

        return json.toString(2)
    }
}
