package com.aydanailroom.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.aydanailroom.app.BuildConfig
import com.aydanailroom.app.R
import com.aydanailroom.app.data.ServiceEntity
import com.aydanailroom.app.ui.components.ChalkButton
import com.aydanailroom.app.ui.components.ChalkCard
import com.aydanailroom.app.ui.components.ChalkEmptyState
import com.aydanailroom.app.ui.components.ChalkField
import com.aydanailroom.app.ui.theme.ChalkColors
import com.aydanailroom.app.util.PriceMenuRenderer
import com.aydanailroom.app.util.formatDuration
import com.aydanailroom.app.util.formatToman
import com.aydanailroom.app.util.normalizeDigits
import com.aydanailroom.app.util.parseNonNegativeLong
import androidx.compose.ui.text.input.KeyboardOptions

@Composable
fun ServicesScreen(
    services: List<ServiceEntity>,
    latinDigits: Boolean,
    onAdd: () -> Unit,
    onUpdate: (ServiceEntity) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val context = LocalContext.current
    var editing by remember { mutableStateOf<ServiceEntity?>(null) }
    var deleting by remember { mutableStateOf<ServiceEntity?>(null) }
    val active = services.filter { it.isActive }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(stringResource(R.string.services_title), color = ChalkColors.Chalk, style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.tagline), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Rounded.MenuBook, contentDescription = stringResource(R.string.action_add_service), tint = ChalkColors.Pink)
            }
        }
        if (services.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                ChalkEmptyState(stringResource(R.string.services_empty), doodle = "✿")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(services, key = { it.id }) { service ->
                    ChalkCard(contentPadding = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f).clickable { editing = service }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(service.name, color = if (service.isActive) ChalkColors.Chalk else ChalkColors.ChalkDim, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.weight(1f))
                                    DottedLeader(Modifier.weight(1f).padding(horizontal = 8.dp))
                                    Text(formatToman(context, service.defaultPrice, latinDigits), color = ChalkColors.Yellow, style = MaterialTheme.typography.titleSmall)
                                }
                                Text(formatDuration(context, service.defaultDurationMin, latinDigits), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = service.isActive,
                                onCheckedChange = { onUpdate(service.copy(isActive = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ChalkColors.BoardDeep,
                                    checkedTrackColor = ChalkColors.Mint,
                                    uncheckedThumbColor = ChalkColors.ChalkDim,
                                    uncheckedTrackColor = ChalkColors.Board,
                                ),
                            )
                            IconButton(onClick = { deleting = service }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.action_delete), tint = ChalkColors.Coral)
                            }
                        }
                    }
                }
            }
        }
        ChalkButton(
            text = stringResource(R.string.services_price_menu),
            onClick = {
                runCatching {
                    val image = PriceMenuRenderer.render(context, active)
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", image)
                    val share = Intent(Intent.ACTION_SEND).setType("image/png")
                        .putExtra(Intent.EXTRA_STREAM, uri)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(Intent.createChooser(share, context.getString(R.string.services_price_menu)))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            secondary = true,
        )
    }

    editing?.let { service ->
        ServiceEditDialog(
            service = service,
            latinDigits = latinDigits,
            onDismiss = { editing = null },
            onSave = { updated -> onUpdate(updated); editing = null },
        )
    }
    deleting?.let { service ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.services_delete_confirm, service.name)) },
            confirmButton = { TextButton(onClick = { onDelete(service.id); deleting = null }) { Text(stringResource(R.string.action_delete), color = ChalkColors.Coral) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.action_cancel)) } },
            containerColor = ChalkColors.BoardDeep,
            titleContentColor = ChalkColors.Chalk,
            textContentColor = ChalkColors.Chalk,
        )
    }
}

@Composable
private fun DottedLeader(modifier: Modifier = Modifier) {
    Canvas(modifier.height(8.dp)) {
        drawLine(
            color = ChalkColors.ChalkDim.copy(alpha = 0.75f),
            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 4.dp.toPx())),
        )
    }
}

@Composable
private fun ServiceEditDialog(
    service: ServiceEntity,
    latinDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (ServiceEntity) -> Unit,
) {
    var name by rememberSaveable(service.id) { mutableStateOf(service.name) }
    var price by rememberSaveable(service.id) { mutableStateOf(service.defaultPrice.toString()) }
    var duration by rememberSaveable(service.id) { mutableStateOf(service.defaultDurationMin.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ChalkField(name, { name = it }, stringResource(R.string.label_name))
                ChalkField(price, { price = normalizeDigits(it).filter(Char::isDigit) }, stringResource(R.string.label_price_toman), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                ChalkField(duration, { duration = normalizeDigits(it).filter(Char::isDigit) }, stringResource(R.string.services_default_duration), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(service.copy(
                    name = name.trim().ifBlank { service.name },
                    defaultPrice = parseNonNegativeLong(price),
                    defaultDurationMin = parseNonNegativeLong(duration).toInt().coerceIn(15, 720),
                ))
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        containerColor = ChalkColors.BoardDeep,
        titleContentColor = ChalkColors.Chalk,
        textContentColor = ChalkColors.Chalk,
    )
}

@Composable
fun SettingsScreen(
    latinDigits: Boolean,
    onLatinDigitsChange: (Boolean) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onRequestNotifications: () -> Unit,
) {
    val context = LocalContext.current
    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.settings_title), color = ChalkColors.Chalk, style = MaterialTheme.typography.headlineSmall)
        ChalkCard(contentPadding = 14.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_digits), color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.settings_digits_summary), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = latinDigits,
                    onCheckedChange = onLatinDigitsChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = ChalkColors.BoardDeep, checkedTrackColor = ChalkColors.Pink),
                )
            }
        }
        ChalkCard(contentPadding = 14.dp) {
            Text(stringResource(R.string.settings_work_hours), color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.settings_work_hours_value), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodyMedium)
        }
        ChalkCard(contentPadding = 14.dp) {
            Text(stringResource(R.string.settings_notifications), color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(if (notificationsEnabled) R.string.settings_permission_granted else R.string.settings_notification_permission),
                color = if (notificationsEnabled) ChalkColors.Mint else ChalkColors.Coral,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            if (!notificationsEnabled) ChalkButton(stringResource(R.string.settings_grant_permission), onRequestNotifications, Modifier.fillMaxWidth())
        }
        ChalkCard(contentPadding = 14.dp) {
            Text(stringResource(R.string.settings_backup), color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.settings_backup_hint), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
            ChalkButton(stringResource(R.string.settings_export_backup), onExport, Modifier.fillMaxWidth())
            TextButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.settings_import_backup)) }
        }
        ChalkCard(contentPadding = 14.dp) {
            Text(stringResource(R.string.settings_about), color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.brand_name), color = ChalkColors.Pink, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.brand_handle), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall)
        }
    }
}
