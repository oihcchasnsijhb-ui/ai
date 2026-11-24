package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.SpecialistAgent
import com.example.data.local.AgentMissionEntity
import com.example.ui.JarvisUiState
import com.example.ui.JarvisViewModel
import com.example.ui.components.AgentBadgeRow
import com.example.ui.components.FirebasePlaceholderBanner
import com.example.ui.components.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    uiState: JarvisUiState,
    onLaunchMission: (String, String, List<SpecialistAgent>) -> Unit,
    onToggleMilestone: (AgentMissionEntity, Int) -> Unit,
    onUpdateMissionStatus: (AgentMissionEntity, String) -> Unit,
    onDeleteMission: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewMissionSheet by remember { mutableStateOf(false) }
    var inspectedAgent by remember { mutableStateOf<SpecialistAgent?>(null) }

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
                // Hero Command Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .clip(MaterialTheme.shapes.large)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_jarvis_core_banner),
                        contentDescription = stringResource(R.string.cd_aira_banner),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "AIRA 11-Agent Team",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "UNDERSTAND → PLAN → DELEGATE → EXECUTE → VERIFY → DELIVER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                FirebasePlaceholderBanner(
                    selectedEngine = uiState.preferences.selectedEngine,
                    isSelectedEngineLive = uiState.preferences.selectedEngine in uiState.liveEngines
                )
            }

            item {
                Text(
                    text = "SPECIALIST AGENTS (${SpecialistAgent.entries.size})",
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
                    SpecialistAgent.entries.forEach { agent ->
                        Card(
                            modifier = Modifier
                                .width(176.dp)
                                .clickable { inspectedAgent = agent }
                                .testTag("agent_card_${agent.name.lowercase()}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (inspectedAgent == agent) {
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
                                StatusPill(
                                    label = agent.badgeCode,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = agent.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = agent.roleSummary,
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE MISSIONS (${uiState.missions.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Button(
                        onClick = { showNewMissionSheet = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("launch_mission_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("New Mission")
                    }
                }
            }

            if (uiState.missions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
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
                                imageVector = Icons.Filled.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No Missions Assigned Yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Assign a goal like producing a YouTube + Instagram Reel series, building an app, or researching competitors across AIRA's 11-agent squad.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.missions, key = { it.id }) { mission ->
                    MissionCard(
                        mission = mission,
                        onToggleMilestone = { index -> onToggleMilestone(mission, index) },
                        onUpdateStatus = { status -> onUpdateMissionStatus(mission, status) },
                        onDelete = { onDeleteMission(mission.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }

        FloatingActionButton(
            onClick = { showNewMissionSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_new_mission"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.cd_add_mission)
            )
        }
    }

    if (showNewMissionSheet) {
        var title by remember { mutableStateOf("") }
        var goal by remember { mutableStateOf("") }
        val selectedAgents = remember {
            mutableStateListOf(
                SpecialistAgent.AIRA_CORE,
                SpecialistAgent.RESEARCH,
                SpecialistAgent.VIDEO,
                SpecialistAgent.SOCIAL_MEDIA,
                SpecialistAgent.CODING
            )
        }

        ModalBottomSheet(onDismissRequest = { showNewMissionSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Assign 11-Agent Mission",
                    style = MaterialTheme.typography.headlineSmall
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Mission Title (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mission_title_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Goal or Objective") },
                    placeholder = { Text("e.g., Make a YouTube video + Instagram Reel about AI") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mission_goal_input"),
                    minLines = 2
                )
                Text(
                    text = "Select Specialist Agents",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpecialistAgent.entries.forEach { agent ->
                        val active = agent in selectedAgents
                        FilterChip(
                            selected = active,
                            onClick = {
                                if (active) selectedAgents.remove(agent) else selectedAgents.add(agent)
                            },
                            label = { Text(agent.displayName) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }
                Button(
                    onClick = {
                        if (goal.isNotBlank()) {
                            onLaunchMission(title, goal, selectedAgents.toList())
                            showNewMissionSheet = false
                        }
                    },
                    enabled = goal.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("confirm_launch_mission_button")
                ) {
                    Text("Deploy AIRA Squad")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    inspectedAgent?.let { agent ->
        ModalBottomSheet(onDismissRequest = { inspectedAgent = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusPill(label = agent.badgeCode, color = MaterialTheme.colorScheme.primary)
                Text(text = agent.displayName, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = agent.roleSummary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        inspectedAgent = null
                        showNewMissionSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                ) {
                    Text("Assign a Mission")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MissionCard(
    mission: AgentMissionEntity,
    onToggleMilestone: (Int) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val milestones = remember(mission.milestonesEncoded) {
        JarvisViewModel.parseMilestones(mission.milestonesEncoded)
    }
    val statusColor = when (mission.status) {
        "COMPLETED" -> MaterialTheme.colorScheme.tertiary
        "NEEDS_APPROVAL" -> MaterialTheme.colorScheme.secondary
        "FAILED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mission_card_${mission.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mission.goal,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(label = mission.status.replace("_", " "), color = statusColor)
                    IconButton(
                        onClick = onDelete,
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

            AgentBadgeRow(agentNamesCsv = mission.assignedAgents)

            Text(
                text = mission.summaryOutput,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (milestones.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "PROTOCOL MILESTONES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    milestones.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleMilestone(idx) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isDone,
                                onCheckedChange = { onToggleMilestone(idx) }
                            )
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (item.isDone) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("PENDING", "IN_PROGRESS", "NEEDS_APPROVAL", "COMPLETED").forEach { st ->
                    FilterChip(
                        selected = mission.status == st,
                        onClick = { onUpdateStatus(st) },
                        label = {
                            Text(
                                text = st.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (st == "COMPLETED") Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
