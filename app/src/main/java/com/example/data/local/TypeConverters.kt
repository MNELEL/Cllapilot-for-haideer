package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.SyncState

class Converters {
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        if (value.trim().isEmpty()) return emptyList()
        return value.split(":::")
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return list.joinToString(":::")
    }

    @TypeConverter
    fun fromSyncState(value: String): SyncState {
        return try { SyncState.valueOf(value) } catch (e: Exception) { SyncState.PENDING }
    }

    @TypeConverter
    fun toSyncState(state: SyncState): String {
        return state.name
    }
}
