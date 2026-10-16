package com.adarsh.syllabusmission.logic

import com.adarsh.syllabusmission.data.Subject
import java.time.LocalDate

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
     * [studyDates] = the calendar dates on which chapters are completed (duplicates are fine,
     * many chapters on one day count as one study day).
     * Current streak = consecutive days ending today. If today has no study yet but yesterday
     * does, the streak is still alive (the student still has today to continue it).
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
