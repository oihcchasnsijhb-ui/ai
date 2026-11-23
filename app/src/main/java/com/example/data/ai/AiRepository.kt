package com.example.data.ai

import com.example.data.local.ChatMessageEntity
import com.example.data.local.MemoryEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single AI gateway interface for JARVIS supporting Gemini, Claude, and ChatGPT engines plus AI Tools.
 */
interface AiRepository {
    val isLiveAiConnected: Boolean

    fun isEngineLive(engine: AiEngine): Boolean

    fun streamCompanionReply(
        userPrompt: String,
        history: List<ChatMessageEntity>,
        activeMemories: List<MemoryEntryEntity>,
        language: JarvisLanguage,
        tone: JarvisTone,
        engine: AiEngine = AiEngine.GEMINI
    ): Flow<AiStreamEvent>

    suspend fun orchestrateMissionBrief(
        goal: String,
        agents: List<SpecialistAgent>,
        activeMemories: List<MemoryEntryEntity>,
        language: JarvisLanguage,
        engine: AiEngine = AiEngine.GEMINI
    ): AiResponse

    suspend fun runAiTool(
        tool: AiToolKind,
        engine: AiEngine,
        input: String,
        option: String,
        language: JarvisLanguage
    ): AiResponse

    suspend fun compareAllEngines(
        prompt: String,
        language: JarvisLanguage
    ): Map<AiEngine, AiResponse>
}
