package com.aydanailroom.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aydanailroom.app.R
import com.aydanailroom.app.data.AppointmentEntity
import com.aydanailroom.app.data.ClientEntity
import com.aydanailroom.app.data.TransactionEntity
import com.aydanailroom.app.ui.expenseCategoryName
import com.aydanailroom.app.ui.components.ChalkCard
import com.aydanailroom.app.ui.components.ChalkChip
import com.aydanailroom.app.ui.components.ChalkEmptyState
import com.aydanailroom.app.ui.theme.ChalkColors
import com.aydanailroom.app.util.JalaliDate
import com.aydanailroom.app.util.formatShortDate
import com.aydanailroom.app.util.formatToman
import com.aydanailroom.app.util.jalaliMonthRange
import com.aydanailroom.app.util.localizeDigits
import java.time.Instant
import java.time.ZoneId

@Composable
fun FinanceScreen(
    transactions: List<TransactionEntity>,
    appointments: List<AppointmentEntity>,
    clients: List<ClientEntity>,
    latinDigits: Boolean,
    onAddExpense: () -> Unit,
) {
    val context = LocalContext.current
    val current = remember { JalaliDate.today() }
    var year by rememberSaveable { mutableStateOf(current.year) }
    var month by rememberSaveable { mutableStateOf(current.month) }
    var debtorsMode by rememberSaveable { mutableStateOf(false) }
    val range = jalaliMonthRange(year, month)
    val monthTransactions = transactions.filter { it.date >= range.first && it.date < range.last + 1 }
        .sortedByDescending { it.date }
    val income = monthTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val expense = monthTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    val profit = income - expense
    val monthNames = stringArrayResource(R.array.jalali_months)
    val days = JalaliDate.monthLength(year, month)
    val dailyIncome = (1..days).map { day ->
        val date = JalaliDate(year, month, day).toGregorianDate()
        monthTransactions.filter { transaction ->
            transaction.type == "income" && Instant.ofEpochMilli(transaction.date).atZone(ZoneId.systemDefault()).toLocalDate() == date
        }.sumOf { it.amount }
    }
    val debtors = appointments.filter {
        it.status == "done" && (it.totalPrice - it.discount).coerceAtLeast(0L) > it.paidAmount
    }.sortedByDescending { it.startAt }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { if (month == 1) { month = 12; year -= 1 } else month -= 1 }) {
                Icon(Icons.Rounded.ArrowForwardIos, stringResource(R.string.a11y_previous_month), tint = ChalkColors.Chalk)
            }
            Text("${monthNames[month - 1]} ${localizeDigits(year.toString(), latinDigits)}", color = ChalkColors.Chalk, style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { if (month == 12) { month = 1; year += 1 } else month += 1 }) {
                Icon(Icons.Rounded.ArrowBackIosNew, stringResource(R.string.a11y_next_month), tint = ChalkColors.Chalk)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
            MetricCard(stringResource(R.string.finance_income), formatToman(context, income, latinDigits), ChalkColors.Mint, Modifier.weight(1f))
            MetricCard(stringResource(R.string.finance_expense), formatToman(context, expense, latinDigits), ChalkColors.Coral, Modifier.weight(1f))
            MetricCard(stringResource(R.string.finance_profit), formatToman(context, profit, latinDigits), if (profit >= 0) ChalkColors.Yellow else ChalkColors.Coral, Modifier.weight(1f))
        }
        if (dailyIncome.any { it > 0L }) {
            ChalkCard(contentPadding = 12.dp) {
                Text(stringResource(R.string.finance_chart_description), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.labelMedium)
                FinanceBarChart(dailyIncome, Modifier.fillMaxWidth().height(100.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)) {
            ChalkChip(stringResource(R.string.finance_transactions), !debtorsMode, { debtorsMode = false })
            ChalkChip(stringResource(R.string.finance_debtors), debtorsMode, { debtorsMode = true })
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onAddExpense) { Text(stringResource(R.string.action_add_expense)) }
            IconButton(onClick = {
                val content = monthTransactions.joinToString(separator = "\n") { transaction ->
                    "${formatShortDate(context, transaction.date, true)},${transaction.type},${transaction.category},${transaction.amount},${transaction.note.replace(',', ' ')}"
                }
                val send = Intent(Intent.ACTION_SEND).setType("text/csv").putExtra(Intent.EXTRA_TEXT, content)
                runCatching { context.startActivity(Intent.createChooser(send, context.getString(R.string.action_share))) }
            }) {
                Icon(Icons.Rounded.Share, contentDescription = stringResource(R.string.action_export_csv), tint = ChalkColors.Pink)
            }
        }
        if (debtorsMode) {
            if (debtors.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ChalkEmptyState(stringResource(R.string.finance_no_debtors), doodle = "✓")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 90.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(debtors, key = { it.id }) { appointment ->
                        val client = clients.firstOrNull { it.id == appointment.clientId }
                        val remaining = (appointment.totalPrice - appointment.discount - appointment.paidAmount).coerceAtLeast(0L)
                        ChalkCard(contentPadding = 12.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(client?.fullName.orEmpty(), color = ChalkColors.Chalk, style = MaterialTheme.typography.titleMedium)
                                    Text(formatShortDate(context, appointment.startAt, latinDigits), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall)
                                    Text(formatToman(context, remaining, latinDigits), color = ChalkColors.Coral, style = MaterialTheme.typography.labelLarge)
                                }
                                TextButton(onClick = {
                                    val message = context.getString(R.string.finance_payment_reminder_text, client?.fullName.orEmpty(), formatToman(context, remaining, latinDigits))
                                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, message)
                                    runCatching { context.startActivity(Intent.createChooser(send, context.getString(R.string.finance_payment_reminder))) }
                                }) { Text(stringResource(R.string.finance_payment_reminder)) }
                            }
                        }
                    }
                }
            }
        } else if (monthTransactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ChalkEmptyState(stringResource(R.string.finance_empty), doodle = "◉")
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 90.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(monthTransactions, key = { it.id }) { transaction ->
                    val isIncome = transaction.type == "income"
                    ChalkCard(contentPadding = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    expenseCategoryName(transaction.category),
                                    color = ChalkColors.Chalk,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    transaction.note.ifBlank { formatShortDate(context, transaction.date, latinDigits) },
                                    color = ChalkColors.ChalkDim,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                )
                            }
                            Text(
                                (if (isIncome) "+ " else "− ") + formatToman(context, transaction.amount, latinDigits),
                                color = if (isIncome) ChalkColors.Mint else ChalkColors.Coral,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    ChalkCard(modifier = modifier, contentPadding = 9.dp) {
        Text(title, color = ChalkColors.ChalkDim, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Text(value, color = accent, style = MaterialTheme.typography.labelMedium, maxLines = 2)
    }
}

@Composable
private fun FinanceBarChart(values: List<Long>, modifier: Modifier = Modifier) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    Canvas(modifier) {
        val gap = 3.dp.toPx()
        val barWidth = ((size.width - gap * (values.size + 1)) / values.size).coerceAtLeast(1f)
        val baseY = size.height - 2.dp.toPx()
        values.forEachIndexed { index, amount ->
            val height = (amount.toFloat() / maxValue.toFloat() * (size.height - 10.dp.toPx())).coerceAtLeast(if (amount > 0) 3.dp.toPx() else 0f)
            val left = gap + index * (barWidth + gap)
            if (amount > 0L) {
                drawRoundRect(
                    color = ChalkColors.Mint.copy(alpha = 0.78f),
                    topLeft = androidx.compose.ui.geometry.Offset(left, baseY - height),
                    size = androidx.compose.ui.geometry.Size(barWidth, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 3f),
                )
                val hatchCount = (barWidth / 4.dp.toPx()).toInt().coerceAtLeast(1)
                repeat(hatchCount) { hatch ->
                    val x = left + hatch * 4.dp.toPx()
                    drawLine(
                        color = ChalkColors.Chalk.copy(alpha = 0.42f),
                        start = androidx.compose.ui.geometry.Offset(x, baseY - 2.dp.toPx()),
                        end = androidx.compose.ui.geometry.Offset((x + 4.dp.toPx()).coerceAtMost(left + barWidth), baseY - height + 2.dp.toPx()),
                        strokeWidth = 0.8.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
        drawLine(
            color = ChalkColors.ChalkDim,
            start = androidx.compose.ui.geometry.Offset(0f, baseY),
            end = androidx.compose.ui.geometry.Offset(size.width, baseY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 4.dp.toPx())),
        )
    }
}
