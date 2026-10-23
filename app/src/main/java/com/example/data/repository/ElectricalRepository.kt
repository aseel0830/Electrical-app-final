package com.example.data.repository

import com.example.data.local.ElectricalDao
import com.example.data.local.ItemEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.StageEntity
import kotlinx.coroutines.flow.Flow

class ElectricalRepository(private val dao: ElectricalDao) {

    val projects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val currentProject: Flow<ProjectEntity?> = dao.getCurrentProjectFlow()

    fun getStagesForProject(projectId: Long): Flow<List<StageEntity>> =
        dao.getStagesForProject(projectId)

    suspend fun getStagesForProjectSnapshot(projectId: Long): List<StageEntity> =
        dao.getStagesForProjectSnapshot(projectId)

    fun getItemsForProject(projectId: Long): Flow<List<ItemEntity>> =
        dao.getItemsForProject(projectId)

    suspend fun getItemsForProjectSnapshot(projectId: Long): List<ItemEntity> =
        dao.getItemsForProjectSnapshot(projectId)

    suspend fun initializeDefaultsIfNeeded() {
        val existingProjects = dao.getAllProjectsSnapshot()
        val currentProjectId: Long

        if (existingProjects.isEmpty()) {
            // Create "المشروع الأساسي" as the immutable base template
            val baseProject = ProjectEntity(
                name = "المشروع الأساسي",
                clientName = "عميل افتراضي",
                notes = "المشروع الأساسي الشامل - لا يمكن حذفه ويتم نسخ النوافذ والعناصر منه للمشاريع الجديدة",
                isCurrent = true,
                isBase = true
            )
            currentProjectId = dao.insertProject(baseProject)
            seedBaseStagesAndItems(currentProjectId)
        } else {
            val base = existingProjects.firstOrNull { it.isBase } ?: existingProjects.firstOrNull { it.name == "المشروع الأساسي" }
            if (base != null && !base.isBase) {
                dao.updateProject(base.copy(isBase = true))
            }
            val current = existingProjects.firstOrNull { it.isCurrent }
            currentProjectId = current?.id ?: existingProjects.first().id
            if (current == null) {
                dao.setProjectAsCurrent(currentProjectId)
            }

            // Verify if stages exist for this project
            val stageCount = dao.getStageCountForProject(currentProjectId)
            if (stageCount == 0) {
                seedBaseStagesAndItems(currentProjectId)
            }
        }
    }

    private suspend fun seedBaseStagesAndItems(projectId: Long) {
        // Stages requested by the user:
        // 1. تأسيس السقف
        // 2. تأسيس داخلي
        // 3. مفاتيح وأفياش (was التشطيبات)
        // 4. انارة وملحقات (new)
        // 5. ملحقات (new)
        // + الأسلاك (retained for wire color multi-box feature)
        val defaultStages = listOf(
            StageEntity(projectId = projectId, name = "تأسيس السقف", orderIndex = 0, isDefault = true),
            StageEntity(projectId = projectId, name = "تأسيس داخلي", orderIndex = 1, isDefault = true),
            StageEntity(projectId = projectId, name = "الأسلاك", orderIndex = 2, isDefault = true),
            StageEntity(projectId = projectId, name = "مفاتيح وأفياش", orderIndex = 3, isDefault = true),
            StageEntity(projectId = projectId, name = "انارة وملحقات", orderIndex = 4, isDefault = true),
            StageEntity(projectId = projectId, name = "ملحقات", orderIndex = 5, isDefault = true)
        )
        dao.insertStages(defaultStages)

        val starterList = listOf(
            // --- 1. تأسيس السقف ---
            ItemEntity(projectId = projectId, stageName = "تأسيس السقف", name = "قسام مدور ٤ خط", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس السقف", name = "لي كهرباء احمر الأصلي", quantityMode = "SINGLE", unit = "لفة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس السقف", name = "مسامير دق", quantityMode = "SINGLE", unit = "باكت"),
            ItemEntity(projectId = projectId, stageName = "تأسيس السقف", name = "شطرطون رصاصي أصلي", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس السقف", name = "ماسورة ماء مقاس 2 بوصة", quantityMode = "SINGLE", unit = "عود"),

            // --- 2. تأسيس داخلي ---
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "علبة افياش حديد ابو١", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "علبة افياش حديد ابو٢", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "علبة افياش حديد ابو٣", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "علبة قسام ٣٠×٦٠", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "علبة قسام ٢٠×١٥", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "علبة قسام ١٥×١٠", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "علبة قسام ٢٠×٤٠", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "علبة قسام ٣٠×١٥", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "علبة فيش بلاستيك بارز", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "طبلون بلاستيك ١٣", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "لي حنجور ١/٢ صغير", quantityMode = "SINGLE", unit = "لفة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "لي حنجور ٣/٤ كبير", quantityMode = "SINGLE", unit = "لفة"),
            ItemEntity(projectId = projectId, stageName = "تأسيس داخلي", name = "قسام مدور ٤ خط", quantityMode = "SINGLE", unit = "حبة"),

            // --- 3. الأسلاك (نظام الـ 3 خانات مع الألوان) ---
            ItemEntity(projectId = projectId, stageName = "الأسلاك", name = "سلك نحاس 1.5 مم (إنارة)", quantityMode = "TRIPLE_COLOR", c1 = "أحمر", c2 = "أسود", c3 = "أخضر مخطط بأصفر", unit = "لفة"),
            ItemEntity(projectId = projectId, stageName = "الأسلاك", name = "سلك نحاس 2.5 مم (أفياش وبرايز)", quantityMode = "TRIPLE_COLOR", c1 = "أزرق", c2 = "أسود", c3 = "أخضر مخطط بأصفر", unit = "لفة"),
            ItemEntity(projectId = projectId, stageName = "الأسلاك", name = "سلك نحاس 4 مم (مكيفات وسخانات)", quantityMode = "TRIPLE_COLOR", c1 = "أصفر", c2 = "أسود", c3 = "أخضر مخطط بأصفر", unit = "لفة"),
            ItemEntity(projectId = projectId, stageName = "الأسلاك", name = "سلك نحاس 6 مم (أحمال كبرى)", quantityMode = "TRIPLE_COLOR", c1 = "أحمر", c2 = "أزرق", c3 = "أصفر", unit = "لفة"),
            ItemEntity(projectId = projectId, stageName = "الأسلاك", name = "سلك نحاس تأريض أرضي (Earth) 2.5 مم", quantityMode = "TRIPLE_COLOR", c1 = "أخضر مخطط بأصفر", c2 = "أخضر", c3 = "أصفر", unit = "لفة"),
            ItemEntity(projectId = projectId, stageName = "الأسلاك", name = "كابل رئيسي 4*16 مم نحاس معزول", quantityMode = "SINGLE", unit = "متر"),
            ItemEntity(projectId = projectId, stageName = "الأسلاك", name = "سلك دش وإنترنت Cat6 أصلي", quantityMode = "SINGLE", unit = "لفة"),

            // --- 4. مفاتيح وأفياش ---
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "مفتاح ابو ١", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "مفتاح ابو ٢", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "مفتاح ابو ٣", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "مفتاح ابو ٤", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "مفتاح دركسون ابو ١", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "مفتاح دركسون ابو ٢", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "مفتاح دركسون ابو ٣", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "مفتاح دركسون ابو ٤", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "مفتاح سخان", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "مفتاح جرس", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "فيش تلفزيون", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "فيش تلفون", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "غطاء فيش", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "فيش ثلاثي عادي", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "فيش ثلاثي مع منفذ USB", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "فيش ثلاثي ثنائي", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "فيش ثلاثي مع مفتاح مفرد", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "مفاتيح وأفياش", name = "فيش ثلاثي مع مفتاح مزوج", quantityMode = "SINGLE", unit = "حبة"),

            // --- 5. انارة وملحقات ---
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "قاطع رئيسي ٣٢", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "قاطع رئيسي ٦٢", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "قاطع فرعي ١٠", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "قاطع فرعي ١٦", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "قاطع فرعي ٢٠", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "قاطع فرعي ٢٥", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "لمبة ٩ واط", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "لمبة طاقة شمسية ٩ واط", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "قواعد ديكور قلويز", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "قواعد لمبة ثنائي", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "لمبة سهاري ملون", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "قواعد لمبة سهاري", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "جرس عصفور", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "شريط ليد", quantityMode = "SINGLE", unit = "متر"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "شريط ليد لاصق", quantityMode = "SINGLE", unit = "لفة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "سبوت ٧ سم", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "سبوت ثنائي٧سم", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "لمبة جدارية خارجي", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "لمبة ممرات خارجي", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "انارة وملحقات", name = "غطاء كهرباء مدور بلاستيك", quantityMode = "SINGLE", unit = "حبة"),

            // --- 6. ملحقات ---
            ItemEntity(projectId = projectId, stageName = "ملحقات", name = "شطرطون كهرباء", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "ملحقات", name = "مسمار ربط ٤×٣٥", quantityMode = "SINGLE", unit = "علبة"),
            ItemEntity(projectId = projectId, stageName = "ملحقات", name = "مسمار ربط ٤×٤٠", quantityMode = "SINGLE", unit = "علبة"),
            ItemEntity(projectId = projectId, stageName = "ملحقات", name = "مسمار ربط ٤×٥٠", quantityMode = "SINGLE", unit = "علبة"),
            ItemEntity(projectId = projectId, stageName = "ملحقات", name = "مسمار ربط ٤×١٠٠", quantityMode = "SINGLE", unit = "علبة"),
            ItemEntity(projectId = projectId, stageName = "ملحقات", name = "رباط بلاستيك", quantityMode = "SINGLE", unit = "كيس"),
            ItemEntity(projectId = projectId, stageName = "ملحقات", name = "قاعدة تثبيت قواطع الطبلون", quantityMode = "SINGLE", unit = "حبة"),
            ItemEntity(projectId = projectId, stageName = "ملحقات", name = "قطعة توصيل كهرباء بين القواطع", quantityMode = "SINGLE", unit = "حبة")
        )
        dao.insertItems(starterList)
    }

    suspend fun insertStage(projectId: Long, name: String, imageUri: String? = null): Long {
        val count = dao.getStageCountForProject(projectId)
        return dao.insertStage(
            StageEntity(
                projectId = projectId,
                name = name.trim(),
                imageUri = imageUri,
                isDefault = false,
                orderIndex = count
            )
        )
    }

    suspend fun updateStage(stage: StageEntity, newName: String, newImageUri: String? = null) {
        val oldName = stage.name
        val cleanName = newName.trim()
        val finalImage = if (newImageUri != null) newImageUri else stage.imageUri
        dao.updateStage(stage.copy(name = cleanName, imageUri = finalImage))
        if (oldName != cleanName) {
            dao.updateItemStageNames(stage.projectId, oldName, cleanName)
        }
    }

    suspend fun deleteStage(stage: StageEntity) {
        dao.deleteStage(stage)
        dao.deleteItemsByStageName(stage.projectId, stage.name)
    }

    suspend fun insertItem(item: ItemEntity): Long {
        return dao.insertItem(item)
    }

    suspend fun updateItem(item: ItemEntity) {
        dao.updateItem(item)
    }

    suspend fun deleteItem(item: ItemEntity) {
        dao.deleteItem(item)
    }

    suspend fun createProject(
        name: String,
        clientName: String,
        notes: String,
        sourceProjectId: Long? = null,
        preserveQuantities: Boolean = true
    ): Long {
        val project = ProjectEntity(
            name = name,
            clientName = clientName,
            notes = notes,
            isCurrent = true,
            isBase = false
        )
        dao.clearCurrentProjectFlag()
        val newId = dao.insertProject(project)

        val targetSourceId = sourceProjectId
            ?: dao.getAllProjectsSnapshot().firstOrNull { it.isCurrent }?.id
            ?: dao.getAllProjectsSnapshot().firstOrNull()?.id

        if (targetSourceId != null) {
            // Transfer all customized stages (windows) as they are!
            val sourceStages = dao.getStagesForProjectSnapshot(targetSourceId)
            if (sourceStages.isNotEmpty()) {
                val clonedStages = sourceStages.map {
                    it.copy(id = 0, projectId = newId)
                }
                dao.insertStages(clonedStages)
            } else {
                seedBaseStagesAndItems(newId)
            }

            // Transfer all customized items, images, units, modes, colors and quantities as they are!
            val sourceItems = dao.getItemsForProjectSnapshot(targetSourceId)
            if (sourceItems.isNotEmpty()) {
                val clonedItems = sourceItems.map {
                    it.copy(
                        id = 0,
                        projectId = newId,
                        quantity = if (preserveQuantities) it.quantity else 0,
                        q1 = if (preserveQuantities) it.q1 else 0,
                        q2 = if (preserveQuantities) it.q2 else 0,
                        q3 = if (preserveQuantities) it.q3 else 0
                    )
                }
                dao.insertItems(clonedItems)
            }
        } else {
            seedBaseStagesAndItems(newId)
        }
        return newId
    }

    suspend fun switchProject(projectId: Long) {
        dao.switchCurrentProject(projectId)
    }

    suspend fun updateProject(project: ProjectEntity) {
        dao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(project: ProjectEntity) {
        // Protect base template project from deletion
        if (project.isBase) {
            return
        }
        dao.deleteProject(project)
        dao.deleteAllStagesForProject(project.id)
        dao.deleteAllItemsForProject(project.id)
        val remaining = dao.getAllProjectsSnapshot()
        if (remaining.isNotEmpty()) {
            dao.switchCurrentProject(remaining.first().id)
        } else {
            initializeDefaultsIfNeeded()
        }
    }

    suspend fun resetQuantitiesForProject(projectId: Long) {
        val items = dao.getItemsForProjectSnapshot(projectId)
        items.forEach { item ->
            dao.updateItem(item.copy(quantity = 0, q1 = 0, q2 = 0, q3 = 0))
        }
    }

    suspend fun getAllDataForBackup(): Map<String, Any> {
        val projects = dao.getAllProjectsSnapshot()
        val allStages = dao.getAllStagesSnapshot()
        val allItems = mutableListOf<ItemEntity>()
        projects.forEach { p ->
            allItems.addAll(dao.getItemsForProjectSnapshot(p.id))
        }
        return mapOf(
            "app" to "أصيل المليكي",
            "version" to 3,
            "timestamp" to System.currentTimeMillis(),
            "projects" to projects,
            "stages" to allStages,
            "items" to allItems
        )
    }
}
