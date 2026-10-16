package com.adarsh.syllabusmission.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.adarsh.syllabusmission.data.Chapter
import com.adarsh.syllabusmission.data.ChapterData
import com.adarsh.syllabusmission.data.Subject
import com.adarsh.syllabusmission.logic.ProgressCalc
import com.adarsh.syllabusmission.logic.StreakCalc
import java.time.LocalDate

const val USER_NAME = "Adarsh"

// ------------------------------------------------------------------ HOME

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

// --------------------------------------------------------------- SUBJECT

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
            if (c.section != null && c.section != last) {
                add(ListRow.Header(c.section))
                last = c.section
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

// ---------------------------------------------------------------- STREAK

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
