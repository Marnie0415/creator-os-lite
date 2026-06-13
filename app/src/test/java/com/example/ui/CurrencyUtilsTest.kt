package com.example.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [CurrencyUtils] formatting.
 */
class CurrencyUtilsTest {

    @Test
    fun `integer amount formats with dot zero zero`() {
        assertEquals("$45.00", CurrencyUtils.format(45.0))
        assertEquals("$100.00", CurrencyUtils.format(100.0))
        assertEquals("$0.00", CurrencyUtils.format(0.0))
        assertEquals("$1.00", CurrencyUtils.format(1.0))
    }

    @Test
    fun `decimal amount formats with two decimal places`() {
        assertEquals("$45.50", CurrencyUtils.format(45.5))
        assertEquals("$99.99", CurrencyUtils.format(99.99))
        assertEquals("$0.99", CurrencyUtils.format(0.99))
    }

    @Test
    fun `large amounts format correctly`() {
        assertEquals("$1000.00", CurrencyUtils.format(1000.0))
        assertEquals("$9999.99", CurrencyUtils.format(9999.99))
        assertEquals("$1234567.89", CurrencyUtils.format(1234567.89))
    }

    @Test
    fun `negative amounts format correctly`() {
        assertEquals("$-50.00", CurrencyUtils.format(-50.0))
        assertEquals("$-50.50", CurrencyUtils.format(-50.5))
    }

    @Test
    fun `very small decimal preserves precision`() {
        assertEquals("$0.10", CurrencyUtils.format(0.1))
        assertEquals("$0.01", CurrencyUtils.format(0.01))
    }
}
