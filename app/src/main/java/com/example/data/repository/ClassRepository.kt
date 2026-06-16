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
    private val gradeDao = database.gradeDao()
    private val pacingDao = database.pacingDao()

    val allStudents: Flow<List<StudentEntity>> = studentDao.getStudentsFlow()
    val allDesks: Flow<List<DeskEntity>> = deskDao.getDesksFlow()
    val allMaterials: Flow<List<AcademicMaterialEntity>> = academicDao.getMaterialsFlow()
    val allLogs: Flow<List<AttendanceLogEntity>> = attendanceDao.getLogsFlow()
    val allGrades: Flow<List<StudentGradeEntity>> = gradeDao.getGradesFlow()
    val allPacing: Flow<List<PacingEntity>> = pacingDao.getPacingFlow()


    suspend fun insertStudent(student: StudentEntity) {
        studentDao.insertStudent(student)
    }

    suspend fun insertStudents(students: List<StudentEntity>) {
        studentDao.insertStudents(students)
    }

    suspend fun deleteStudent(id: String) {
        studentDao.deleteStudentById(id)
    }

    suspend fun deleteStudents(ids: List<String>) {
        studentDao.deleteStudentsByIds(ids)
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

    suspend fun insertGrade(grade: StudentGradeEntity) {
        gradeDao.insertGrade(grade)
    }

    suspend fun clearAllGrades() {
        gradeDao.clearAll()
    }

    suspend fun insertPacing(pacing: PacingEntity) {
        pacingDao.insertPacing(pacing)
    }

    suspend fun deletePacing(id: String) {
        pacingDao.deletePacing(id)
    }

    // Force Sync trigger
    suspend fun forceCloudSync(): Boolean {
        val pendingStudents = studentDao.getPendingSyncStudents()
        val pendingLogs = attendanceDao.getPendingSyncLogs()
        
        if (pendingStudents.isEmpty() && pendingLogs.isEmpty()) return true
        
        delay(1500) // Simulate cloud transit time
        for (st in pendingStudents) {
            studentDao.updateSyncStatus(st.id, SyncState.SYNCED)
        }
        for (log in pendingLogs) {
            attendanceDao.updateSyncStatus(log.id, SyncState.SYNCED)
        }
        return true
    }
}
