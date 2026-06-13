package com.example.ai

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// --- OpenAI-compatible Chat Completions data types ---

@JsonClass(generateAdapter = true)
data class OpenAiMessage(
    val role: String,     // "system", "user", "assistant"
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Double? = null,
    val maxTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class Choice(
    val message: OpenAiMessage?,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    val choices: List<Choice>?
)

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val error: ErrorDetail?
)

@JsonClass(generateAdapter = true)
data class ErrorDetail(
    val message: String?,
    val type: String?,
    val code: String?
)

interface OpenAiApiService {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

// --- Provider implementation ---

private val moshi = Moshi.Builder().build()
private val errorAdapter = moshi.adapter(ErrorResponse::class.java)

class OpenAiService(private val config: AiProviderConfig) : AiService {

    private val baseUrl = config.effectiveBaseUrl().trimEnd('/')
    private val modelName = config.effectiveModel()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val key = config.apiKey
        if (key.isEmpty()) return@Interceptor chain.proceed(original)
        original.newBuilder()
            .header("Authorization", "Bearer $key")
            .build()
            .let { chain.proceed(it) }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("$baseUrl/")  // ensure trailing slash for Retrofit
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val service: OpenAiApiService = retrofit.create(OpenAiApiService::class.java)

    override fun isConfigured(): Boolean = config.apiKey.isNotBlank()

    override suspend fun generateResponse(
        systemPrompt: String?,
        messages: List<ChatMessage>,
        temperature: Double
    ): AiResult {
        return try {
            val openAiMessages = mutableListOf<OpenAiMessage>()

            // Add system prompt as a system message
            if (!systemPrompt.isNullOrBlank()) {
                openAiMessages.add(OpenAiMessage(role = "system", content = systemPrompt.trim()))
            }

            // Add conversation messages
            openAiMessages.addAll(messages.map { msg ->
                OpenAiMessage(role = msg.role, content = msg.content)
            })

            val request = ChatCompletionRequest(
                model = modelName,
                messages = openAiMessages,
                temperature = temperature,
                maxTokens = 4096
            )

            val response = service.chatCompletion(request)

            val text = response.choices?.firstOrNull()?.message?.content

            if (text != null) {
                AiResult.Success(text.trim())
            } else {
                AiResult.Error("The AI returned an empty response. Please try again.")
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val parsed = errorBody?.let { runCatching { errorAdapter.fromJson(it) }.getOrNull() }
            val message = parsed?.error?.message ?: e.localizedMessage ?: "HTTP ${e.code()}"
            AiResult.Error("API error ($modelName): $message")
        } catch (e: Exception) {
            AiResult.Error("API error ($modelName): ${e.localizedMessage ?: "Connection failed"}")
        }
    }

    companion object {
        /**
         * Popular model suggestions for the UI.
         * Key = display label, Value = model ID.
         */
        val POPULAR_MODELS = linkedMapOf(
            "OpenAI GPT-4o" to "gpt-4o",
            "OpenAI GPT-4o-mini" to "gpt-4o-mini",
            "OpenAI o3-mini" to "o3-mini",
            "DeepSeek V3" to "deepseek-chat",
            "DeepSeek R1" to "deepseek-reasoner",
            "Groq Llama 3.3 70B" to "llama-3.3-70b-versatile",
            "Groq DeepSeek R1" to "deepseek-r1-distill-llama-70b",
            "Together AI Llama 3.3" to "meta-llama/Llama-3.3-70B-Instruct-Turbo",
            "OpenRouter (auto-select)" to "openrouter/auto",
            "Perplexity Sonar" to "sonar-pro",
            "Mistral Large" to "mistral-large-latest"
        )

        /**
         * Known provider base URLs for quick selection.
         */
        val KNOWN_PROVIDERS = linkedMapOf(
            "OpenAI" to "https://api.openai.com/v1",
            "DeepSeek" to "https://api.deepseek.com/v1",
            "Groq" to "https://api.groq.com/openai/v1",
            "Together AI" to "https://api.together.xyz/v1",
            "OpenRouter" to "https://openrouter.ai/api/v1",
            "Perplexity" to "https://api.perplexity.ai",
            "GitHub Models" to "https://models.inference.ai.azure.com",
            "Mistral AI" to "https://api.mistral.ai/v1",
            "xAI Grok" to "https://api.x.ai/v1"
        )
    }
}
