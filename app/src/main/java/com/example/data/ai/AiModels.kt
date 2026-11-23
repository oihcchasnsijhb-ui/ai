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
        subtitle = "Craft detailed art & video prompts",
        defaultOption = "Cinematic Visual",
        options = listOf("Cinematic Visual", "App UI Concept", "Reels Script")
    ),
    COMPARE(
        title = "Compare All 3 AIs",
        subtitle = "Gemini vs Claude vs ChatGPT",
        defaultOption = "Side-by-Side",
        options = listOf("Side-by-Side")
    )
}

enum class SpecialistAgent(
    val displayName: String,
    val roleSummary: String,
    val badgeCode: String
) {
    MANAGER(
        displayName = "Manager AI",
        roleSummary = "Coordinates the specialist team, monitors milestones, and merges final deliverables.",
        badgeCode = "MGR"
    ),
    PLANNER(
        displayName = "Planner AI",
        roleSummary = "Breaks complex objectives into actionable steps, timelines, and daily schedules.",
        badgeCode = "PLN"
    ),
    RESEARCH(
        displayName = "Research AI",
        roleSummary = "Gathers facts, compares options, and synthesizes multi-source findings.",
        badgeCode = "RSC"
    ),
    CODING(
        displayName = "Coding AI",
        roleSummary = "Architects, writes, reviews, and debugs clean production software.",
        badgeCode = "DEV"
    ),
    CREATIVE(
        displayName = "Creative AI",
        roleSummary = "Crafts UI concepts, stories, scripts, logos, and campaign ideas.",
        badgeCode = "CRT"
    ),
    ANALYST(
        displayName = "Analyst AI",
        roleSummary = "Evaluates trade-offs, metrics, opportunities, and edge cases.",
        badgeCode = "ANL"
    ),
    SECURITY(
        displayName = "Security AI",
        roleSummary = "Audits permissions, privacy boundaries, credentials, and operational risks.",
        badgeCode = "SEC"
    ),
    TESTING(
        displayName = "Testing AI",
        roleSummary = "Verifies functionality, checks quality, and catches regressions.",
        badgeCode = "TST"
    ),
    COMMUNICATION(
        displayName = "Communication AI",
        roleSummary = "Drafts messages, emails, replies, and posts with confirmation gating.",
        badgeCode = "COM"
    )
}

enum class JarvisLanguage(val label: String) {
    AUTO("Auto-Detect"),
    ENGLISH("English"),
    HINGLISH("Hinglish"),
    HINDI("हिन्दी")
}

enum class JarvisTone(val label: String, val subtitle: String) {
    COMPANION("Warm Companion", "Friendly, witty, emotionally perceptive"),
    ENGINEER("Lead Engineer", "Direct, architectural, code-first"),
    STRATEGIST("Chief Strategist", "Structured, decisive, multi-agent")
}

data class DraftPayload(
    val channel: String,
    val recipient: String,
    val body: String
)

data class AiResponse(
    val replyText: String,
    val agentsUsed: List<SpecialistAgent>,
    val engineUsed: AiEngine = AiEngine.GEMINI,
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val draftPayload: DraftPayload? = null,
    val suggestedPlannerTasks: List<Pair<String, String>> = emptyList(),
    val suggestedMilestones: List<String> = emptyList(),
    val isPlaceholder: Boolean = true
)

sealed interface AiStreamEvent {
    data class Partial(val accumulatedText: String) : AiStreamEvent
    data class Completed(val response: AiResponse) : AiStreamEvent
}
