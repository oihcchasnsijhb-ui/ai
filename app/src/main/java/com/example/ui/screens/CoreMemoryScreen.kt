package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.JarvisLanguage
import com.example.data.ai.JarvisTone
import com.example.data.local.MemoryEntryEntity
import com.example.ui.JarvisUiState
import com.example.ui.components.FirebasePlaceholderBanner
import com.example.ui.components.StatusPill
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoreMemoryScreen(
    uiState: JarvisUiState,
    onToggleGlobalMemory: (Boolean) -> Unit,
    onAddMemory: (String, String, String) -> Unit,
    onToggleMemoryEntry: (MemoryEntryEntity) -> Unit,
    onDeleteMemoryEntry: (Long) -> Unit,
    onSelectLanguage: (JarvisLanguage) -> Unit,
    onSelectTone: (JarvisTone) -> Unit,
    onSelectTheme: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMemorySheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Memory & Core",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                FirebasePlaceholderBanner(
                    selectedEngine = uiState.preferences.selectedEngine,
                    isSelectedEngineLive = uiState.preferences.selectedEngine in uiState.liveEngines
                )
            }

            // AI Engines Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Connected AI Engines",
                            style = MaterialTheme.typography.titleMedium
                        )
                        com.example.data.ai.AiEngine.entries.forEach { engine ->
                            val isLive = engine in uiState.liveEngines
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${engine.displayName} (${engine.provider})",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = when {
                                            isLive -> "${engine.modelLabel} connected"
                                            engine == com.example.data.ai.AiEngine.GEMINI -> "Needs Firebase AI Logic enabled"
                                            engine == com.example.data.ai.AiEngine.CLAUDE -> "Set ANTHROPIC_API_KEY in AI Studio Secrets"
                                            else -> "Set OPENAI_API_KEY in AI Studio Secrets"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusPill(
                                    label = if (isLive) "LIVE" else "SAMPLE",
                                    color = if (isLive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Master Memory Control
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Persistent Personalization Memory",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Remember preferences, active projects, and instructions locally on this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = uiState.preferences.memoryEnabled,
                            onCheckedChange = onToggleGlobalMemory,
                            modifier = Modifier.testTag("global_memory_switch")
                        )
                    }
                }
            }

            // Personality & Language Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Companion Personality", style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            JarvisTone.entries.forEach { tone ->
                                FilterChip(
                                    selected = uiState.preferences.personalityTone == tone,
                                    onClick = { onSelectTone(tone) },
                                    label = { Text(tone.label) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("tone_chip_${tone.name.lowercase()}")
                                )
                            }
                        }

                        Text(text = "Default Language", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            JarvisLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = uiState.preferences.preferredLanguage == lang,
                                    onClick = { onSelectLanguage(lang) },
                                    label = { Text(lang.label) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                )
                            }
                        }

                        Text(text = "Visual Theme", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = uiState.preferences.themeMode == mode,
                                    onClick = { onSelectTheme(mode) },
                                    label = { Text(mode.label) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("theme_chip_${mode.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // Memory Vault Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MEMORY VAULT (${uiState.memories.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Button(
                        onClick = { showAddMemorySheet = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_memory_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.cd_add_memory),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Add Memory")
                    }
                }
            }

            if (uiState.memories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Memory Vault Is Empty",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Save preferences like your tech stack, active projects, or communication style so JARVIS stays tailored to you. You can toggle or erase any entry anytime.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.memories, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_entry_${entry.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                StatusPill(
                                    label = entry.category.uppercase(),
                                    color = if (entry.isEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = entry.memoryKey,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = entry.memoryValue,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = entry.isEnabled,
                                onCheckedChange = { onToggleMemoryEntry(entry) }
                            )
                            IconButton(
                                onClick = { onDeleteMemoryEntry(entry.id) },
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

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showAddMemorySheet) {
        var category by remember { mutableStateOf("Preference") }
        var key by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }

        ModalBottomSheet(onDismissRequest = { showAddMemorySheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Save Memory Entry", style = MaterialTheme.typography.headlineSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Preference", "Project", "Style", "Instruction").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Topic / Key") },
                    placeholder = { Text("e.g., Preferred Tech Stack") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("memory_key_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Detail to Remember") },
                    placeholder = { Text("e.g., Kotlin, Jetpack Compose, and clean MVVM") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("memory_value_input"),
                    minLines = 2
                )
                Button(
                    onClick = {
                        if (key.isNotBlank() && value.isNotBlank()) {
                            onAddMemory(category, key, value)
                            showAddMemorySheet = false
                        }
                    },
                    enabled = key.isNotBlank() && value.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("save_memory_button")
                ) {
                    Text("Save to Memory Vault")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
