package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.AiEngine
import com.example.data.ai.JarvisLanguage
import com.example.data.local.ChatMessageEntity
import com.example.ui.JarvisUiState
import com.example.ui.JarvisViewModel
import com.example.ui.components.AgentBadgeRow
import com.example.ui.components.AiEngineSelectorStrip
import com.example.ui.components.CodeBlockCard
import com.example.ui.components.DraftConfirmationCard
import com.example.ui.components.FirebasePlaceholderBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionScreen(
    uiState: JarvisUiState,
    onSendMessage: (String) -> Unit,
    onNewConversation: () -> Unit,
    onSelectConversation: (Long) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onConfirmDraft: (Long, String) -> Unit,
    onDiscardDraft: (Long, String) -> Unit,
    onImportSuggestedTasks: (Long, String) -> Unit,
    onSelectLanguage: (JarvisLanguage) -> Unit,
    onSelectEngine: (AiEngine) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    var showHistorySheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val selectedEngine = uiState.preferences.selectedEngine
    val isEngineLive = selectedEngine in uiState.liveEngines

    LaunchedEffect(uiState.messages.size, uiState.streamingReply) {
        val total = uiState.messages.size + if (uiState.streamingReply != null) 1 else 0
        if (total > 0) {
            listState.animateScrollToItem(total - 1)
        }
    }

    val sendCurrentMessage = {
        val toSend = inputText.trim()
        if (toSend.isNotEmpty() && !uiState.isGenerating) {
            inputText = ""
            onSendMessage(toSend)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
        ) {
            // Top HUD Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_jarvis_avatar),
                        contentDescription = stringResource(R.string.cd_jarvis_avatar),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "JARVIS",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isEngineLive) {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                }
                            ) {
                                Text(
                                    text = if (isEngineLive) "${selectedEngine.shortBadge} LIVE" else "${selectedEngine.shortBadge} SAMPLE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isEngineLive) {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${selectedEngine.modelLabel} • ${uiState.preferences.preferredLanguage.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("thread_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = stringResource(R.string.cd_thread_history),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onNewConversation,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("new_thread_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.cd_new_thread),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Gemini / Claude / ChatGPT Engine Switcher
            AiEngineSelectorStrip(
                selectedEngine = selectedEngine,
                liveEngines = uiState.liveEngines,
                onSelectEngine = onSelectEngine,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(6.dp))

            if (!isEngineLive) {
                FirebasePlaceholderBanner(
                    selectedEngine = selectedEngine,
                    isSelectedEngineLive = false,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(6.dp))
            }

            // Language Quick Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JarvisLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = uiState.preferences.preferredLanguage == lang,
                        onClick = { onSelectLanguage(lang) },
                        label = { Text(lang.label, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("lang_chip_${lang.name.lowercase()}")
                    )
                }
            }

            AnimatedVisibility(visible = uiState.isGenerating) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Conversation Feed or Welcome State
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.messages.isEmpty() && uiState.streamingReply == null) {
                    item {
                        CompanionWelcomeCard(
                            selectedEngine = selectedEngine,
                            onPromptClick = { prompt -> onSendMessage(prompt) }
                        )
                    }
                }

                items(uiState.messages, key = { it.id }) { msg ->
                    ChatMessageItem(
                        message = msg,
                        onConfirmDraft = { edited -> onConfirmDraft(msg.id, edited) },
                        onDiscardDraft = { onDiscardDraft(msg.id, msg.draftBody.orEmpty()) },
                        onImportTasks = { onImportSuggestedTasks(msg.id, msg.suggestedTasksEncoded) }
                    )
                }

                if (uiState.streamingReply != null) {
                    item(key = "streaming_bubble") {
                        StreamingJarvisBubble(
                            engine = selectedEngine,
                            text = uiState.streamingReply
                        )
                    }
                }
            }

            // Compact, IME-friendly Composer Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 104.dp)
                            .testTag("companion_input_field"),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.composer_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendCurrentMessage() }),
                        shape = MaterialTheme.shapes.large
                    )
                    FilledIconButton(
                        onClick = sendCurrentMessage,
                        enabled = inputText.isNotBlank() && !uiState.isGenerating,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("send_command_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.cd_send_command)
                        )
                    }
                }
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Conversation History",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (uiState.conversations.isEmpty()) {
                    Text(
                        text = "No saved conversations yet. Send a message to start your first JARVIS session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    uiState.conversations.forEach { conv ->
                        val isSelected = conv.id == uiState.activeConversationId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectConversation(conv.id)
                                    showHistorySheet = false
                                }
                                .testTag("thread_item_${conv.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = conv.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onDeleteConversation(conv.id) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = stringResource(R.string.cd_delete_item),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun CompanionWelcomeCard(
    selectedEngine: AiEngine,
    onPromptClick: (String) -> Unit
) {
    val starters = listOf(
        "आज मेरा पूरा दिन प्लान कर दो।",
        "Build a clean Kotlin architecture for an offline-first app",
        "Draft a friendly WhatsApp update for my team",
        "Launch an online store project with the multi-agent team"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("companion_welcome_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_jarvis_avatar),
                    contentDescription = stringResource(R.string.cd_jarvis_avatar),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Column {
                    Text(
                        text = "Hey! I'm JARVIS.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Engine: ${selectedEngine.displayName} • Gemini, Claude & ChatGPT Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = "Switch effortlessly between Gemini, Claude, and ChatGPT above, or open the AI Tools tab for Summarizer, Translator, Code Studio, Writer, and Side-by-Side Model Compare.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "QUICK COMMANDS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                starters.forEachIndexed { idx, prompt ->
                    AssistChip(
                        onClick = { onPromptClick(prompt) },
                        label = {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("starter_prompt_$idx")
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessageEntity,
    onConfirmDraft: (String) -> Unit,
    onDiscardDraft: () -> Unit,
    onImportTasks: () -> Unit
) {
    val isUser = message.sender == "USER"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .testTag(if (isUser) "user_message_${message.id}" else "jarvis_message_${message.id}"),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            border = if (isUser) null else BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isUser) {
                    AgentBadgeRow(
                        agentNamesCsv = message.agentsInvolved,
                        engineName = message.engineName
                    )
                }

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (!message.codeSnippet.isNullOrBlank()) {
                    CodeBlockCard(
                        code = message.codeSnippet,
                        language = message.codeLanguage
                    )
                }

                if (!message.draftBody.isNullOrBlank() && !message.draftChannel.isNullOrBlank()) {
                    DraftConfirmationCard(
                        channel = message.draftChannel,
                        recipient = message.draftRecipient ?: "Recipient",
                        body = message.draftBody,
                        status = message.draftStatus ?: "AWAITING_CONFIRMATION",
                        onConfirmAndShare = onConfirmDraft,
                        onDiscard = onDiscardDraft
                    )
                }

                val suggestedTasks = remember(message.suggestedTasksEncoded) {
                    JarvisViewModel.parseSuggestedTasks(message.suggestedTasksEncoded)
                }
                if (suggestedTasks.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Suggested Day Schedule",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            suggestedTasks.forEach { (time, title) ->
                                Text(
                                    text = "• $time — $title",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = onImportTasks,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("import_suggested_tasks_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Add All to Studio Planner")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingJarvisBubble(
    engine: AiEngine,
    text: String
) {
    Surface(
        modifier = Modifier
            .widthIn(max = 580.dp)
            .testTag("streaming_jarvis_bubble"),
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "${engine.displayName.uppercase()} STREAMING…",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text.ifBlank { "Thinking across ${engine.displayName}…" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
