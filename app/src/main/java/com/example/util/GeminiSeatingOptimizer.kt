package com.example.util

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.DeskEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiSeatingOptimizer {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun optimizeSeating(
        students: List<StudentEntity>,
        unlockedDesks: List<DeskEntity>,
        allDesks: List<DeskEntity>,
        layoutRows: Int,
        grades: List<com.example.data.model.StudentGradeEntity> = emptyList()
    ): Map<Pair<Int, Int>, StudentEntity> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiOptimizer", "No API key configured for Gemini. Bypassing AI optimization.")
            // Use fallback heuristic or return empty to let the main heuristic run
            return@withContext emptyMap()
        }

        // Aggregate actual database grades
        val studentGradesMap = grades.groupBy { it.studentId }.mapValues { entry ->
            entry.value.mapNotNull { it.gradeValue.toIntOrNull() }.average().let { if (it.isNaN()) 0.0 else it }
        }

        // Build a prompt summarizing the constraints
        val desksStr = unlockedDesks.joinToString("; ") { "Desk(${it.row},${it.col})" }
        val studentsStrString = StringBuilder()
        for (st in students) {
            val dbAverage = studentGradesMap[st.id] ?: 0.0
            
            // Also keep legacy point tracking if needed
            val ptsPrefix = "ניקוד: "
            val currentPoints = if (st.notes.startsWith(ptsPrefix)) {
                val parts = st.notes.split(" | ", limit = 2)
                parts.first().removePrefix(ptsPrefix).toIntOrNull() ?: 0
            } else 0
            
            val totalAcademicValue = dbAverage + currentPoints
            
            studentsStrString.append("- ${st.id} (${st.name}): height=${st.height}, rowPref=${st.rowPreference}, lovesToSitNextTo=[${st.loves.joinToString()}], forbidsNextTo=[${st.forbids.joinToString()}], separateFrom=[${st.separate.joinToString()}], academicPerformanceMetric=$totalAcademicValue, notes='${st.notes}'\n")
        }

        val prompt = """
            You are an advanced AI seating chart generator matching students to classroom desks based on student characteristics, academic performance, and behavioral constraints.
            
            We have a classroom map with ${allDesks.size} total cells, layout has $layoutRows rows. Small row numbers are at the front, large row numbers are at the back.
            Available physical desks to place the students in: $desksStr
            
            Students to place:
            $studentsStrString
            
            Constraints:
            1. 'Low' height students should sit at the front (row <= ${layoutRows / 3}), 'Tall' in the back (row >= ${layoutRows * 2 / 3}).
            2. Respect 'rowPreference' ('Front', 'Middle', 'Back').
            3. Behavioral Constraints: Students in 'lovesToSitNextTo' MUST sit at adjacent desks. Students in 'forbidsNextTo' or 'separateFrom' MUST NOT sit next to each other. Use the 'notes' field for any behavioral red flags to separate disruptive students.
            4. Academic Performance: Use 'academicPerformanceMetric' to balance the classroom academically. Don't clump all low-performing or high-performing students together. Place stronger peer mentors near weaker ones if possible.

            Return exactly ONE valid JSON object, structured as an array of assignments mapping a desk to a student.
            Format output strictly as raw JSON (no markdown text):
            {
              "allocations": [
                {
                  "row": 0,
                  "col": 0,
                  "studentId": "..."
                }
              ]
            }
        """.trimIndent()

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseFormat", JSONObject().apply {
                        put("type", "JSON_OBJECT")
                    })
                    put("temperature", 0.0) // Low temp for more determinism
                })
            }

            val requestBody = requestBodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val responseBody = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e("GeminiOptimizer", "API Error HTTP ${response.code}: $errorBody")
                    throw Exception("API Error ${response.code}")
                }
                response.body?.string()
            }

            if (responseBody == null) {
                return@withContext emptyMap()
            }

            // Parse response
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return@withContext emptyMap()
            val content = candidates.optJSONObject(0)?.optJSONObject("content") ?: return@withContext emptyMap()
            val textContent = content.optJSONArray("parts")?.optJSONObject(0)?.optString("text", "") ?: ""

            val cleanJson = textContent.replace("```json", "").replace("```", "").trim()
            val outputObj = JSONObject(cleanJson)
            val allocations = outputObj.optJSONArray("allocations") ?: return@withContext emptyMap()

            val result = mutableMapOf<Pair<Int, Int>, StudentEntity>()
            val studentMap = students.associateBy { it.id }

            for (i in 0 until allocations.length()) {
                val allocation = allocations.getJSONObject(i)
                val r = allocation.getInt("row")
                val c = allocation.getInt("col")
                val stId = allocation.getString("studentId")

                val st = studentMap[stId]
                if (st != null) {
                    result[Pair(r, c)] = st
                }
            }
            Log.d("GeminiOptimizer", "Successfully parsed ${result.size} AI-driven optimized seat assignments.")
            return@withContext result
        } catch (e: Exception) {
            Log.e("GeminiOptimizer", "Error during Gemini API call", e)
            return@withContext emptyMap()
        }
    }
}
