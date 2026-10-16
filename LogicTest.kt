package com.adarsh.syllabusmission

import com.adarsh.syllabusmission.data.ChapterData
import com.adarsh.syllabusmission.logic.ProgressCalc
import com.adarsh.syllabusmission.logic.StreakCalc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LogicTest {
    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun percentIsSafeWhenNoChapters() {
        assertEquals(0, ProgressCalc.percent(0, 0))
    }

    @Test
    fun percentIsWholeNumberRoundedDown() {
        assertEquals(33, ProgressCalc.percent(1, 3))
        assertEquals(92, ProgressCalc.percent(12, 13))
        assertEquals(100, ProgressCalc.percent(13, 13))
    }

    @Test
    fun overallProgressCountsAllSubjects() {
        val all = ChapterData.subjects.flatMap { it.chapters }.map { it.id }
        val p = ProgressCalc.overall(ChapterData.subjects, setOf(all[0], all[1]))
        assertEquals(2, p.done)
        assertEquals(all.size, p.total)
    }

    @Test
    fun chapterDataHasFiveSubjectsAndUniqueIds() {
        assertEquals(5, ChapterData.subjects.size)
        val ids = ChapterData.subjects.flatMap { it.chapters }.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ChapterData.subjects.all { it.chapters.isNotEmpty() })
    }

    @Test
    fun noStudyMeansZeroStreak() {
        val s = StreakCalc.compute(emptyList(), today)
        assertEquals(0, s.current)
        assertEquals(0, s.longest)
        assertEquals(0, s.totalDays)
    }

    @Test
    fun manyChaptersOnOneDayCountOnce() {
        val s = StreakCalc.compute(listOf(today, today, today), today)
        assertEquals(1, s.current)
        assertEquals(1, s.totalDays)
    }

    @Test
    fun consecutiveDaysBuildStreak() {
        val s = StreakCalc.compute(listOf(today, today.minusDays(1), today.minusDays(2)), today)
        assertEquals(3, s.current)
        assertEquals(3, s.longest)
    }

    @Test
    fun streakStaysAliveIfOnlyYesterdayStudied() {
        val s = StreakCalc.compute(listOf(today.minusDays(1), today.minusDays(2)), today)
        assertEquals(2, s.current)
    }

    @Test
    fun streakBreaksAfterMissedDay() {
        val s = StreakCalc.compute(listOf(today.minusDays(2), today.minusDays(3)), today)
        assertEquals(0, s.current)
        assertEquals(2, s.longest)
    }

    @Test
    fun longestStreakIsKeptAfterGap() {
        val dates = listOf(
            today.minusDays(10), today.minusDays(9), today.minusDays(8), today.minusDays(7),
            today
        )
        val s = StreakCalc.compute(dates, today)
        assertEquals(1, s.current)
        assertEquals(4, s.longest)
        assertEquals(5, s.totalDays)
    }
}
