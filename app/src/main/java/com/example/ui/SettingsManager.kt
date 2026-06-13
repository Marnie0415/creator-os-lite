package com.example.ui

import android.content.Context
import com.example.ai.AiProviderConfig
import com.example.ai.AiProviderType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("creator_os_prefs", Context.MODE_PRIVATE)

    // --- Gemini API Key (legacy, kept for backward compatibility) ---
    private val _apiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val apiKey: StateFlow<String> = _apiKey

    fun setApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key).apply()
        _apiKey.value = key
    }

    // --- Multi-provider configuration ---

    private val _providerType = MutableStateFlow(
        prefs.getString("ai_provider_type", AiProviderType.GEMINI.name)?.let {
            runCatching { AiProviderType.valueOf(it) }.getOrDefault(AiProviderType.GEMINI)
        } ?: AiProviderType.GEMINI
    )
    val providerType: StateFlow<AiProviderType> = _providerType

    private val _providerApiKey = MutableStateFlow(prefs.getString("ai_provider_api_key", "") ?: "")
    val providerApiKey: StateFlow<String> = _providerApiKey

    private val _providerModel = MutableStateFlow(prefs.getString("ai_provider_model", "") ?: "")
    val providerModel: StateFlow<String> = _providerModel

    private val _providerBaseUrl = MutableStateFlow(prefs.getString("ai_provider_base_url", "") ?: "")
    val providerBaseUrl: StateFlow<String> = _providerBaseUrl

    /** Returns the full [AiProviderConfig] from stored preferences. */
    fun getProviderConfig(): AiProviderConfig {
        val type = _providerType.value
        return AiProviderConfig(
            type = type,
            apiKey = _providerApiKey.value,
            model = _providerModel.value,
            baseUrl = _providerBaseUrl.value
        )
    }

    /** Save full provider config and update all flows. */
    fun setProviderConfig(config: AiProviderConfig) {
        prefs.edit().apply {
            putString("ai_provider_type", config.type.name)
            putString("ai_provider_api_key", config.apiKey)
            putString("ai_provider_model", config.model)
            putString("ai_provider_base_url", config.baseUrl)
            apply()
        }
        _providerType.value = config.type
        _providerApiKey.value = config.apiKey
        _providerModel.value = config.model
        _providerBaseUrl.value = config.baseUrl

        // Also sync the legacy apiKey for backward compat
        setApiKey(config.apiKey)
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsManager(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
