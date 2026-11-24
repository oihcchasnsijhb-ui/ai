package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Query("UPDATE conversations SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateConversationMeta(id: Long, title: String, updatedAt: Long)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Long)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC, id ASC")
    fun observeMessages(conversationId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE draftBody IS NOT NULL OR codeSnippet IS NOT NULL ORDER BY timestamp DESC")
    fun observeStudioArtifacts(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("UPDATE chat_messages SET draftBody = :body, draftStatus = :status WHERE id = :messageId")
    suspend fun updateDraft(messageId: Long, body: String, status: String)

    @Query("UPDATE chat_messages SET suggestedTasksEncoded = '' WHERE id = :messageId")
    suspend fun clearSuggestedTasks(messageId: Long)
}

@Dao
interface AgentMissionDao {
    @Query("SELECT * FROM agent_missions ORDER BY updatedAt DESC")
    fun observeMissions(): Flow<List<AgentMissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: AgentMissionEntity): Long

    @Update
    suspend fun updateMission(mission: AgentMissionEntity)

    @Query("DELETE FROM agent_missions WHERE id = :id")
    suspend fun deleteMission(id: Long)
}

@Dao
interface PlannerTaskDao {
    @Query("SELECT * FROM planner_tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun observeTasks(): Flow<List<PlannerTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PlannerTaskEntity): Long

    @Query("UPDATE planner_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM planner_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_entries ORDER BY updatedAt DESC")
    fun observeMemories(): Flow<List<MemoryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntryEntity)

    @Query("DELETE FROM memory_entries WHERE id = :id")
    suspend fun deleteMemory(id: Long)
}

@Dao
interface CreatorPackDao {
    @Query("SELECT * FROM creator_packs ORDER BY updatedAt DESC")
    fun observeCreatorPacks(): Flow<List<CreatorPackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreatorPack(pack: CreatorPackEntity): Long

    @Update
    suspend fun updateCreatorPack(pack: CreatorPackEntity)

    @Query("DELETE FROM creator_packs WHERE id = :id")
    suspend fun deleteCreatorPack(id: Long)
}
