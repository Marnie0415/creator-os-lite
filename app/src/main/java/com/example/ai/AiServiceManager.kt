package com.example.ai

/**
 * Factory that creates the appropriate [AiService] implementation
 * based on the [AiProviderConfig].
 */
object AiServiceManager {

    @Volatile
    private var currentService: AiService? = null

    @Volatile
    private var currentConfig: AiProviderConfig? = null

    /**
     * Get or create the service for the given config.
     * Reuses the existing service if config hasn't changed.
     */
    fun getService(config: AiProviderConfig): AiService {
        if (currentConfig == config && currentService != null) {
            return currentService!!
        }

        val service = createService(config)
        currentService = service
        currentConfig = config
        return service
    }

    /**
     * Force-create a new service, ignoring cached instance.
     */
    fun createService(config: AiProviderConfig): AiService {
        return when (config.type) {
            AiProviderType.GEMINI -> GeminiService(config)
            AiProviderType.OPENAI_COMPATIBLE -> OpenAiService(config)
            AiProviderType.ANTHROPIC -> AnthropicService(config)
        }
    }

    /**
     * Invalidate the cached service so the next call creates a fresh one.
     */
    fun reset() {
        currentService = null
        currentConfig = null
    }
}
