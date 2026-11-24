package com.example.data.ai

enum class AiEngine(
    val displayName: String,
    val provider: String,
    val modelLabel: String,
    val shortBadge: String
) {
    GEMINI(
        displayName = "Gemini",
        provider = "Google",
        modelLabel = "Gemini 3.8 Flash",
        shortBadge = "GEM"
    ),
    CLAUDE(
        displayName = "Claude",
        provider = "Anthropic",
        modelLabel = "Claude 3.5 Sonnet",
        shortBadge = "CLD"
    ),
    CHATGPT(
        displayName = "ChatGPT",
        provider = "OpenAI",
        modelLabel = "GPT-4o Mini",
        shortBadge = "GPT"
    )
}

enum class AiToolKind(
    val title: String,
    val subtitle: String,
    val defaultOption: String,
    val options: List<String>
) {
    VIDEO_STUDIO(
        title = "Video & Social Studio",
        subtitle = "YouTube, Shorts & Instagram Reels packs",
        defaultOption = "YouTube + Reel",
        options = listOf("YouTube + Reel", "YouTube Video", "YouTube Shorts", "Instagram Reel")
    ),
    SUMMARIZER(
        title = "Smart Summarizer",
        subtitle = "Condense articles, notes & chats",
        defaultOption = "Bullet Summary",
        options = listOf("Bullet Summary", "TL;DR (2 lines)", "Action Items")
    ),
    TRANSLATOR(
        title = "Polyglot Translator",
        subtitle = "Translate English, Hindi & Hinglish",
        defaultOption = "Hindi (हिन्दी)",
        options = listOf("Hindi (हिन्दी)", "English", "Hinglish", "Spanish")
    ),
    CODE_STUDIO(
        title = "Code & Debug Studio",
        subtitle = "Write, fix & explain code fast",
        defaultOption = "Generate Code",
        options = listOf("Generate Code", "Find Bugs", "Optimize & Explain")
    ),
    WRITER(
        title = "Message & Email Pro",
        subtitle = "Draft crisp WhatsApp & emails",
        defaultOption = "WhatsApp",
        options = listOf("WhatsApp", "Email", "LinkedIn Post")
    ),
    PROMPT_LAB(
        title = "Creative & Image Lab",
        subtitle = "Craft detailed art & thumbnail prompts",
        defaultOption = "Thumbnail Visual",
        options = listOf("Thumbnail Visual", "Cinematic Video", "App UI Concept")
    ),
    COMPARE(
        title = "Compare All 3 AIs",
        subtitle = "Gemini vs Claude vs ChatGPT",
        defaultOption = "Side-by-Side",
        options = listOf("Side-by-Side")
    )
}

enum class MemoryLayer(
    val label: String,
    val description: String
) {
    CORE("Core", "Preferences, communication style, personal goals & settings"),
    LONG_TERM("Long-Term", "Important conversations, past decisions & long-term plans"),
    PROJECT("Project", "Project specs, architecture, bugs & pending milestones"),
    CONTEXTUAL("Contextual", "Active context connecting ongoing conversations"),
    DAILY("Daily", "Daily summaries, routines, events & progress"),
    KNOWLEDGE("Knowledge", "Research notes, tutorials, references & facts")
}

enum class CreatorPlatform(val label: String) {
    YOUTUBE_VIDEO("YouTube Video"),
    YOUTUBE_SHORTS("YouTube Shorts"),
    INSTAGRAM_REEL("Instagram Reel"),
    INSTAGRAM_CAROUSEL("Instagram Post")
}

enum class SpecialistAgent(
    val displayName: String,
    val roleSummary: String,
    val badgeCode: String
) {
    AIRA_CORE(
        displayName = "AIRA Core",
        roleSummary = "Main intelligence, warm companion dialogue, planning, and task coordination.",
        badgeCode = "AIRA"
    ),
    RESEARCH(
        displayName = "Research Agent",
        roleSummary = "Researches topics, compares options, gathers references, and verifies facts.",
        badgeCode = "RSC"
    ),
    CODING(
        displayName = "Coding Agent",
        roleSummary = "Architects, writes, reviews, and debugs apps, websites, APIs, and scripts.",
        badgeCode = "DEV"
    ),
    VIDEO(
        displayName = "Video Agent",
        roleSummary = "Produces hooks, scripts, visual scene lists, voice-over cues, and edit plans.",
        badgeCode = "VID"
    ),
    SOCIAL_MEDIA(
        displayName = "Social Media Agent",
        roleSummary = "Prepares YouTube titles, descriptions, tags, Instagram captions, and hashtags.",
        badgeCode = "SOC"
    ),
    DESIGN(
        displayName = "Design Agent",
        roleSummary = "Designs UI concepts, logos, thumbnails, graphics, and creative visual direction.",
        badgeCode = "DSN"
    ),
    ANALYSIS(
        displayName = "Analysis Agent",
        roleSummary = "Evaluates trade-offs, performance metrics, options, and recommendations.",
        badgeCode = "ANL"
    ),
    SECURITY(
        displayName = "Security Agent",
        roleSummary = "Audits permissions, privacy, credentials, policies, and high-impact actions.",
        badgeCode = "SEC"
    ),
    TESTING(
        displayName = "Testing Agent",
        roleSummary = "Tests code, content workflows, edge cases, and final deliverable quality.",
        badgeCode = "TST"
    ),
    EXECUTION(
        displayName = "Execution Agent",
        roleSummary = "Runs connected tools, share flows, and authorized workflows step-by-step.",
        badgeCode = "EXE"
    ),
    MEMORY(
        displayName = "Memory Agent",
        roleSummary = "Indexes 6-layer memories, retrieves past context, and tracks project continuity.",
        badgeCode = "MEM"
    )
}

enum class JarvisLanguage(val label: String) {
    AUTO("Auto-Detect"),
    ENGLISH("English"),
    HINGLISH("Hinglish"),
    HINDI("हिन्दी")
}

enum class JarvisTone(val label: String, val subtitle: String) {
    COMPANION("Friendly Companion", "Warm, playful, supportive & emotionally aware"),
    ENGINEER("Tech Architect", "Direct, clean code & system design focused"),
    STRATEGIST("Chief Coordinator", "Decisive, multi-agent & execution driven")
}

data class DraftPayload(
    val channel: String,
    val recipient: String,
    val body: String
)

data class CreatorDraftPayload(
    val platform: CreatorPlatform,
    val topic: String,
    val title: String,
    val hook: String,
    val script: String,
    val captionOrDescription: String,
    val tagsOrHashtags: String
)

data class AiResponse(
    val replyText: String,
    val agentsUsed: List<SpecialistAgent>,
    val engineUsed: AiEngine = AiEngine.GEMINI,
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val draftPayload: DraftPayload? = null,
    val creatorDraft: CreatorDraftPayload? = null,
    val suggestedPlannerTasks: List<Pair<String, String>> = emptyList(),
    val suggestedMilestones: List<String> = emptyList(),
    val isPlaceholder: Boolean = true
)

sealed interface AiStreamEvent {
    data class Partial(val accumulatedText: String) : AiStreamEvent
    data class Completed(val response: AiResponse) : AiStreamEvent
}
