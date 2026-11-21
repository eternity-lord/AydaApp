package com.aydanailroom.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aydanailroom.app.R
import com.aydanailroom.app.data.AppointmentEntity
import com.aydanailroom.app.data.ClientEntity
import com.aydanailroom.app.data.DesignRecordEntity
import com.aydanailroom.app.data.ExpenseCategoryEntity
import com.aydanailroom.app.data.ServiceEntity
import com.aydanailroom.app.ui.components.ChalkButton
import com.aydanailroom.app.ui.components.ChalkCard
import com.aydanailroom.app.ui.components.ChalkChip
import com.aydanailroom.app.ui.components.ChalkField
import com.aydanailroom.app.ui.theme.ChalkColors
import com.aydanailroom.app.util.JalaliDate
import com.aydanailroom.app.util.formatClock
import com.aydanailroom.app.util.formatToman
import com.aydanailroom.app.util.normalizeDigits
import com.aydanailroom.app.util.parseNonNegativeLong
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

@Composable
fun ClientEditorDialog(
    latinDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var allergies by rememberSaveable { mutableStateOf("") }
    var shape by rememberSaveable { mutableStateOf("unknown") }
    var error by remember { mutableStateOf(false) }
    val shapeOptions = listOf(
        "almond" to R.string.nail_shape_almond,
        "square" to R.string.nail_shape_square,
        "round" to R.string.nail_shape_round,
        "stiletto" to R.string.nail_shape_stiletto,
        "oval" to R.string.nail_shape_oval,
        "unknown" to R.string.nail_shape_unknown,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_client_title), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ChalkField(name, { name = it; error = false }, stringResource(R.string.label_name))
                ChalkField(
                    phone,
                    { phone = it },
                    stringResource(R.string.label_phone),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                Text(stringResource(R.string.label_nail_shape), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    shapeOptions.take(3).forEach { (key, label) ->
                        ChalkChip(stringResource(label), shape == key, { shape = key })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    shapeOptions.drop(3).forEach { (key, label) ->
                        ChalkChip(stringResource(label), shape == key, { shape = key })
                    }
                }
                ChalkField(notes, { notes = it }, stringResource(R.string.label_notes), singleLine = false)
                ChalkField(allergies, { allergies = it }, stringResource(R.string.label_allergies), singleLine = false)
                if (error) Text(stringResource(R.string.clients_name_required), color = ChalkColors.Coral, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) error = true else onSave(name, phone, notes, shape, allergies)
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        containerColor = ChalkColors.BoardDeep,
        titleContentColor = ChalkColors.Chalk,
        textContentColor = ChalkColors.Chalk,
    )
}

@Composable
fun AppointmentEditorDialog(
    clients: List<ClientEntity>,
    services: List<ServiceEntity>,
    appointments: List<AppointmentEntity>,
    latinDigits: Boolean,
    initialNotes: String = "",
    onDismiss: () -> Unit,
    onFindNextSlot: (JalaliDate, Int) -> Long?,
    onSave: (AppointmentEntity, List<ServiceEntity>) -> Unit,
    onAddClient: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedClientId by rememberSaveable { mutableStateOf(clients.firstOrNull()?.id ?: 0L) }
    var selectedServiceIds by remember { mutableStateOf(emptySet<Long>()) }
    var selectedDate by remember { mutableStateOf(JalaliDate.today()) }
    var hour by rememberSaveable { mutableStateOf(LocalDateTime.now().hour) }
    var minute by rememberSaveable { mutableStateOf((LocalDateTime.now().minute / 15) * 15) }
    var discountText by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable(initialNotes) { mutableStateOf(initialNotes) }
    var selectedReminder by rememberSaveable { mutableStateOf<Int?>(60) }
    var showClientMenu by remember { mutableStateOf(false) }
    var showReminderMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pendingConflict by remember { mutableStateOf<AppointmentEntity?>(null) }
    var inputError by remember { mutableStateOf(false) }

    val selectedClient = clients.firstOrNull { it.id == selectedClientId }
    val selectedServices = services.filter { it.id in selectedServiceIds }
    val duration = selectedServices.sumOf { it.defaultDurationMin }.coerceAtLeast(15)
    val totalPrice = selectedServices.sumOf { it.defaultPrice }
    val discount = parseNonNegativeLong(discountText).coerceAtMost(totalPrice)
    val startAt = runCatching {
        LocalDateTime.of(selectedDate.toGregorianDate(), java.time.LocalTime.of(hour, minute))
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.getOrDefault(System.currentTimeMillis())

    val saveAppointment: () -> Unit = {
        if (selectedClient == null || selectedServices.isEmpty()) {
            inputError = true
        } else {
            val reminderMinutes = when (selectedReminder) {
                null -> null
                -1 -> {
                    val previousEvening = selectedDate.toGregorianDate().minusDays(1).atTime(20, 0)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    ((startAt - previousEvening) / 60_000L).toInt().coerceAtLeast(0)
                }
                else -> selectedReminder
            }
            onSave(
                AppointmentEntity(
                    clientId = selectedClient.id,
                    startAt = startAt,
                    durationMin = duration,
                    notes = notes.trim(),
                    reminderMinutesBefore = reminderMinutes,
                    totalPrice = totalPrice,
                    discount = discount,
                ),
                selectedServices,
            )
        }
    }
    val submit: (Boolean) -> Unit = { force ->
        if (selectedClient == null || selectedServices.isEmpty()) {
            inputError = true
        } else {
            val conflict = if (force) null else appointments.firstOrNull {
                it.status == "booked" && startAt < it.startAt + it.durationMin * 60_000L &&
                    it.startAt < startAt + duration * 60_000L
            }
            if (conflict != null) pendingConflict = conflict else saveAppointment()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_appointment_title), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (clients.isEmpty()) {
                    Text(stringResource(R.string.appointments_no_client), color = ChalkColors.ChalkDim)
                    ChalkButton(stringResource(R.string.action_add_client), onAddClient, Modifier.fillMaxWidth())
                } else {
                    Box {
                        Surface(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { showClientMenu = true },
                            shape = RoundedCornerShape(16.dp),
                            color = ChalkColors.BoardLight,
                        ) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedClient?.fullName.orEmpty(), color = ChalkColors.Chalk, modifier = Modifier.weight(1f))
                                Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = ChalkColors.Pink)
                            }
                        }
                        DropdownMenu(expanded = showClientMenu, onDismissRequest = { showClientMenu = false }) {
                            clients.forEach { client ->
                                DropdownMenuItem(
                                    text = { Text(client.fullName) },
                                    onClick = { selectedClientId = client.id; showClientMenu = false },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_add_client)) },
                                onClick = { showClientMenu = false; onAddClient() },
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ChalkButton(
                        text = selectedDate.format(context, latinDigits),
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        secondary = true,
                    )
                    TimeAdjuster(
                        value = formatClock(startAt, latinDigits),
                        hour = hour,
                        minute = minute,
                        onHourChange = { hour = it.coerceIn(0, 23) },
                        onMinuteStep = { step ->
                            val totalMinutes = hour * 60 + minute + step
                            hour = Math.floorMod(totalMinutes / 60, 24)
                            minute = Math.floorMod(totalMinutes, 60)
                        },
                    )
                }

                if (services.isEmpty()) {
                    Text(stringResource(R.string.services_empty), color = ChalkColors.ChalkDim)
                } else {
                    Text(stringResource(R.string.label_services), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.labelLarge)
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(services.size) { index ->
                            val service = services[index]
                            ChalkChip(
                                text = service.name,
                                selected = service.id in selectedServiceIds,
                                onClick = {
                                    selectedServiceIds = if (service.id in selectedServiceIds) selectedServiceIds - service.id else selectedServiceIds + service.id
                                },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(formatToman(context, totalPrice, latinDigits), color = ChalkColors.Mint, style = MaterialTheme.typography.titleSmall)
                        Text(formatDurationLabel(context, duration, latinDigits), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChalkField(
                        value = discountText,
                        onValueChange = { discountText = normalizeDigits(it).filter(Char::isDigit) },
                        label = stringResource(R.string.label_discount),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        ChalkButton(
                            text = stringResource(reminderLabel(selectedReminder)),
                            onClick = { showReminderMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            secondary = true,
                        )
                        DropdownMenu(expanded = showReminderMenu, onDismissRequest = { showReminderMenu = false }) {
                            listOf<Int?>(null, 60, 180, -1).forEach { value ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(reminderLabel(value))) },
                                    onClick = { selectedReminder = value; showReminderMenu = false },
                                )
                            }
                        }
                    }
                }
                ChalkField(notes, { notes = it }, stringResource(R.string.label_notes), singleLine = false)
                val freeSlot = onFindNextSlot(selectedDate, duration)
                if (freeSlot != null) {
                    TextButton(onClick = {
                        val local = Instant.ofEpochMilli(freeSlot).atZone(ZoneId.systemDefault()).toLocalDateTime()
                        selectedDate = JalaliDate.fromGregorian(local.toLocalDate())
                        hour = local.hour
                        minute = (local.minute / 15) * 15
                        pendingConflict = null
                    }) { Text(stringResource(R.string.appointments_free_slot)) }
                } else {
                    TextButton(onClick = { inputError = false }) { Text(stringResource(R.string.appointments_no_free_slot), color = ChalkColors.ChalkDim) }
                }
                if (inputError) Text(
                    text = stringResource(if (selectedClient == null) R.string.appointments_no_client else R.string.generic_error),
                    color = ChalkColors.Coral,
                    style = MaterialTheme.typography.bodySmall,
                )
                pendingConflict?.let { conflict ->
                    val conflictClient = clients.firstOrNull { it.id == conflict.clientId }?.fullName.orEmpty()
                    ChalkCard(contentPadding = 12.dp) {
                        Text(
                            stringResource(R.string.appointments_conflict, conflictClient),
                            color = ChalkColors.Coral,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            TextButton(onClick = { pendingConflict = null }) { Text(stringResource(R.string.action_cancel)) }
                            TextButton(onClick = { submit(true) }) { Text(stringResource(R.string.action_confirm), color = ChalkColors.Pink) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { submit(false) }, enabled = clients.isNotEmpty() && selectedServices.isNotEmpty()) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        containerColor = ChalkColors.BoardDeep,
        titleContentColor = ChalkColors.Chalk,
        textContentColor = ChalkColors.Chalk,
    )

    if (showDatePicker) {
        JalaliDatePickerDialog(
            selected = selectedDate,
            latinDigits = latinDigits,
            onDismiss = { showDatePicker = false },
            onSelect = { selectedDate = it; showDatePicker = false },
        )
    }

}

@Composable
private fun TimeAdjuster(
    value: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteStep: (Int) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = ChalkColors.Yellow, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onHourChange(hour - 1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Remove, contentDescription = stringResource(R.string.label_hour), tint = ChalkColors.ChalkDim)
            }
            IconButton(onClick = { onHourChange(hour + 1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.label_hour), tint = ChalkColors.ChalkDim)
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = { onMinuteStep(-15) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Remove, contentDescription = stringResource(R.string.label_minute), tint = ChalkColors.ChalkDim)
            }
            IconButton(onClick = { onMinuteStep(15) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.label_minute), tint = ChalkColors.ChalkDim)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JalaliDatePickerDialog(
    selected: JalaliDate,
    latinDigits: Boolean,
    onDismiss: () -> Unit,
    onSelect: (JalaliDate) -> Unit,
) {
    var year by remember(selected.year) { mutableStateOf(selected.year) }
    var month by remember(selected.month) { mutableStateOf(selected.month) }
    val weekdays = stringArrayResource(R.array.jalali_weekdays_short)
    val today = remember { JalaliDate.today() }
    val firstOffset = JalaliDate.firstDaySaturdayIndex(year, month)
    val dayCount = JalaliDate.monthLength(year, month)
    val cells = (0 until 42).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_date_title), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = {
                        if (month == 1) { month = 12; year -= 1 } else month -= 1
                    }) { Icon(Icons.Rounded.ArrowForwardIos, stringResource(R.string.a11y_previous_month), tint = ChalkColors.Chalk) }
                    val months = stringArrayResource(R.array.jalali_months)
                    Text("${months[month - 1]} ${com.aydanailroom.app.util.localizeDigits(year.toString(), latinDigits)}", color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = {
                        if (month == 12) { month = 1; year += 1 } else month += 1
                    }) { Icon(Icons.Rounded.ArrowBackIosNew, stringResource(R.string.a11y_next_month), tint = ChalkColors.Chalk) }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekdays.forEachIndexed { index, day ->
                        Text(
                            day,
                            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                            color = if (index == 6) ChalkColors.Coral else ChalkColors.ChalkDim,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                ) {
                    items(cells) { index ->
                        val day = index - firstOffset + 1
                        if (day in 1..dayCount) {
                            val isSelected = year == selected.year && month == selected.month && day == selected.day
                            val isToday = year == today.year && month == today.month && day == today.day
                            val weekday = index % 7
                            Box(
                                modifier = Modifier.padding(2.dp).size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> ChalkColors.Pink.copy(alpha = 0.86f)
                                            isToday -> ChalkColors.Yellow.copy(alpha = 0.2f)
                                            else -> ChalkColors.BoardDeep
                                        },
                                    )
                                    .clickable { onSelect(JalaliDate(year, month, day)) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    com.aydanailroom.app.util.localizeDigits(day.toString(), latinDigits),
                                    color = when {
                                        isSelected -> ChalkColors.BoardDeep
                                        weekday == 6 -> ChalkColors.Coral
                                        else -> ChalkColors.Chalk
                                    },
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        } else {
                            Spacer(Modifier.size(42.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) } },
        containerColor = ChalkColors.BoardDeep,
        titleContentColor = ChalkColors.Chalk,
        textContentColor = ChalkColors.Chalk,
    )
}

@Composable
fun PaymentEditorDialog(
    appointment: AppointmentEntity,
    latinDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val payable = (appointment.totalPrice - appointment.discount).coerceAtLeast(0L)
    var amount by rememberSaveable(appointment.id) { mutableStateOf(payable.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_payment_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.label_remaining) + ": " + formatToman(context, payable, latinDigits), color = ChalkColors.Mint)
                ChalkField(
                    amount,
                    { amount = normalizeDigits(it).filter(Char::isDigit) },
                    stringResource(R.string.label_paid),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(parseNonNegativeLong(amount).coerceAtMost(payable)) }) { Text(stringResource(R.string.action_done)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        containerColor = ChalkColors.BoardDeep,
        titleContentColor = ChalkColors.Chalk,
        textContentColor = ChalkColors.Chalk,
    )
}

@Composable
fun ExpenseEditorDialog(
    categories: List<ExpenseCategoryEntity>,
    latinDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (Long, String, String) -> Unit,
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(categories.firstOrNull()?.key ?: "other") }
    var expanded by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_expense_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ChalkField(
                    amount,
                    { amount = normalizeDigits(it).filter(Char::isDigit) },
                    stringResource(R.string.label_price_toman),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Box {
                    ChalkButton(
                        expenseCategoryName(category),
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        secondary = true,
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(expenseCategoryName(option.key)) },
                                onClick = { category = option.key; expanded = false },
                            )
                        }
                    }
                }
                ChalkField(note, { note = it }, stringResource(R.string.label_notes))
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(parseNonNegativeLong(amount), category, note) }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        containerColor = ChalkColors.BoardDeep,
        titleContentColor = ChalkColors.Chalk,
        textContentColor = ChalkColors.Chalk,
    )
}

@Composable
fun ServiceEditorDialog(
    latinDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Long, Int) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf("60") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_service_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ChalkField(name, { name = it }, stringResource(R.string.label_name))
                ChalkField(price, { price = normalizeDigits(it).filter(Char::isDigit) }, stringResource(R.string.label_price_toman), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                ChalkField(duration, { duration = normalizeDigits(it).filter(Char::isDigit) }, stringResource(R.string.services_default_duration), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, parseNonNegativeLong(price), parseNonNegativeLong(duration).toInt().coerceAtLeast(15)) }) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        containerColor = ChalkColors.BoardDeep,
        titleContentColor = ChalkColors.Chalk,
        textContentColor = ChalkColors.Chalk,
    )
}

@Composable
fun DesignEditorDialog(
    client: ClientEntity,
    onDismiss: () -> Unit,
    onSave: (DesignRecordEntity) -> Unit,
) {
    var colors by rememberSaveable { mutableStateOf("") }
    var products by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    var feedback by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_design_title)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ChalkField(colors, { colors = it }, stringResource(R.string.label_colors))
                ChalkField(products, { products = it }, stringResource(R.string.label_products))
                ChalkField(description, { description = it }, stringResource(R.string.label_design_description), singleLine = false)
                ChalkField(tags, { tags = it }, stringResource(R.string.label_tags))
                ChalkField(feedback, { feedback = it }, stringResource(R.string.label_feedback), singleLine = false)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    DesignRecordEntity(
                        clientId = client.id,
                        colorsText = colors.trim(),
                        brandProductText = products.trim(),
                        designDescription = description.trim(),
                        tags = tags.trim(),
                        clientFeedback = feedback.trim(),
                    ),
                )
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        containerColor = ChalkColors.BoardDeep,
        titleContentColor = ChalkColors.Chalk,
        textContentColor = ChalkColors.Chalk,
    )
}

@Composable
private fun reminderLabel(value: Int?): Int = when (value) {
    null -> R.string.reminder_none
    60 -> R.string.reminder_1_hour
    180 -> R.string.reminder_3_hours
    -1 -> R.string.reminder_night_before
    else -> R.string.label_reminder
}

@Composable
private fun formatDurationLabel(context: android.content.Context, minutes: Int, latinDigits: Boolean): String =
    "${com.aydanailroom.app.util.localizeDigits(minutes.toString(), latinDigits)} ${context.getString(R.string.generic_minutes)}"
