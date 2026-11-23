package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.AiEngine
import com.example.data.ai.AiToolKind
import com.example.ui.JarvisUiState
import com.example.ui.components.AiEngineSelectorStrip
import com.example.ui.components.CodeBlockCard
import com.example.ui.components.FirebasePlaceholderBanner
import com.example.ui.components.StatusPill
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.shareDraftIntent

@Composable
fun AiToolsScreen(
    uiState: JarvisUiState,
    onSelectEngine: (AiEngine) -> Unit,
    onSelectTool: (AiToolKind) -> Unit,
    onRunTool: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTool = uiState.activeTool
    var inputText by rememberSaveable { mutableStateOf("") }
    var selectedOption by remember(activeTool) { mutableStateOf(activeTool.defaultOption) }
    val context = LocalContext.current

    LaunchedEffect(activeTool) {
        selectedOption = activeTool.defaultOption
    }

    val selectedEngine = uiState.preferences.selectedEngine
    val isEngineLive = selectedEngine in uiState.liveEngines

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "AI Tools Suite",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Switch between Gemini, Claude & ChatGPT or compare all 3 side-by-side",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (activeTool != AiToolKind.COMPARE) {
                item {
                    AiEngineSelectorStrip(
                        selectedEngine = selectedEngine,
                        liveEngines = uiState.liveEngines,
                        onSelectEngine = onSelectEngine
                    )
                }
            }

            item {
                FirebasePlaceholderBanner(
                    selectedEngine = selectedEngine,
                    isSelectedEngineLive = isEngineLive && activeTool != AiToolKind.COMPARE
                )
            }

            // Tool Selector Grid / Row
            item {
                Text(
                    text = "CHOOSE AN AI TOOL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AiToolKind.entries.forEach { tool ->
                        val isSelected = tool == activeTool
                        val icon = when (tool) {
                            AiToolKind.SUMMARIZER -> Icons.Filled.Summarize
                            AiToolKind.TRANSLATOR -> Icons.Filled.Translate
                            AiToolKind.CODE_STUDIO -> Icons.Filled.Code
                            AiToolKind.WRITER -> Icons.Filled.EditNote
                            AiToolKind.PROMPT_LAB -> Icons.Filled.Palette
                            AiToolKind.COMPARE -> Icons.Filled.CompareArrows
                        }
                        Card(
                            modifier = Modifier
                                .width(168.dp)
                                .clickable { onSelectTool(tool) }
                                .testTag("ai_tool_card_${tool.name.lowercase()}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = tool.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = tool.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Active Tool Workspace Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = activeTool.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (activeTool.options.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                activeTool.options.forEach { option ->
                                    FilterChip(
                                        selected = selectedOption == option,
                                        onClick = { selectedOption = option },
                                        label = { Text(option) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            label = {
                                Text(
                                    when (activeTool) {
                                        AiToolKind.SUMMARIZER -> "Paste text, notes, or article"
                                        AiToolKind.TRANSLATOR -> "Enter text to translate"
                                        AiToolKind.CODE_STUDIO -> "Describe code or paste error snippet"
                                        AiToolKind.WRITER -> "What message or email should JARVIS write?"
                                        AiToolKind.PROMPT_LAB -> "Describe your image, UI, or video idea"
                                        AiToolKind.COMPARE -> "Ask one question to Gemini, Claude & ChatGPT"
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_tool_input"),
                            minLines = 3,
                            maxLines = 6
                        )

                        Button(
                            onClick = { onRunTool(inputText, selectedOption) },
                            enabled = inputText.isNotBlank() && !uiState.isRunningTool,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("run_ai_tool_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoFixHigh,
                                contentDescription = stringResource(R.string.cd_run_ai_tool),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (activeTool == AiToolKind.COMPARE) {
                                    "Compare Gemini, Claude & ChatGPT"
                                } else {
                                    "Run with ${selectedEngine.displayName}"
                                }
                            )
                        }

                        AnimatedVisibility(visible = uiState.isRunningTool) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            // Single Tool Result
            uiState.toolOutput?.let { result ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_tool_result_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusPill(
                                    label = "${result.engineUsed.displayName} • ${if (result.isPlaceholder) "SAMPLE" else "LIVE"}",
                                    color = if (result.isPlaceholder) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                                )
                                IconButton(
                                    onClick = { copyTextToClipboard(context, result.replyText) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = stringResource(R.string.cd_copy_code),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = result.replyText,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (!result.codeSnippet.isNullOrBlank()) {
                                CodeBlockCard(
                                    code = result.codeSnippet,
                                    language = result.codeLanguage
                                )
                            }

                            result.draftPayload?.let { draft ->
                                OutlinedButton(
                                    onClick = { shareDraftIntent(context, draft.channel, draft.body) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = stringResource(R.string.cd_share_draft),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Share ${draft.channel} Draft")
                                }
                            }
                        }
                    }
                }
            }

            // Multi-Model Comparison Results
            if (uiState.toolComparisons.isNotEmpty()) {
                AiEngine.entries.forEach { engine ->
                    val resp = uiState.toolComparisons[engine] ?: return@forEach
                    item(key = "compare_${engine.name}") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("compare_card_${engine.name.lowercase()}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${engine.displayName} (${engine.provider})",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    StatusPill(
                                        label = if (resp.isPlaceholder) "SAMPLE" else "LIVE",
                                        color = if (resp.isPlaceholder) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Text(
                                    text = resp.replyText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
