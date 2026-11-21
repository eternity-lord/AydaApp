package com.aydanailroom.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.PersonAddAlt1
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aydanailroom.app.R
import com.aydanailroom.app.data.AppointmentEntity
import com.aydanailroom.app.data.ClientEntity
import com.aydanailroom.app.data.DesignRecordEntity
import com.aydanailroom.app.ui.components.ChalkButton
import com.aydanailroom.app.ui.components.ChalkCard
import com.aydanailroom.app.ui.components.ChalkChip
import com.aydanailroom.app.ui.components.ChalkEmptyState
import com.aydanailroom.app.ui.components.ChalkField
import com.aydanailroom.app.ui.theme.ChalkColors
import com.aydanailroom.app.util.formatShortDate
import com.aydanailroom.app.util.formatToman
import java.time.Instant
import java.time.ZoneId

@Composable
fun ClientsScreen(
    clients: List<ClientEntity>,
    appointments: List<AppointmentEntity>,
    latinDigits: Boolean,
    onAdd: () -> Unit,
    onOpen: (ClientEntity) -> Unit,
    onArchive: (ClientEntity) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("all") }
    val now = System.currentTimeMillis()
    val visible = clients.filter { client ->
        val matchesQuery = query.isBlank() || client.fullName.contains(query, true) || client.phone.contains(query, true)
        val lastVisit = appointments.filter { it.clientId == client.id && it.status == "done" }.maxOfOrNull { it.startAt }
        val matchesFilter = when (filter) {
            "archived" -> client.isArchived
            "inactive" -> !client.isArchived && (lastVisit == null || now - lastVisit > 30L * 24 * 60 * 60 * 1000)
            else -> !client.isArchived
        }
        matchesQuery && matchesFilter
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChalkField(query, { query = it }, stringResource(R.string.label_search_clients), modifier = Modifier.weight(1f))
            IconButton(onClick = onAdd) {
                Icon(Icons.Rounded.PersonAddAlt1, contentDescription = stringResource(R.string.action_add_client), tint = ChalkColors.Pink)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)) {
            ChalkChip(stringResource(R.string.clients_active), filter == "all", { filter = "all" })
            ChalkChip(stringResource(R.string.clients_archived), filter == "archived", { filter = "archived" })
            ChalkChip(stringResource(R.string.clients_dormant), filter == "inactive", { filter = "inactive" })
        }
        if (visible.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ChalkEmptyState(stringResource(R.string.clients_empty), doodle = "♡")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(visible, key = { it.id }) { client ->
                    val lastVisit = appointments.filter { it.clientId == client.id && it.status == "done" }.maxOfOrNull { it.startAt }
                    val visits = appointments.count { it.clientId == client.id && it.status == "done" }
                    ChalkCard(onClick = { onOpen(client) }, contentPadding = 14.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(48.dp)
                                    .background(ChalkColors.Pink.copy(alpha = 0.16f), androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("✿", color = ChalkColors.Pink, fontSize = 23.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(client.fullName, color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
                                if (client.phone.isNotBlank()) Text(client.phone, color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    lastVisit?.let { stringResource(R.string.clients_last_visit, formatShortDate(LocalContext.current, it, latinDigits)) }
                                        ?: stringResource(R.string.clients_no_visits),
                                    color = ChalkColors.ChalkDim,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(com.aydanailroom.app.util.localizeDigits(visits.toString(), latinDigits), color = ChalkColors.Yellow, style = MaterialTheme.typography.titleLarge)
                                Text(stringResource(R.string.clients_visits_short), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientDetailsDialog(
    client: ClientEntity,
    appointments: List<AppointmentEntity>,
    designRecords: List<DesignRecordEntity>,
    latinDigits: Boolean,
    onDismiss: () -> Unit,
    onAddDesign: () -> Unit,
    onRepeatLast: (String) -> Unit,
    onArchive: () -> Unit,
) {
    val context = LocalContext.current
    var confirmArchive by remember { mutableStateOf(false) }
    val visits = appointments.filter { it.status == "done" }.sortedByDescending { it.startAt }
    val paidTotal = visits.sumOf { it.paidAmount }
    val lastVisit = visits.firstOrNull()?.startAt
    val shapeName = stringResource(nailShapeLabelId(client.nailShape))
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(client.fullName, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    if (client.phone.isNotBlank()) {
                        IconButton(onClick = {
                            runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(client.phone)}"))) }
                        }) { Icon(Icons.Rounded.Call, contentDescription = stringResource(R.string.action_call), tint = ChalkColors.Mint) }
                        IconButton(onClick = {
                            runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(client.phone)}"))) }
                        }) { Icon(Icons.Rounded.Message, contentDescription = stringResource(R.string.action_message), tint = ChalkColors.Pink) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DetailStat(
                        stringResource(R.string.clients_visit_history),
                        com.aydanailroom.app.util.localizeDigits(visits.size.toString(), latinDigits),
                        Modifier.weight(1f),
                    )
                    DetailStat(stringResource(R.string.label_total_income), formatToman(context, paidTotal, latinDigits), Modifier.weight(1f))
                }
                Text("${stringResource(R.string.label_nail_shape)}: $shapeName", color = ChalkColors.ChalkDim)
                Text(
                    lastVisit?.let { stringResource(R.string.clients_last_visit, formatShortDate(context, it, latinDigits)) }
                        ?: stringResource(R.string.clients_no_visits),
                    color = ChalkColors.ChalkDim,
                )
                if (client.notes.isNotBlank()) {
                    ChalkCard(contentPadding = 12.dp) {
                        Text(stringResource(R.string.label_notes), color = ChalkColors.Pink, style = MaterialTheme.typography.labelLarge)
                        Text(client.notes, color = ChalkColors.Chalk)
                    }
                }
                if (client.allergiesNote.isNotBlank()) {
                    ChalkCard(contentPadding = 12.dp) {
                        Text(stringResource(R.string.clients_allergy_warning), color = ChalkColors.Coral, style = MaterialTheme.typography.labelLarge)
                        Text(client.allergiesNote, color = ChalkColors.Chalk)
                    }
                }
                Text(stringResource(R.string.clients_design_history), color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
                designRecords.sortedByDescending { it.date }.take(5).forEach { record ->
                    ChalkCard(contentPadding = 12.dp) {
                        Text(formatShortDate(context, record.date, latinDigits), color = ChalkColors.Yellow, style = MaterialTheme.typography.labelMedium)
                        if (record.colorsText.isNotBlank()) Text("${stringResource(R.string.label_colors)}: ${record.colorsText}", color = ChalkColors.ChalkDim)
                        if (record.brandProductText.isNotBlank()) Text("${stringResource(R.string.label_products)}: ${record.brandProductText}", color = ChalkColors.ChalkDim)
                        if (record.designDescription.isNotBlank()) Text(record.designDescription, color = ChalkColors.Chalk)
                        if (record.clientFeedback.isNotBlank()) Text(record.clientFeedback, color = ChalkColors.Pink, style = MaterialTheme.typography.bodySmall)
                    }
                }
                ChalkButton(stringResource(R.string.clients_add_design), onAddDesign, Modifier.fillMaxWidth())
                designRecords.maxByOrNull { it.date }?.designDescription?.takeIf(String::isNotBlank)?.let { lastDesign ->
                    TextButton(onClick = { onRepeatLast(lastDesign) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.clients_same_as_last), color = ChalkColors.Pink)
                    }
                }
                if (visits.isNotEmpty()) {
                    Text(stringResource(R.string.clients_visit_history), color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
                    visits.take(5).forEach { appointment ->
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(formatShortDate(context, appointment.startAt, latinDigits), color = ChalkColors.ChalkDim)
                            Text(formatToman(context, appointment.paidAmount, latinDigits), color = ChalkColors.Mint)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (client.isArchived) onArchive() else confirmArchive = true }) {
                Text(stringResource(if (client.isArchived) R.string.action_undo else R.string.clients_archive_action))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) } },
        containerColor = ChalkColors.BoardDeep,
        titleContentColor = ChalkColors.Chalk,
        textContentColor = ChalkColors.Chalk,
    )
    if (confirmArchive) {
        AlertDialog(
            onDismissRequest = { confirmArchive = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.clients_archive_confirm)) },
            confirmButton = {
                TextButton(onClick = { confirmArchive = false; onArchive() }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmArchive = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            containerColor = ChalkColors.BoardDeep,
            titleContentColor = ChalkColors.Chalk,
            textContentColor = ChalkColors.Chalk,
        )
    }
}

@Composable
private fun DetailStat(title: String, value: String, modifier: Modifier = Modifier) {
    ChalkCard(modifier = modifier, contentPadding = 10.dp) {
        Text(title, color = ChalkColors.ChalkDim, style = MaterialTheme.typography.labelSmall)
        Text(value, color = ChalkColors.Mint, style = MaterialTheme.typography.titleSmall)
    }
}

private fun nailShapeLabelId(shape: String): Int = when (shape) {
    "almond" -> R.string.nail_shape_almond
    "square" -> R.string.nail_shape_square
    "round" -> R.string.nail_shape_round
    "stiletto" -> R.string.nail_shape_stiletto
    "oval" -> R.string.nail_shape_oval
    else -> R.string.nail_shape_unknown
}
