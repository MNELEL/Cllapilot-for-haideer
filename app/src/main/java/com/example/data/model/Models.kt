package com.example.data.model

import androidx.room.*

enum class SyncState { PENDING, SYNCED, ERROR }

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val height: String, // "Low" | "Medium" | "Tall"
    val rowPreference: String, // "Front" | "Middle" | "Back"
    val loves: List<String>,
    val forbids: List<String>,
    val separate: List<String>,
    val notes: String,
    val syncStatus: SyncState
)

@Entity(tableName = "desks", primaryKeys = ["row", "col"])
data class DeskEntity(
    val row: Int,
    val col: Int,
    val type: String, // "DESK" | "WALKWAY" | "BLOCK"
    val studentId: String?,
    val isLocked: Boolean
)

@Entity(tableName = "academic_materials")
data class AcademicMaterialEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summaryNotes: String,
    val lessonTimeline: String,
    val quizJson: String, // JSON representation of multiple-choice questions
    val coveragePercentage: Int, // coverage metrics
    val timestamp: Long
)

@Entity(tableName = "attendance_logs")
data class AttendanceLogEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val date: String, // YYYY-MM-DD
    val status: String, // "PRESENT" | "ABSENT" | "LATE"
    val syncStatus: SyncState = SyncState.PENDING
)

@Entity(tableName = "student_grades")
data class StudentGradeEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val assignmentId: String,
    val gradeValue: String
)

@Entity(tableName = "pacing_milestones")
data class PacingEntity(
    @PrimaryKey val id: String,
    val moduleName: String,
    val rangeStart: String,
    val rangeEnd: String,
    val associatedMaterialId: String,
    val completionStatus: Boolean,
    val behaviorStyleScore: Int = 0 // Used for behavioral tracking learning style
)
