package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.*

@Database(
    entities = [
        StudentEntity::class,
        DeskEntity::class,
        AcademicMaterialEntity::class,
        AttendanceLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ClassProDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun deskDao(): DeskDao
    abstract fun academicDao(): AcademicDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: ClassProDatabase? = null

        fun getDatabase(context: Context): ClassProDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClassProDatabase::class.java,
                    "classpro_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
