package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.*

@Database(
    entities = [
        StudentEntity::class,
        DeskEntity::class,
        AcademicMaterialEntity::class,
        AttendanceLogEntity::class,
        StudentGradeEntity::class,
        PacingEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ClassProDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun deskDao(): DeskDao
    abstract fun academicDao(): AcademicDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun gradeDao(): GradeDao
    abstract fun pacingDao(): PacingDao


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
