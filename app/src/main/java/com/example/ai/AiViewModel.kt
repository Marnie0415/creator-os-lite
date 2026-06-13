package com.example.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.ui.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val MAX_INPUT_LENGTH = 5000

sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val result: String) : AiState()
    data class Error(val message: String) : AiState()
}

class AiViewModel(
    application: Application,
    private val settingsManager: SettingsManager
) : AndroidViewModel(application) {

    /** Whether any API key has been configured. */
    val hasAnyKeyConfigured: StateFlow<Boolean> = settingsManager.providerApiKey.map { it.isNotBlank() }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)

    init {
        val savedKey = settingsManager.apiKey.value
        if (savedKey.isNotBlank()) {
            GeminiApiClient.userApiKey = savedKey
        }
    }

    private fun resolveService(): AiService {
        return AiServiceManager.getService(settingsManager.getProviderConfig())
    }

    private val _followUpState = MutableStateFlow<AiState>(AiState.Idle)
    val followUpState: StateFlow<AiState> = _followUpState

    private val _quoteState = MutableStateFlow<AiState>(AiState.Idle)
    val quoteState: StateFlow<AiState> = _quoteState

    fun generateFollowUp(
        clientName: String,
        amount: Double,
        contextInfo: String,
        tone: String
    ) {
        val service = resolveService()
        if (!service.isConfigured()) {
            _followUpState.value = AiState.Error(getApplication<Application>().getString(R.string.error_api_key_missing))
            return
        }

        if (clientName.length > MAX_INPUT_LENGTH || contextInfo.length > MAX_INPUT_LENGTH) {
            _followUpState.value = AiState.Error(getApplication<Application>().getString(R.string.error_input_too_long))
            return
        }

        _followUpState.value = AiState.Loading
        viewModelScope.launch {
            val amountStr = if (amount > 0) com.example.ui.CurrencyUtils.format(amount) else "outstanding"
            val systemPrompt = "You are a risk-control payment assistant for solo freelancers."
            val userPrompt = """
                Write an immediately copyable, professional follow-up message to a client named '$clientName'.
                Situation: Client has an invoice / project status of $contextInfo. The pending balance is $amountStr.
                Tone of the message: $tone.

                Rules:
                - Do NOT include any placeholder brackets (like [Your Name], [Client Name] etc.).
                - Use a generic professional signature or sign off as 'Client Success Team' or leave simple spacing at the bottom.
                - Do NOT include any legal threat or legal advice.
                - Keep the response extremely clean, and immediately copyable.
                - Avoid conversational prefix chat; return ONLY the message itself.
            """.trimIndent()

            when (val result = service.generateResponse(
                systemPrompt = systemPrompt,
                messages = listOf(ChatMessage(role = "user", content = userPrompt)),
                temperature = 0.7
            )) {
                is AiResult.Success -> _followUpState.value = AiState.Success(result.text)
                is AiResult.Error -> _followUpState.value = AiState.Error(result.message)
            }
        }
    }

    fun generateQuote(rawRequirements: String) {
        val service = resolveService()
        if (!service.isConfigured()) {
            _quoteState.value = AiState.Error(getApplication<Application>().getString(R.string.error_api_key_missing))
            return
        }

        if (rawRequirements.length > MAX_INPUT_LENGTH) {
            _quoteState.value = AiState.Error(
                getApplication<Application>().getString(R.string.error_input_limit, MAX_INPUT_LENGTH)
            )
            return
        }

        _quoteState.value = AiState.Loading
        viewModelScope.launch {
            val sanitized = rawRequirements.take(MAX_INPUT_LENGTH).trim()
            val systemPrompt = "You are a freelance business consultant."
            val userPrompt = """
                Analyze the following raw brief or informal client requirements:
                "$sanitized"

                Generate a highly structured, copy-pasteable quote for the client.
                Format the response beautifully in clean Markdown with clear headings:

                ### Project Scope
                (Concise summary of what will be done)

                ### Key Deliverables
                (Bullet points of target deliverables)

                ### Suggested Timeline
                (Estimated milestones and hours/days required)

                ### Suggested Pricing
                (Standard flat rate and/or deposit recommendation)

                Rules:
                - Avoid any legal boilerplate or threats.
                - Make it professional and ready to send to a client.
                - Do NOT include conversational prefaces like "Here is your quote:". Start directly with the markdown.
            """.trimIndent()

            when (val result = service.generateResponse(
                systemPrompt = systemPrompt,
                messages = listOf(ChatMessage(role = "user", content = userPrompt)),
                temperature = 0.7
            )) {
                is AiResult.Success -> _quoteState.value = AiState.Success(result.text)
                is AiResult.Error -> _quoteState.value = AiState.Error(result.message)
            }
        }
    }

    fun clearFollowUp() {
        _followUpState.value = AiState.Idle
    }

    fun clearQuote() {
        _quoteState.value = AiState.Idle
    }
}
