package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val languageHint: String = "AUTO",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val conversationId: Long,
    val sender: String, // "USER" or "JARVIS"
    val content: String,
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val agentsInvolved: String = "", // Comma-separated SpecialistAgent names
    val draftChannel: String? = null,
    val draftRecipient: String? = null,
    val draftBody: String? = null,
    val draftStatus: String? = null, // "AWAITING_CONFIRMATION", "CONFIRMED_SHARED", "DISCARDED"
    val suggestedTasksEncoded: String = "", // "time|title;;time|title"
    val engineName: String = "GEMINI",
    val isPlaceholderNotice: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "agent_missions")
data class AgentMissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val goal: String,
    val assignedAgents: String, // Comma-separated SpecialistAgent names
    val status: String, // "PENDING", "IN_PROGRESS", "NEEDS_APPROVAL", "COMPLETED", "FAILED"
    val milestonesEncoded: String, // "done|title;;done|title" where done is 0 or 1
    val summaryOutput: String,
    val isPlaceholderNotice: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "planner_tasks")
data class PlannerTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val timeSlot: String,
    val category: String,
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memory_entries")
data class MemoryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val category: String, // "Preference", "Project", "Style", "Instruction"
    val memoryKey: String,
    val memoryValue: String,
    val isEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
