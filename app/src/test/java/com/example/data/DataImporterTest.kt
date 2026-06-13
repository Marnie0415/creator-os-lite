package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [DataImporter] CSV parsing logic.
 */
class DataImporterTest {

    @Test
    fun `parseCsvLine splits simple comma values`() {
        val line = "abc,def,ghi"
        val result = DataImporter.parseCsvLine(line)
        assertEquals(listOf("abc", "def", "ghi"), result)
    }

    @Test
    fun `parseCsvLine handles quoted values with commas`() {
        val line = "abc,\"def,ghi\",jkl"
        val result = DataImporter.parseCsvLine(line)
        assertEquals(listOf("abc", "def,ghi", "jkl"), result)
    }

    @Test
    fun `parseCsvLine handles quoted values with newlines`() {
        val line = "abc,\"def\nghi\",jkl"
        val result = DataImporter.parseCsvLine(line)
        assertEquals(listOf("abc", "def\nghi", "jkl"), result)
    }

    @Test
    fun `parseCsvLine handles escaped quotes`() {
        val line = "abc,\"def\"\"ghi\",jkl"
        val result = DataImporter.parseCsvLine(line)
        assertEquals(listOf("abc", "def\"ghi", "jkl"), result)
    }

    @Test
    fun `parseCsvLine handles empty values`() {
        val line = "abc,,ghi"
        val result = DataImporter.parseCsvLine(line)
        assertEquals(listOf("abc", "", "ghi"), result)
    }

    @Test
    fun `parseCsvLine handles leading and trailing whitespace`() {
        val line = "  abc , def , ghi  "
        val result = DataImporter.parseCsvLine(line)
        assertEquals(listOf("abc", "def", "ghi"), result)
    }

    @Test
    fun `parseCsvLine handles single value`() {
        val line = "justone"
        val result = DataImporter.parseCsvLine(line)
        assertEquals(listOf("justone"), result)
    }
}
