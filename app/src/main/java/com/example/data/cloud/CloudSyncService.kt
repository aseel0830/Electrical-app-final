package com.example.data.cloud

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.ItemEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.StageEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class CloudSyncService(private val context: Context, private val db: AppDatabase) {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )
    private val credentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("معرّف عميل جوجل غير متوفر: ${e.message}")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    _currentUser.value = auth.currentUser
                    onSuccess()
                } else {
                    onError("نوع تسجيل الدخول غير متطابق")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("CloudSync", "Google Sign-In cancelled: ${e.message}")
                onError("تم إلغاء تسجيل الدخول")
            } catch (e: Exception) {
                Log.e("CloudSync", "Sign in failed", e)
                onError(e.localizedMessage ?: "فشل تسجيل الدخول بحساب جوجل")
            }
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        auth.signOut()
        _currentUser.value = null
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("CloudSync", "Failed to clear credentials", e)
            } finally {
                onComplete()
            }
        }
    }

    suspend fun syncDataToGoogleCloud(): Result<String> = withContext(Dispatchers.IO) {
        val user = auth.currentUser
        if (user == null) {
            return@withContext Result.failure(IllegalStateException("يجب تسجيل الدخول بحساب جوجل أولاً"))
        }

        _isSyncing.value = true
        _syncStatusMessage.value = "جاري رفع البيانات إلى جوجل كلاود..."

        try {
            val dao = db.electricalDao()
            val projects = dao.getAllProjectsSnapshot()
            val stages = dao.getAllStagesSnapshot()

            val allItems = mutableListOf<ItemEntity>()
            for (p in projects) {
                allItems.addAll(dao.getItemsForProjectSnapshot(p.id))
            }

            // Build JSON payload of the entire inventory
            val rootJson = JSONObject().apply {
                put("version", 2)
                put("technician", "أصيل المليكي")
                put("syncedAt", System.currentTimeMillis())
                put("userEmail", user.email ?: "")

                val stagesArray = JSONArray()
                stages.forEach { s ->
                    stagesArray.put(JSONObject().apply {
                        put("id", s.id)
                        put("projectId", s.projectId)
                        put("name", s.name)
                        put("imageUri", s.imageUri ?: "")
                        put("isDefault", s.isDefault)
                        put("orderIndex", s.orderIndex)
                    })
                }
                put("stages", stagesArray)

                val projectsArray = JSONArray()
                projects.forEach { p ->
                    projectsArray.put(JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("clientName", p.clientName)
                        put("notes", p.notes)
                        put("isCurrent", p.isCurrent)
                        put("isBase", p.isBase)
                        put("createdAt", p.createdAt)
                    })
                }
                put("projects", projectsArray)

                val itemsArray = JSONArray()
                allItems.forEach { itm ->
                    itemsArray.put(JSONObject().apply {
                        put("id", itm.id)
                        put("projectId", itm.projectId)
                        put("stageName", itm.stageName)
                        put("name", itm.name)
                        put("quantityMode", itm.quantityMode)
                        put("quantity", itm.quantity)
                        put("q1", itm.q1)
                        put("q2", itm.q2)
                        put("q3", itm.q3)
                        put("wireColor", itm.wireColor ?: "")
                        put("c1", itm.c1 ?: "")
                        put("c2", itm.c2 ?: "")
                        put("c3", itm.c3 ?: "")
                        put("unit", itm.unit)
                    })
                }
                put("items", itemsArray)
            }

            val syncData = mapOf(
                "userId" to user.uid,
                "backupData" to rootJson.toString(),
                "projectCount" to projects.size,
                "itemCount" to allItems.size,
                "syncedAt" to FieldValue.serverTimestamp()
            )

            // Save under user's isolated subcollection in Firestore
            firestore.collection("users")
                .document(user.uid)
                .collection("sync")
                .document("latest_backup")
                .set(syncData, SetOptions.merge())
                .await()

            // Also mirror projects as top-level documents for granular sync
            for (p in projects) {
                val projData = mapOf(
                    "userId" to user.uid,
                    "name" to p.name,
                    "clientName" to p.clientName,
                    "notes" to p.notes,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("users")
                    .document(user.uid)
                    .collection("projects")
                    .document(p.id.toString())
                    .set(projData, SetOptions.merge())
                    .await()
            }

            _syncStatusMessage.value = "تمت المزامنة وحفظ النسخة الاحتياطية بنجاح على جوجل كلاود ⚡"
            Result.success("تم رفع وحفظ ${projects.size} ملف و ${allItems.size} صنف بنجاح على جوجل كلاود")
        } catch (e: Exception) {
            Log.e("CloudSync", "Sync failed", e)
            _syncStatusMessage.value = "فشل الرفع: ${e.localizedMessage}"
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun restoreDataFromGoogleCloud(): Result<String> = withContext(Dispatchers.IO) {
        val user = auth.currentUser
        if (user == null) {
            return@withContext Result.failure(IllegalStateException("يجب تسجيل الدخول بحساب جوجل أولاً"))
        }

        _isSyncing.value = true
        _syncStatusMessage.value = "جاري استرجاع البيانات من جوجل كلاود..."

        try {
            val doc = firestore.collection("users")
                .document(user.uid)
                .collection("sync")
                .document("latest_backup")
                .get()
                .await()

            if (!doc.exists()) {
                return@withContext Result.failure(IllegalStateException("لا توجد نسخة احتياطية محفوظة لهذا الحساب في السحابة"))
            }

            val backupStr = doc.getString("backupData")
                ?: return@withContext Result.failure(IllegalStateException("ملف النسخة السحابية فارغ"))

            val json = JSONObject(backupStr)
            val dao = db.electricalDao()

            // Restore Projects, Stages and Items
            val projectsArray = json.optJSONArray("projects")
            val stagesArray = json.optJSONArray("stages")
            val itemsArray = json.optJSONArray("items")

            var importedProjects = 0
            if (projectsArray != null && projectsArray.length() > 0) {
                val oldToNewProjectId = mutableMapOf<Long, Long>()

                for (i in 0 until projectsArray.length()) {
                    val pObj = projectsArray.getJSONObject(i)
                    val oldId = pObj.getLong("id")
                    val name = pObj.getString("name")
                    val client = pObj.optString("clientName", "")
                    val notes = pObj.optString("notes", "")
                    val isBase = pObj.optBoolean("isBase", false)

                    val newProjId = dao.insertProject(
                        ProjectEntity(
                            name = name,
                            clientName = client,
                            notes = notes,
                            isCurrent = (i == 0),
                            isBase = isBase
                        )
                    )
                    oldToNewProjectId[oldId] = newProjId
                    importedProjects++
                }

                // Restore Stages per project
                if (stagesArray != null) {
                    for (i in 0 until stagesArray.length()) {
                        val sObj = stagesArray.getJSONObject(i)
                        val oldPId = sObj.optLong("projectId", 1L)
                        val newPId = oldToNewProjectId[oldPId] ?: oldToNewProjectId.values.firstOrNull() ?: 1L
                        val sName = sObj.getString("name")
                        val img = sObj.optString("imageUri").ifBlank { null }

                        dao.insertStage(
                            StageEntity(
                                projectId = newPId,
                                name = sName,
                                imageUri = img,
                                isDefault = sObj.optBoolean("isDefault", false),
                                orderIndex = sObj.optInt("orderIndex", 0)
                            )
                        )
                    }
                }

                if (itemsArray != null) {
                    val itemsToInsert = mutableListOf<ItemEntity>()
                    for (i in 0 until itemsArray.length()) {
                        val iObj = itemsArray.getJSONObject(i)
                        val oldPId = iObj.getLong("projectId")
                        val newPId = oldToNewProjectId[oldPId] ?: continue

                        itemsToInsert.add(
                            ItemEntity(
                                projectId = newPId,
                                stageName = iObj.getString("stageName"),
                                name = iObj.getString("name"),
                                quantityMode = iObj.optString("quantityMode", "SINGLE"),
                                quantity = iObj.optInt("quantity", 0),
                                q1 = iObj.optInt("q1", 0),
                                q2 = iObj.optInt("q2", 0),
                                q3 = iObj.optInt("q3", 0),
                                wireColor = iObj.optString("wireColor").ifBlank { null },
                                c1 = iObj.optString("c1").ifBlank { null },
                                c2 = iObj.optString("c2").ifBlank { null },
                                c3 = iObj.optString("c3").ifBlank { null },
                                unit = iObj.optString("unit", "حبة")
                            )
                        )
                    }
                    if (itemsToInsert.isNotEmpty()) {
                        dao.insertItems(itemsToInsert)
                    }
                }
            }

            _syncStatusMessage.value = "تم استرجاع البيانات بنجاح من جوجل كلاود ⚡"
            Result.success("تم استرجاع $importedProjects ملف بنجاح من جوجل كلاود")
        } catch (e: Exception) {
            Log.e("CloudSync", "Restore failed", e)
            _syncStatusMessage.value = "فشل الاسترجاع: ${e.localizedMessage}"
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }
}
