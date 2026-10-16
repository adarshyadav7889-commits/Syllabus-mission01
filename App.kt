package com.adarsh.syllabusmission

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

// ============================================================================
// SYLLABUS MISSION - whole app in one file.
// Sections: 1 Chapter data | 2 Storage | 3 Progress & streak logic |
//           4 Theme | 5 Screens | 6 ViewModel & Activity
// ============================================================================

const val USER_NAME = "Adarsh"

// ============================================================================
// 1. CHAPTER DATA - edit here to change chapters.
//
// !! NEEDS VERIFICATION !!
// Written from my knowledge of the NCERT Class 10 books (rationalised syllabus).
// I could NOT check them against the current official CBSE/NCERT session.
// Compare with the Contents page of YOUR textbooks. Hindi assumes Course A
// (Kshitij + Kritika).
//
// RULES FOR EDITING: each chapter id (e.g. "math_3") is what saved progress
// uses. Add new chapters at the END of a list. Do not reorder or delete
// chapters you already ticked.
// ============================================================================

data class Chapter(val id: String, val title: String, val section: String? = null)

data class Subject(val id: String, val name: String, val chapters: List<Chapter>)

object ChapterData {

    private fun chapters(prefix: String, section: String?, vararg titles: String): List<Chapter> =
        titles.mapIndexed { i, t -> Chapter("${prefix}_${i + 1}", t, section) }

    val subjects: List<Subject> = listOf(
        Subject(
            "math", "Mathematics",
            chapters(
                "math", null,
                "Real Numbers",
                "Polynomials",
                "Pair of Linear Equations in Two Variables",
                "Quadratic Equations",
                "Arithmetic Progressions",
                "Triangles",
                "Coordinate Geometry",
                "Introduction to Trigonometry",
                "Some Applications of Trigonometry",
                "Circles",
                "Areas Related to Circles",
                "Surface Areas and Volumes",
                "Statistics",
                "Probability"
            )
        ),
        Subject(
            "sci", "Science",
            chapters(
                "sci", null,
                "Chemical Reactions and Equations",
                "Acids, Bases and Salts",
                "Metals and Non-metals",
                "Carbon and its Compounds",
                "Life Processes",
                "Control and Coordination",
                "How do Organisms Reproduce?",
                "Heredity",
                "Light - Reflection and Refraction",
                "The Human Eye and the Colourful World",
                "Electricity",
                "Magnetic Effects of Electric Current",
                "Our Environment"
            )
        ),
        Subject(
            "sst", "Social Science",
            chapters(
                "hist", "History - India and the Contemporary World II",
                "The Rise of Nationalism in Europe",
                "Nationalism in India",
                "The Making of a Global World",
                "The Age of Industrialisation",
                "Print Culture and the Modern World"
            ) + chapters(
                "geo", "Geography - Contemporary India II",
                "Resources and Development",
                "Forest and Wildlife Resources",
                "Water Resources",
                "Agriculture",
                "Minerals and Energy Resources",
                "Manufacturing Industries",
                "Lifelines of National Economy"
            ) + chapters(
                "pol", "Political Science - Democratic Politics II",
                "Power Sharing",
                "Federalism",
                "Gender, Religion and Caste",
                "Political Parties",
                "Outcomes of Democracy"
            ) + chapters(
                "eco", "Economics - Understanding Economic Development",
                "Development",
                "Sectors of the Indian Economy",
                "Money and Credit",
                "Globalisation and the Indian Economy",
                "Consumer Rights"
            )
        ),
        Subject(
            "eng", "English",
            chapters(
                "ffp", "First Flight - Prose",
                "A Letter to God",
                "Nelson Mandela: Long Walk to Freedom",
                "Two Stories about Flying",
                "From the Diary of Anne Frank",
                "The Hundred Dresses - I",
                "The Hundred Dresses - II",
                "Glimpses of India",
                "Mijbil the Otter",
                "Madam Rides the Bus",
                "The Sermon at Benares",
                "The Proposal"
            ) + chapters(
                "ffv", "First Flight - Poetry",
                "Dust of Snow",
                "Fire and Ice",
                "A Tiger in the Zoo",
                "How to Tell Wild Animals",
                "The Ball Poem",
                "Amanda!",
                "The Trees",
                "Fog",
                "The Tale of Custard the Dragon",
                "For Anne Gregory"
            ) + chapters(
                "fwf", "Footprints Without Feet",
                "A Triumph of Surgery",
                "The Thief's Story",
                "The Midnight Visitor",
                "A Question of Trust",
                "Footprints without Feet",
                "The Making of a Scientist",
                "The Necklace",
                "Bholi",
                "The Book That Saved the Earth"
            )
        ),
        Subject(
            "hin", "Hindi",
            chapters(
                "ksh", "क्षितिज भाग 2",
                "पद (सूरदास)",
                "राम-लक्ष्मण-परशुराम संवाद (तुलसीदास)",
                "सवैया और कवित्त (देव)",
                "आत्मकथ्य (जयशंकर प्रसाद)",
                "उत्साह और अट नहीं रही है (निराला)",
                "यह दंतुरित मुसकान और फसल (नागार्जुन)",
                "छाया मत छूना (गिरिजा कुमार माथुर)",
                "कन्यादान (ऋतुराज)",
                "संगतकार (मंगलेश डबराल)",
                "नेताजी का चश्मा (स्वयं प्रकाश)",
                "बालगोबिन भगत (रामवृक्ष बेनीपुरी)",
                "लखनवी अंदाज़ (यशपाल)",
                "मानवीय करुणा की दिव्य चमक (सर्वेश्वर दयाल सक्सेना)",
                "एक कहानी यह भी (मन्नू भंडारी)",
                "स्त्री शिक्षा के विरोधी कुतर्कों का खंडन (महावीर प्रसाद द्विवेदी)",
                "नौबतखाने में इबादत (यतींद्र मिश्र)",
                "संस्कृति (भदंत आनंद कौसल्यायन)"
            ) + chapters(
                "kri", "कृतिका भाग 2",
                "माता का आँचल",
                "जॉर्ज पंचम की नाक",
                "साना-साना हाथ जोड़ि",
                "एही ठैयाँ झुलनी हेरानी हो रामा!",
                "मैं क्यों लिखता हूँ?"
            )
        )
    )

    val allIds: Set<String> by lazy { subjects.flatMap { it.chapters }.map { it.id }.toSet() }

    fun find(subjectId: String?): Subject? = subjects.firstOrNull { it.id == subjectId }
}

// ============================================================================
// 2. STORAGE (Room database, works offline, survives restarts)
// One row = one completed chapter + the calendar day it was completed.
// ============================================================================

@Entity(tableName = "completions")
data class Completion(
    @PrimaryKey val chapterId: String,
    val completedOn: Long
)

@Dao
interface CompletionDao {
    @Query("SELECT * FROM completions")
    fun observeAll(): Flow<List<Completion>>

    @Query("SELECT * FROM completions WHERE chapterId = :id")
    suspend fun find(id: String): Completion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: Completion)

    @Query("DELETE FROM completions WHERE chapterId = :id")
    suspend fun delete(id: String)
}

@Database(entities = [Completion::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): CompletionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "syllabus.db"
                ).build().also { instance = it }
            }
    }
}

class Repository(private val db: AppDatabase) {
    private val dao = db.dao()

    fun observe(): Flow<List<Completion>> = dao.observeAll()

    /** Completed -> incomplete (row deleted). Incomplete -> completed today. Saved immediately. */
    suspend fun toggle(chapterId: String, todayEpochDay: Long) {
        db.withTransaction {
            if (dao.find(chapterId) != null) {
                dao.delete(chapterId)
            } else {
                dao.insert(Completion(chapterId, todayEpochDay))
            }
        }
    }
}

// ============================================================================
// 3. PROGRESS & STREAK LOGIC
// ============================================================================

data class Progress(val done: Int, val total: Int) {
    val percent: Int get() = ProgressCalc.percent(done, total)
    val fraction: Float get() = if (total <= 0) 0f else done.toFloat() / total
}

object ProgressCalc {
    /** Whole-number percent, rounded down, so 100% only appears when everything is done. */
    fun percent(done: Int, total: Int): Int =
        if (total <= 0) 0 else (done.coerceIn(0, total) * 100) / total

    fun forSubject(subject: Subject, doneIds: Set<String>): Progress =
        Progress(subject.chapters.count { it.id in doneIds }, subject.chapters.size)

    fun overall(subjects: List<Subject>, doneIds: Set<String>): Progress {
        val parts = subjects.map { forSubject(it, doneIds) }
        return Progress(parts.sumOf { it.done }, parts.sumOf { it.total })
    }
}

data class StreakStats(val current: Int, val longest: Int, val totalDays: Int)

object StreakCalc {
    /**
     * studyDates = calendar dates on which chapters were completed (duplicates are fine:
     * many chapters on one day count as one study day).
     * Current streak = consecutive days ending today. If today has no study yet but
     * yesterday does, the streak is still alive (you still have today to continue it).
     */
    fun compute(studyDates: Collection<LocalDate>, today: LocalDate): StreakStats {
        val days = studyDates.toSortedSet()
        if (days.isEmpty()) return StreakStats(0, 0, 0)

        var longest = 0
        var run = 0
        var previous: LocalDate? = null
        for (day in days) {
            run = if (previous != null && previous.plusDays(1) == day) run + 1 else 1
            if (run > longest) longest = run
            previous = day
        }

        var cursor = if (today in days) today else today.minusDays(1)
        var current = 0
        while (cursor in days) {
            current++
            cursor = cursor.minusDays(1)
        }
        return StreakStats(current, longest, days.size)
    }

    fun message(current: Int, studiedToday: Boolean): String = when {
        current == 0 -> "Complete one chapter today to start your streak."
        !studiedToday -> "Your $current-day streak is alive. Finish a chapter today to keep it going!"
        current < 3 -> "Great start! Come back tomorrow."
        current < 7 -> "Nice momentum - $current days in a row."
        else -> "Amazing! $current days in a row. Keep going until the boards!"
    }
}

// ============================================================================
// 4. THEME (follows your phone's light / dark mode)
// ============================================================================

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F5D8A),
    onPrimary = Color.White,
    background = Color(0xFFFBF8F1),
    onBackground = Color(0xFF1F2933),
    surface = Color(0xFFFBF8F1),
    onSurface = Color(0xFF1F2933),
    surfaceVariant = Color(0xFFE6E1D6),
    onSurfaceVariant = Color(0xFF4A5560),
    outline = Color(0xFF4A5560)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8DB8E8),
    onPrimary = Color(0xFF0B2239),
    background = Color(0xFF14181F),
    onBackground = Color(0xFFE8E6E1),
    surface = Color(0xFF14181F),
    onSurface = Color(0xFFE8E6E1),
    surfaceVariant = Color(0xFF2A313B),
    onSurfaceVariant = Color(0xFFB4BBC4),
    outline = Color(0xFF8A93A0)
)

@Composable
fun SyllabusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}

// ============================================================================
// 5. SCREENS
// ============================================================================

@Composable
fun HomeScreen(
    done: Map<String, LocalDate>,
    onSubject: (String) -> Unit,
    onStreak: () -> Unit
) {
    val doneIds = done.keys
    val overall = ProgressCalc.overall(ChapterData.subjects, doneIds)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                "Hello,\n$USER_NAME",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = onStreak,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("STREAK", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(18.dp))

        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Syllabus completed", style = MaterialTheme.typography.titleMedium)
                Text("${overall.percent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { overall.fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${overall.done} of ${overall.total} chapters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        val panel = RoundedCornerShape(22.dp)
        Column(
            Modifier
                .fillMaxWidth()
                .border(1.5.dp, MaterialTheme.colorScheme.outline, panel)
                .padding(18.dp)
        ) {
            Text("Subjects", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            ChapterData.subjects.forEach { subject ->
                SubjectRow(subject, doneIds) { onSubject(subject.id) }
            }
        }
    }
}

@Composable
private fun SubjectRow(subject: Subject, doneIds: Set<String>, onClick: () -> Unit) {
    val p = ProgressCalc.forSubject(subject, doneIds)
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(12.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Row(
            Modifier
                .weight(1f)
                .heightIn(min = 64.dp)
                .clip(shape)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(subject.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { p.fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
            Spacer(Modifier.width(12.dp))
            Text("${p.done}/${p.total}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private sealed interface ListRow {
    data class Header(val title: String) : ListRow
    data class Item(val chapter: Chapter) : ListRow
}

@Composable
fun SubjectScreen(
    subject: Subject,
    done: Map<String, LocalDate>,
    onToggle: (String) -> Unit,
    onBack: () -> Unit
) {
    val p = ProgressCalc.forSubject(subject, done.keys)
    val rows = buildList<ListRow> {
        var last: String? = null
        subject.chapters.forEach { c ->
            val section = c.section
            if (section != null && section != last) {
                add(ListRow.Header(section))
                last = section
            }
            add(ListRow.Item(c))
        }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                TextButton(onClick = onBack) { Text("← Back") }
                Text(subject.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "${p.done} of ${p.total} completed  ·  ${p.percent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { p.fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(6.dp))
                if (subject.chapters.isEmpty()) {
                    Text("No chapters yet.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        items(
            rows,
            key = { r ->
                when (r) {
                    is ListRow.Header -> "h_${r.title}"
                    is ListRow.Item -> r.chapter.id
                }
            }
        ) { r ->
            when (r) {
                is ListRow.Header -> Text(
                    r.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 10.dp)
                )
                is ListRow.Item -> ChapterRow(
                    chapter = r.chapter,
                    completed = r.chapter.id in done,
                    onClick = { onToggle(r.chapter.id) }
                )
            }
        }
    }
}

@Composable
private fun ChapterRow(chapter: Chapter, completed: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val textColor by animateColorAsState(
        if (completed) onSurface.copy(alpha = 0.5f) else onSurface,
        label = "textColor"
    )
    val fill by animateColorAsState(
        if (completed) primary else Color.Transparent,
        label = "fill"
    )
    val checkScale by animateFloatAsState(if (completed) 1f else 0f, label = "check")

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(shape)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(fill)
                .border(2.dp, if (completed) primary else MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "✓",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(checkScale)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            chapter.title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            textDecoration = if (completed) TextDecoration.LineThrough else null
        )
    }
}

@Composable
fun StreakScreen(done: Map<String, LocalDate>, today: LocalDate, onBack: () -> Unit) {
    val dates = done.values.toSet()
    val stats = StreakCalc.compute(dates, today)
    val studiedToday = today in dates
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Streak", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox("Current", stats.current, Modifier.weight(1f))
            StatBox("Longest", stats.longest, Modifier.weight(1f))
            StatBox("Study days", stats.totalDays, Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))
        Text("This week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (i in 0..6) {
                val d = monday.plusDays(i.toLong())
                val studied = d in dates
                val isToday = d == today
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MTWTFSS"[i].toString(), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (studied) primary else Color.Transparent)
                            .border(if (isToday) 2.dp else 1.dp, if (isToday) primary else outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (studied) {
                            Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        } else {
                            Text("${d.dayOfMonth}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        val shape = RoundedCornerShape(16.dp)
        Text(
            StreakCalc.message(stats.current, studiedToday),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, outline, shape)
                .padding(16.dp)
        )

        Spacer(Modifier.height(16.dp))
        Text(
            "How it works: a day counts when you complete at least one chapter on that date. " +
                "Several chapters on one day count once. Opening the app does not count. " +
                "If you un-tick the only chapter you finished on a day, that day is removed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatBox(label: String, value: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier
            .border(1.5.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$value", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}

// ============================================================================
// 6. VIEWMODEL, NAVIGATION & ACTIVITY
// ============================================================================

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(AppDatabase.get(app))

    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today

    /** chapterId -> date completed. null = still loading from the database. */
    val completions: StateFlow<Map<String, LocalDate>?> = repo.observe()
        .map { list ->
            list.filter { it.chapterId in ChapterData.allIds }
                .associate { it.chapterId to LocalDate.ofEpochDay(it.completedOn) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Called when the app returns to the screen, so a new calendar day is noticed. */
    fun refreshToday() {
        _today.value = LocalDate.now()
    }

    fun toggle(chapterId: String) {
        viewModelScope.launch { repo.toggle(chapterId, LocalDate.now().toEpochDay()) }
    }
}

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SyllabusTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNav(vm)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshToday()
    }
}

@Composable
fun AppNav(vm: MainViewModel) {
    val nav = rememberNavController()
    val doneOrNull by vm.completions.collectAsState()
    val today by vm.today.collectAsState()
    val done = doneOrNull

    if (done == null) {
        Box(Modifier.fillMaxSize())
        return
    }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                done = done,
                onSubject = { nav.navigate("subject/$it") },
                onStreak = { nav.navigate("streak") }
            )
        }
        composable(
            "subject/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val subject = ChapterData.find(entry.arguments?.getString("id"))
            if (subject != null) {
                SubjectScreen(
                    subject = subject,
                    done = done,
                    onToggle = vm::toggle,
                    onBack = { nav.popBackStack() }
                )
            }
        }
        composable("streak") {
            StreakScreen(done = done, today = today, onBack = { nav.popBackStack() })
        }
    }
}
