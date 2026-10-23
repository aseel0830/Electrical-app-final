package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ElectricalDao {

    // --- Stages ---
    @Query("SELECT * FROM stages WHERE projectId = :projectId ORDER BY orderIndex ASC, id ASC")
    fun getStagesForProject(projectId: Long): Flow<List<StageEntity>>

    @Query("SELECT * FROM stages WHERE projectId = :projectId ORDER BY orderIndex ASC, id ASC")
    suspend fun getStagesForProjectSnapshot(projectId: Long): List<StageEntity>

    @Query("SELECT * FROM stages ORDER BY orderIndex ASC, id ASC")
    fun getAllStages(): Flow<List<StageEntity>>

    @Query("SELECT * FROM stages ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllStagesSnapshot(): List<StageEntity>

    @Query("SELECT COUNT(*) FROM stages WHERE projectId = :projectId")
    suspend fun getStageCountForProject(projectId: Long): Int

    @Query("SELECT COUNT(*) FROM stages")
    suspend fun getStageCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStage(stage: StageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(stages: List<StageEntity>)

    @Update
    suspend fun updateStage(stage: StageEntity)

    @Delete
    suspend fun deleteStage(stage: StageEntity)

    @Query("UPDATE items SET stageName = :newName WHERE projectId = :projectId AND stageName = :oldName")
    suspend fun updateItemStageNames(projectId: Long, oldName: String, newName: String)

    @Query("UPDATE items SET stageName = :newName WHERE stageName = :oldName")
    suspend fun updateItemStageNames(oldName: String, newName: String)

    @Query("DELETE FROM items WHERE projectId = :projectId AND stageName = :stageName")
    suspend fun deleteItemsByStageName(projectId: Long, stageName: String)

    @Query("DELETE FROM items WHERE stageName = :stageName")
    suspend fun deleteItemsByStageName(stageName: String)

    @Query("DELETE FROM stages WHERE projectId = :projectId")
    suspend fun deleteAllStagesForProject(projectId: Long)

    // --- Projects ---
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentProjectFlow(): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    suspend fun getAllProjectsSnapshot(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("UPDATE projects SET isCurrent = 0")
    suspend fun clearCurrentProjectFlag()

    @Query("UPDATE projects SET isCurrent = 1 WHERE id = :id")
    suspend fun setProjectAsCurrent(id: Long)

    @Transaction
    suspend fun switchCurrentProject(id: Long) {
        clearCurrentProjectFlag()
        setProjectAsCurrent(id)
    }

    // --- Items ---
    @Query("SELECT * FROM items WHERE projectId = :projectId ORDER BY id ASC")
    fun getItemsForProject(projectId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE projectId = :projectId AND stageName = :stageName ORDER BY id ASC")
    fun getItemsForStage(projectId: Long, stageName: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE projectId = :projectId")
    suspend fun getItemsForProjectSnapshot(projectId: Long): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM items WHERE projectId = :projectId")
    suspend fun deleteAllItemsForProject(projectId: Long)
}
