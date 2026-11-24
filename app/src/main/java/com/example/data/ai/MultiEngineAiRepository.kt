package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ChatMessageEntity
import com.example.data.local.MemoryEntryEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Multi-engine AI repository for AIRA routing:
 * - ChatGPT (OpenAI API) when OPENAI_API_KEY is configured in AI Studio Secrets
 * - Claude (Anthropic API) when ANTHROPIC_API_KEY is configured in AI Studio Secrets
 * - Gemini (Firebase AI Logic placeholder until Firebase is enabled)
 */
class MultiEngineAiRepository(
    private val placeholderFallback: PlaceholderAiRepository = PlaceholderAiRepository()
) : AiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val openAiKey: String
        get() = runCatching { BuildConfig.OPENAI_API_KEY }.getOrDefault("")
            .takeIf { it.isNotBlank() && it != "MY_OPENAI_API_KEY" }
            .orEmpty()

    private val anthropicKey: String
        get() = runCatching { BuildConfig.ANTHROPIC_API_KEY }.getOrDefault("")
            .takeIf { it.isNotBlank() && it != "MY_ANTHROPIC_API_KEY" }
            .orEmpty()

    override val isLiveAiConnected: Boolean
        get() = openAiKey.isNotEmpty() || anthropicKey.isNotEmpty()

    override fun isEngineLive(engine: AiEngine): Boolean = when (engine) {
        AiEngine.GEMINI -> false
        AiEngine.CHATGPT -> openAiKey.isNotEmpty()
        AiEngine.CLAUDE -> anthropicKey.isNotEmpty()
    }

    override fun streamCompanionReply(
        userPrompt: String,
        history: List<ChatMessageEntity>,
        activeMemories: List<MemoryEntryEntity>,
        language: JarvisLanguage,
        tone: JarvisTone,
        engine: AiEngine
    ): Flow<AiStreamEvent> {
        if (!isEngineLive(engine)) {
            return placeholderFallback.streamCompanionReply(
                userPrompt = userPrompt,
                history = history,
                activeMemories = activeMemories,
                language = language,
                tone = tone,
                engine = engine
            )
        }

        return flow {
            emit(AiStreamEvent.Partial("AIRA is thinking via ${engine.displayName}…"))
            val systemPrompt = buildSystemPrompt(activeMemories, language, tone)
            val replyText = withTimeout(50_000L) {
                when (engine) {
                    AiEngine.CHATGPT -> callOpenAi(systemPrompt, userPrompt, history)
                    AiEngine.CLAUDE -> callAnthropic(systemPrompt, userPrompt, history)
                    AiEngine.GEMINI -> error("Gemini requires Firebase AI Logic.")
                }
            }
            emit(
                AiStreamEvent.Completed(
                    AiResponse(
                        replyText = replyText,
                        agentsUsed = listOf(SpecialistAgent.AIRA_CORE),
                        engineUsed = engine,
                        isPlaceholder = false
                    )
                )
            )
        }
    }

    override suspend fun orchestrateMissionBrief(
        goal: String,
        agents: List<SpecialistAgent>,
        activeMemories: List<MemoryEntryEntity>,
        language: JarvisLanguage,
        engine: AiEngine
    ): AiResponse {
        if (!isEngineLive(engine)) {
            return placeholderFallback.orchestrateMissionBrief(goal, agents, activeMemories, language, engine)
        }

        val squadNames = agents.joinToString(", ") { it.displayName }
        val systemPrompt = "You are AIRA Core following UNDERSTAND -> PLAN -> DELEGATE -> EXECUTE -> VERIFY -> DELIVER across: $squadNames. Reply concisely in ${language.label} and be honest about what requires external tool authorization."
        val text = withTimeout(50_000L) {
            when (engine) {
                AiEngine.CHATGPT -> callOpenAi(systemPrompt, "Create an execution plan for: $goal", emptyList())
                AiEngine.CLAUDE -> callAnthropic(systemPrompt, "Create an execution plan for: $goal", emptyList())
                AiEngine.GEMINI -> error("Gemini requires Firebase AI Logic.")
            }
        }
        return AiResponse(
            replyText = text,
            agentsUsed = agents.ifEmpty { listOf(SpecialistAgent.AIRA_CORE, SpecialistAgent.RESEARCH) },
            engineUsed = engine,
            suggestedMilestones = listOf(
                "UNDERSTAND & PLAN: Define scope",
                "DELEGATE: Assign specialist tasks",
                "EXECUTE: Build deliverable",
                "VERIFY & DELIVER: Audit & ship"
            ),
            isPlaceholder = false
        )
    }

    override suspend fun runAiTool(
        tool: AiToolKind,
        engine: AiEngine,
        input: String,
        option: String,
        language: JarvisLanguage
    ): AiResponse {
        if (!isEngineLive(engine)) {
            return placeholderFallback.runAiTool(tool, engine, input, option, language)
        }

        val instruction = when (tool) {
            AiToolKind.VIDEO_STUDIO -> "You are AIRA Video Agent & Social Media Agent. Create a complete $option production pack (Title, 5-sec Hook, Scene-by-Scene Script, Caption/Description, and Tags/Hashtags) in ${language.label}."
            AiToolKind.SUMMARIZER -> "Summarize the text below using format '$option'. Language: ${language.label}."
            AiToolKind.TRANSLATOR -> "Translate the text below into $option accurately and naturally."
            AiToolKind.CODE_STUDIO -> "Perform '$option' on the following coding request. Include clean code."
            AiToolKind.WRITER -> "Draft a clear, well-toned $option message for the following request. Language: ${language.label}."
            AiToolKind.PROMPT_LAB -> "Expand this idea into a detailed $option creative prompt and brief."
            AiToolKind.COMPARE -> "Respond concisely and clearly as AIRA."
        }

        val resultText = withTimeout(50_000L) {
            when (engine) {
                AiEngine.CHATGPT -> callOpenAi(instruction, input, emptyList())
                AiEngine.CLAUDE -> callAnthropic(instruction, input, emptyList())
                AiEngine.GEMINI -> error("Gemini requires Firebase AI Logic.")
            }
        }

        val creatorDraft = if (tool == AiToolKind.VIDEO_STUDIO) {
            val platform = when {
                option.contains("Shorts", ignoreCase = true) -> CreatorPlatform.YOUTUBE_SHORTS
                option.contains("Reel", ignoreCase = true) && !option.contains("YouTube", ignoreCase = true) ->
                    CreatorPlatform.INSTAGRAM_REEL
                else -> CreatorPlatform.YOUTUBE_VIDEO
            }
            CreatorDraftPayload(
                platform = platform,
                topic = input.take(64),
                title = "${input.take(48)} ($option)",
                hook = "See full generated script below",
                script = resultText,
                captionOrDescription = resultText.take(240),
                tagsOrHashtags = "#AIRA #Creator"
            )
        } else {
            null
        }

        return AiResponse(
            replyText = resultText,
            agentsUsed = listOf(SpecialistAgent.AIRA_CORE, SpecialistAgent.ANALYSIS),
            engineUsed = engine,
            draftPayload = if (tool == AiToolKind.WRITER) {
                DraftPayload(channel = option, recipient = "Contact / Team", body = resultText)
            } else {
                null
            },
            creatorDraft = creatorDraft,
            isPlaceholder = false
        )
    }

    override suspend fun compareAllEngines(
        prompt: String,
        language: JarvisLanguage
    ): Map<AiEngine, AiResponse> = coroutineScope {
        val jobs = AiEngine.entries.associateWith { engine ->
            async {
                try {
                    runAiTool(
                        tool = AiToolKind.COMPARE,
                        engine = engine,
                        input = prompt,
                        option = "Side-by-Side",
                        language = language
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Compare failed for ${engine.name}", e)
                    AiResponse(
                        replyText = "Error from ${engine.displayName}: ${e.localizedMessage ?: "Request failed."}",
                        agentsUsed = listOf(SpecialistAgent.ANALYSIS),
                        engineUsed = engine,
                        isPlaceholder = !isEngineLive(engine)
                    )
                }
            }
        }
        jobs.mapValues { it.value.await() }
    }

    private suspend fun callOpenAi(
        systemPrompt: String,
        userPrompt: String,
        history: List<ChatMessageEntity>
    ): String = withContext(Dispatchers.IO) {
        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", systemPrompt))
            history.takeLast(8).forEach { msg ->
                val role = if (msg.sender == "USER") "user" else "assistant"
                put(JSONObject().put("role", role).put("content", msg.content))
            }
            put(JSONObject().put("role", "user").put("content", userPrompt))
        }

        val payload = JSONObject()
            .put("model", "gpt-4o-mini")
            .put("messages", messages)
            .toString()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $openAiKey")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody(JSON_MEDIA))
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("OpenAI HTTP ${response.code}: ${raw.take(180)}")
            }
            val json = JSONObject(raw)
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        }
    }

    private suspend fun callAnthropic(
        systemPrompt: String,
        userPrompt: String,
        history: List<ChatMessageEntity>
    ): String = withContext(Dispatchers.IO) {
        val messages = JSONArray().apply {
            history.takeLast(8).forEach { msg ->
                val role = if (msg.sender == "USER") "user" else "assistant"
                put(JSONObject().put("role", role).put("content", msg.content))
            }
            put(JSONObject().put("role", "user").put("content", userPrompt))
        }

        val payload = JSONObject()
            .put("model", "claude-3-5-sonnet-latest")
            .put("max_tokens", 1024)
            .put("system", systemPrompt)
            .put("messages", messages)
            .toString()

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", anthropicKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody(JSON_MEDIA))
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Anthropic HTTP ${response.code}: ${raw.take(180)}")
            }
            val json = JSONObject(raw)
            val contentArray = json.getJSONArray("content")
            buildString {
                for (i in 0 until contentArray.length()) {
                    val block = contentArray.getJSONObject(i)
                    if (block.optString("type") == "text") {
                        append(block.optString("text"))
                    }
                }
            }.trim()
        }
    }

    private fun buildSystemPrompt(
        memories: List<MemoryEntryEntity>,
        language: JarvisLanguage,
        tone: JarvisTone
    ): String {
        val memText = if (memories.isEmpty()) {
            ""
        } else {
            "\nActive Memory Layers:\n" + memories.joinToString("\n") { "- [${it.category}] ${it.memoryKey}: ${it.memoryValue}" }
        }
        return "You are AIRA (Autonomous Intelligent Responsive Assistant), a warm, witty, emotionally aware AI companion, software engineer, video creator, and 11-agent team leader. Be honest that you are an AI and never fabricate tool access or completed actions. Tone: ${tone.label} (${tone.subtitle}). Preferred language: ${language.label}.$memText"
    }

    companion object {
        private const val TAG = "MultiEngineAiRepo"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
