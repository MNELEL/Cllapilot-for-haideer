package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getStudentsFlow(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students")
    suspend fun getStudentsOnce(): List<StudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: String)

    @Query("DELETE FROM students WHERE id IN (:ids)")
    suspend fun deleteStudentsByIds(ids: List<String>)

    @Query("SELECT * FROM students WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncStudents(): List<StudentEntity>

    @Query("UPDATE students SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncState)

    @Query("DELETE FROM students")
    suspend fun clearAll()
}

@Dao
interface DeskDao {
    @Query("SELECT * FROM desks ORDER BY `row` ASC, col ASC")
    fun getDesksFlow(): Flow<List<DeskEntity>>

    @Query("SELECT * FROM desks")
    suspend fun getDesksOnce(): List<DeskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesk(desk: DeskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesks(desks: List<DeskEntity>)

    @Query("DELETE FROM desks WHERE `row` = :row AND col = :col")
    suspend fun deleteDesk(row: Int, col: Int)

    @Query("DELETE FROM desks")
    suspend fun clearAll()
}

@Dao
interface AcademicDao {
    @Query("SELECT * FROM academic_materials ORDER BY timestamp DESC")
    fun getMaterialsFlow(): Flow<List<AcademicMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: AcademicMaterialEntity)

    @Query("DELETE FROM academic_materials WHERE id = :id")
    suspend fun deleteMaterial(id: String)

    @Query("DELETE FROM academic_materials")
    suspend fun clearAll()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_logs ORDER BY date DESC")
    fun getLogsFlow(): Flow<List<AttendanceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AttendanceLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<AttendanceLogEntity>)

    @Query("DELETE FROM attendance_logs")
    suspend fun clearAll()
}

@Dao
interface GradeDao {
    @Query("SELECT * FROM student_grades")
    fun getGradesFlow(): Flow<List<StudentGradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: StudentGradeEntity)

    @Query("DELETE FROM student_grades")
    suspend fun clearAll()
}
