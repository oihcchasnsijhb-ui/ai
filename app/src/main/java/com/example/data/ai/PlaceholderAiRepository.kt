package com.example.data.ai

import com.example.data.local.ChatMessageEntity
import com.example.data.local.MemoryEntryEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Placeholder implementation for AIRA used when live provider credentials or
 * Firebase AI Logic are not yet enabled. Always labels outputs clearly as local sample previews.
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
                delay(22L)
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
        delay(180L)
        val lang = detectHindiOrHinglish(goal, language)
        val selectedAgents = if (agents.isEmpty()) {
            listOf(
                SpecialistAgent.AIRA_CORE,
                SpecialistAgent.RESEARCH,
                SpecialistAgent.CODING,
                SpecialistAgent.DESIGN,
                SpecialistAgent.SECURITY,
                SpecialistAgent.TESTING
            )
        } else {
            agents
        }

        val agentLines = selectedAgents.joinToString("\n") { agent ->
            "• ${agent.displayName} [${agent.badgeCode}]: ${agent.roleSummary}"
        }

        val reply = if (lang == DetectedLang.HINDI) {
            """[Sample Preview • AIRA on ${engine.displayName}]
प्रोटोकॉल: UNDERSTAND → PLAN → DELEGATE → EXECUTE → VERIFY → DELIVER
AIRA Core ने "$goal" के लिए ${selectedAgents.size} एजेंट्स को नियुक्त किया है:

$agentLines

यह स्थानीय सैंपल प्रीव्यू है। लाइव AI निष्पादन के लिए Firebase AI Logic या API Secrets जोड़ें।"""
        } else {
            """[Sample Preview • AIRA on ${engine.displayName}]
Protocol: UNDERSTAND → PLAN → DELEGATE → EXECUTE → VERIFY → DELIVER
AIRA Core routed "$goal" across ${selectedAgents.size} specialist agents:

$agentLines

Note: This is a local placeholder brief. Enable Firebase AI Logic (for Gemini) or configure API Secrets in AI Studio (for ChatGPT/Claude) for live runs."""
        }

        val milestones = listOf(
            "UNDERSTAND & PLAN: Scope & acceptance criteria (${SpecialistAgent.AIRA_CORE.badgeCode})",
            "RESEARCH & DESIGN: References & architecture (${SpecialistAgent.RESEARCH.badgeCode} / ${SpecialistAgent.DESIGN.badgeCode})",
            "EXECUTE: Build core deliverable (${SpecialistAgent.CODING.badgeCode})",
            "VERIFY & DELIVER: Security & QA audit (${SpecialistAgent.SECURITY.badgeCode} / ${SpecialistAgent.TESTING.badgeCode})"
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
        val engineHeader = "[Sample Preview • AIRA via ${engine.displayName} (${engine.modelLabel})]"

        return when (tool) {
            AiToolKind.VIDEO_STUDIO -> {
                val platform = when {
                    option.contains("Shorts", ignoreCase = true) -> CreatorPlatform.YOUTUBE_SHORTS
                    option.contains("Reel", ignoreCase = true) && !option.contains("YouTube", ignoreCase = true) ->
                        CreatorPlatform.INSTAGRAM_REEL
                    else -> CreatorPlatform.YOUTUBE_VIDEO
                }
                val creatorDraft = buildCreatorDraft(platform, trimmed, lang)
                AiResponse(
                    replyText = """$engineHeader
Video Agent & Social Media Agent prepared your production pack for "$trimmed" ($option).

• Hook: ${creatorDraft.hook}
• Title: ${creatorDraft.title}
• Tags: ${creatorDraft.tagsOrHashtags}

Note: Direct video rendering & auto-upload require connected rendering/YouTube/Instagram APIs — you can track every production step and share metadata from Studio -> Creator!""",
                    agentsUsed = listOf(
                        SpecialistAgent.VIDEO,
                        SpecialistAgent.SOCIAL_MEDIA,
                        SpecialistAgent.DESIGN
                    ),
                    engineUsed = engine,
                    creatorDraft = creatorDraft,
                    isPlaceholder = true
                )
            }

            AiToolKind.SUMMARIZER -> {
                val reply = if (lang == DetectedLang.HINDI) {
                    """$engineHeader
मोड: $option

• मुख्य सार: "${trimmed.take(70)}..." से 3 प्रमुख बिंदु निकाले गए।
• फोकस: स्पष्ट प्राथमिकताएँ, समय की बचत और अगले कदम।
• सुझाव: इसे AIRA Memory (Knowledge) या Studio Planner में सेव करें।"""
                } else {
                    """$engineHeader
Mode: $option

• Core Takeaway: Condensed "${trimmed.take(72)}..." into crisp key points.
• Signal vs Noise: Highlights actionable decisions & facts.
• Next Step: Save key facts to Knowledge Memory or Studio Planner."""
                }
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(SpecialistAgent.ANALYSIS, SpecialistAgent.RESEARCH),
                    engineUsed = engine,
                    isPlaceholder = true
                )
            }

            AiToolKind.TRANSLATOR -> {
                val reply = when {
                    option.contains("Hindi", ignoreCase = true) ->
                        "$engineHeader\nअनुवाद ($option):\n\"$trimmed\" -> यह आपका प्राकृतिक हिंदी सैंपल अनुवाद पूर्वावलोकन है।"
                    option.contains("Hinglish", ignoreCase = true) ->
                        "$engineHeader\nHinglish Translation:\n\"$trimmed\" -> Yeh aapka friendly Hinglish sample translation hai."
                    option.contains("Spanish", ignoreCase = true) ->
                        "$engineHeader\nTraducción ($option):\n\"$trimmed\" -> Vista previa de traducción natural generada localmente."
                    else ->
                        "$engineHeader\nEnglish Translation:\n\"$trimmed\" -> Sample localized English translation preview preserving warmth and tone."
                }
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(SpecialistAgent.AIRA_CORE),
                    engineUsed = engine,
                    isPlaceholder = true
                )
            }

            AiToolKind.CODE_STUDIO -> {
                val snippet = """// Generated by AIRA Coding Agent via ${engine.displayName} ($option)
suspend fun executeWorkflow(goal: String): Result<String> = runCatching {
    require(goal.isNotBlank()) { "Goal cannot be empty" }
    "AIRA [${engine.shortBadge}] completed: ${'$'}goal"
}"""
                AiResponse(
                    replyText = "$engineHeader\n$option complete for: \"${trimmed.take(60)}\". Modular Kotlin snippet ready below.",
                    agentsUsed = listOf(SpecialistAgent.CODING, SpecialistAgent.TESTING),
                    engineUsed = engine,
                    codeSnippet = snippet,
                    codeLanguage = "kotlin",
                    isPlaceholder = true
                )
            }

            AiToolKind.WRITER -> {
                val body = if (lang == DetectedLang.HINDI) {
                    "नमस्ते! \"$trimmed\" के बारे में छोटा सा अपडेट — सब कुछ सही दिशा में आगे बढ़ रहा है। अपनी राय ज़रूर साझा करें!"
                } else {
                    "Hey! Quick $option note about \"$trimmed\" — everything is moving forward smoothly. Let me know what you think!"
                }
                AiResponse(
                    replyText = "$engineHeader\nSocial Media & AIRA Core drafted your $option message below for review.",
                    agentsUsed = listOf(SpecialistAgent.SOCIAL_MEDIA, SpecialistAgent.AIRA_CORE),
                    engineUsed = engine,
                    draftPayload = DraftPayload(channel = option, recipient = "Contact / Team", body = body),
                    isPlaceholder = true
                )
            }

            AiToolKind.PROMPT_LAB -> {
                val reply = """$engineHeader
Design Agent Brief ($option):
• Concept: $trimmed
• Visual Direction: High-contrast obsidian canvas, luminous cyan & warm amber rim lighting, crisp focal subject.
• Prompt: "Ultra-clean editorial $option featuring $trimmed, bold typography-safe negative space, studio lighting." """
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(SpecialistAgent.DESIGN),
                    engineUsed = engine,
                    isPlaceholder = true
                )
            }

            AiToolKind.COMPARE -> {
                AiResponse(
                    replyText = "$engineHeader\nComparison preview ready for \"$trimmed\".",
                    agentsUsed = listOf(SpecialistAgent.ANALYSIS),
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
        delay(200L)
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

    private fun buildCreatorDraft(
        platform: CreatorPlatform,
        topic: String,
        lang: DetectedLang
    ): CreatorDraftPayload {
        val cleanTopic = topic.take(64).ifBlank { "AI Productivity" }
        return if (lang == DetectedLang.HINDI) {
            CreatorDraftPayload(
                platform = platform,
                topic = cleanTopic,
                title = "$cleanTopic — पूरी गाइड और आसान टिप्स (2026)",
                hook = "सिर्फ 30 सेकंड में जानिए $cleanTopic आपका समय कैसे आधा कर सकता है!",
                script = "[0:00-0:05 HOOK] क्या आप अभी भी $cleanTopic को पुराने तरीके से कर रहे हैं?\n[0:05-0:25 VALUE] आज के 3 सबसे स्मार्ट स्टेप्स...\n[0:25-0:45 DEMO] लाइव उदाहरण और वर्कफ़्लो टिप्स।\n[0:45-0:60 CTA] सेव करें और अगले वीडियो के लिए फॉलो करें!",
                captionOrDescription = "आज हम समझेंगे $cleanTopic के सबसे असरदार तरीके। वीडियो पसंद आए तो लाइक और शेयर जरूर करें!",
                tagsOrHashtags = "#AIRA #AI #TechHindi #Productivity #Shorts #Reels"
            )
        } else {
            CreatorDraftPayload(
                platform = platform,
                topic = cleanTopic,
                title = "$cleanTopic Explained: 3 Smart Workflows That Save Hours",
                hook = "Stop doing $cleanTopic the slow way — here's the 60-second upgrade!",
                script = "[0:00-0:05 HOOK] Most people miss this simple shift in $cleanTopic.\n[0:05-0:30 CORE] Step 1: Plan fast. Step 2: Automate repetition. Step 3: Verify quality.\n[0:30-0:50 EXAMPLE] Quick walkthrough you can apply today.\n[0:50-0:60 CTA] Save this for later & drop your topic below!",
                captionOrDescription = "A concise breakdown of $cleanTopic with practical takeaways you can use right away.",
                tagsOrHashtags = "#AIRA #AIWorkflow #CreatorTools #YouTube #InstagramReels"
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
            val summary = activeMemories.take(2).joinToString(", ") { "[${it.category}] ${it.memoryKey}: ${it.memoryValue}" }
            "\n\nMemory Agent context: $summary"
        } else {
            ""
        }

        val engineNote = when (engine) {
            AiEngine.GEMINI -> "[Sample Preview • AIRA on $engineTag — Firebase AI Logic required for live Gemini]"
            AiEngine.CLAUDE -> "[Sample Preview • AIRA on $engineTag — Configure ANTHROPIC_API_KEY in AI Studio Secrets for live Claude]"
            AiEngine.CHATGPT -> "[Sample Preview • AIRA on $engineTag — Configure OPENAI_API_KEY in AI Studio Secrets for live ChatGPT]"
        }

        val isVideoOrSocialRequest = lower.contains("youtube") || lower.contains("reel") ||
            lower.contains("instagram") || lower.contains("video") || lower.contains("shorts") ||
            lower.contains("वीडियो") || lower.contains("रील")
        val isPlanRequest = lower.contains("plan") || lower.contains("schedule") ||
            lower.contains("routine") || lower.contains("दिन") || lower.contains("प्लान") || lower.contains("आज")
        val isMessageRequest = lower.contains("whatsapp") || lower.contains("email") ||
            lower.contains("message") || lower.contains("draft") || lower.contains("मैसेज")
        val isCodeRequest = lower.contains("code") || lower.contains("kotlin") ||
            lower.contains("compose") || lower.contains("android") || lower.contains("api") ||
            lower.contains("build") || lower.contains("website") || lower.contains("app")
        val isSadOrStress = lower.contains("sad") || lower.contains("tired") ||
            lower.contains("stressed") || lower.contains("frustrated") || lower.contains("उदास")

        return when {
            isVideoOrSocialRequest -> {
                val platform = when {
                    lower.contains("instagram") || lower.contains("reel") -> CreatorPlatform.INSTAGRAM_REEL
                    lower.contains("short") -> CreatorPlatform.YOUTUBE_SHORTS
                    else -> CreatorPlatform.YOUTUBE_VIDEO
                }
                val creatorDraft = buildCreatorDraft(platform, userPrompt, lang)
                val reply = when (lang) {
                    DetectedLang.HINDI -> "$engineNote\n\nबिल्कुल! Video Agent, Design Agent और Social Media Agent ने मिलकर आपका कंटेंट पैक तैयार कर दिया है।\n\n• Hook: ${creatorDraft.hook}\n• Title: ${creatorDraft.title}\n• Hashtags: ${creatorDraft.tagsOrHashtags}\n\nइसे Studio → Creator में सेव करें! (सीधे वीडियो रेंडरिंग/अपलोड के लिए अधिकृत प्लेटफॉर्म API आवश्यक हैं।)$memoryContext"
                    DetectedLang.HINGLISH -> "$engineNote\n\nDone! Video Agent aur Social Media Agent ne aapka ${platform.label} pack ready kar diya hai:\n• Hook: ${creatorDraft.hook}\n• Title: ${creatorDraft.title}\n\nNeeche button se isse seedhe Studio → Creator mein save karein!$memoryContext"
                    DetectedLang.ENGLISH -> "$engineNote\n\nI teamed up Video Agent, Design Agent, and Social Media Agent to build your ${platform.label} production pack!\n• Hook: ${creatorDraft.hook}\n• Title: ${creatorDraft.title}\n\nSave it straight to Studio → Creator below (note: direct video rendering & YouTube/Instagram publishing require connected platform APIs).$memoryContext"
                }
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(
                        SpecialistAgent.AIRA_CORE,
                        SpecialistAgent.VIDEO,
                        SpecialistAgent.DESIGN,
                        SpecialistAgent.SOCIAL_MEDIA
                    ),
                    engineUsed = engine,
                    creatorDraft = creatorDraft,
                    isPlaceholder = true
                )
            }

            isMessageRequest -> {
                val channel = when {
                    lower.contains("email") -> "Email"
                    lower.contains("instagram") || lower.contains("social") -> "Instagram"
                    else -> "WhatsApp"
                }
                val draftBody = when (lang) {
                    DetectedLang.HINDI -> "नमस्ते टीम! आज के काम पर छोटा अपडेट — मुख्य हिस्से तैयार हो रहे हैं, शाम 5 बजे एक बार रिव्यू कर लेते हैं।"
                    DetectedLang.HINGLISH -> "Hey team! Quick update — kaam badhiya speed se chal raha hai. Shaam 5 baje ek quick sync kar lete hain!"
                    DetectedLang.ENGLISH -> "Hey team! Quick update — things are moving smoothly on today's milestone. Let's do a quick check-in at 5 PM!"
                }
                AiResponse(
                    replyText = "$engineNote\n\nGot your $channel draft ready! Review or tweak it below before sharing — I never send anything without your okay.$memoryContext",
                    agentsUsed = listOf(SpecialistAgent.AIRA_CORE, SpecialistAgent.SOCIAL_MEDIA, SpecialistAgent.SECURITY),
                    engineUsed = engine,
                    draftPayload = DraftPayload(channel = channel, recipient = "Team / Contact", body = draftBody),
                    isPlaceholder = true
                )
            }

            isPlanRequest -> {
                val tasks = if (lang == DetectedLang.HINDI) {
                    listOf(
                        "09:00 AM" to "मुख्य लक्ष्य पर 90 मिनट फोकस्ड वर्क",
                        "11:30 AM" to "मैसेज, ईमेल और क्रिएटर अपडेट्स",
                        "03:00 PM" to "रिसर्च, कोडिंग और स्किल ग्रोथ",
                        "06:30 PM" to "डेली मेमोरी रिव्यू और कल का प्लान"
                    )
                } else {
                    listOf(
                        "09:00 AM" to "90-min Deep Focus on Top Priority",
                        "11:30 AM" to "Messages, Emails & Quick Wins",
                        "03:00 PM" to "Build, Research & Creative Flow",
                        "06:30 PM" to "Daily Memory Wrap-up & Tomorrow's Prep"
                    )
                }
                val reply = when (lang) {
                    DetectedLang.HINDI -> "$engineNote\n\nचलिए आज का दिन सुपर प्रोडक्टिव बनाते हैं! मैंने आपके लिए एक संतुलित शेड्यूल तैयार किया है — एक टैप में Studio Planner में जोड़ें।$memoryContext"
                    DetectedLang.HINGLISH -> "$engineNote\n\nSorted! Maine aaj ka smart plan bana diya hai taaki energy aur focus dono balanced rahein. Neeche tap karke Planner mein add karo!$memoryContext"
                    DetectedLang.ENGLISH -> "$engineNote\n\nLet's make today awesome! I mapped out a balanced schedule for you — tap below to drop all blocks right into Studio Planner.$memoryContext"
                }
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(SpecialistAgent.AIRA_CORE, SpecialistAgent.MEMORY, SpecialistAgent.ANALYSIS),
                    engineUsed = engine,
                    suggestedPlannerTasks = tasks,
                    isPlaceholder = true
                )
            }

            isCodeRequest -> {
                val sampleCode = """interface AiraExecutor {
    suspend fun execute(goal: String): Result<String>
}

class AutonomousPipeline(
    private val executor: AiraExecutor
) {
    suspend fun deliver(goal: String): Result<String> =
        executor.execute("Plan, verify & deliver: ${'$'}goal")
}"""
                AiResponse(
                    replyText = "$engineNote\n\nCoding Agent & Security Agent put together a clean, testable Kotlin pattern for ${engine.displayName}. Want me to adapt it to another stack?$memoryContext",
                    agentsUsed = listOf(SpecialistAgent.CODING, SpecialistAgent.SECURITY, SpecialistAgent.TESTING),
                    engineUsed = engine,
                    codeSnippet = sampleCode,
                    codeLanguage = "kotlin",
                    isPlaceholder = true
                )
            }

            isSadOrStress -> {
                val reply = when (lang) {
                    DetectedLang.HINDI -> "$engineNote\n\nअरे दोस्त, मैं यहीं हूँ आपके साथ। थोड़ा पानी पीजिए और गहरी सांस लीजिए — हम सब कुछ छोटे-छोटे आसान कदमों में बाँटकर संभाल लेंगे। बताइए अभी किस चीज़ में मदद करूँ?$memoryContext"
                    DetectedLang.HINGLISH -> "$engineNote\n\nHey, main hoon na! Thoda sa pause lo — har cheez ek saath solve karne ki zaroorat nahi hai. Batao abhi sabse pehle kya easy banaun?$memoryContext"
                    DetectedLang.ENGLISH -> "$engineNote\n\nHey, I'm right here with you. Take a quick breather — you don't have to carry everything at once. Tell me the one thing feeling heaviest right now and let's tackle it together.$memoryContext"
                }
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(SpecialistAgent.AIRA_CORE),
                    engineUsed = engine,
                    isPlaceholder = true
                )
            }

            else -> {
                val toneSignOff = when (tone) {
                    JarvisTone.COMPANION -> "Always excited to brainstorm, joke around, build apps, or plan reels with you!"
                    JarvisTone.ENGINEER -> "Ready to architect systems, write code, or debug edge cases."
                    JarvisTone.STRATEGIST -> "Ready to route tasks across our 11-agent squad and deliver."
                }
                val reply = when (lang) {
                    DetectedLang.HINDI -> "$engineNote\n\nहाय दोस्त! मैं AIRA हूँ — आपकी पर्सनल AI साथी। आपने कहा: \"$userPrompt\"\n\n$toneSignOff$memoryContext"
                    DetectedLang.HINGLISH -> "$engineNote\n\nHey dost! Main AIRA hoon. Aapne kaha: \"$userPrompt\"\n\n$toneSignOff$memoryContext"
                    DetectedLang.ENGLISH -> "$engineNote\n\nHey! I'm AIRA — your friendly autonomous companion. Got your note: \"$userPrompt\"\n\n$toneSignOff$memoryContext"
                }
                AiResponse(
                    replyText = reply,
                    agentsUsed = listOf(SpecialistAgent.AIRA_CORE, SpecialistAgent.MEMORY),
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
                    val hinglishMarkers = listOf("aaj", "mera", "kar do", "kaise", "hai", "bhai", "dost", "kya", "banao", "samjhao", "mujhe")
                    if (hinglishMarkers.any { lower.contains(it) }) DetectedLang.HINGLISH else DetectedLang.ENGLISH
                }
            }
        }
    }
}
