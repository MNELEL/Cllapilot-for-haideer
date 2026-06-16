package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.ClassProDatabase
import com.example.util.GeminiParser
import java.io.File

class PedagogicalStyleWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("PedagogicalStyleWorker", "Starting pedagogical style estimation...")
        val database = ClassProDatabase.getDatabase(applicationContext)
        val academicDao = database.academicDao()
        val pacingDao = database.pacingDao()

        try {
            val materials = academicDao.getMaterials()
            val pacings = database.pacingDao().getPacingList()

            val textAggregator = StringBuilder()
            materials.forEach {
                textAggregator.append(it.title).append("\n").append(it.summaryNotes).append("\n")
            }
            pacings.forEach {
                textAggregator.append(it.moduleName).append("\n")
            }

            val rawText = textAggregator.toString()
            var learnedTone = "עיוני ומעמיק, מותאם לצורכי החינוך וההוראה"
            var formattingPreference = "סיכום מובנה, ציר זמן פדגוגי ברור ושאלות חזרה בהבנה מעמיקה"

            if (rawText.isNotEmpty()) {
                // Background heuristics
                val talmudicCount = countOccurrences(rawText, listOf("משנה", "גמרא", "תלמוד", "מסכת", "פרק", "אמר", "רבי", "סוגיא"))
                val modernCount = countOccurrences(rawText, listOf("מדע", "מתמטיקה", "אנגלית", "תרגיל", "טכנולוגיה", "מודרני", "חקר"))
                
                if (talmudicCount > modernCount) {
                    learnedTone = "תורני, ישיבתי, מנתח סוגיות לעומק עם ביטויים ארמיים קלאסיים (כמו 'קא משמע לן')"
                    formattingPreference = "חלוקה לפי סעיפי סוגיה, מראי מקומות ברורים, ושאלות הבנה בסגנון שקלא וטריא"
                } else if (modernCount > talmudicCount) {
                    learnedTone = "מדעי, אנליטי, שיטתי ומנוסח בשפה עברית בהירה ומודרנית"
                    formattingPreference = "שאלות רב-ברירה (אמריקאיות), חלקי שיעור מתוזמנים לפי דקות, מטרות אופרטיביות"
                }
            }

            // Save learned tone and preference in shared preferences
            val sharedPref = applicationContext.getSharedPreferences("classpro_prefs", Context.MODE_PRIVATE)
            sharedPref.edit()
                .putString("pedagogical_tone", learnedTone)
                .putString("pedagogical_format", formattingPreference)
                .apply()

            Log.d("PedagogicalStyleWorker", "Pedagogical style profile updated: Tone = $learnedTone, Format = $formattingPreference")
            return Result.success()
        } catch (e: Exception) {
            Log.e("PedagogicalStyleWorker", "Error analyzing style profile", e)
            return Result.retry()
        }
    }

    private fun countOccurrences(text: String, keywords: List<String>): Int {
        var count = 0
        keywords.forEach { keyword ->
            var idx = 0
            while (true) {
                idx = text.indexOf(keyword, idx)
                if (idx != -1) {
                    count++
                    idx += keyword.length
                } else {
                    break
                }
            }
        }
        return count
    }
}
