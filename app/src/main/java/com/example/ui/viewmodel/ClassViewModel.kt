package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ClassRepository
import com.example.util.GeminiParser
import com.example.util.QuizQuestion
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ClassViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ClassRepository(application)

    // UI Configuration States
    val selectedTheme = MutableStateFlow("MODERN") // "MODERN" | "CONSERVATIVE"
    val layoutRows = MutableStateFlow(6) // default grid rows
    val layoutCols = MutableStateFlow(6) // default grid cols
    val selectedMode = MutableStateFlow("STRUCTURE") // "STRUCTURE" | "PLACEMENT"
    val isSyncing = MutableStateFlow(false)
    val syncMessage = MutableStateFlow("")

    // Flow Data Streams
    val students = repository.allStudents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val desks = repository.allDesks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val materials = repository.allMaterials.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val attendanceLogs = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Seating Selection Tracks
    val selectedStudentForHighlight = MutableStateFlow<StudentEntity?>(null)
    val selectedUnassignedStudent = MutableStateFlow<StudentEntity?>(null)

    // Undo / Redo Stacks for Seat Placements
    private val undoStack = mutableListOf<List<DeskEntity>>()
    private val redoStack = mutableListOf<List<DeskEntity>>()
    val canUndo = MutableStateFlow(false)
    val canRedo = MutableStateFlow(false)

    // Gamification & Timers
    val selectedStudentWheelName = MutableStateFlow("")
    val isWheelSpinning = MutableStateFlow(false)
    val countdownSeconds = MutableStateFlow(0)
    val isTimerActive = MutableStateFlow(false)
    val generatedGroups = MutableStateFlow<List<List<StudentEntity>>>(emptyList())

    // Library parsing states
    val parsedSummary = MutableStateFlow("")
    val parsedTimeline = MutableStateFlow("")
    val parsedQuiz = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val isParsingFile = MutableStateFlow(false)

    init {
        // Initialize default database records if empty
        viewModelScope.launch {
            students.collectLatest { list ->
                if (list.isEmpty()) {
                    loadDemoData()
                }
            }
        }
        viewModelScope.launch {
            desks.collectLatest { list ->
                if (list.isEmpty()) {
                    generateDefaultGrid(6, 6)
                }
            }
        }
    }

    fun setTheme(theme: String) {
        selectedTheme.value = theme
    }

    fun gridResize(rows: Int, cols: Int) {
        if (rows in 1..20 && cols in 1..20) {
            layoutRows.value = rows
            layoutCols.value = cols
            viewModelScope.launch {
                generateDefaultGrid(rows, cols)
            }
        }
    }

    private suspend fun generateDefaultGrid(rows: Int, cols: Int) {
        val currentDesks = desks.value
        val list = mutableListOf<DeskEntity>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val existing = currentDesks.find { it.row == r && it.col == c }
                if (existing != null) {
                    list.add(existing)
                } else {
                    // standard alternate desk walkways
                    val type = if (c % 2 == 1) "WALKWAY" else "DESK"
                    list.add(DeskEntity(r, c, type, null, false))
                }
            }
        }
        repository.clearAllDesks()
        repository.insertDesks(list)
    }

    // Toggle Cell Type in STRUC MODE
    fun toggleCellType(row: Int, col: Int) {
        viewModelScope.launch {
            val list = desks.value.toMutableList()
            val index = list.indexOfFirst { it.row == row && it.col == col }
            if (index != -index) {
                val current = list[index]
                val nextType = when (current.type) {
                    "DESK" -> "WALKWAY"
                    "WALKWAY" -> "BLOCK"
                    else -> "DESK"
                }
                val updated = current.copy(type = nextType, studentId = null)
                repository.insertDesk(updated)
            }
        }
    }

    // Toggle Lock status
    fun toggleDeskLock(row: Int, col: Int) {
        viewModelScope.launch {
            val list = desks.value
            val desk = list.find { it.row == row && it.col == col }
            if (desk != null && desk.type == "DESK") {
                repository.insertDesk(desk.copy(isLocked = !desk.isLocked))
            }
        }
    }

    // Undo / Redo Management
    private fun savePlacementState() {
        val currentState = desks.value.map { it.copy() }
        undoStack.add(currentState)
        redoStack.clear()
        if (undoStack.size > 15) undoStack.removeAt(0)
        canUndo.value = undoStack.isNotEmpty()
        canRedo.value = redoStack.isNotEmpty()
    }

    fun undoPlacement() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            val currentState = desks.value.map { it.copy() }
            redoStack.add(currentState)
            canUndo.value = undoStack.isNotEmpty()
            canRedo.value = redoStack.isNotEmpty()
            viewModelScope.launch {
                repository.clearAllDesks()
                repository.insertDesks(previous)
            }
        }
    }

    fun redoPlacement() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            val currentState = desks.value.map { it.copy() }
            undoStack.add(currentState)
            canUndo.value = undoStack.isNotEmpty()
            canRedo.value = redoStack.isNotEmpty()
            viewModelScope.launch {
                repository.clearAllDesks()
                repository.insertDesks(next)
            }
        }
    }

    // Interactive placement toggle
    fun selectUnassignedStudent(student: StudentEntity?) {
        selectedUnassignedStudent.value = student
    }

    fun placeStudentAt(row: Int, col: Int) {
        val student = selectedUnassignedStudent.value ?: return
        savePlacementState()
        viewModelScope.launch {
            // Find desk
            val list = desks.value.toMutableList()
            val targetIndex = list.indexOfFirst { it.row == row && it.col == col }
            if (targetIndex != -1) {
                val targetDesk = list[targetIndex]
                if (targetDesk.type == "DESK") {
                    // Evict existing student from this desk first
                    val evictedStudentId = targetDesk.studentId

                    // Remove this student from any previous desk they occupied
                    val previousDeskIndex = list.indexOfFirst { it.studentId == student.id }
                    if (previousDeskIndex != -1) {
                        list[previousDeskIndex] = list[previousDeskIndex].copy(studentId = evictedStudentId)
                    }

                    list[targetIndex] = targetDesk.copy(studentId = student.id)
                    repository.clearAllDesks()
                    repository.insertDesks(list)
                }
            }
            selectedUnassignedStudent.value = null
        }
    }

    fun removeStudentFromLayout(studentId: String) {
        savePlacementState()
        viewModelScope.launch {
            val list = desks.value.toMutableList()
            val index = list.indexOfFirst { it.studentId == studentId }
            if (index != -1) {
                list[index] = list[index].copy(studentId = null)
                repository.clearAllDesks()
                repository.insertDesks(list)
            }
        }
    }

    // AI Multi-row / Height Smart Seat Placement Solver
    fun runIntelligentAIPlacement() {
        savePlacementState()
        viewModelScope.launch {
            val allSt = students.value
            val allDs = desks.value.toMutableList()
            
            // Collect unlocked desks of type "DESK"
            val unlockedDesks = allDs.filter { it.type == "DESK" && !it.isLocked }
            if (unlockedDesks.isEmpty()) return@launch

            // Filter students that can be adjusted (not sitting on a locked desk)
            val lockedDesks = allDs.filter { it.type == "DESK" && it.isLocked }
            val lockedStudentIds = lockedDesks.mapNotNull { it.studentId }.toSet()
            val moveableStudents = allSt.filter { !lockedStudentIds.contains(it.id) }

            // Mathematical Optimization heuristics: Seating by Heights, preferences & Social relationships
            // We want to optimize compatibility of students placed on the unlocked desks.
            val numDesks = unlockedDesks.size
            val stToPlace = moveableStudents.take(numDesks)

            // Dynamic arrangement algorithm using simulated annealing style score optimizer
            val bestArrangement = optimizeSeatingLayout(stToPlace, unlockedDesks, allDs, allSt)

            // Write back optimized layout to desks mutable list
            for (desk in unlockedDesks) {
                val idx = allDs.indexOfFirst { it.row == desk.row && it.col == desk.col }
                if (idx != -1) {
                    val assignedStudent = bestArrangement[Pair(desk.row, desk.col)]
                    allDs[idx] = allDs[idx].copy(studentId = assignedStudent?.id)
                }
            }

            repository.clearAllDesks()
            repository.insertDesks(allDs)
            Log.d("PlacementEngine", "הושלמה אופטימיזציית ישיבה כיתתית חכמה בהצלחה!")
        }
    }

    // Heuristic solver scoring layout compatibility
    private fun optimizeSeatingLayout(
        students: List<StudentEntity>,
        desksToPlace: List<DeskEntity>,
        allDesks: List<DeskEntity>,
        fullRoster: List<StudentEntity>
    ): Map<Pair<Int, Int>, StudentEntity?> {
        val result = mutableMapOf<Pair<Int, Int>, StudentEntity?>()
        if (students.isEmpty()) return result

        // Best configuration starts with a simple direct assignment
        val placementMap = desksToPlace.mapIndexed { idx, d -> d to students.getOrNull(idx) }.toMap()
        
        // Let's do several iterations of randomized swapping to maximize compatibility score
        var bestConfig = placementMap.map { it.key.row to it.key.col to it.value }.toMap()
        var bestScore = computeLayoutScore(bestConfig, allDesks, fullRoster)

        val deskList = desksToPlace.toList()
        repeat(300) {
            val d1 = deskList.random()
            val d2 = deskList.random()
            if (d1 != d2) {
                val currentMap = bestConfig.toMutableMap()
                val temp = currentMap[Pair(d1.row, d1.col)]
                currentMap[Pair(d1.row, d1.col)] = currentMap[Pair(d2.row, d2.col)]
                currentMap[Pair(d2.row, d2.col)] = temp

                val score = computeLayoutScore(currentMap, allDesks, fullRoster)
                if (score > bestScore) {
                    bestScore = score
                    bestConfig = currentMap
                }
            }
        }

        return bestConfig
    }

    private fun computeLayoutScore(
        config: Map<Pair<Int, Int>, StudentEntity?>,
        allDesks: List<DeskEntity>,
        fullRoster: List<StudentEntity>
    ): Double {
        var score = 0.0

        for (entry in config) {
            val r = entry.key.first
            val c = entry.key.second
            val student = entry.value

            if (student == null) continue

            // 1. HEIGHT CONSTRAINT PENALTY: Shorts in front, Talls in back. Max row index corresponds to back of room.
            val heightPref = student.height
            val deskRowRatio = r.toDouble() / (layoutRows.value.coerceAtLeast(1)) // 0.0 to 1.0 (0.0 is front, 1.0 is back)

            score += when (heightPref) {
                "Low" -> if (deskRowRatio < 0.4) 30.0 else -30.0 * (deskRowRatio - 0.4)
                "Tall" -> if (deskRowRatio > 0.6) 30.0 else -30.0 * (0.6 - deskRowRatio)
                else -> 15.0 // Medium has no strict penalty
            }

            // 2. ROW PREFERENCE CONSTRAINT: Front, Middle, Back
            score += when (student.rowPreference) {
                "Front" -> if (r <= layoutRows.value / 3) 25.0 else -15.0
                "Back" -> if (r >= layoutRows.value * 2 / 3) 25.0 else -15.0
                "Middle" -> if (r > layoutRows.value / 3 && r < layoutRows.value * 2 / 3) 15.0 else -10.0
                else -> 10.0
            }

            // 3. SOCIAL MATRIX CONSTRAINTS (loves side-by-side / forbids separate)
            // Look at neighboring desks
            val neighbors = listOf(
                Pair(r, c - 1), Pair(r, c + 1), // lateral neighbors
                Pair(r - 1, c), Pair(r + 1, c)  // front/back neighbors
            )

            for (neighborPair in neighbors) {
                val nr = neighborPair.first
                val nc = neighborPair.second
                val neighborStudent = config[Pair(nr, nc)]
                if (neighborStudent != null) {
                    // Loves peer
                    if (student.loves.contains(neighborStudent.id)) {
                        score += 50.0
                    }
                    // Forbids/separate peer
                    if (student.forbids.contains(neighborStudent.id) || student.separate.contains(neighborStudent.id)) {
                        score -= 100.0
                    }
                }
            }
        }

        return score
    }

    // CRUD Student Controls
    fun addOrUpdateStudent(
        id: String,
        name: String,
        height: String,
        rowPreference: String,
        loves: List<String>,
        forbids: List<String>,
        separate: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            val entity = StudentEntity(
                id = id.ifEmpty { UUID.randomUUID().toString() },
                name = name,
                height = height,
                rowPreference = rowPreference,
                loves = loves,
                forbids = forbids,
                separate = separate,
                notes = notes,
                syncStatus = SyncState.PENDING
            )
            repository.insertStudent(entity)
        }
    }

    fun deleteStudent(id: String) {
        viewModelScope.launch {
            repository.deleteStudent(id)
            // Evict student from any assigned desk
            val deskList = desks.value.map {
                if (it.studentId == id) it.copy(studentId = null) else it
            }
            repository.clearAllDesks()
            repository.insertDesks(deskList)
        }
    }

    // Ingest JSON or text batch formats
    fun processBulkIntake(bulkInput: String) {
        viewModelScope.launch {
            try {
                // Try JSON format
                if (bulkInput.trim().startsWith("[")) {
                    val arr = JSONArray(bulkInput)
                    val newStudents = mutableListOf<StudentEntity>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val lovesList = mutableListOf<String>()
                        val forbidsList = mutableListOf<String>()
                        val sepList = mutableListOf<String>()

                        if (obj.has("loves")) {
                            val l = obj.getJSONArray("loves")
                            for (x in 0 until l.length()) lovesList.add(l.getString(x))
                        }
                        if (obj.has("forbids")) {
                            val f = obj.getJSONArray("forbids")
                            for (x in 0 until f.length()) forbidsList.add(f.getString(x))
                        }
                        if (obj.has("separate")) {
                            val s = obj.getJSONArray("separate")
                            for (x in 0 until s.length()) sepList.add(s.getString(x))
                        }

                        newStudents.add(
                            StudentEntity(
                                id = UUID.randomUUID().toString(),
                                name = obj.getString("name"),
                                height = obj.optString("height", "Medium"),
                                rowPreference = obj.optString("rowPreference", "Middle"),
                                loves = lovesList,
                                forbids = forbidsList,
                                separate = sepList,
                                notes = obj.optString("notes", ""),
                                syncStatus = SyncState.PENDING
                            )
                        )
                    }
                    repository.insertStudents(newStudents)
                } else {
                    // Try comma / newline separation bulk import
                    val lines = bulkInput.split("\n")
                    val newStudents = mutableListOf<StudentEntity>()
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty()) {
                            val parts = trimmed.split(",")
                            val name = parts.firstOrNull()?.trim() ?: continue
                            val height = parts.getOrNull(1)?.trim() ?: "Medium"
                            val rowPref = parts.getOrNull(2)?.trim() ?: "Middle"
                            newStudents.add(
                                StudentEntity(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    height = height,
                                    rowPreference = rowPref,
                                    loves = emptyList(),
                                    forbids = emptyList(),
                                    separate = emptyList(),
                                    notes = "ייבוא מהיר",
                                    syncStatus = SyncState.PENDING
                                )
                            )
                        }
                    }
                    repository.insertStudents(newStudents)
                }
            } catch (e: Exception) {
                Log.e("IntakeEngine", "error parsing bulk intake payload", e)
            }
        }
    }

    // Cloud Manual Sync Force
    fun forceSyncNow() {
        viewModelScope.launch {
            isSyncing.value = true
            syncMessage.value = "הסנכרון לענן הופעל באופן ידני..."
            val result = repository.forceCloudSync()
            isSyncing.value = false
            syncMessage.value = if (result) "הנתונים סונכרנו בהצלחה לענן!" else "סנכרון נכשל."
        }
    }

    // Academic Parsing Engine
    fun parseLibraryDocument(title: String, documentContent: String) {
        viewModelScope.launch {
            isParsingFile.value = true
            val res = GeminiParser.parseAcademicDocument(title, documentContent)
            
            // Build the MC answers JSON
            val mcJsonBuilder = StringBuilder("[")
            res.quiz.forEachIndexed { i, q ->
                mcJsonBuilder.append("{")
                    .append("\"question\":\"").append(q.question).append("\",")
                    .append("\"options\":[")
                q.options.forEachIndexed { j, opt ->
                    mcJsonBuilder.append("\"").append(opt).append("\"")
                    if (j < q.options.lastIndex) mcJsonBuilder.append(",")
                }
                mcJsonBuilder.append("],")
                    .append("\"correctAnswerIndex\":").append(q.correctAnswerIndex)
                    .append("}")
                if (i < res.quiz.lastIndex) mcJsonBuilder.append(",")
            }
            mcJsonBuilder.append("]")

            val material = AcademicMaterialEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                summaryNotes = res.summary,
                lessonTimeline = res.timeline,
                quizJson = mcJsonBuilder.toString(),
                coveragePercentage = res.coveragePercentage,
                timestamp = System.currentTimeMillis()
            )

            repository.insertMaterial(material)
            
            // Populate screen targets
            parsedSummary.value = res.summary
            parsedTimeline.value = res.timeline
            parsedQuiz.value = res.quiz
            isParsingFile.value = false
        }
    }

    fun deleteMaterial(id: String) {
        viewModelScope.launch {
            repository.deleteMaterial(id)
        }
    }

    // Gamification and tools
    fun spinWheel() {
        val list = students.value
        if (list.isEmpty()) return
        viewModelScope.launch {
            isWheelSpinning.value = true
            repeat(15) {
                selectedStudentWheelName.value = list.random().name
                delay(120)
            }
            isWheelSpinning.value = false
        }
    }

    fun incrementScore(studentId: String, amount: Int) {
        viewModelScope.launch {
            val student = students.value.find { it.id == studentId }
            if (student != null) {
                // We keep the score inside the persistent "notes" or extend entities.
                // In notes: "נקודות: X. [notes]"
                val ptsPrefix = "ניקוד: "
                var currentPoints = 0
                var cleanNotes = student.notes
                if (student.notes.startsWith(ptsPrefix)) {
                    val parts = student.notes.split(" | ", limit = 2)
                    currentPoints = parts.first().removePrefix(ptsPrefix).toIntOrNull() ?: 0
                    cleanNotes = parts.getOrNull(1) ?: ""
                }
                val nextScore = currentPoints + amount
                val updatedNotes = "$ptsPrefix$nextScore | $cleanNotes"
                repository.insertStudent(student.copy(notes = updatedNotes))
            }
        }
    }

    fun getStudentPoints(student: StudentEntity): Int {
        val ptsPrefix = "ניקוד: "
        if (student.notes.startsWith(ptsPrefix)) {
            val parts = student.notes.split(" | ", limit = 2)
            return parts.first().removePrefix(ptsPrefix).toIntOrNull() ?: 0
        }
        return 0
    }

    fun startTimerCount(minutes: Int) {
        isTimerActive.value = true
        countdownSeconds.value = minutes * 60
        viewModelScope.launch {
            while (countdownSeconds.value > 0 && isTimerActive.value) {
                delay(1000)
                countdownSeconds.value -= 1
            }
            isTimerActive.value = false
        }
    }

    fun stopTimerCount() {
        isTimerActive.value = false
    }

    fun generateGroupsOfSize(size: Int) {
        val list = students.value.shuffled()
        val result = mutableListOf<List<StudentEntity>>()
        var currentGroup = mutableListOf<StudentEntity>()
        
        for (st in list) {
            currentGroup.add(st)
            if (currentGroup.size == size) {
                result.add(currentGroup)
                currentGroup = mutableListOf()
            }
        }
        if (currentGroup.isNotEmpty()) {
            result.add(currentGroup)
        }
        generatedGroups.value = result
    }

    // Attendance state logging
    fun toggleAttendance(studentId: String, status: String) {
        viewModelScope.launch {
            // Find or insert unique for today YYYY-MM-DD
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            repository.insertLog(
                AttendanceLogEntity(
                    id = "${studentId}_$today",
                    studentId = studentId,
                    date = today,
                    status = status
                )
            )
        }
    }

    private suspend fun loadDemoData() {
        val demo = listOf(
            StudentEntity("1", "אהרון שלום", "Low", "Front", emptyList(), emptyList(), emptyList(), "תלמיד מצטיין, אוהב לשאול שאלות", SyncState.SYNCED),
            StudentEntity("2", "יעקב לוי", "Tall", "Back", listOf("1"), emptyList(), emptyList(), "נדרש לשבת בשורה האחרונה בגלל גובהו", SyncState.SYNCED),
            StudentEntity("3", "משה הכהן", "Medium", "Middle", emptyList(), listOf("1"), emptyList(), "הפרעות קשב וריכוז קלות", SyncState.SYNCED),
            StudentEntity("4", "דוד אבוחצירא", "Low", "Front", emptyList(), emptyList(), listOf("3"), "ניקוד: 5 | חרוץ ומשקיע בלמידה עצמית", SyncState.SYNCED),
            StudentEntity("5", "יוסף אלבז", "Medium", "Middle", listOf("4"), emptyList(), emptyList(), "נוטה לסייע לחבריו לכיתה", SyncState.SYNCED),
            StudentEntity("6", "מאיר אוחנה", "Tall", "Back", emptyList(), emptyList(), emptyList(), "ייבוא מהיר ממאגר מוסדות התורה", SyncState.SYNCED)
        )
        repository.insertStudents(demo)

        // Prepopulate attendance logs
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val logs = listOf(
            AttendanceLogEntity("1_$today", "1", today, "PRESENT"),
            AttendanceLogEntity("2_$today", "2", today, "PRESENT"),
            AttendanceLogEntity("3_$today", "3", today, "ABSENT"),
            AttendanceLogEntity("4_$today", "4", today, "PRESENT"),
            AttendanceLogEntity("5_$today", "5", today, "LATE"),
            AttendanceLogEntity("6_$today", "6", today, "PRESENT")
        )
        repository.insertLogs(logs)
    }

    fun exportToCSV(context: android.content.Context) {
        val allDesks = desks.value
        val allSt = students.value
        val rCount = layoutRows.value
        val cCount = layoutCols.value

        val csvBuilder = java.lang.StringBuilder()
        // CSV Headers
        csvBuilder.append("Row,Column,Type,StudentName,Height,RowPreference,Points,Locked\n")
        for (r in 0 until rCount) {
            for (c in 0 until cCount) {
                val d = allDesks.find { it.row == r && it.col == c }
                if (d != null) {
                    val s = allSt.find { it.id == d.studentId }
                    val nameStr = s?.name ?: ""
                    val heightStr = s?.height ?: ""
                    val rPrefStr = s?.rowPreference ?: ""
                    val pts = if (s != null) getStudentPoints(s) else 0
                    csvBuilder.append("$r,$c,${d.type},\"${nameStr.replace("\"", "\"\"")}\",${heightStr},${rPrefStr},$pts,${d.isLocked}\n")
                } else {
                    csvBuilder.append("$r,$c,WALKWAY,,,,,\n")
                }
            }
        }

        try {
            val file = java.io.File(context.cacheDir, "ClassPro_Seating_Layout.csv")
            file.writeText(csvBuilder.toString(), charset = java.nio.charset.StandardCharsets.UTF_8)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "לוח הושבה כיתתי - ClassPro Seating Layout")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = android.content.Intent.createChooser(intent, "ייצא מפת ישיבה כיתתית - CSV")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ExportCSV", "Error sharing seating layout CSV", e)
        }
    }

    fun exportToPDF(context: android.content.Context) {
        val allDesks = desks.value
        val allSt = students.value
        val rCount = layoutRows.value
        val cCount = layoutCols.value

        val pdfDoc = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(30, 27, 75) // #1E1B4B
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }

        val activeDeskPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(224, 231, 255) // light blue
            style = android.graphics.Paint.Style.FILL
        }

        val emptyDeskPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(243, 244, 246)
            style = android.graphics.Paint.Style.FILL
        }

        val borderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1f
        }

        // Header
        canvas.drawText("מפת ישיבה כיתתית - ClassPro", 40f, 50f, titlePaint)
        val dateText = "תאריך הדפסה: " + java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date())
        canvas.drawText(dateText, 40f, 75f, paint)

        // classroom layout params
        val startX = 40f
        val startY = 130f
        val maxGridWidth = 515f
        val cellSizeX = (maxGridWidth / cCount.coerceAtLeast(1).toFloat()).coerceAtMost(100f)
        val cellSizeY = 55f

        // "Front of Classroom" bar
        val barPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(165, 180, 252)
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(startX, startY - 35f, startX + (cCount * cellSizeX), startY - 15f, barPaint)

        val barTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("חזית הכיתה / הלוח החכם", startX + 20f, startY - 21f, barTextPaint)

        // Draw Desks
        for (r in 0 until rCount) {
            for (c in 0 until cCount) {
                val d = allDesks.find { it.row == r && it.col == c }
                val x = startX + (c * cellSizeX)
                val y = startY + (r * cellSizeY)

                if (d != null) {
                    when (d.type) {
                        "DESK" -> {
                            val student = allSt.find { it.id == d.studentId }
                            if (student != null) {
                                canvas.drawRect(x, y, x + cellSizeX - 4f, y + cellSizeY - 4f, activeDeskPaint)
                                canvas.drawRect(x, y, x + cellSizeX - 4f, y + cellSizeY - 4f, borderPaint)
                                
                                val displayName = if (student.name.length > 14) student.name.substring(0, 12) + ".." else student.name
                                canvas.drawText(displayName, x + 6f, y + 20f, textPaint)

                                val pts = getStudentPoints(student)
                                val infoText = "שורה: ${when(student.rowPreference) { "Front" -> "ק" "Back" -> "א" else -> "אמ" }} | $pts נק'"
                                val subTextPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textSize = 7.5f
                                    isAntiAlias = true
                                }
                                canvas.drawText(infoText, x + 6f, y + 36f, subTextPaint)
                            } else {
                                canvas.drawRect(x, y, x + cellSizeX - 4f, y + cellSizeY - 4f, emptyDeskPaint)
                                canvas.drawRect(x, y, x + cellSizeX - 4f, y + cellSizeY - 4f, borderPaint)
                                val textP = android.graphics.Paint().apply { textSize = 8f; color = android.graphics.Color.GRAY }
                                canvas.drawText("[מושב פנוי]", x + 6f, y + 25f, textP)
                            }
                        }
                        "BLOCK" -> {
                            val blockFill = android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(209, 213, 219)
                                style = android.graphics.Paint.Style.FILL
                            }
                            canvas.drawRect(x, y, x + cellSizeX - 4f, y + cellSizeY - 4f, blockFill)
                            canvas.drawRect(x, y, x + cellSizeX - 4f, y + cellSizeY - 4f, borderPaint)
                            val textP = android.graphics.Paint().apply { textSize = 8f; color = android.graphics.Color.DKGRAY }
                            canvas.drawText("[מחסום]", x + 6f, y + 25f, textP)
                        }
                    }
                }
            }
        }

        // Legend
        val legendY = startY + (rCount * cellSizeY) + 30f
        canvas.drawText("סטטיסטיקת כיתה:", startX, legendY, headerPaint)
        canvas.drawText("סה\"כ תלמידים רשומים: ${allSt.size} | תלמידים שהושבו: ${allDesks.count { it.studentId != null }}", startX, legendY + 20f, paint)

        pdfDoc.finishPage(page)

        try {
            val file = java.io.File(context.cacheDir, "ClassPro_Seating_Layout.pdf")
            val outputStream = java.io.FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            pdfDoc.close()
            outputStream.close()

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "לוח ישיבה כיתתי - Seating Layout PDF")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = android.content.Intent.createChooser(intent, "ייצא כ-PDF")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ExportPDF", "Error saving PDF file", e)
        }
    }
}
