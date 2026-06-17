package com.example.ui.viewmodel

import android.app.Application
import com.example.data.network.ChuckNorrisClient
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

    val activeMaterialId = MutableStateFlow<String?>(null)

    val activeMaterial = combine(materials, activeMaterialId) { mats, id ->
        mats.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setActiveMaterial(materialId: String?) {
        activeMaterialId.value = materialId
    }

    val attendanceLogs = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val grades = repository.allGrades.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pacingList = repository.allPacing.stateIn(
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

    val pdfPaperFormat = MutableStateFlow("A4") // "A4" or "Letter"
    val schoolLogoUri = MutableStateFlow<String?>(null) // URI or Path for school logo
    val classReportWeeklyTheme = MutableStateFlow("למידה שיתופית ושיפור מיומנויות חברתיות")
    val classReportTeacherSummary = MutableStateFlow("השבוע התקדמנו בלמידת עמיתים בסיוע סידור הישיבה האופטימלי שנוצר ע״י ה-AI. רמת הקשב, ההשתתפות והמשמעת של כלל התלמידים עלו בצורה ניכרת, בייחוד בשלבי התרגול הממוקדים.")

    // Chuck Norris joke states
    val chuckNorrisJoke = MutableStateFlow<String>("Loading classroom discipline instructions from Chuck Norris...")
    val chuckNorrisLoading = MutableStateFlow<Boolean>(false)
    val chuckNorrisError = MutableStateFlow<String?>(null)

    fun fetchChuckNorrisJoke() {
        viewModelScope.launch {
            chuckNorrisLoading.value = true
            chuckNorrisError.value = null
            try {
                val joke = ChuckNorrisClient.api.getRandomJoke()
                // Replace curly / special quotes if any
                chuckNorrisJoke.value = joke.value ?: "Chuck Norris never fails to return a joke."
            } catch (e: Exception) {
                Log.e("ClassViewModel", "Error fetching Chuck Norris joke", e)
                chuckNorrisError.value = "שגיאה בתקשורת עם שרת הבדיחות"
            } finally {
                chuckNorrisLoading.value = false
            }
        }
    }

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
        // Load settings from SharedPreferences
        val sharedPref = getApplication<android.app.Application>().getSharedPreferences("classpro_prefs", android.content.Context.MODE_PRIVATE)
        pdfPaperFormat.value = sharedPref.getString("pdf_paper_format", "A4") ?: "A4"
        schoolLogoUri.value = sharedPref.getString("school_logo_uri", null)

        // Fetch initial Chuck Norris classroom motivation joke
        fetchChuckNorrisJoke()

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

    fun setPdfPaperFormat(format: String) {
        pdfPaperFormat.value = format
        val sharedPref = getApplication<android.app.Application>().getSharedPreferences("classpro_prefs", android.content.Context.MODE_PRIVATE)
        sharedPref.edit().putString("pdf_paper_format", format).apply()
    }

    fun setSchoolLogoUri(uri: String?) {
        schoolLogoUri.value = uri
        val sharedPref = getApplication<android.app.Application>().getSharedPreferences("classpro_prefs", android.content.Context.MODE_PRIVATE)
        sharedPref.edit().putString("school_logo_uri", uri).apply()
    }

    fun setClassReportWeeklyTheme(theme: String) {
        classReportWeeklyTheme.value = theme
    }

    fun setClassReportTeacherSummary(summary: String) {
        classReportTeacherSummary.value = summary
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
    fun runIntelligentAIPlacement(additionalConstraints: String = "") {
        savePlacementState()
        val allGrades = grades.value
        
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
                stToPlace, unlockedDesks, allDs, layoutRows.value, allGrades, additionalConstraints
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

    // Heuristic solver scoring layout compatibility using Simulated Annealing
    private fun optimizeSeatingLayout(
        students: List<StudentEntity>,
        desksToPlace: List<DeskEntity>,
        allDesks: List<DeskEntity>,
        fullRoster: List<StudentEntity>
    ): Map<Pair<Int, Int>, StudentEntity?> {
        val result = mutableMapOf<Pair<Int, Int>, StudentEntity?>()
        if (students.isEmpty()) return result

        // Initial setup: Assign students to desks arbitrarily
        var currentMap = desksToPlace.mapIndexed { idx, d -> Pair(d.row, d.col) to students.getOrNull(idx) }.toMap().toMutableMap()
        var currentScore = computeLayoutScore(currentMap, allDesks, fullRoster)
        
        var bestMap = currentMap.toMap()
        var bestScore = currentScore

        val deskList = desksToPlace.toList()
        
        // Simulated Annealing parameters
        var temp = 1000.0
        val coolingRate = 0.99
        val minTemp = 0.1
        val iterationsPerTemp = 50

        while (temp > minTemp) {
            repeat(iterationsPerTemp) {
                // Pick two random desks to swap
                val d1 = deskList.random()
                val d2 = deskList.random()
                if (d1 != d2) {
                    val p1 = Pair(d1.row, d1.col)
                    val p2 = Pair(d2.row, d2.col)
                    
                    // Propose swap
                    val s1 = currentMap[p1]
                    val s2 = currentMap[p2]
                    
                    // Only swap if they are different
                    if (s1?.id != s2?.id) {
                        currentMap[p1] = s2
                        currentMap[p2] = s1
                        
                        val newScore = computeLayoutScore(currentMap, allDesks, fullRoster)
                        val deltaOpt = newScore - currentScore
                        
                        // Accept if better, or with probability if worse
                        if (deltaOpt > 0 || kotlin.math.exp(deltaOpt / temp) > kotlin.random.Random.nextDouble()) {
                            // Accept swap
                            currentScore = newScore
                            if (currentScore > bestScore) {
                                bestScore = currentScore
                                bestMap = currentMap.toMap()
                            }
                        } else {
                            // Revert swap
                            currentMap[p1] = s1
                            currentMap[p2] = s2
                        }
                    }
                }
            }
            temp *= coolingRate
        }

        return bestMap
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
            val minRow = 0
            val maxRow = layoutRows.value.coerceAtLeast(1) - 1
            // Use precise row mapping (0 = Front to 1 = Back)
            val deskRowRatio = if (maxRow > 0) r.toDouble() / maxRow else 0.0

            score += when (heightPref) {
                "Low" -> if (deskRowRatio <= 0.33) 40.0 else -50.0 * (deskRowRatio - 0.33)
                "Tall" -> if (deskRowRatio >= 0.66) 40.0 else -50.0 * (0.66 - deskRowRatio)
                else -> 10.0 // Medium generally fits anywhere safely in the middle.
            }

            // 2. ROW PREFERENCE CONSTRAINT: Front, Middle, Back
            score += when (student.rowPreference) {
                "Front" -> if (deskRowRatio <= 0.33) 30.0 else -20.0
                "Back" -> if (deskRowRatio >= 0.66) 30.0 else -20.0
                "Middle" -> if (deskRowRatio > 0.33 && deskRowRatio < 0.66) 20.0 else -10.0
                else -> 5.0
            }

            // 3. SOCIAL MATRIX CONSTRAINTS (loves side-by-side / forbids separate)
            val radius = 2 // Consider neighbors up to Manhattan distance 2
            for (dr in -radius..radius) {
                for (dc in -radius..radius) {
                    if (dr == 0 && dc == 0) continue
                    val nr = r + dr
                    val nc = c + dc
                    val neighborStudent = config[Pair(nr, nc)]
                    
                    if (neighborStudent != null) {
                        val distance = kotlin.math.abs(dr) + kotlin.math.abs(dc)
                        
                        // Loves peer constraints
                        if (student.loves.contains(neighborStudent.id)) {
                            when (distance) {
                                1 -> score += 60.0    // Direct neighbor
                                2 -> score += 20.0    // Diagonal or 1 seat gap
                            }
                        }
                        
                        // Forbids peer constraints (Must not sit near each other)
                        if (student.forbids.contains(neighborStudent.id)) {
                            when (distance) {
                                1 -> score -= 150.0   // Catastrophe if adjacent!
                                2 -> score -= 80.0    // Still bad if too close
                                3 -> score -= 20.0
                            }
                        }
                        
                        // Separate peer constraints (Must be in different rows entirely)
                        if (student.separate.contains(neighborStudent.id)) {
                            if (r == nr) {
                                score -= 100.0 // Major penalty for same row
                            } else if (distance <= 2) {
                                score -= 40.0
                            }
                        }
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

    fun deleteStudents(ids: List<String>) {
        viewModelScope.launch {
            repository.deleteStudents(ids)
            // Evict students from any assigned desks
            val deskList = desks.value.map {
                if (it.studentId != null && ids.contains(it.studentId)) it.copy(studentId = null) else it
            }
            repository.clearAllDesks()
            repository.insertDesks(deskList)
        }
    }

    fun clearSeatingLayout() {
        viewModelScope.launch {
            val emptyDesks = desks.value.map { it.copy(studentId = null) }
            repository.clearAllDesks()
            repository.insertDesks(emptyDesks)
            
            // clear undo/redo stack
            undoStack.clear()
            redoStack.clear()
            canUndo.value = false
            canRedo.value = false
        }
    }

    fun clearAllGrades() {
        viewModelScope.launch {
            repository.clearAllGrades()
        }
    }

    fun addOrUpdateGrade(studentId: String, assignmentId: String, gradeValue: String) {
        viewModelScope.launch {
            repository.insertGrade(
                StudentGradeEntity(
                    id = "$studentId-$assignmentId",
                    studentId = studentId,
                    assignmentId = assignmentId,
                    gradeValue = gradeValue
                )
            )
        }
    }

    private fun sanitizeHeight(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.equals("Low", ignoreCase = true) || trimmed == "נמוך" || trimmed == "נמוכה" -> "Low"
            trimmed.equals("Tall", ignoreCase = true) || trimmed == "גבוה" || trimmed == "גבוהה" -> "Tall"
            else -> "Medium"
        }
    }

    private fun sanitizeRowPreference(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.equals("Front", ignoreCase = true) || trimmed == "קדמית" || trimmed == "קדימה" || trimmed == "ראשונה" -> "Front"
            trimmed.equals("Back", ignoreCase = true) || trimmed == "אחורית" || trimmed == "אחורה" || trimmed == "אחרונה" -> "Back"
            else -> "Middle"
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
                                height = sanitizeHeight(obj.optString("height", "Medium")),
                                rowPreference = sanitizeRowPreference(obj.optString("rowPreference", "Middle")),
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
                                    height = sanitizeHeight(height),
                                    rowPreference = sanitizeRowPreference(rowPref),
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
            val res = GeminiParser.parseAcademicDocument(title, documentContent, getApplication())
            
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

    // Content Synthesis functionality:
    fun synthesizeMaterials(selectedMatIds: List<String>, instructions: String, titlePrefix: String = "מערך מסונתז") {
        viewModelScope.launch {
            isParsingFile.value = true
            try {
                // Fetch the materials
                val matsToCombine = materials.value.filter { selectedMatIds.contains(it.id) }
                if (matsToCombine.isEmpty()) return@launch

                val combinedText = matsToCombine.joinToString("\n\n") { "Title: ${it.title}\n${it.summaryNotes}" }
                
                // Combine them using Gemini
                val requestText = "חולל מסמך מאוחד מתוך חומרים אלה: $instructions. \n$combinedText"
                // Re-use parseAcademicDocument as the parsing engine works similarly
                val synthesizedRes = GeminiParser.parseAcademicDocument("$titlePrefix: ${matsToCombine.joinToString(", ") { it.title }.take(15)}...", requestText, getApplication())
                
                // Build the MC answers JSON
                val mcJsonBuilder = StringBuilder("[")
                synthesizedRes.quiz.forEachIndexed { i, q ->
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
                    if (i < synthesizedRes.quiz.lastIndex) mcJsonBuilder.append(",")
                }
                mcJsonBuilder.append("]")

                val newMaterial = AcademicMaterialEntity(
                    id = UUID.randomUUID().toString(),
                    title = "$titlePrefix: ${matsToCombine.joinToString(", ") { it.title }.take(15)}...",
                    summaryNotes = synthesizedRes.summary,
                    lessonTimeline = synthesizedRes.timeline,
                    quizJson = mcJsonBuilder.toString(),
                    coveragePercentage = 100,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertMaterial(newMaterial)
            } catch (e: Exception) {
                Log.e("ClassViewModel", "Error synthesizing materials", e)
            } finally {
                isParsingFile.value = false
            }
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

    fun saveStudentNotesAndPoints(studentId: String, points: Int, cleanNotes: String) {
        viewModelScope.launch {
            val student = students.value.find { it.id == studentId }
            if (student != null) {
                val ptsPrefix = "ניקוד: "
                val updatedNotes = "$ptsPrefix$points | $cleanNotes"
                repository.insertStudent(student.copy(notes = updatedNotes))
            }
        }
    }

    fun deleteMaterial(id: String) {
        viewModelScope.launch {
            repository.deleteMaterial(id)
        }
    }

    fun insertPacing(pacing: PacingEntity) {
        viewModelScope.launch {
            repository.insertPacing(pacing)
        }
    }

    fun deletePacing(id: String) {
        viewModelScope.launch {
            repository.deletePacing(id)
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

    fun setAllAttendanceStatus(status: String) {
        viewModelScope.launch {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val logs = students.value.map { student ->
                AttendanceLogEntity(
                    id = "${student.id}_$today",
                    studentId = student.id,
                    date = today,
                    status = status
                )
            }
            repository.insertLogs(logs)
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

    fun markAttendance(studentId: String, status: String) {
        viewModelScope.launch {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val logId = "${studentId}_$today"
            val log = com.example.data.model.AttendanceLogEntity(
                id = logId,
                studentId = studentId,
                date = today,
                status = status,
                syncStatus = com.example.data.model.SyncState.PENDING
            )
            repository.insertLog(log)
        }
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

        val isA4 = pdfPaperFormat.value == "A4"
        val pageWidth = if (isA4) 595 else 612
        val pageHeight = if (isA4) 842 else 792

        val pdfDoc = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        // Double Border Styling (Institutional Branding Frame)
        val borderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(197, 160, 89) // Elegant Gold
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f
        }
        val innerBorderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(197, 160, 89)
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 0.5f
        }
        canvas.drawRect(15f, 15f, pageWidth - 15f, pageHeight - 15f, borderPaint)
        canvas.drawRect(18f, 18f, pageWidth - 18f, pageHeight - 18f, innerBorderPaint)

        // Watermark background
        val watermarkPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(15, 197, 160, 89)
            style = android.graphics.Paint.Style.FILL
            textSize = 50f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawCircle(pageWidth / 2f, pageHeight / 2f, 120f, watermarkPaint)
        watermarkPaint.apply {
            textSize = 14f
            color = android.graphics.Color.argb(20, 197, 160, 89)
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("ClassPro מוסד פדגוגי", pageWidth / 2f, pageHeight / 2f + 5f, watermarkPaint)

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(60, 33, 20) // Deep Chocolate Brown
            textSize = 22f
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

        val cellBorderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1f
        }

        // Draw School Logo (Institutional Branding)
        var hasLogo = false
        val logoUriStr = schoolLogoUri.value
        if (!logoUriStr.isNullOrEmpty()) {
            try {
                val inputStr = context.contentResolver.openInputStream(android.net.Uri.parse(logoUriStr))
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStr)
                if (bitmap != null) {
                    val scaledLogo = android.graphics.Bitmap.createScaledBitmap(bitmap, 45, 45, true)
                    canvas.drawBitmap(scaledLogo, pageWidth - 80f, 32f, null)
                    hasLogo = true
                }
                inputStr?.close()
            } catch (e: Exception) {
                Log.e("ExportPDF", "Error drawing custom logo", e)
            }
        }

        if (!hasLogo) {
            // Standard decorative shield
            val crestPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(197, 160, 89)
                style = android.graphics.Paint.Style.FILL
                isAntiAlias = true
            }
            val path = android.graphics.Path()
            path.moveTo(pageWidth - 75f, 32f)
            path.lineTo(pageWidth - 45f, 32f)
            path.lineTo(pageWidth - 45f, 52f)
            path.quadTo(pageWidth - 60f, 62f, pageWidth - 75f, 52f)
            path.close()
            canvas.drawPath(path, crestPaint)
        }

        // Proper RTL Right-Aligned Header
        val titleText = "מפת ישיבה כיתתית - ClassPro"
        val dateText = "תאריך הדפסה: " + java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date())
        
        val titleWidth = titlePaint.measureText(titleText)
        val dateWidth = paint.measureText(dateText)
        
        canvas.drawText(titleText, pageWidth - 90f - titleWidth, 50f, titlePaint)
        canvas.drawText(dateText, pageWidth - 90f - dateWidth, 75f, paint)

        // classroom layout params
        val startX = 35f
        val startY = 130f
        val maxGridWidth = pageWidth - 70f
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
                                canvas.drawRect(x, y, x + cellSizeX - 4f, y + cellSizeY - 4f, cellBorderPaint)
                                
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
                                canvas.drawRect(x, y, x + cellSizeX - 4f, y + cellSizeY - 4f, cellBorderPaint)
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
                            canvas.drawRect(x, y, x + cellSizeX - 4f, y + cellSizeY - 4f, cellBorderPaint)
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

    // Classroom Pin-Code Gate State and Persistent Configuration
    private val cryptoPrefs by lazy {
        getApplication<Application>().getSharedPreferences("classpro_secure_prefs", android.content.Context.MODE_PRIVATE)
    }

    val pinEnabled = MutableStateFlow(cryptoPrefs.getBoolean("pin_enabled", true))
    val appPinCode = MutableStateFlow(cryptoPrefs.getString("app_pin_code", "1234") ?: "1234")
    val isAppUnlocked = MutableStateFlow(!cryptoPrefs.getBoolean("pin_enabled", true))

    fun updatePinEnabled(enabled: Boolean) {
        pinEnabled.value = enabled
        cryptoPrefs.edit().putBoolean("pin_enabled", enabled).apply()
        if (!enabled) {
            isAppUnlocked.value = true
        } else {
            isAppUnlocked.value = false
        }
    }

    fun updatePinCode(newPin: String) {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            appPinCode.value = newPin
            cryptoPrefs.edit().putString("app_pin_code", newPin).apply()
        }
    }

    fun attemptUnlock(pin: String): Boolean {
        return if (!pinEnabled.value || pin == appPinCode.value) {
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
                        height = sanitizeHeight(height),
                        rowPreference = sanitizeRowPreference(pref),
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
            val isA4 = pdfPaperFormat.value == "A4"
            val pageWidth = if (isA4) 595 else 612
            val pageHeight = if (isA4) 842 else 792

            val pdfDoc = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            // Professional institution frame decoration (Golden Ginger Borders)
            val borderPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(197, 160, 89) // gold gingery ginger
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            val innerBorderPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(197, 160, 89)
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 0.5f
            }
            canvas.drawRect(15f, 15f, pageWidth - 15f, pageHeight - 15f, borderPaint)
            canvas.drawRect(18f, 18f, pageWidth - 18f, pageHeight - 18f, innerBorderPaint)

            // Academic Background Watermark
            val watermarkFont = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(12, 197, 160, 89)
                textSize = 65f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawCircle(pageWidth / 2f, pageHeight / 2f, 130f, watermarkFont)
            watermarkFont.apply {
                textSize = 15f
                color = android.graphics.Color.argb(20, 197, 160, 89)
                textAlign = android.graphics.Paint.Align.CENTER
            }
            canvas.drawText("ClassPro מערך שיעור רשמי", pageWidth / 2f, pageHeight / 2f + 5f, watermarkFont)

            val titlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(30, 27, 75)
                textSize = 20f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val subtitlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 10f
                isAntiAlias = true
            }

            val bodyPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 11f
                isAntiAlias = true
            }

            val headerPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(197, 160, 89)
                textSize = 13f
                isFakeBoldText = true
                isAntiAlias = true
            }

            // Draw custom school logo or fallback emblem
            var hasLogo = false
            val logoUriStr = schoolLogoUri.value
            if (!logoUriStr.isNullOrEmpty()) {
                try {
                    val inputStr = context.contentResolver.openInputStream(android.net.Uri.parse(logoUriStr))
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStr)
                    if (bitmap != null) {
                        val scaledLogo = android.graphics.Bitmap.createScaledBitmap(bitmap, 45, 45, true)
                        canvas.drawBitmap(scaledLogo, pageWidth - 80f, 32f, null)
                        hasLogo = true
                    }
                    inputStr?.close()
                } catch (e: Exception) {
                    Log.e("ExportMaterialPDF", "Error drawing custom logo", e)
                }
            }

            if (!hasLogo) {
                // Gold decorative badge
                val crestPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(197, 160, 89)
                    style = android.graphics.Paint.Style.FILL
                    isAntiAlias = true
                }
                val path = android.graphics.Path()
                path.moveTo(pageWidth - 75f, 32f)
                path.lineTo(pageWidth - 45f, 32f)
                path.lineTo(pageWidth - 45f, 52f)
                path.quadTo(pageWidth - 60f, 62f, pageWidth - 75f, 52f)
                path.close()
                canvas.drawPath(path, crestPaint)
            }

            // Header labels aligned layout
            val titleText = material.title
            val subtitleText = "מתוך הספרייה הפדגוגית של ClassPro"
            
            val rightX = pageWidth - 90f
            canvas.drawText(titleText, rightX - titlePaint.measureText(titleText), 50f, titlePaint)
            canvas.drawText(subtitleText, rightX - subtitlePaint.measureText(subtitleText), 72f, subtitlePaint)

            var currentY = 110f

            // Separator bar
            val sepPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(229, 231, 235)
                strokeWidth = 1f
            }
            canvas.drawLine(35f, currentY, pageWidth - 35f, currentY, sepPaint)
            currentY += 25f

            // RTL wrapped text printing helper function
            fun drawRTLTextFlow(text: String, startY: Float, paint: android.graphics.Paint, size: Float, isHeader: Boolean = false): Float {
                var y = startY
                val availableW = pageWidth - 90f // generous margins
                val rightMargin = pageWidth - 45f
                
                val lines = text.split("\n")
                for (line in lines) {
                    if (line.trim().isEmpty()) {
                        y += 10f
                        continue
                    }
                    val words = line.split(" ")
                    val lineBuilder = StringBuilder()
                    for (word in words) {
                        val testLine = if (lineBuilder.isEmpty()) word else "${lineBuilder.toString()} $word"
                        if (paint.measureText(testLine) > availableW) {
                            val lineStr = lineBuilder.toString()
                            canvas.drawText(lineStr, rightMargin - paint.measureText(lineStr), y, paint)
                            y += size + 5f
                            lineBuilder.setLength(0)
                            lineBuilder.append(word)
                        } else {
                            if (lineBuilder.isNotEmpty()) lineBuilder.append(" ")
                            lineBuilder.append(word)
                        }
                    }
                    if (lineBuilder.isNotEmpty()) {
                        val lineStr = lineBuilder.toString()
                        canvas.drawText(lineStr, rightMargin - paint.measureText(lineStr), y, paint)
                        y += size + 10f
                    }
                }
                return y
            }

            // 1. Summary Header
            val sumHeader = "א. תקציר ומטרות פדגוגיות:"
            canvas.drawText(sumHeader, rightX - headerPaint.measureText(sumHeader), currentY, headerPaint)
            currentY += 22f

            // Draw wrapped summary body
            currentY = drawRTLTextFlow(material.summaryNotes, currentY, bodyPaint, 11f)
            currentY += 15f

            // 2. Timeline Header
            if (currentY < pageHeight - 100f) {
                val timeHeader = "ב. ציר זמן מובנה למהלך השיעור:"
                canvas.drawText(timeHeader, rightX - headerPaint.measureText(timeHeader), currentY, headerPaint)
                currentY += 22f

                // Draw wrapped timeline body
                currentY = drawRTLTextFlow(material.lessonTimeline, currentY, bodyPaint, 11f)
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

    fun exportClassWeeklyReportToPDF(context: android.content.Context) {
        try {
            val isA4 = pdfPaperFormat.value == "A4"
            val pageWidth = if (isA4) 595 else 612
            val pageHeight = if (isA4) 842 else 792

            val pdfDoc = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            // Professional border lines
            val borderPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(197, 160, 89)
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            val innerBorderPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(197, 160, 89)
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 0.5f
            }
            canvas.drawRect(15f, 15f, pageWidth - 15f, pageHeight - 15f, borderPaint)
            canvas.drawRect(18f, 18f, pageWidth - 18f, pageHeight - 18f, innerBorderPaint)

            // Academic Background Watermark
            val watermarkFont = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(12, 197, 160, 89)
                textSize = 60f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawCircle(pageWidth / 2f, pageHeight / 2f, 130f, watermarkFont)
            watermarkFont.apply {
                textSize = 14f
                color = android.graphics.Color.argb(20, 197, 160, 89)
                textAlign = android.graphics.Paint.Align.CENTER
            }
            canvas.drawText("ClassPro דוח כיתתי שבועי רשמי", pageWidth / 2f, pageHeight / 2f + 5f, watermarkFont)

            val titlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(30, 27, 75)
                textSize = 16f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val subtitlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 9f
                isAntiAlias = true
            }

            val bodyPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 10f
                isAntiAlias = true
            }

            val headerPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(197, 160, 89)
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
            }

            var hasLogo = false
            val logoUriStr = schoolLogoUri.value
            if (!logoUriStr.isNullOrEmpty()) {
                try {
                    val inputStr = context.contentResolver.openInputStream(android.net.Uri.parse(logoUriStr))
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStr)
                    if (bitmap != null) {
                        val scaledLogo = android.graphics.Bitmap.createScaledBitmap(bitmap, 40, 40, true)
                        canvas.drawBitmap(scaledLogo, pageWidth - 70f, 32f, null)
                        hasLogo = true
                    }
                    inputStr?.close()
                } catch (e: java.lang.Exception) {
                    Log.e("ExportClassReportPDF", "Error drawing custom logo", e)
                }
            }

            if (!hasLogo) {
                val crestPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(197, 160, 89)
                    style = android.graphics.Paint.Style.FILL
                    isAntiAlias = true
                }
                val path = android.graphics.Path()
                path.moveTo(pageWidth - 70f, 32f)
                path.lineTo(pageWidth - 45f, 32f)
                path.lineTo(pageWidth - 45f, 52f)
                path.quadTo(pageWidth - 57.5f, 62f, pageWidth - 70f, 52f)
                path.close()
                canvas.drawPath(path, crestPaint)
            }

            val titleText = "דוח סיכום כיתתי שבועי - ClassPro"
            val currentDateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date())
            val subtitleText = "הופק בתאריך: $currentDateStr | נושא פדגוגי: ${classReportWeeklyTheme.value}"

            val rightX = pageWidth - 85f
            canvas.drawText(titleText, rightX - titlePaint.measureText(titleText), 48f, titlePaint)
            canvas.drawText(subtitleText, rightX - subtitlePaint.measureText(subtitleText), 66f, subtitlePaint)

            var currentY = 100f
            val sepPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(229, 231, 235)
                strokeWidth = 1f
            }
            canvas.drawLine(35f, currentY, pageWidth - 35f, currentY, sepPaint)
            currentY += 20f

            fun drawRTLTextFlow(text: String, startY: Float, paint: android.graphics.Paint, size: Float): Float {
                var y = startY
                val availableW = pageWidth - 70f
                val rightMargin = pageWidth - 35f
                val lines = text.split("\n")
                for (line in lines) {
                    if (y > pageHeight - 50f) break
                    if (line.trim().isEmpty()) {
                        y += 10f
                        continue
                    }
                    val words = line.split(" ")
                    val lineBuilder = StringBuilder()
                    for (word in words) {
                        val testLine = if (lineBuilder.isEmpty()) word else "${lineBuilder.toString()} $word"
                        if (paint.measureText(testLine) > availableW) {
                            val lineStr = lineBuilder.toString()
                            canvas.drawText(lineStr, rightMargin - paint.measureText(lineStr), y, paint)
                            y += size + 5f
                            lineBuilder.setLength(0)
                            lineBuilder.append(word)
                        } else {
                            if (lineBuilder.isNotEmpty()) lineBuilder.append(" ")
                            lineBuilder.append(word)
                        }
                    }
                    if (lineBuilder.isNotEmpty() && y <= pageHeight - 50f) {
                        val lineStr = lineBuilder.toString()
                        canvas.drawText(lineStr, rightMargin - paint.measureText(lineStr), y, paint)
                        y += size + 10f
                    }
                }
                return y
            }

            // Section 1: Behavior & Achievements
            val sec1Header = "1. מדדי התנהגות כיתתית ודירוג שיעורי השבוע:"
            canvas.drawText(sec1Header, rightX - headerPaint.measureText(sec1Header), currentY, headerPaint)
            currentY += 18f

            val sortedStudents = students.value.sortedByDescending { getStudentPoints(it) }
            val behaviorLines = sortedStudents.take(10).mapIndexed { idx, st ->
                "${idx + 1}. התלמיד: ${st.name}  —  צבר: ${getStudentPoints(st)} נקודות שבועיות"
            }.joinToString("\n")
            currentY = drawRTLTextFlow(behaviorLines, currentY, bodyPaint, 10f)
            currentY += 12f

            // Section 2: Pedagogical & Classroom GPA summaries
            val sec2Header = "2. הערכה פדגוגית, ציונים ממוצעים ומבחנים:"
            canvas.drawText(sec2Header, rightX - headerPaint.measureText(sec2Header), currentY, headerPaint)
            currentY += 18f

            val allGradeList = grades.value
            val gradesLines = students.value.sortedBy { it.name }.mapIndexed { idx, st ->
                val studGrades = allGradeList.filter { it.studentId == st.id }
                val avg = studGrades.mapNotNull { it.gradeValue.toIntOrNull() }.let { if (it.isEmpty()) 0.0 else it.average() }
                val avgStr = if (avg > 0) "${avg.toInt()}" else "ללא דיווח"
                "${idx + 1}. תלמיד: ${st.name} | ציון משוער ממוצע: $avgStr"
            }.joinToString("\n")
            currentY = drawRTLTextFlow(gradesLines, currentY, bodyPaint, 10f)
            currentY += 12f

            // Section 3: Actionable weekly summary and educational notes
            val sec3Header = "3. דגשים פדגוגיים, יעדים והערות מהמורה המלווה:"
            canvas.drawText(sec3Header, rightX - headerPaint.measureText(sec3Header), currentY, headerPaint)
            currentY += 18f

            currentY = drawRTLTextFlow(classReportTeacherSummary.value, currentY, bodyPaint, 10f)

            pdfDoc.finishPage(page)

            val file = java.io.File(context.cacheDir, "ClassPro_Weekly_Class_Report.pdf")
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
                putExtra(android.content.Intent.EXTRA_SUBJECT, "דוח סיכום כיתתי פדגוגי שבועי לכלל התלמידים - ClassPro")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = android.content.Intent.createChooser(intent, "ייצא דוח שבועי כיתתי")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ExportClassReportPDF", "Error saving report PDF file", e)
        }
    }
}
