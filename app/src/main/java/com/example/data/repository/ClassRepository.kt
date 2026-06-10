package com.example.data.repository

import android.content.Context
import com.example.data.local.ClassProDatabase
import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class ClassRepository(context: Context) {
    private val database = ClassProDatabase.getDatabase(context)
    private val studentDao = database.studentDao()
    private val deskDao = database.deskDao()
    private val academicDao = database.academicDao()
    private val attendanceDao = database.attendanceDao()

    val allStudents: Flow<List<StudentEntity>> = studentDao.getStudentsFlow()
    val allDesks: Flow<List<DeskEntity>> = deskDao.getDesksFlow()
    val allMaterials: Flow<List<AcademicMaterialEntity>> = academicDao.getMaterialsFlow()
    val allLogs: Flow<List<AttendanceLogEntity>> = attendanceDao.getLogsFlow()

    suspend fun insertStudent(student: StudentEntity) {
        studentDao.insertStudent(student)
    }

    suspend fun insertStudents(students: List<StudentEntity>) {
        studentDao.insertStudents(students)
    }

    suspend fun deleteStudent(id: String) {
        studentDao.deleteStudentById(id)
    }

    suspend fun clearAllStudents() {
        studentDao.clearAll()
    }

    suspend fun insertDesks(desks: List<DeskEntity>) {
        deskDao.insertDesks(desks)
    }

    suspend fun insertDesk(desk: DeskEntity) {
        deskDao.insertDesk(desk)
    }

    suspend fun clearAllDesks() {
        deskDao.clearAll()
    }

    suspend fun insertMaterial(material: AcademicMaterialEntity) {
        academicDao.insertMaterial(material)
    }

    suspend fun deleteMaterial(id: String) {
        academicDao.deleteMaterial(id)
    }

    suspend fun insertLog(log: AttendanceLogEntity) {
        attendanceDao.insertLog(log)
    }

    suspend fun insertLogs(logs: List<AttendanceLogEntity>) {
        attendanceDao.insertLogs(logs)
    }

    suspend fun clearLogs() {
        attendanceDao.clearAll()
    }

    // Force Sync trigger
    suspend fun forceCloudSync(): Boolean {
        val pending = studentDao.getPendingSyncStudents()
        if (pending.isEmpty()) return true
        
        delay(1500) // Simulate cloud transit time
        for (st in pending) {
            studentDao.updateSyncStatus(st.id, SyncState.SYNCED)
        }
        return true
    }
}
