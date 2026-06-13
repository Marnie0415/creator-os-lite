package com.example.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [TimeFormatter] utility.
 */
class TimeFormatterTest {

    private val now = 1_000_000_000_000L
    private val minuteMs = 60L * 1000
    private val hourMs = 60L * minuteMs
    private val dayMs = 24L * hourMs

    // ==================== getRelativeTime ====================

    @Test
    fun `less than a minute shows Just now`() {
        val result = TimeFormatter.getRelativeTime(now - 30 * 1000, now)
        assertEquals("Just now", result)
    }

    @Test
    fun `one hour shows 1 hr ago`() {
        val result = TimeFormatter.getRelativeTime(now - hourMs, now)
        assertEquals("1 hr ago", result)
    }

    @Test
    fun `three hours shows 3 hr ago`() {
        val result = TimeFormatter.getRelativeTime(now - 3 * hourMs, now)
        assertEquals("3 hr ago", result)
    }

    @Test
    fun `less than an hour but more than a minute shows correct hours`() {
        val result = TimeFormatter.getRelativeTime(now - 45 * minuteMs, now)
        assertEquals("1 hr ago", result)
    }

    @Test
    fun `over 24 hours uses relative span`() {
        val result = TimeFormatter.getRelativeTime(now - 2 * dayMs, now)
        // android.text.format.DateUtils.getRelativeTimeSpanString returns something like "2 days ago"
        assertTrue("Should contain 'day'", result.contains("day", ignoreCase = true))
    }

    // ==================== getDeadlineCountdown ====================

    @Test
    fun `deadline 24 hours away shows correct hours`() {
        val result = TimeFormatter.getDeadlineCountdown(now + 24 * hourMs, now)
        assertEquals("24 hr left", result)
    }

    @Test
    fun `deadline less than 1 hour shows less than an hr left`() {
        val result = TimeFormatter.getDeadlineCountdown(now + 30 * minuteMs, now)
        assertEquals("Less than an hr left", result)
    }

    @Test
    fun `deadline 48 hours away shows 48 hr left`() {
        val result = TimeFormatter.getDeadlineCountdown(now + 48 * hourMs, now)
        assertEquals("48 hr left", result)
    }

    @Test
    fun `deadline 72 hours away shows days left`() {
        val result = TimeFormatter.getDeadlineCountdown(now + 72 * hourMs, now)
        assertEquals("3 days left", result)
    }

    @Test
    fun `overdue deadline shows hours overdue`() {
        val result = TimeFormatter.getDeadlineCountdown(now - 10 * hourMs, now)
        assertEquals("10 hr overdue", result)
    }

    @Test
    fun `overdue by more than a day shows days overdue`() {
        val result = TimeFormatter.getDeadlineCountdown(now - 50 * hourMs, now)
        assertEquals("2 days overdue", result)
    }

    @Test
    fun `exactly at deadline shows less than an hr left`() {
        val result = TimeFormatter.getDeadlineCountdown(now, now)
        assertEquals("Less than an hr left", result)
    }
}
