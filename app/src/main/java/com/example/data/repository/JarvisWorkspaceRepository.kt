package com.example.data.repository

import com.example.data.ai.AiEngine
import com.example.data.ai.AiRepository
import com.example.data.ai.AiResponse
import com.example.data.ai.AiStreamEvent
import com.example.data.ai.AiToolKind
import com.example.data.ai.JarvisLanguage
import com.example.data.ai.JarvisTone
import com.example.data.ai.SpecialistAgent
import com.example.data.local.AgentMissionEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisPreferences
import com.example.data.local.MemoryEntryEntity
import com.example.data.local.PlannerTaskEntity
import com.example.data.local.PreferencesRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

class JarvisWorkspaceRepository(
    private val db: JarvisDatabase,
    private val preferencesRepository: PreferencesRepository,
    private val aiRepository: AiRepository
) {
    val isLiveAiConnected: Boolean get() = aiRepository.isLiveAiConnected

    fun isEngineLive(engine: AiEngine): Boolean = aiRepository.isEngineLive(engine)

    val preferencesFlow: Flow<JarvisPreferences> = preferencesRepository.preferencesFlow
    val conversationsFlow: Flow<List<ConversationEntity>> = db.conversationDao().observeConversations()
    val missionsFlow: Flow<List<AgentMissionEntity>> = db.agentMissionDao().observeMissions()
    val plannerTasksFlow: Flow<List<PlannerTaskEntity>> = db.plannerTaskDao().observeTasks()
    val memoriesFlow: Flow<List<MemoryEntryEntity>> = db.memoryDao().observeMemories()
    val studioArtifactsFlow: Flow<List<ChatMessageEntity>> = db.chatMessageDao().observeStudioArtifacts()

    fun observeMessages(conversationId: Long): Flow<List<ChatMessageEntity>> =
        db.chatMessageDao().observeMessages(conversationId)

    suspend fun createConversation(title: String, languageHint: String = "AUTO"): Long {
        return db.conversationDao().insertConversation(
            ConversationEntity(
                title = title.take(48).ifBlank { "New JARVIS Session" },
                languageHint = languageHint
            )
        )
    }

    suspend fun deleteConversation(id: Long) {
        db.conversationDao().deleteConversation(id)
    }

    suspend fun appendUserMessage(conversationId: Long, text: String, updateTitleIfDefault: Boolean) {
        db.chatMessageDao().insertMessage(
            ChatMessageEntity(
                conversationId = conversationId,
                sender = "USER",
                content = text,
                isPlaceholderNotice = false
            )
        )
        val now = System.currentTimeMillis()
        val newTitle = if (updateTitleIfDefault) text.take(40) else ""
        if (newTitle.isNotBlank()) {
            db.conversationDao().updateConversationMeta(conversationId, newTitle, now)
        }
    }

    fun streamCompanionReply(
        prompt: String,
        history: List<ChatMessageEntity>,
        activeMemories: List<MemoryEntryEntity>,
        language: JarvisLanguage,
        tone: JarvisTone,
        engine: AiEngine
    ): Flow<AiStreamEvent> {
        return aiRepository.streamCompanionReply(
            userPrompt = prompt,
            history = history,
            activeMemories = activeMemories,
            language = language,
            tone = tone,
            engine = engine
        )
    }

    suspend fun runAiTool(
        tool: AiToolKind,
        engine: AiEngine,
        input: String,
        option: String,
        language: JarvisLanguage
    ): AiResponse = aiRepository.runAiTool(tool, engine, input, option, language)

    suspend fun compareAllEngines(
        prompt: String,
        language: JarvisLanguage
    ): Map<AiEngine, AiResponse> = aiRepository.compareAllEngines(prompt, language)

    suspend fun saveJarvisReply(conversationId: Long, response: AiResponse) {
        val tasksEncoded = response.suggestedPlannerTasks.joinToString(";;") { (time, title) ->
            "${time.replace("|", " ")}|${title.replace("|", " ")}"
        }
        db.chatMessageDao().insertMessage(
            ChatMessageEntity(
                conversationId = conversationId,
                sender = "JARVIS",
                content = response.replyText,
                codeSnippet = response.codeSnippet,
                codeLanguage = response.codeLanguage,
                agentsInvolved = response.agentsUsed.joinToString(",") { it.name },
                draftChannel = response.draftPayload?.channel,
                draftRecipient = response.draftPayload?.recipient,
                draftBody = response.draftPayload?.body,
                draftStatus = if (response.draftPayload != null) "AWAITING_CONFIRMATION" else null,
                suggestedTasksEncoded = tasksEncoded,
                engineName = response.engineUsed.name,
                isPlaceholderNotice = response.isPlaceholder
            )
        )
    }

    suspend fun updateDraft(messageId: Long, newBody: String, status: String) {
        db.chatMessageDao().updateDraft(messageId, newBody, status)
    }

    suspend fun importSuggestedTasks(messageId: Long, tasks: List<Pair<String, String>>) {
        tasks.forEach { (time, title) ->
            db.plannerTaskDao().insertTask(
                PlannerTaskEntity(
                    title = title,
                    timeSlot = time,
                    category = "JARVIS Plan",
                    priority = "HIGH"
                )
            )
        }
        db.chatMessageDao().clearSuggestedTasks(messageId)
    }

    suspend fun createMission(
        title: String,
        goal: String,
        agents: List<SpecialistAgent>,
        activeMemories: List<MemoryEntryEntity>,
        language: JarvisLanguage,
        engine: AiEngine
    ) {
        val brief = aiRepository.orchestrateMissionBrief(
            goal = goal,
            agents = agents,
            activeMemories = activeMemories,
            language = language,
            engine = engine
        )
        val milestonesEncoded = brief.suggestedMilestones.joinToString(";;") { "0|${it.replace("|", " ")}" }
        db.agentMissionDao().insertMission(
            AgentMissionEntity(
                title = title.ifBlank { goal.take(42) },
                goal = goal,
                assignedAgents = brief.agentsUsed.joinToString(",") { it.name },
                status = "IN_PROGRESS",
                milestonesEncoded = milestonesEncoded,
                summaryOutput = brief.replyText,
                isPlaceholderNotice = brief.isPlaceholder
            )
        )
    }

    suspend fun updateMission(mission: AgentMissionEntity) {
        db.agentMissionDao().updateMission(mission.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteMission(id: Long) {
        db.agentMissionDao().deleteMission(id)
    }

    suspend fun addPlannerTask(title: String, timeSlot: String, category: String, priority: String) {
        db.plannerTaskDao().insertTask(
            PlannerTaskEntity(
                title = title,
                timeSlot = timeSlot.ifBlank { "Anytime" },
                category = category.ifBlank { "Personal" },
                priority = priority
            )
        )
    }

    suspend fun togglePlannerTask(id: Long, completed: Boolean) {
        db.plannerTaskDao().setTaskCompleted(id, completed)
    }

    suspend fun deletePlannerTask(id: Long) {
        db.plannerTaskDao().deleteTask(id)
    }

    suspend fun addMemory(category: String, key: String, value: String) {
        db.memoryDao().insertMemory(
            MemoryEntryEntity(
                category = category,
                memoryKey = key,
                memoryValue = value,
                isEnabled = true
            )
        )
    }

    suspend fun toggleMemoryEntry(entry: MemoryEntryEntity) {
        db.memoryDao().updateMemory(
            entry.copy(isEnabled = !entry.isEnabled, updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun deleteMemoryEntry(id: Long) {
        db.memoryDao().deleteMemory(id)
    }

    suspend fun setMemoryEnabled(enabled: Boolean) = preferencesRepository.setMemoryEnabled(enabled)
    suspend fun setPreferredLanguage(language: JarvisLanguage) = preferencesRepository.setPreferredLanguage(language)
    suspend fun setPersonalityTone(tone: JarvisTone) = preferencesRepository.setPersonalityTone(tone)
    suspend fun setThemeMode(mode: ThemeMode) = preferencesRepository.setThemeMode(mode)
    suspend fun setSelectedEngine(engine: AiEngine) = preferencesRepository.setSelectedEngine(engine)
}
