package com.aydanailroom.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aydanailroom.app.R
import com.aydanailroom.app.data.AppointmentEntity
import com.aydanailroom.app.data.AppointmentServiceEntity
import com.aydanailroom.app.data.ClientEntity
import com.aydanailroom.app.data.ServiceEntity
import com.aydanailroom.app.data.TransactionEntity
import com.aydanailroom.app.ui.components.ChalkButton
import com.aydanailroom.app.ui.components.ChalkCard
import com.aydanailroom.app.ui.components.ChalkChip
import com.aydanailroom.app.ui.components.ChalkDivider
import com.aydanailroom.app.ui.components.ChalkEmptyState
import com.aydanailroom.app.ui.theme.ChalkColors
import com.aydanailroom.app.util.JalaliDate
import com.aydanailroom.app.util.formatClock
import com.aydanailroom.app.util.formatShortDate
import com.aydanailroom.app.util.formatToman
import com.aydanailroom.app.util.jalaliDayRange
import com.aydanailroom.app.util.jalaliMonthRange
import com.aydanailroom.app.util.localizeDigits
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HomeScreen(
    clients: List<ClientEntity>,
    services: List<ServiceEntity>,
    appointments: List<AppointmentEntity>,
    appointmentServices: List<AppointmentServiceEntity>,
    transactions: List<TransactionEntity>,
    latinDigits: Boolean,
    onAddAppointment: () -> Unit,
    onMarkDone: (AppointmentEntity) -> Unit,
    onCancel: (Long) -> Unit,
    onClientClick: (Long) -> Unit,
) {
    val context = LocalContext.current
    val today = remember { JalaliDate.today() }
    val weekdayIndex = (today.toGregorianDate().dayOfWeek.value + 1) % 7
    val weekdayNames = stringArrayResource(R.array.jalali_weekdays_full)
    val todayRange = jalaliDayRange(today)
    val todayAppointments = appointments
        .filter { it.startAt >= todayRange.first && it.startAt < todayRange.last + 1 && it.status != "cancelled" }
        .sortedBy { it.startAt }
    val currentMonthRange = jalaliMonthRange(today.year, today.month)
    val todayIncome = transactions.filter {
        it.type == "income" && it.date >= todayRange.first && it.date < todayRange.last + 1
    }.sumOf { it.amount }
    val monthIncome = transactions.filter {
        it.type == "income" && it.date >= currentMonthRange.first && it.date < currentMonthRange.last + 1
    }.sumOf { it.amount }
    val quote = remember {
        context.resources.getStringArray(R.array.motivational_lines).randomOrNull() ?: context.getString(R.string.today_motivation_1)
    }
    val now = System.currentTimeMillis()
    val nextAppointment = todayAppointments.firstOrNull { it.status == "booked" && it.startAt >= now }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp, top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
                Text(
                    stringResource(R.string.today_date_line, weekdayNames[weekdayIndex], today.format(context, latinDigits)),
                    color = ChalkColors.Chalk,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(quote, color = ChalkColors.Pink, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard(
                    title = stringResource(R.string.label_total_appointments),
                    value = localizeDigits(todayAppointments.count { it.status == "booked" }.toString(), latinDigits),
                    accent = ChalkColors.Yellow,
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    title = stringResource(R.string.label_total_income),
                    value = formatToman(context, todayIncome, latinDigits),
                    accent = ChalkColors.Mint,
                    modifier = Modifier.weight(1.15f),
                )
                SummaryCard(
                    title = stringResource(R.string.label_month_income),
                    value = formatToman(context, monthIncome, latinDigits),
                    accent = ChalkColors.Pink,
                    modifier = Modifier.weight(1.15f),
                )
            }
        }
        if (nextAppointment != null) {
            val nextClient = clients.firstOrNull { it.id == nextAppointment.clientId }
            item {
                ChalkCard(contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = ChalkColors.Yellow)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.today_next_appointment), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.labelMedium)
                            Text(nextClient?.fullName.orEmpty(), color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(formatClock(nextAppointment.startAt, latinDigits), color = ChalkColors.Yellow, style = MaterialTheme.typography.headlineSmall)
                    }
                    Spacer(Modifier.height(6.dp))
                    ChalkDivider(colorAlpha = 0.9f)
                }
            }
        }
        item {
            Text(stringResource(R.string.appointments_title), color = ChalkColors.Chalk, style = MaterialTheme.typography.headlineSmall)
        }
        if (todayAppointments.isEmpty()) {
            item {
                ChalkCard {
                    ChalkEmptyState(stringResource(R.string.today_empty), doodle = "✿")
                    ChalkButton(stringResource(R.string.action_add_appointment), onAddAppointment, Modifier.fillMaxWidth())
                }
            }
        } else {
            items(todayAppointments, key = { it.id }) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    client = clients.firstOrNull { it.id == appointment.clientId },
                    services = services.filter { service -> appointmentServices.any { it.appointmentId == appointment.id && it.serviceId == service.id } },
                    latinDigits = latinDigits,
                    onMarkDone = { onMarkDone(appointment) },
                    onCancel = { onCancel(appointment.id) },
                    onClientClick = { onClientClick(appointment.clientId) },
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    ChalkCard(modifier = modifier, contentPadding = 10.dp) {
        Text(title, color = ChalkColors.ChalkDim, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Text(value, color = accent, style = MaterialTheme.typography.titleSmall, maxLines = 2)
    }
}

@Composable
fun AppointmentsScreen(
    clients: List<ClientEntity>,
    services: List<ServiceEntity>,
    appointments: List<AppointmentEntity>,
    appointmentServices: List<AppointmentServiceEntity>,
    latinDigits: Boolean,
    onAdd: () -> Unit,
    onMarkDone: (AppointmentEntity) -> Unit,
    onCancel: (Long) -> Unit,
) {
    val today = remember { JalaliDate.today() }
    var listMode by rememberSaveable { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(today) }
    var calendarYear by rememberSaveable { mutableStateOf(today.year) }
    var calendarMonth by rememberSaveable { mutableStateOf(today.month) }
    val selectedRange = jalaliDayRange(selectedDate)
    val selectedAppointments = appointments.filter {
        it.startAt >= selectedRange.first && it.startAt < selectedRange.last + 1
    }.sortedBy { it.startAt }
    val allSorted = appointments.sortedBy { it.startAt }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChalkChip(stringResource(R.string.appointments_list), listMode, { listMode = true })
            ChalkChip(stringResource(R.string.appointments_month), !listMode, { listMode = false })
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onAdd) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.action_add_appointment), tint = ChalkColors.Pink)
            }
        }

        if (!listMode) {
            CalendarMonth(
                year = calendarYear,
                month = calendarMonth,
                selected = selectedDate,
                appointments = appointments,
                latinDigits = latinDigits,
                onPrevious = {
                    if (calendarMonth == 1) { calendarMonth = 12; calendarYear -= 1 } else calendarMonth -= 1
                },
                onNext = {
                    if (calendarMonth == 12) { calendarMonth = 1; calendarYear += 1 } else calendarMonth += 1
                },
                onSelectDate = { selectedDate = it },
            )
            Text(
                selectedDate.format(LocalContext.current, latinDigits),
                modifier = Modifier.padding(vertical = 8.dp),
                color = ChalkColors.Yellow,
                style = MaterialTheme.typography.titleMedium,
            )
            AppointmentList(
                appointments = selectedAppointments,
                clients = clients,
                services = services,
                links = appointmentServices,
                latinDigits = latinDigits,
                onMarkDone = onMarkDone,
                onCancel = onCancel,
            )
        } else {
            AppointmentList(
                appointments = allSorted,
                clients = clients,
                services = services,
                links = appointmentServices,
                latinDigits = latinDigits,
                onMarkDone = onMarkDone,
                onCancel = onCancel,
            )
        }
    }
}

@Composable
private fun AppointmentList(
    appointments: List<AppointmentEntity>,
    clients: List<ClientEntity>,
    services: List<ServiceEntity>,
    links: List<AppointmentServiceEntity>,
    latinDigits: Boolean,
    onMarkDone: (AppointmentEntity) -> Unit,
    onCancel: (Long) -> Unit,
) {
    if (appointments.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ChalkEmptyState(stringResource(R.string.appointments_empty), doodle = "◷")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(appointments, key = { it.id }) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    client = clients.firstOrNull { it.id == appointment.clientId },
                    services = services.filter { service -> links.any { it.appointmentId == appointment.id && it.serviceId == service.id } },
                    latinDigits = latinDigits,
                    onMarkDone = { onMarkDone(appointment) },
                    onCancel = { onCancel(appointment.id) },
                )
            }
        }
    }
}

@Composable
private fun CalendarMonth(
    year: Int,
    month: Int,
    selected: JalaliDate,
    appointments: List<AppointmentEntity>,
    latinDigits: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelectDate: (JalaliDate) -> Unit,
) {
    val context = LocalContext.current
    val months = stringArrayResource(R.array.jalali_months)
    val weekdays = stringArrayResource(R.array.jalali_weekdays_short)
    val offset = JalaliDate.firstDaySaturdayIndex(year, month)
    val count = JalaliDate.monthLength(year, month)
    val today = remember { JalaliDate.today() }
    val scheduledDays = remember(appointments, year, month) {
        appointments.filter { it.status == "booked" }.mapNotNull { appointment ->
            runCatching {
                JalaliDate.fromGregorian(Instant.ofEpochMilli(appointment.startAt).atZone(ZoneId.systemDefault()).toLocalDate())
            }.getOrNull()?.takeIf { it.year == year && it.month == month }?.day
        }.toSet()
    }
    ChalkCard(contentPadding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onPrevious) { Icon(Icons.Rounded.ArrowForwardIos, stringResource(R.string.a11y_previous_month), tint = ChalkColors.Chalk) }
            Text("${months[month - 1]} ${localizeDigits(year.toString(), latinDigits)}", color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onNext) { Icon(Icons.Rounded.ArrowBackIosNew, stringResource(R.string.a11y_next_month), tint = ChalkColors.Chalk) }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEachIndexed { index, day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                    color = if (index == 6) ChalkColors.Coral else ChalkColors.ChalkDim,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        repeat(6) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val day = row * 7 + column - offset + 1
                    if (day in 1..count) {
                        val date = JalaliDate(year, month, day)
                        val isSelected = date == selected
                        val isToday = date == today
                        Box(
                            modifier = Modifier.weight(1f).padding(2.dp).aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> ChalkColors.Pink.copy(alpha = 0.9f)
                                        isToday -> ChalkColors.Yellow.copy(alpha = 0.18f)
                                        else -> Color.Transparent
                                    },
                                )
                                .clickable { onSelectDate(date) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(
                                    localizeDigits(day.toString(), latinDigits),
                                    color = when {
                                        isSelected -> ChalkColors.BoardDeep
                                        column == 6 -> ChalkColors.Coral
                                        else -> ChalkColors.Chalk
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                                if (day in scheduledDays) {
                                    Box(Modifier.padding(top = 1.dp).size(4.dp).clip(CircleShape).background(if (isSelected) ChalkColors.BoardDeep else ChalkColors.Mint))
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: AppointmentEntity,
    client: ClientEntity?,
    services: List<ServiceEntity>,
    latinDigits: Boolean,
    onMarkDone: () -> Unit,
    onCancel: () -> Unit,
    onClientClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val statusText = stringResource(
        when (appointment.status) {
            "done" -> R.string.appointments_status_done
            "cancelled" -> R.string.appointments_status_cancelled
            "no_show" -> R.string.appointments_status_no_show
            else -> R.string.appointments_status_booked
        },
    )
    val statusColor = when (appointment.status) {
        "done" -> ChalkColors.Mint
        "cancelled", "no_show" -> ChalkColors.Coral
        else -> ChalkColors.Yellow
    }
    val endAt = appointment.startAt + appointment.durationMin * 60_000L
    ChalkCard(contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                "${formatClock(appointment.startAt, latinDigits)} – ${formatClock(endAt, latinDigits)}",
                color = ChalkColors.Yellow,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(statusText, color = statusColor, style = MaterialTheme.typography.labelMedium)
        }
        Text(
            client?.fullName ?: stringResource(R.string.generic_not_available),
            modifier = Modifier.padding(top = 4.dp).then(if (onClientClick != null) Modifier.clickable(onClick = onClientClick) else Modifier),
            color = ChalkColors.Chalk,
            style = MaterialTheme.typography.headlineSmall,
        )
        if (services.isNotEmpty()) {
            Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                services.take(3).forEach { service ->
                    Surface(color = ChalkColors.BoardLight, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, ChalkColors.ChalkDim.copy(alpha = 0.45f))) {
                        Text(service.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (services.size > 3) Text("+${localizeDigits((services.size - 3).toString(), latinDigits)}", color = ChalkColors.ChalkDim)
            }
        }
        if (appointment.notes.isNotBlank()) {
            Text(appointment.notes, modifier = Modifier.padding(top = 6.dp), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(formatToman(context, (appointment.totalPrice - appointment.discount).coerceAtLeast(0L), latinDigits), color = ChalkColors.Mint, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            if (appointment.status == "booked") {
                androidx.compose.material3.IconButton(onClick = {
                    val message = context.getString(
                        R.string.appointments_reminder_message,
                        client?.fullName.orEmpty(),
                        formatShortDate(context, appointment.startAt, latinDigits),
                        formatClock(appointment.startAt, latinDigits),
                    )
                    val share = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, message)
                    runCatching { context.startActivity(Intent.createChooser(share, context.getString(R.string.action_share))) }
                }) {
                    Icon(Icons.Rounded.Share, contentDescription = stringResource(R.string.action_share), tint = ChalkColors.Pink, modifier = Modifier.size(18.dp))
                }
                TextButton(onClick = onCancel) {
                    Icon(Icons.Rounded.Close, contentDescription = null, tint = ChalkColors.Coral, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.action_cancel), color = ChalkColors.Coral, style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = onMarkDone) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = ChalkColors.Mint, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.action_done), color = ChalkColors.Mint, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
