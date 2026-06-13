package com.example.ai

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// --- Anthropic Messages API data types ---

@JsonClass(generateAdapter = true)
data class AnthropicMessage(
    val role: String,     // "user" or "assistant"
    val content: String
)

@JsonClass(generateAdapter = true)
data class MessagesRequest(
    val model: String,
    val max_tokens: Int,
    val messages: List<AnthropicMessage>,
    val system: String? = null,
    val temperature: Double? = null
)

@JsonClass(generateAdapter = true)
data class TextContent(
    val text: String?,
    val type: String? = null
)

@JsonClass(generateAdapter = true)
data class MessagesResponse(
    val content: List<TextContent>?,
    val type: String? = null
)

@JsonClass(generateAdapter = true)
data class AnthropicErrorResponse(
    val error: AnthropicErrorDetail?
)

@JsonClass(generateAdapter = true)
data class AnthropicErrorDetail(
    val message: String?,
    val type: String?
)

interface AnthropicApiService {
    @POST("v1/messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String,
        @Body request: MessagesRequest
    ): MessagesResponse
}

// --- Provider implementation ---

private val anthropicMoshi = Moshi.Builder().build()
private val anthropicErrorAdapter = anthropicMoshi.adapter(AnthropicErrorResponse::class.java)

class AnthropicService(private val config: AiProviderConfig) : AiService {

    private val baseUrl = config.effectiveBaseUrl().trimEnd('/')
    private val modelName = config.effectiveModel()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)  // Claude can take longer
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("$baseUrl/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val service: AnthropicApiService = retrofit.create(AnthropicApiService::class.java)

    override fun isConfigured(): Boolean = config.apiKey.isNotBlank()

    override suspend fun generateResponse(
        systemPrompt: String?,
        messages: List<ChatMessage>,
        temperature: Double
    ): AiResult {
        return try {
            // Anthropic requires alternating user/assistant messages.
            // Convert ChatMessage list to Anthropic format.
            val anthropicMessages = messages.map { msg ->
                AnthropicMessage(
                    role = if (msg.role == "system") "user" else msg.role,
                    content = msg.content
                )
            }

            // If there's a system prompt and the first message isn't system,
            // prepend a user message containing the system instructions.
            val effectiveSystem = systemPrompt?.takeIf { it.isNotBlank() }

            val request = MessagesRequest(
                model = modelName,
                max_tokens = 4096,
                messages = anthropicMessages.ifEmpty {
                    listOf(AnthropicMessage(role = "user", content = "Hello"))
                },
                system = effectiveSystem,
                temperature = temperature
            )

            val response = service.createMessage(
                apiKey = config.apiKey,
                version = "2023-06-01",
                request = request
            )

            val text = response.content
                ?.firstOrNull { it.type == "text" }
                ?.text

            if (text != null) {
                AiResult.Success(text.trim())
            } else {
                AiResult.Error("The AI returned an empty response. Please try again.")
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val parsed = errorBody?.let { runCatching { anthropicErrorAdapter.fromJson(it) }.getOrNull() }
            val message = parsed?.error?.message ?: e.localizedMessage ?: "HTTP ${e.code()}"
            AiResult.Error("Claude API error ($modelName): $message")
        } catch (e: Exception) {
            AiResult.Error("Claude API error ($modelName): ${e.localizedMessage ?: "Connection failed"}")
        }
    }

    companion object {
        val POPULAR_MODELS = linkedMapOf(
            "Claude Sonnet 4" to "claude-sonnet-4",
            "Claude Sonnet 4.5" to "claude-sonnet-4-5",
            "Claude Haiku 3.5" to "claude-3-5-haiku-latest",
            "Claude Opus 4" to "claude-opus-4",
            "Claude Opus 4.8" to "claude-opus-4-8"
        )
    }
}
