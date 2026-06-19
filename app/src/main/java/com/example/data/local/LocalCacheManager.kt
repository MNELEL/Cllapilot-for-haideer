package com.example.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.StudentEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "classroom_indexed_cache")

/**
 * A local storage layer intended to mirror the document-caching capabilities 
 * of IndexedDB for offline resilience in Android.
 */
class LocalCacheManager(private val context: Context) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val studentListType = Types.newParameterizedType(List::class.java, StudentEntity::class.java)
    private val studentListAdapter = moshi.adapter<List<StudentEntity>>(studentListType)

    private val materialListType = Types.newParameterizedType(List::class.java, com.example.data.model.AcademicMaterialEntity::class.java)
    private val materialListAdapter = moshi.adapter<List<com.example.data.model.AcademicMaterialEntity>>(materialListType)

    companion object {
        val CACHED_STUDENTS_KEY = stringPreferencesKey("cached_students_data")
        val CACHED_NOTES_KEY = stringPreferencesKey("cached_notes_data")
        val CACHED_SEATING_KEY = stringPreferencesKey("cached_seating_layout")
        val CACHED_MATERIALS_KEY = stringPreferencesKey("cached_materials_data")
    }

    // --- Students Cache ---
    suspend fun saveCachedStudents(students: List<StudentEntity>) {
        val jsonString = studentListAdapter.toJson(students)
        context.dataStore.edit { preferences ->
            preferences[CACHED_STUDENTS_KEY] = jsonString
        }
        Log.d("LocalCacheManager", "Saved ${students.size} students to IndexedDB-style cache")
    }

    val cachedStudentsFlow: Flow<List<StudentEntity>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[CACHED_STUDENTS_KEY]
            if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                try {
                    studentListAdapter.fromJson(jsonString) ?: emptyList()
                } catch (e: Exception) {
                    Log.e("LocalCacheManager", "Failed to deserialize students: ${e.message}")
                    emptyList()
                }
            }
        }

    // --- Notes Cache (Key-Value) ---
    suspend fun cacheDraftNote(draft: String) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_NOTES_KEY] = draft
        }
        Log.d("LocalCacheManager", "Saved draft note to cache")
    }

    val cachedDraftNoteFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
             preferences[CACHED_NOTES_KEY] ?: ""
        }

    // --- Seating Plan Layout State Cache ---
    suspend fun cacheSeatingLayout(layoutJson: String) {
        context.dataStore.edit { preferences ->
             preferences[CACHED_SEATING_KEY] = layoutJson
        }
        Log.d("LocalCacheManager", "Saved seating plan to cache")
    }

    val cachedSeatingLayoutFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[CACHED_SEATING_KEY] ?: ""
        }

    // --- Materials (Notes) Cache ---
    suspend fun saveCachedMaterials(materials: List<com.example.data.model.AcademicMaterialEntity>) {
        val jsonString = materialListAdapter.toJson(materials)
        context.dataStore.edit { preferences ->
            preferences[CACHED_MATERIALS_KEY] = jsonString
        }
        Log.d("LocalCacheManager", "Saved ${materials.size} notes/materials to IndexedDB-style cache")
    }

    val cachedMaterialsFlow: Flow<List<com.example.data.model.AcademicMaterialEntity>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[CACHED_MATERIALS_KEY]
            if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                try {
                    materialListAdapter.fromJson(jsonString) ?: emptyList()
                } catch (e: Exception) {
                    Log.e("LocalCacheManager", "Failed to deserialize materials: ${e.message}")
                    emptyList()
                }
            }
        }
}
