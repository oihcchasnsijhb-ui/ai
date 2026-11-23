package com.example.data.ai

import com.example.data.local.ChatMessageEntity
import com.example.data.local.MemoryEntryEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Placeholder implementation used when live provider credentials or Firebase AI Logic
 * are not yet enabled. Always labels outputs clearly as local sample previews.
 */
class PlaceholderAiRepository : AiRepository {
    override val isLiveAiConnected: Boolean = false

    override fun isEngineLive(engine: AiEngine): Boolean = false

    override fun streamCompanionReply(
        userPrompt: String,
        history: List<ChatMessageEntity>,
        activeMemories: List<MemoryEntryEntity>,
        language: JarvisLanguage,
        tone: JarvisTone,
        engine: AiEngine
    ): Flow<AiStreamEvent> = flow {
        val response = buildSampleResponse(
            userPrompt = userPrompt,
            activeMemories = activeMemories,
            language = language,
            tone = tone,
            engine = engine
        )

        val words = response.replyText.split(" ")
        val buffer = StringBuilder()
        for (index in words.indices) {
            if (index > 0) buffer.append(' ')
            buffer.append(words[index])
            if (index % 4 == 0 || index == words.lastIndex) {
                emit(AiStreamEvent.Partial(buffer.toString()))
                delay(24L)
            }
        }
        emit(AiStreamEvent.Completed(response))
    }

    override suspend fun orchestrateMissionBrief(
        goal: String,
        agents: List<SpecialistAgent>,
        activeMemories: List<MemoryEntryEntity>,
        language: JarvisLanguage,
        engine: AiEngine
    ): AiResponse {
        delay(200L)
        val useHindi = detectHindiOrHinglish(goal, language)
        val selectedAgents = if (agents.isEmpty()) {
            listOf(
                SpecialistAgent.MANAGER,
                SpecialistAgent.PLANNER,
                SpecialistAgent.CODING,
                SpecialistAgent.SECURITY
            )
        } else {
            agents
        }

        val agentLines = selectedAgents.joinToString("\n") { agent ->
            "• ${agent.displayName} [${agent.badgeCode}]: ${agent.roleSummary}"
        }

        val reply = if (useHindi == DetectedLang.HINDI) {
            """[Sample Preview • ${engine.displayName}]
Manager AI ने "$goal" के लिए ${selectedAgents.size} एजेंट्स को असाइन किया है:

$agentLines

यह एक स्थानीय सैंपल प्लेसहोल्डर आउटपुट है। लाइव ${engine.displayName} निष्पादन के लिए संबंधित AI सेवा या Secret सक्षम करें।"""
        } else {
            """[Sample Preview • ${engine.displayName}]
Manager AI routed "$goal" across ${selectedAgents.size} specialist agents:

$agentLines

Note: This is a local placeholder brief. Enable Firebase AI Logic (for Gemini) or configure API Secrets in AI Studio (for ChatGPT/Claude) for live runs."""
        }

        val milestones = listOf(
            "Define scope & acceptance criteria (${SpecialistAgent.PLANNER.badgeCode})",
            "Research architecture & edge cases (${SpecialistAgent.RESEARCH.badgeCode})",
            "Build core deliverable (${SpecialistAgent.CODING.badgeCode})",
            "Security & QA review (${SpecialistAgent.SECURITY.badgeCode} / ${SpecialistAgent.TESTING.badgeCode})"
        )

        return AiResponse(
            replyText = reply,
            agentsUsed = selectedAgents,
            engineUsed = engine,
            suggestedMilestones = milestones,
            isPlaceholder = true
        )
    }

    override suspend fun runAiTool(
        tool: AiToolKind,
        engine: AiEngine,
        input: String,
        option: String,
        language: JarvisLanguage
    ): AiResponse {
        delay(180L)
        val trimmed = input.trim()
        val lang = detectHindiOrHinglish(trimmed, language)
        val engineHeader = "[Sample Preview • ${engine.displayName} (${engine.modelLabel})]"

        return when (tool) {
            AiToolKind.SUMMARIZER -> {
                val reply = if (lang == DetectedLang.HINDI) {
                    """$engineHeader
मोड: $option

• मुख्य सार: "${trimmed.take(70)}..." से 3 प्रमुख बिंदु निकाले गए।
• फोकस: स्पष्ट प्राथमिकताएँ, समय की बचत और अगले कदम।
• सुझाव: इसे Studio Planner या Notes में सेव करें।"""
                } else {
                    """$engineHeader
Mode: $option

• Core Takeaway: Condensed "${trimmed.take(72)}..." into key points.
• Signal vs Noise: Highlights actionable facts and next decisions.
• Next Step: Review or copy into your Studio workspace."""
                }
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(SpecialistAgent.ANALYST, SpecialistAgent.RESEARCH),
                    engineUsed = engine,
                    isPlaceholder = true
                )
            }

            AiToolKind.TRANSLATOR -> {
                val reply = when {
                    option.contains("Hindi", ignoreCase = true) ->
                        "$engineHeader\nअनुवाद ($option):\n\"$trimmed\" -> यह आपका सैंपल हिंदी अनुवाद पूर्वावलोकन है। लाइव अनुवाद के लिए सक्रिय AI इंजन जोड़ें।"
                    option.contains("Hinglish", ignoreCase = true) ->
                        "$engineHeader\nHinglish Translation:\n\"$trimmed\" -> Yeh aapka natural Hinglish sample translation hai. Live translation ke liye engine connect karein."
                    option.contains("Spanish", ignoreCase = true) ->
                        "$engineHeader\nTraducción ($option):\n\"$trimmed\" -> Vista previa de traducción natural generada localmente."
                    else ->
                        "$engineHeader\nEnglish Translation:\n\"$trimmed\" -> Sample localized English translation preview preserving intent and tone."
                }
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(SpecialistAgent.COMMUNICATION),
                    engineUsed = engine,
                    isPlaceholder = true
                )
            }

            AiToolKind.CODE_STUDIO -> {
                val snippet = """// Generated in ${engine.displayName} Sample Mode ($option)
suspend fun runTask(input: String): Result<String> = runCatching {
    require(input.isNotBlank()) { "Input cannot be empty" }
    "Processed by ${engine.displayName}: ${'$'}input"
}"""
                AiResponse(
                    replyText = "$engineHeader\n$option complete for: \"${trimmed.take(60)}\". Check the modular Kotlin snippet below.",
                    agentsUsed = listOf(SpecialistAgent.CODING, SpecialistAgent.TESTING),
                    engineUsed = engine,
                    codeSnippet = snippet,
                    codeLanguage = "kotlin",
                    isPlaceholder = true
                )
            }

            AiToolKind.WRITER -> {
                val body = if (lang == DetectedLang.HINDI) {
                    "नमस्ते! \"$trimmed\" के संबंध में अपडेट साझा कर रहा हूँ — सभी प्रमुख बिंदुओं पर काम प्रगति पर है। कृपया अपने सुझाव बताएं।"
                } else {
                    "Hi there! Quick $option note regarding \"$trimmed\" — everything is moving ahead cleanly. Let me know your thoughts on the next step."
                }
                AiResponse(
                    replyText = "$engineHeader\nCommunication AI drafted a ready-to-review $option message below.",
                    agentsUsed = listOf(SpecialistAgent.COMMUNICATION),
                    engineUsed = engine,
                    draftPayload = DraftPayload(channel = option, recipient = "Contact / Team", body = body),
                    isPlaceholder = true
                )
            }

            AiToolKind.PROMPT_LAB -> {
                val reply = """$engineHeader
Creative Brief ($option):
• Concept: $trimmed
• Visual Direction: High-contrast obsidian backdrop, luminous cyan & warm amber rim lighting, rule-of-thirds composition.
• Hook / Prompt: "Ultra-clean editorial $option centered around $trimmed, crisp depth of field, atmospheric studio lighting." """
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(SpecialistAgent.CREATIVE),
                    engineUsed = engine,
                    isPlaceholder = true
                )
            }

            AiToolKind.COMPARE -> {
                AiResponse(
                    replyText = "$engineHeader\nComparison preview ready for \"$trimmed\".",
                    agentsUsed = listOf(SpecialistAgent.ANALYST),
                    engineUsed = engine,
                    isPlaceholder = true
                )
            }
        }
    }

    override suspend fun compareAllEngines(
        prompt: String,
        language: JarvisLanguage
    ): Map<AiEngine, AiResponse> {
        delay(220L)
        return AiEngine.entries.associateWith { engine ->
            buildSampleResponse(
                userPrompt = prompt,
                activeMemories = emptyList(),
                language = language,
                tone = JarvisTone.STRATEGIST,
                engine = engine
            )
        }
    }

    private fun buildSampleResponse(
        userPrompt: String,
        activeMemories: List<MemoryEntryEntity>,
        language: JarvisLanguage,
        tone: JarvisTone,
        engine: AiEngine
    ): AiResponse {
        val lower = userPrompt.lowercase()
        val lang = detectHindiOrHinglish(userPrompt, language)
        val engineTag = "${engine.displayName} (${engine.modelLabel})"
        val memoryContext = if (activeMemories.isNotEmpty()) {
            val summary = activeMemories.take(2).joinToString(", ") { "${it.memoryKey}: ${it.memoryValue}" }
            "\n\nMemory active: $summary"
        } else {
            ""
        }

        val engineNote = when (engine) {
            AiEngine.GEMINI -> "[Sample Preview • $engineTag — Firebase AI Logic required for live Gemini]"
            AiEngine.CLAUDE -> "[Sample Preview • $engineTag — Configure ANTHROPIC_API_KEY in AI Studio Secrets for live Claude]"
            AiEngine.CHATGPT -> "[Sample Preview • $engineTag — Configure OPENAI_API_KEY in AI Studio Secrets for live ChatGPT]"
        }

        val isPlanRequest = lower.contains("plan") || lower.contains("schedule") ||
            lower.contains("दिन") || lower.contains("प्लान") || lower.contains("आज")
        val isMessageRequest = lower.contains("whatsapp") || lower.contains("email") ||
            lower.contains("message") || lower.contains("draft") || lower.contains("मैसेज")
        val isCodeRequest = lower.contains("code") || lower.contains("kotlin") ||
            lower.contains("compose") || lower.contains("android") || lower.contains("api") ||
            lower.contains("build") || lower.contains("website") || lower.contains("app")
        val isStoreOrTeamRequest = lower.contains("store") || lower.contains("स्टोर") ||
            lower.contains("team") || lower.contains("project") || lower.contains("प्रोजेक्ट")

        return when {
            isMessageRequest -> {
                val channel = when {
                    lower.contains("email") -> "Email"
                    lower.contains("social") || lower.contains("post") -> "Social"
                    else -> "WhatsApp"
                }
                val draftBody = when (lang) {
                    DetectedLang.HINDI -> "नमस्ते टीम! आज के माइलस्टोन पर अपडेट: मुख्य फीचर्स तैयार हो रहे हैं, कृपया शाम 5 बजे तक अपना रिव्यू शेयर करें।"
                    DetectedLang.HINGLISH -> "Hey team! Quick update on today's milestone — core work is moving smoothly. Let's sync briefly at 5 PM to finalize next steps."
                    DetectedLang.ENGLISH -> "Hi team — quick update on today's sprint: core tasks are on track. Let's review final details together at 5:00 PM."
                }
                AiResponse(
                    replyText = "$engineNote\n\nCommunication AI prepared your $channel draft below. Confirm before sharing.$memoryContext",
                    agentsUsed = listOf(SpecialistAgent.COMMUNICATION, SpecialistAgent.SECURITY),
                    engineUsed = engine,
                    draftPayload = DraftPayload(channel = channel, recipient = "Team / Contact", body = draftBody),
                    isPlaceholder = true
                )
            }

            isPlanRequest -> {
                val tasks = if (lang == DetectedLang.HINDI) {
                    listOf(
                        "09:00 AM" to "मुख्य प्रोजेक्ट पर 90 मिनट डीप वर्क",
                        "11:30 AM" to "ईमेल, मैसेज और टीम फॉलो-अप",
                        "03:00 PM" to "रिसर्च और स्किल अपग्रेड सेशन",
                        "06:30 PM" to "दिन का रिव्यू और कल की प्राथमिकताएँ"
                    )
                } else {
                    listOf(
                        "09:00 AM" to "90-min Deep Work on Top Priority",
                        "11:30 AM" to "Communications & Follow-up Block",
                        "03:00 PM" to "Research, Review & Problem Solving",
                        "06:30 PM" to "Evening Wrap-up & Tomorrow's Plan"
                    )
                }
                AiResponse(
                    replyText = "$engineNote\n\nPlanner AI mapped out a focused day for you. Tap below to add these blocks directly into your Studio Planner.$memoryContext",
                    agentsUsed = listOf(SpecialistAgent.PLANNER, SpecialistAgent.ANALYST),
                    engineUsed = engine,
                    suggestedPlannerTasks = tasks,
                    isPlaceholder = true
                )
            }

            isCodeRequest -> {
                val sampleCode = """interface AiGateway {
    suspend fun execute(prompt: String): Result<String>
}

class ${engine.displayName}Orchestrator(
    private val gateway: AiGateway
) {
    suspend fun runGoal(goal: String): Result<String> =
        gateway.execute("Decompose and execute: ${'$'}goal")
}"""
                AiResponse(
                    replyText = "$engineNote\n\nCoding AI & Security AI drafted an interface-driven Kotlin pattern tailored for ${engine.displayName}.$memoryContext",
                    agentsUsed = listOf(SpecialistAgent.CODING, SpecialistAgent.SECURITY, SpecialistAgent.TESTING),
                    engineUsed = engine,
                    codeSnippet = sampleCode,
                    codeLanguage = "kotlin",
                    isPlaceholder = true
                )
            }

            isStoreOrTeamRequest -> {
                AiResponse(
                    replyText = "$engineNote\n\nManager AI coordinated a parallel squad brief:\n• Research AI: Market & reference synthesis\n• Creative AI: Visual & UX concepting\n• Coding AI: Core architecture\n• Security AI: Permission & data safety audit\n\nHead to the Team tab to track this mission.$memoryContext",
                    agentsUsed = listOf(
                        SpecialistAgent.MANAGER,
                        SpecialistAgent.RESEARCH,
                        SpecialistAgent.CREATIVE,
                        SpecialistAgent.CODING,
                        SpecialistAgent.SECURITY
                    ),
                    engineUsed = engine,
                    isPlaceholder = true
                )
            }

            else -> {
                val styleLine = when (engine) {
                    AiEngine.GEMINI -> "Structured multimodal reasoning perspective (${tone.label})."
                    AiEngine.CLAUDE -> "Nuanced, clear editorial & architectural perspective (${tone.label})."
                    AiEngine.CHATGPT -> "Fast, direct conversational & actionable perspective (${tone.label})."
                }
                val reply = when (lang) {
                    DetectedLang.HINDI -> "$engineNote\n\nनमस्ते! आपने पूछा: \"$userPrompt\"\n$styleLine$memoryContext"
                    DetectedLang.HINGLISH -> "$engineNote\n\nHey! Aapka prompt mila: \"$userPrompt\".\n$styleLine$memoryContext"
                    DetectedLang.ENGLISH -> "$engineNote\n\nGot it: \"$userPrompt\"\n$styleLine$memoryContext"
                }
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(SpecialistAgent.MANAGER, SpecialistAgent.ANALYST),
                    engineUsed = engine,
                    isPlaceholder = true
                )
            }
        }
    }

    private enum class DetectedLang { ENGLISH, HINGLISH, HINDI }

    private fun detectHindiOrHinglish(text: String, preference: JarvisLanguage): DetectedLang {
        return when (preference) {
            JarvisLanguage.HINDI -> DetectedLang.HINDI
            JarvisLanguage.HINGLISH -> DetectedLang.HINGLISH
            JarvisLanguage.ENGLISH -> DetectedLang.ENGLISH
            JarvisLanguage.AUTO -> {
                val hasDevanagari = text.any { it in '\u0900'..'\u097F' }
                if (hasDevanagari) {
                    DetectedLang.HINDI
                } else {
                    val lower = text.lowercase()
                    val hinglishMarkers = listOf("aaj", "mera", "kar do", "kaise", "hai", "bhai", "kya", "banao", "samjhao", "mujhe")
                    if (hinglishMarkers.any { lower.contains(it) }) DetectedLang.HINGLISH else DetectedLang.ENGLISH
                }
            }
        }
    }
}
