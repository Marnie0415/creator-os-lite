package com.example.ai

import com.example.BuildConfig

/**
 * Legacy Gemini API client — kept for backward compatibility.
 *
 * New code should use [AiServiceManager] with [AiProviderConfig] instead.
 * This class provides:
 * - `userApiKey` — runtime override for the API key
 * - `getApiKey()` — resolves API key from user override or BuildConfig
 * - `sanitizeInput()` — input validation utility
 */
object GeminiApiClient {
    private const val MAX_INPUT_LENGTH = 5000

    /**
     * Runtime API key override set via SettingsScreen.
     * Takes precedence over BuildConfig.GEMINI_API_KEY.
     */
    @Volatile
    var userApiKey: String = ""

    /**
     * Resolve the effective Gemini API key:
     * 1. User-provided key (set in Settings)
     * 2. BuildConfig key (from .env / Secrets plugin during development)
     */
    fun getApiKey(): String {
        if (userApiKey.isNotBlank()) return userApiKey
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key == "MY_GEMINI_API_KEY" || key.isEmpty()) "" else key
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Validate and truncate user input to prevent abuse.
     */
    fun sanitizeInput(input: String): String {
        return input.take(MAX_INPUT_LENGTH).trim()
    }
}
