package com.example.ai

/**
 * Supported AI provider types.
 * OpenAI-compatible covers: OpenAI, DeepSeek, Groq, Together AI, OpenRouter,
 * Perplexity, GitHub Models, 零一万物 (Yi), 智谱 (GLM), 月之暗面 (Moonshot), etc.
 */
enum class AiProviderType(val displayName: String, val description: String) {
    GEMINI("Google Gemini", "gemini-2.5-flash / gemini-2.5-pro"),
    OPENAI_COMPATIBLE("OpenAI Compatible", "OpenAI, DeepSeek, Groq, Together, OpenRouter..."),
    ANTHROPIC("Anthropic Claude", "claude-sonnet-4 / claude-haiku-3.5")
}

/**
 * Runtime configuration for the selected AI provider.
 */
data class AiProviderConfig(
    val type: AiProviderType = AiProviderType.GEMINI,
    val apiKey: String = "",
    val model: String = "",
    val baseUrl: String = ""
) {
    /** Returns the default model name for this provider type. */
    fun getDefaultModel(): String = when (type) {
        AiProviderType.GEMINI -> "gemini-2.5-flash"
        AiProviderType.OPENAI_COMPATIBLE -> "gpt-4o-mini"
        AiProviderType.ANTHROPIC -> "claude-sonnet-4"
    }

    /** Returns the default base URL for this provider type. */
    fun getDefaultBaseUrl(): String = when (type) {
        AiProviderType.GEMINI -> "https://generativelanguage.googleapis.com/"
        AiProviderType.OPENAI_COMPATIBLE -> "https://api.openai.com/v1"
        AiProviderType.ANTHROPIC -> "https://api.anthropic.com"
    }

    /** Returns the resolved model name (custom or default). */
    fun effectiveModel(): String = model.ifBlank { getDefaultModel() }

    /** Returns the resolved base URL (custom or default). */
    fun effectiveBaseUrl(): String = baseUrl.ifBlank { getDefaultBaseUrl() }
}

/**
 * Unified chat message used across all providers.
 */
data class ChatMessage(
    val role: String,  // "user", "assistant", "system"
    val content: String
)

/**
 * Result from any AI provider call.
 */
sealed class AiResult {
    data class Success(val text: String) : AiResult()
    data class Error(val message: String) : AiResult()

    companion object {
        fun fromException(e: Exception): Error =
            Error("AI service error: ${e.localizedMessage ?: "Unknown error"}")
    }
}

/**
 * Unified interface that every AI provider implements.
 */
interface AiService {
    /**
     * Send a chat completion request and return the response text.
     * @param systemPrompt Optional system-level instruction.
     * @param messages Conversation messages (user/assistant).
     * @param temperature Creativity (0.0 - 2.0, default 0.7).
     */
    suspend fun generateResponse(
        systemPrompt: String?,
        messages: List<ChatMessage>,
        temperature: Double
    ): AiResult

    /** Quick check whether the service has a valid API key configured. */
    fun isConfigured(): Boolean
}
