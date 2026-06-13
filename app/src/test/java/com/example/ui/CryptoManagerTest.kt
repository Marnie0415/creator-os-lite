package com.example.ui

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [CryptoManager] encryption wrapper.
 * Tests the fallback behavior when Android Keystore is unavailable (unit test env).
 */
class CryptoManagerTest {

    @Test
    fun `encrypt with empty string returns empty`() {
        val result = CryptoManager.encrypt("")
        assertEquals("", result)
    }

    @Test
    fun `decrypt with empty string returns empty`() {
        val result = CryptoManager.decrypt("")
        assertEquals("", result)
    }

    @Test
    fun `decrypt with non-encrypted string returns original`() {
        // When Keystore is unavailable, encryption returns plaintext
        val result = CryptoManager.decrypt("hello")
        assertEquals("hello", result)
    }

    @Test
    fun `encrypt returns same string when Keystore unavailable`() {
        // In unit test environment (no Android), Keystore returns null
        // Encryption falls back to plaintext
        val input = "test-api-key-12345"
        val encrypted = CryptoManager.encrypt(input)
        val decrypted = CryptoManager.decrypt(encrypted)
        assertEquals(input, decrypted)
    }

    @Test
    fun `roundtrip with special characters`() {
        val input = "sk-ant-abc123!@#$%^&*()_+"
        val encrypted = CryptoManager.encrypt(input)
        val decrypted = CryptoManager.decrypt(encrypted)
        assertEquals(input, decrypted)
    }

    @Test
    fun `roundtrip with unicode characters`() {
        val input = "AIzaSyD-중국어-日本語-Тест"
        val encrypted = CryptoManager.encrypt(input)
        val decrypted = CryptoManager.decrypt(encrypted)
        assertEquals(input, decrypted)
    }
}
