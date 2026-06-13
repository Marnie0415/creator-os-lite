package com.example.ai

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

// --- Gemini API data types (reused from GeminiApiClient) ---

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Double? = 0.7,
    val safetySettings: List<SafetySetting>? = null
)

@JsonClass(generateAdapter = true)
data class SafetySetting(
    val category: String,
    val threshold: String
)

@JsonClass(generateAdapter = true)
data class PartResponse(val text: String?)

@JsonClass(generateAdapter = true)
data class ContentResponse(val parts: List<PartResponse>?)

@JsonClass(generateAdapter = true)
data class Candidate(val content: ContentResponse?)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(val candidates: List<Candidate>?)

interface GeminiApiService {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Provider implementation ---

private const val GEMINI_TIMEOUT_SECONDS = 60L

class GeminiService(private val config: AiProviderConfig) : AiService {

    private val modelName = config.effectiveModel()
    private val baseUrl = config.effectiveBaseUrl()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(GEMINI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(GEMINI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(GEMINI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    val defaultSafetySettings = listOf(
        SafetySetting(category = "HARM_CATEGORY_HARASSMENT", threshold = "BLOCK_MEDIUM_AND_ABOVE"),
        SafetySetting(category = "HARM_CATEGORY_HATE_SPEECH", threshold = "BLOCK_MEDIUM_AND_ABOVE"),
        SafetySetting(category = "HARM_CATEGORY_SEXUALLY_EXPLICIT", threshold = "BLOCK_MEDIUM_AND_ABOVE"),
        SafetySetting(category = "HARM_CATEGORY_DANGEROUS_CONTENT", threshold = "BLOCK_MEDIUM_AND_ABOVE")
    )

    override fun isConfigured(): Boolean = config.apiKey.isNotBlank()

    override suspend fun generateResponse(
        systemPrompt: String?,
        messages: List<ChatMessage>,
        temperature: Double
    ): AiResult {
        return try {
            val geminiContents = messages.map { msg ->
                Content(parts = listOf(Part(text = msg.content)))
            }

            val systemInstruction = systemPrompt?.let {
                Content(parts = listOf(Part(text = it)))
            }

            val request = GenerateContentRequest(
                contents = geminiContents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(
                    temperature = temperature,
                    safetySettings = defaultSafetySettings
                )
            )

            // ⚠ Gemini API requires the key as a query parameter, NOT as a header
            val key = config.apiKey.ifBlank { GeminiApiClient.getApiKey() }
            val url = "v1beta/models/$modelName:generateContent?key=$key"
            val response = service.generateContent(url, request)

            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            if (text != null) {
                AiResult.Success(text.trim())
            } else {
                AiResult.Error("The AI returned an empty response. Please try again.")
            }
        } catch (e: Exception) {
            AiResult.Error("Gemini API error: ${e.localizedMessage ?: "Connection failed"}")
        }
    }
}
