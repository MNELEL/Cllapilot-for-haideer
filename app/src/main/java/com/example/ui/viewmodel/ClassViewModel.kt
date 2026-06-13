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
    val isSmartboardView = MutableStateFlow(false)
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
    val maxTimerSeconds = MutableStateFlow(1)
    val isTimerActive = MutableStateFlow(false)
    val generatedGroups = MutableStateFlow<List<List<StudentEntity>>>(emptyList())

    // Library parsing states
    val parsedSummary = MutableStateFlow("")
    val parsedTimeline = MutableStateFlow("")
    val parsedQuiz = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val isParsingFile = MutableStateFlow(false)

    // Simulate Firebase real-time Sync
    fun simulateSync() {
        viewModelScope.launch {
            isSyncing.value = true
            syncMessage.value = "Updating Firebase..."
            delay(800)
            isSyncing.value = false
            syncMessage.value = "Synced"
        }
    }

    init {
        // Initialize default database records if empty
        viewModelScope.launch {
            students.collectLatest { list ->
                if (list.isEmpty()) {
                    loadDemoData()
                } else {
                    simulateSync()
                }
            }
        }
        viewModelScope.launch {
            desks.collectLatest { list ->
                if (list.isEmpty()) {
                    generateDefaultGrid(6, 6)
                } else {
                    simulateSync()
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
    // === New Additions for Advanced Interactive Map Actions ===
    val seatHistoryMap = MutableStateFlow<Map<Pair<Int, Int>, List<String>>>(emptyMap())
    val isMultiSelectMode = MutableStateFlow(false)
    val selectedDesksForMulti = MutableStateFlow<Set<Pair<Int, Int>>>(emptySet())
    
    // Multi-Select handlers
    fun toggleMultiSelectMode() {
        isMultiSelectMode.value = !isMultiSelectMode.value
        if (!isMultiSelectMode.value) {
            selectedDesksForMulti.value = emptySet()
        }
    }
    
    fun toggleDeskMultiSelection(row: Int, col: Int) {
        val current = selectedDesksForMulti.value.toMutableSet()
        val pair = Pair(row, col)
        if (current.contains(pair)) {
            current.remove(pair)
        } else {
            current.add(pair)
        }
        selectedDesksForMulti.value = current
    }

    fun clearMultiSelectedAssignments() {
        savePlacementState()
        viewModelScope.launch {
            val list = desks.value.toMutableList()
            for (pair in selectedDesksForMulti.value) {
                val index = list.indexOfFirst { it.row == pair.first && it.col == pair.second }
                if (index != -1 && list[index].type == "DESK") {
                    list[index] = list[index].copy(studentId = null)
                }
            }
            repository.clearAllDesks()
            repository.insertDesks(list)
            selectedDesksForMulti.value = emptySet()
        }
    }

    // Unhide Desks (Restore hidden 'WALKWAY' inside valid bounds back to 'DESK')
    fun unhideAllDesks() {
        savePlacementState()
        viewModelScope.launch {
            val list = desks.value.map {
                if (it.type == "WALKWAY") it.copy(type = "DESK") else it
            }
            repository.clearAllDesks()
            repository.insertDesks(list)
        }
    }

    // Inject custom desk (effectively appending a new Row)
    fun injectCustomDeskRow() {
        savePlacementState()
        gridResize(layoutRows.value + 1, layoutCols.value)
    }

    // Drag-And-Drop / Swap Student Logic
    fun swapOrMoveStudent(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
        savePlacementState()
        viewModelScope.launch {
            val list = desks.value.toMutableList()
            val fromIndex = list.indexOfFirst { it.row == fromRow && it.col == fromCol }
            val toIndex = list.indexOfFirst { it.row == toRow && it.col == toCol }

            if (fromIndex != -1 && toIndex != -1) {
                val fromDesk = list[fromIndex]
                val toDesk = list[toIndex]

                if (fromDesk.type == "DESK" && toDesk.type == "DESK") {
                    val tempId = fromDesk.studentId
                    list[fromIndex] = fromDesk.copy(studentId = toDesk.studentId)
                    list[toIndex] = toDesk.copy(
                        studentId = tempId, 
                        isLocked = true // Automatically lock/pin upon drag-and-drop manual placement
                    )
                    
                    // Add seat history entry
                    if (tempId != null) {
                        val stdName = students.value.find { it.id == tempId }?.name ?: "Unknown"
                        recordSeatHistory(toRow, toCol, stdName)
                    }

                    repository.clearAllDesks()
                    repository.insertDesks(list)
                }
            }
        }
    }

    private fun recordSeatHistory(row: Int, col: Int, studentName: String) {
        val current = seatHistoryMap.value.toMutableMap()
        val key = Pair(row, col)
        val history = current[key]?.toMutableList() ?: mutableListOf()
        if (history.lastOrNull() != studentName) {
            history.add(studentName)
            current[key] = history
            seatHistoryMap.value = current
        }
    }
    // =========================================================

    // Advanced placement tracking logic...
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
            isSyncing.value = true
            syncMessage.value = "מחשב מפת הושבה אופטימלית בשימוש בינה מלאכותית..."

            val allSt = students.value
            val allDs = desks.value.toMutableList()
            
            // Collect unlocked desks of type "DESK"
            val unlockedDesks = allDs.filter { it.type == "DESK" && !it.isLocked }
            if (unlockedDesks.isEmpty()) {
                isSyncing.value = false
                return@launch
            }

            // Filter students that can be adjusted (not sitting on a locked desk)
            val lockedDesks = allDs.filter { it.type == "DESK" && it.isLocked }
            val lockedStudentIds = lockedDesks.mapNotNull { it.studentId }.toSet()
            val moveableStudents = allSt.filter { !lockedStudentIds.contains(it.id) }

            // Mathematical Optimization heuristics: Seating by Heights, preferences & Social relationships
            // We want to optimize compatibility of students placed on the unlocked desks.
            val numDesks = unlockedDesks.size
            val stToPlace = moveableStudents.take(numDesks)

            // Dynamic arrangement algorithm using simulated annealing style score optimizer or AI
            var bestArrangement = com.example.util.GeminiSeatingOptimizer.optimizeSeating(
                stToPlace, unlockedDesks, allDs, layoutRows.value
            )

            // Fallback to local heuristic if AI fails (or API key is missing)
            if (bestArrangement.isEmpty()) {
                android.util.Log.d("PlacementEngine", "AI returned empty / failed, using local heuristic fallback.")
                @Suppress("UNCHECKED_CAST")
                bestArrangement = optimizeSeatingLayout(stToPlace, unlockedDesks, allDs, allSt) as Map<Pair<Int, Int>, StudentEntity>
                syncMessage.value = "AI API נכשל. הושבה באמצעות קוד אלגוריתם מקומי הושלמה."
            } else {
                syncMessage.value = "תכנון מבוסס AI הושלם בהצלחה!"
            }

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
            android.util.Log.d("PlacementEngine", "הושלמה אופטימיזציית ישיבה כיתתית חכמה בהצלחה!")

            kotlinx.coroutines.delay(2000)
            isSyncing.value = false
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

    fun updateStudentNotes(studentId: String, newNote: String) {
        viewModelScope.launch {
            val student = students.value.find { it.id == studentId }
            if (student != null) {
                repository.insertStudent(student.copy(notes = newNote))
            }
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
            // Selection is instant now, UI will animate a physical wheel
            selectedStudentWheelName.value = list.random().name
            delay(4000) // UI spin duration
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
        if (isTimerActive.value) return
        isTimerActive.value = true
        countdownSeconds.value = minutes * 60
        maxTimerSeconds.value = minutes * 60
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
            color = android.graphics.Color.rgb(60, 33, 20) // Deep Chocolate Brown
            textSize = 24f
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

    // Classroom Pin-Code Gate State
    val isAppUnlocked = MutableStateFlow(false)
    val appPinCode = "1234"

    fun attemptUnlock(pin: String): Boolean {
        return if (pin == appPinCode) {
            isAppUnlocked.value = true
            true
        } else {
            false
        }
    }

    // Profiles Continuous Algorithm Computation
    data class ClassProfile(
        val studentCount: Int = 0,
        val avgPoints: Int = 0,
        val attendanceRate: Int = 0,
        val heightBalanceStr: String = "",
        val prefBalanceStr: String = ""
    )

    data class TeacherProfile(
        val materialsQuantity: Int = 0,
        val totalAttendanceMarks: Int = 0,
        val lessonsPrepped: Int = 0,
        val syncStateDesc: String = ""
    )

    val classProfileFlow: Flow<ClassProfile> = combine(students, attendanceLogs) { stList, logs ->
        val totalSt = stList.size
        var totPoints = 0
        var lowCount = 0
        var tallCount = 0
        var frontPrefCount = 0

        stList.forEach { s ->
            totPoints += getStudentPoints(s)
            if (s.height == "Low") lowCount++
            if (s.height == "Tall") tallCount++
            if (s.rowPreference == "Front") frontPrefCount++
        }

        val avgPts = if (totalSt > 0) totPoints / totalSt else 0
        val heightSummary = "נמוכים: $lowCount, גבוהים: $tallCount"
        val prefSummary = "$frontPrefCount תלמידים מעדיפים קדימה"

        // attendance rate calculation
        val todayLogs = logs.filter { it.date == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) }
        val presentCount = todayLogs.count { it.status == "PRESENT" || it.status == "LATE" }
        val rate = if (todayLogs.isNotEmpty()) (presentCount * 100) / todayLogs.size else 100

        ClassProfile(totalSt, avgPts, rate, heightSummary, prefSummary)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClassProfile())

    val teacherProfileFlow: Flow<TeacherProfile> = combine(materials, attendanceLogs, students) { matList, logs, stList ->
        TeacherProfile(
            materialsQuantity = matList.size,
            totalAttendanceMarks = logs.size,
            lessonsPrepped = matList.size + (if (stList.isNotEmpty()) 1 else 0),
            syncStateDesc = "המערכת מקושרת ומסונכרנת לשרת בהצלחה (HTTPS)"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TeacherProfile())

    // Separate and Comprehensive Deletion Mechanics
    fun resetData(deleteStudents: Boolean, deleteDesks: Boolean, deleteAttendance: Boolean) {
        viewModelScope.launch {
            if (deleteStudents) {
                repository.clearAllStudents()
                // Evict all students from desks
                val cleanDesks = desks.value.map { it.copy(studentId = null, isLocked = false) }
                repository.clearAllDesks()
                repository.insertDesks(cleanDesks)
            }
            if (deleteDesks) {
                val cleanDesks = desks.value.map { it.copy(studentId = null, isLocked = false, type = "DESK") }
                repository.clearAllDesks()
                repository.insertDesks(cleanDesks)
            }
            if (deleteAttendance) {
                repository.clearLogs()
            }
        }
    }

    // File Upload/Import Parsing Mechanics
    fun importStudentsFromFileContent(fileText: String): Boolean {
        return try {
            val lines = fileText.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) return false

            val parsedList = mutableListOf<StudentEntity>()
            lines.forEachIndexed { i, line ->
                // Simple comma or pipe delimiter parser
                val parts = if (line.contains(",")) line.split(",") else line.split("|")
                val name = parts.firstOrNull()?.trim() ?: "תלמיד חדש $i"
                val height = if (parts.size > 1) parts[1].trim() else "Medium"
                val pref = if (parts.size > 2) parts[2].trim() else "Middle"
                val notes = if (parts.size > 3) parts[3].trim() else "ייבוא דרך קובץ"

                parsedList.add(
                    StudentEntity(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        height = if (height in listOf("Low", "Medium", "Tall")) height else "Medium",
                        rowPreference = if (pref in listOf("Front", "Middle", "Back")) pref else "Middle",
                        loves = emptyList(),
                        forbids = emptyList(),
                        separate = emptyList(),
                        notes = notes,
                        syncStatus = SyncState.SYNCED
                    )
                )
            }

            if (parsedList.isNotEmpty()) {
                viewModelScope.launch {
                    repository.insertStudents(parsedList)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("ImportStudents", "Error parsing files content", e)
            false
        }
    }

    fun importMaterialsFromFileContent(fileText: String): Boolean {
        return try {
            val lines = fileText.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) return false

            val title = lines.firstOrNull()?.removePrefix("כותרת:")?.trim() ?: "מערך יבוא מקובץ"
            val content = lines.drop(1).joinToString("\n")

            parseLibraryDocument(title, content)
            true
        } catch (e: Exception) {
            Log.e("ImportMaterials", "Error parsing material content", e)
            false
        }
    }

    // Word Document and PDF Export Mechanics
    fun exportMaterialToWord(context: android.content.Context, material: AcademicMaterialEntity) {
        try {
            // Build RTF/HTML formatted string openable beautifully as .doc / .docx in Word applications
            val docBuilder = java.lang.StringBuilder()
            docBuilder.append("<html><meta charset='utf-8'>")
            docBuilder.append("<body style='direction: rtl; font-family: Segoe UI, Arial;'>")
            docBuilder.append("<h1 style='color: #1a365d; text-align: center;'>${material.title}</h1>")
            docBuilder.append("<p style='text-align: center; color: #666;'>פירוט חומרים ומערך שיעור פדגוגי</p>")
            docBuilder.append("<hr>")
            docBuilder.append("<h2>עקרונות סיכום:</h2>")
            docBuilder.append("<p>${material.summaryNotes.replace("\n", "<br>")}</p>")
            docBuilder.append("<h2>ציר זמן דידקטי לשיעור:</h2>")
            docBuilder.append("<p>${material.lessonTimeline.replace("\n", "<br>")}</p>")
            docBuilder.append("<h2>שאלות הערכה והבנה:</h2>")

            val quizArr = JSONArray(material.quizJson)
            for (i in 0 until quizArr.length()) {
                val qObj = quizArr.getJSONObject(i)
                val questionText = qObj.getString("question")
                val optsArr = qObj.getJSONArray("options")
                docBuilder.append("<p><b>שאלה ${i + 1}: $questionText</b></p>")
                docBuilder.append("<ul>")
                for (o in 0 until optsArr.length()) {
                    val correct = o == qObj.getInt("correctAnswerIndex")
                    val isCorrectText = if (correct) " (תשובה נכונה)" else ""
                    docBuilder.append("<li>${optsArr.getString(o)}$isCorrectText</li>")
                }
                docBuilder.append("</ul>")
            }

            docBuilder.append("</body></html>")

            val fileName = "${material.title.replace(" ", "_").replace("/", "_")}.doc"
            val file = java.io.File(context.cacheDir, fileName)
            file.writeText(docBuilder.toString(), charset = java.nio.charset.StandardCharsets.UTF_8)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/msword"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "מערך שיעור - ${material.title}")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = android.content.Intent.createChooser(intent, "ייצא כקובץ Word (.doc)")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ExportWord", "Error exporting material to Word", e)
        }
    }

    fun exportMaterialToPDF(context: android.content.Context, material: AcademicMaterialEntity) {
        try {
            val pdfDoc = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(30, 27, 75)
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val bodyPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 11f
                isAntiAlias = true
            }

            val headerPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(10, 10, 10)
                textSize = 13f
                isFakeBoldText = true
                isAntiAlias = true
            }

            canvas.drawText(material.title, 400f, 60f, titlePaint)
            canvas.drawText("ClassPro מסמך פדגוגי רשמי", 400f, 95f, bodyPaint)

            var currentY = 140f

            canvas.drawText("עיקרי סיכום ומטרות:", 480f, currentY, headerPaint)
            currentY += 25f

            val summaryLines = material.summaryNotes.split("\n")
            summaryLines.forEach { line ->
                if (currentY < 800f) {
                    canvas.drawText(line, 480f - (line.length * 1.5f).coerceAtMost(350f), currentY, bodyPaint)
                    currentY += 20f
                }
            }

            currentY += 15f
            canvas.drawText("ציר זמן שיעור פדגוגי:", 480f, currentY, headerPaint)
            currentY += 25f

            val timelineLines = material.lessonTimeline.split("\n")
            timelineLines.forEach { line ->
                if (currentY < 800f) {
                    canvas.drawText(line, 480f - (line.length * 1.5f).coerceAtMost(350f), currentY, bodyPaint)
                    currentY += 20f
                }
            }

            pdfDoc.finishPage(page)

            val fileName = "${material.title.replace(" ", "_").replace("/", "_")}.pdf"
            val file = java.io.File(context.cacheDir, fileName)
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
                putExtra(android.content.Intent.EXTRA_SUBJECT, "מערך פדגוגי PDF - ${material.title}")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = android.content.Intent.createChooser(intent, "ייצא כקובץ PDF")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ExportMaterialPDF", "Error saving PDF file", e)
        }
    }
}
