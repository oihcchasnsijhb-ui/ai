package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiEngine
import com.example.data.ai.AiResponse
import com.example.data.ai.AiStreamEvent
import com.example.data.ai.AiToolKind
import com.example.data.ai.CreatorDraftPayload
import com.example.data.ai.CreatorPlatform
import com.example.data.ai.JarvisLanguage
import com.example.data.ai.JarvisTone
import com.example.data.ai.MultiEngineAiRepository
import com.example.data.ai.SpecialistAgent
import com.example.data.local.AgentMissionEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.CreatorPackEntity
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisPreferences
import com.example.data.local.MemoryEntryEntity
import com.example.data.local.PlannerTaskEntity
import com.example.data.local.PreferencesRepository
import com.example.data.repository.JarvisWorkspaceRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MilestoneItem(
    val isDone: Boolean,
    val title: String
)

data class JarvisUiState(
    val isLiveAiConnected: Boolean = false,
    val liveEngines: Set<AiEngine> = emptySet(),
    val preferences: JarvisPreferences = JarvisPreferences(),
    val conversations: List<ConversationEntity> = emptyList(),
    val activeConversationId: Long? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val streamingReply: String? = null,
    val isGenerating: Boolean = false,
    val missions: List<AgentMissionEntity> = emptyList(),
    val plannerTasks: List<PlannerTaskEntity> = emptyList(),
    val memories: List<MemoryEntryEntity> = emptyList(),
    val creatorPacks: List<CreatorPackEntity> = emptyList(),
    val studioArtifacts: List<ChatMessageEntity> = emptyList(),
    val activeTool: AiToolKind = AiToolKind.VIDEO_STUDIO,
    val toolOutput: AiResponse? = null,
    val toolComparisons: Map<AiEngine, AiResponse> = emptyMap(),
    val isRunningTool: Boolean = false,
    val statusNotice: String? = null,
    val errorMessage: String? = null
)

private data class WorkspaceCollections(
    val preferences: JarvisPreferences,
    val conversations: List<ConversationEntity>,
    val activeId: Long?,
    val messages: List<ChatMessageEntity>,
    val missions: List<AgentMissionEntity>
)

private data class SecondaryCollections(
    val tasks: List<PlannerTaskEntity>,
    val memories: List<MemoryEntryEntity>,
    val creatorPacks: List<CreatorPackEntity>,
    val artifacts: List<ChatMessageEntity>,
    val streamingReply: String?
)

private data class ToolCollections(
    val activeTool: AiToolKind,
    val toolOutput: AiResponse?,
    val toolComparisons: Map<AiEngine, AiResponse>,
    val isRunningTool: Boolean,
    val isGenerating: Boolean
)

class JarvisViewModel(
    private val repository: JarvisWorkspaceRepository
) : ViewModel() {

    private val _selectedConversationId = MutableStateFlow<Long?>(null)
    private val _streamingReply = MutableStateFlow<String?>(null)
    private val _isGenerating = MutableStateFlow(false)
    private val _statusNotice = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _activeTool = MutableStateFlow(AiToolKind.VIDEO_STUDIO)
    private val _toolOutput = MutableStateFlow<AiResponse?>(null)
    private val _toolComparisons = MutableStateFlow<Map<AiEngine, AiResponse>>(emptyMap())
    private val _isRunningTool = MutableStateFlow(false)

    private val activeConversationIdFlow = combine(
        repository.conversationsFlow,
        _selectedConversationId
    ) { conversations, selected ->
        when {
            selected != null && conversations.any { it.id == selected } -> selected
            else -> conversations.firstOrNull()?.id
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val messagesFlow = activeConversationIdFlow.flatMapLatest { convId ->
        if (convId == null) flowOf(emptyList()) else repository.observeMessages(convId)
    }

    private val primaryFlow = combine(
        repository.preferencesFlow,
        repository.conversationsFlow,
        activeConversationIdFlow,
        messagesFlow,
        repository.missionsFlow
    ) { prefs, conversations, activeId, messages, missions ->
        WorkspaceCollections(prefs, conversations, activeId, messages, missions)
    }

    private val secondaryFlow = combine(
        repository.plannerTasksFlow,
        repository.memoriesFlow,
        repository.creatorPacksFlow,
        repository.studioArtifactsFlow,
        _streamingReply
    ) { tasks, memories, packs, artifacts, streaming ->
        SecondaryCollections(tasks, memories, packs, artifacts, streaming)
    }

    private val toolFlow = combine(
        _activeTool,
        _toolOutput,
        _toolComparisons,
        _isRunningTool,
        _isGenerating
    ) { tool, output, comparisons, running, generating ->
        ToolCollections(tool, output, comparisons, running, generating)
    }

    private val noticesFlow = combine(_statusNotice, _errorMessage) { notice, error ->
        notice to error
    }

    private val liveEnginesSet: Set<AiEngine> =
        AiEngine.entries.filter { repository.isEngineLive(it) }.toSet()

    val uiState: StateFlow<JarvisUiState> = combine(
        primaryFlow,
        secondaryFlow,
        toolFlow,
        noticesFlow
    ) { primary, secondary, tools, (notice, error) ->
        JarvisUiState(
            isLiveAiConnected = repository.isLiveAiConnected,
            liveEngines = liveEnginesSet,
            preferences = primary.preferences,
            conversations = primary.conversations,
            activeConversationId = primary.activeId,
            messages = primary.messages,
            streamingReply = secondary.streamingReply,
            isGenerating = tools.isGenerating,
            missions = primary.missions,
            plannerTasks = secondary.tasks,
            memories = secondary.memories,
            creatorPacks = secondary.creatorPacks,
            studioArtifacts = secondary.artifacts,
            activeTool = tools.activeTool,
            toolOutput = tools.toolOutput,
            toolComparisons = tools.toolComparisons,
            isRunningTool = tools.isRunningTool,
            statusNotice = notice,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = JarvisUiState(
            isLiveAiConnected = repository.isLiveAiConnected,
            liveEngines = liveEnginesSet
        )
    )

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearStatusNotice() {
        _statusNotice.value = null
    }

    fun selectEngine(engine: AiEngine) {
        viewModelScope.launch { repository.setSelectedEngine(engine) }
    }

    fun selectTool(tool: AiToolKind) {
        _activeTool.value = tool
        _toolOutput.value = null
        _toolComparisons.value = emptyMap()
    }

    fun runAiTool(input: String, option: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank() || _isRunningTool.value) return
        val tool = _activeTool.value
        val state = uiState.value

        viewModelScope.launch {
            _isRunningTool.value = true
            _errorMessage.value = null
            try {
                if (tool == AiToolKind.COMPARE) {
                    _toolOutput.value = null
                    _toolComparisons.value = repository.compareAllEngines(
                        prompt = trimmed,
                        language = state.preferences.preferredLanguage
                    )
                } else {
                    _toolComparisons.value = emptyMap()
                    _toolOutput.value = repository.runAiTool(
                        tool = tool,
                        engine = state.preferences.selectedEngine,
                        input = trimmed,
                        option = option,
                        language = state.preferences.preferredLanguage
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "AI Tool execution failed."
            } finally {
                _isRunningTool.value = false
            }
        }
    }

    fun saveCreatorPackFromDraft(draft: CreatorDraftPayload) {
        viewModelScope.launch {
            repository.saveCreatorPackFromDraft(draft)
            _statusNotice.value = "Saved ${draft.platform.label} pack to Studio → Creator"
        }
    }

    fun createCreatorPack(platform: CreatorPlatform, topic: String) {
        val trimmed = topic.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val state = uiState.value
                repository.createCreatorPack(
                    platform = platform,
                    topic = trimmed,
                    engine = state.preferences.selectedEngine,
                    language = state.preferences.preferredLanguage
                )
                _statusNotice.value = "Created ${platform.label} production pack"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Could not create creator project."
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun toggleCreatorStep(pack: CreatorPackEntity, stepIndex: Int) {
        val list = parseMilestones(pack.stepsEncoded).toMutableList()
        if (stepIndex !in list.indices) return
        list[stepIndex] = list[stepIndex].copy(isDone = !list[stepIndex].isDone)
        val allDone = list.isNotEmpty() && list.all { it.isDone }
        val newStatus = if (allDone) "READY_TO_PUBLISH" else "DRAFT"
        viewModelScope.launch {
            repository.updateCreatorPack(
                pack.copy(
                    stepsEncoded = encodeMilestones(list),
                    status = newStatus
                )
            )
        }
    }

    fun updateCreatorPackStatus(pack: CreatorPackEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateCreatorPack(pack.copy(status = newStatus))
        }
    }

    fun deleteCreatorPack(id: Long) {
        viewModelScope.launch { repository.deleteCreatorPack(id) }
    }

    fun selectConversation(conversationId: Long) {
        _selectedConversationId.value = conversationId
    }

    fun startNewConversation() {
        viewModelScope.launch {
            val id = repository.createConversation(
                title = "New AIRA Session",
                languageHint = uiState.value.preferences.preferredLanguage.name
            )
            _selectedConversationId.value = id
        }
    }

    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
            if (_selectedConversationId.value == conversationId) {
                _selectedConversationId.value = null
            }
        }
    }

    fun sendMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            _streamingReply.value = ""
            _errorMessage.value = null
            try {
                val state = uiState.value
                val existingId = state.activeConversationId
                val conversationId = existingId ?: repository.createConversation(
                    title = text.take(40),
                    languageHint = state.preferences.preferredLanguage.name
                )
                if (existingId == null) {
                    _selectedConversationId.value = conversationId
                }

                val currentConv = state.conversations.firstOrNull { it.id == conversationId }
                val shouldRename = currentConv == null || currentConv.title == "New AIRA Session" || currentConv.title == "New JARVIS Session"
                repository.appendUserMessage(conversationId, text, updateTitleIfDefault = shouldRename)

                val activeMemories = if (state.preferences.memoryEnabled) {
                    state.memories.filter { it.isEnabled }
                } else {
                    emptyList()
                }

                repository.streamCompanionReply(
                    prompt = text,
                    history = state.messages,
                    activeMemories = activeMemories,
                    language = state.preferences.preferredLanguage,
                    tone = state.preferences.personalityTone,
                    engine = state.preferences.selectedEngine
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Partial -> _streamingReply.value = event.accumulatedText
                        is AiStreamEvent.Completed -> {
                            _streamingReply.value = null
                            repository.saveAiraReply(conversationId, event.response)
                            event.response.creatorDraft?.let { draft ->
                                repository.saveCreatorPackFromDraft(draft)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _streamingReply.value = null
                _errorMessage.value = e.localizedMessage ?: "Failed to generate AIRA response."
            } finally {
                _isGenerating.value = false
                _streamingReply.value = null
            }
        }
    }

    fun confirmDraft(messageId: Long, editedBody: String) {
        viewModelScope.launch {
            repository.updateDraft(messageId, editedBody.trim(), "CONFIRMED_SHARED")
        }
    }

    fun discardDraft(messageId: Long, currentBody: String) {
        viewModelScope.launch {
            repository.updateDraft(messageId, currentBody, "DISCARDED")
        }
    }

    fun importSuggestedTasks(messageId: Long, encodedTasks: String) {
        val parsed = parseSuggestedTasks(encodedTasks)
        if (parsed.isEmpty()) return
        viewModelScope.launch {
            repository.importSuggestedTasks(messageId, parsed)
            _statusNotice.value = "Added ${parsed.size} tasks to Studio Planner"
        }
    }

    fun launchMission(title: String, goal: String, selectedAgents: List<SpecialistAgent>) {
        val trimmedGoal = goal.trim()
        if (trimmedGoal.isBlank()) return
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val state = uiState.value
                val activeMemories = if (state.preferences.memoryEnabled) {
                    state.memories.filter { it.isEnabled }
                } else {
                    emptyList()
                }
                repository.createMission(
                    title = title.trim(),
                    goal = trimmedGoal,
                    agents = selectedAgents,
                    activeMemories = activeMemories,
                    language = state.preferences.preferredLanguage,
                    engine = state.preferences.selectedEngine
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Could not launch mission."
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun toggleMissionMilestone(mission: AgentMissionEntity, milestoneIndex: Int) {
        val list = parseMilestones(mission.milestonesEncoded).toMutableList()
        if (milestoneIndex !in list.indices) return
        list[milestoneIndex] = list[milestoneIndex].copy(isDone = !list[milestoneIndex].isDone)
        val allDone = list.isNotEmpty() && list.all { it.isDone }
        val newStatus = if (allDone) "COMPLETED" else "IN_PROGRESS"
        viewModelScope.launch {
            repository.updateMission(
                mission.copy(
                    milestonesEncoded = encodeMilestones(list),
                    status = newStatus
                )
            )
        }
    }

    fun updateMissionStatus(mission: AgentMissionEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateMission(mission.copy(status = newStatus))
        }
    }

    fun deleteMission(missionId: Long) {
        viewModelScope.launch {
            repository.deleteMission(missionId)
        }
    }

    fun addPlannerTask(title: String, timeSlot: String, category: String, priority: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addPlannerTask(title.trim(), timeSlot.trim(), category.trim(), priority)
        }
    }

    fun togglePlannerTask(task: PlannerTaskEntity) {
        viewModelScope.launch {
            repository.togglePlannerTask(task.id, !task.isCompleted)
        }
    }

    fun deletePlannerTask(taskId: Long) {
        viewModelScope.launch {
            repository.deletePlannerTask(taskId)
        }
    }

    fun addMemory(category: String, key: String, value: String) {
        if (key.isBlank() || value.isBlank()) return
        viewModelScope.launch {
            repository.addMemory(category.trim(), key.trim(), value.trim())
        }
    }

    fun toggleMemoryEntry(entry: MemoryEntryEntity) {
        viewModelScope.launch {
            repository.toggleMemoryEntry(entry)
        }
    }

    fun deleteMemoryEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteMemoryEntry(id)
        }
    }

    fun setMemoryEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setMemoryEnabled(enabled) }
    }

    fun setProactiveAssistanceEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setProactiveAssistanceEnabled(enabled) }
    }

    fun setVoiceAutoSpeak(enabled: Boolean) {
        viewModelScope.launch { repository.setVoiceAutoSpeak(enabled) }
    }

    fun setPreferredLanguage(language: JarvisLanguage) {
        viewModelScope.launch { repository.setPreferredLanguage(language) }
    }

    fun setPersonalityTone(tone: JarvisTone) {
        viewModelScope.launch { repository.setPersonalityTone(tone) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    companion object {
        fun parseMilestones(encoded: String): List<MilestoneItem> {
            if (encoded.isBlank()) return emptyList()
            return encoded.split(";;").mapNotNull { chunk ->
                val parts = chunk.split("|", limit = 2)
                if (parts.size == 2) {
                    MilestoneItem(isDone = parts[0] == "1", title = parts[1])
                } else {
                    null
                }
            }
        }

        fun encodeMilestones(items: List<MilestoneItem>): String {
            return items.joinToString(";;") { item ->
                "${if (item.isDone) "1" else "0"}|${item.title.replace("|", " ")}"
            }
        }

        fun parseSuggestedTasks(encoded: String): List<Pair<String, String>> {
            if (encoded.isBlank()) return emptyList()
            return encoded.split(";;").mapNotNull { chunk ->
                val parts = chunk.split("|", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
        }

        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            val repository = JarvisWorkspaceRepository(
                db = JarvisDatabase.getInstance(appContext),
                preferencesRepository = PreferencesRepository(appContext),
                aiRepository = MultiEngineAiRepository()
            )
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return JarvisViewModel(repository) as T
                }
            }
        }
    }
}
