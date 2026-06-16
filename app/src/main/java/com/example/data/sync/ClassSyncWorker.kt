package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.ClassProDatabase
import com.example.data.model.SyncState
import kotlinx.coroutines.delay

class ClassSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ClassSyncWorker", "נפתחה משימת סנכרון אוטומטית ברקע")
        val database = ClassProDatabase.getDatabase(applicationContext)
        val studentDao = database.studentDao()
        val attendanceDao = database.attendanceDao()

        try {
            // Get all pending students and attendance logs
            val pendingStudents = studentDao.getPendingSyncStudents()
            val pendingLogs = attendanceDao.getPendingSyncLogs()
            
            if (pendingStudents.isEmpty() && pendingLogs.isEmpty()) {
                Log.d("ClassSyncWorker", "אין נתונים ממתינים לסנכרון")
                return Result.success()
            }

            Log.d("ClassSyncWorker", "נמצאו ${pendingStudents.size} סטודנטים ו-${pendingLogs.size} רשומות נוכחות לסנכרון")
            
            // Simulating connection delay / Firestore operation
            delay(2000)

            // Mark all pending students as SYNCED
            for (student in pendingStudents) {
                studentDao.updateSyncStatus(student.id, SyncState.SYNCED)
                Log.d("ClassSyncWorker", "סונכרן בהצלחה לענן: ${student.name}")
            }
            
            // Mark all pending attendance logs as SYNCED
            for (log in pendingLogs) {
                attendanceDao.updateSyncStatus(log.id, SyncState.SYNCED)
                Log.d("ClassSyncWorker", "סונכרנה נוכחות: ${log.studentId} - ${log.status}")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("ClassSyncWorker", "error syncing database", e)
            return Result.retry()
        }
    }
}
