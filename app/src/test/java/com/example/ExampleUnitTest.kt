package com.example

import com.example.data.ai.AiEngine
import com.example.data.ai.AiStreamEvent
import com.example.data.ai.AiToolKind
import com.example.data.ai.CreatorPlatform
import com.example.data.ai.JarvisLanguage
import com.example.data.ai.JarvisTone
import com.example.data.ai.MemoryLayer
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.ai.SpecialistAgent
import com.example.ui.JarvisViewModel
import com.example.ui.MilestoneItem
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun airaModels_exposeAllElevenAgentsAndSixMemoryLayers() {
        assertEquals(11, SpecialistAgent.entries.size)
        assertEquals(6, MemoryLayer.entries.size)
        assertTrue(SpecialistAgent.entries.contains(SpecialistAgent.AIRA_CORE))
        assertTrue(SpecialistAgent.entries.contains(SpecialistAgent.VIDEO))
        assertTrue(SpecialistAgent.entries.contains(SpecialistAgent.SOCIAL_MEDIA))
        assertTrue(MemoryLayer.entries.contains(MemoryLayer.CORE))
        assertTrue(MemoryLayer.entries.contains(MemoryLayer.LONG_TERM))
        assertTrue(MemoryLayer.entries.contains(MemoryLayer.PROJECT))
        assertTrue(MemoryLayer.entries.contains(MemoryLayer.CONTEXTUAL))
        assertTrue(MemoryLayer.entries.contains(MemoryLayer.DAILY))
        assertTrue(MemoryLayer.entries.contains(MemoryLayer.KNOWLEDGE))
    }

    @Test
    fun placeholderAi_streamsLabeledSampleForDrafts() = runTest {
        val repo = PlaceholderAiRepository()
        assertFalse(repo.isLiveAiConnected)

        val events = repo.streamCompanionReply(
            userPrompt = "Draft a WhatsApp message for my team",
            history = emptyList(),
            activeMemories = emptyList(),
            language = JarvisLanguage.ENGLISH,
            tone = JarvisTone.COMPANION,
            engine = AiEngine.CLAUDE
        ).toList()

        val completed = events.filterIsInstance<AiStreamEvent.Completed>().single().response
        assertTrue(completed.isPlaceholder)
        assertEquals(AiEngine.CLAUDE, completed.engineUsed)
        assertTrue(completed.replyText.contains("Sample Preview"))
        assertNotNull(completed.draftPayload)
        assertEquals("WhatsApp", completed.draftPayload?.channel)
        assertTrue(completed.agentsUsed.contains(SpecialistAgent.SOCIAL_MEDIA))
    }

    @Test
    fun placeholderAi_detectsHindiDayPlanning() = runTest {
        val repo = PlaceholderAiRepository()
        val events = repo.streamCompanionReply(
            userPrompt = "आज मेरा पूरा दिन प्लान कर दो।",
            history = emptyList(),
            activeMemories = emptyList(),
            language = JarvisLanguage.AUTO,
            tone = JarvisTone.COMPANION,
            engine = AiEngine.GEMINI
        ).toList()

        val completed = events.filterIsInstance<AiStreamEvent.Completed>().single().response
        assertTrue(completed.isPlaceholder)
        assertTrue(completed.suggestedPlannerTasks.isNotEmpty())
        assertTrue(completed.agentsUsed.contains(SpecialistAgent.AIRA_CORE))
    }

    @Test
    fun placeholderAi_generatesVideoCreatorPackAndRunsTools() = runTest {
        val repo = PlaceholderAiRepository()

        val videoResult = repo.runAiTool(
            tool = AiToolKind.VIDEO_STUDIO,
            engine = AiEngine.GEMINI,
            input = "AI productivity workflows",
            option = "YouTube Shorts",
            language = JarvisLanguage.ENGLISH
        )
        assertNotNull(videoResult.creatorDraft)
        assertEquals(CreatorPlatform.YOUTUBE_SHORTS, videoResult.creatorDraft?.platform)
        assertTrue(videoResult.agentsUsed.contains(SpecialistAgent.VIDEO))
        assertTrue(videoResult.agentsUsed.contains(SpecialistAgent.SOCIAL_MEDIA))

        val toolResult = repo.runAiTool(
            tool = AiToolKind.TRANSLATOR,
            engine = AiEngine.CHATGPT,
            input = "Hello friend",
            option = "Hindi (हिन्दी)",
            language = JarvisLanguage.AUTO
        )
        assertEquals(AiEngine.CHATGPT, toolResult.engineUsed)
        assertTrue(toolResult.replyText.contains("ChatGPT"))

        val comparison = repo.compareAllEngines("Explain clean architecture", JarvisLanguage.ENGLISH)
        assertEquals(3, comparison.size)
        assertTrue(comparison.containsKey(AiEngine.GEMINI))
        assertTrue(comparison.containsKey(AiEngine.CLAUDE))
        assertTrue(comparison.containsKey(AiEngine.CHATGPT))
    }

    @Test
    fun milestoneAndTaskEncoding_roundTripsCorrectly() {
        val items = listOf(
            MilestoneItem(isDone = true, title = "Scope project"),
            MilestoneItem(isDone = false, title = "Build UI")
        )
        val encoded = JarvisViewModel.encodeMilestones(items)
        assertEquals(items, JarvisViewModel.parseMilestones(encoded))

        val parsedTasks = JarvisViewModel.parseSuggestedTasks("09:00 AM|Deep Work;;03:00 PM|Review")
        assertEquals(2, parsedTasks.size)
        assertEquals("09:00 AM" to "Deep Work", parsedTasks[0])
    }
}
