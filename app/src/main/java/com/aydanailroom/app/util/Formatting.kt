package com.aydanailroom.app.util

import android.content.Context
import com.aydanailroom.app.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

fun formatToman(context: Context, amount: Long, latinDigits: Boolean = false): String {
    val negative = amount < 0
    val digits = amount.absoluteValue.toString()
    val grouped = digits.reversed().chunked(3).joinToString("٬").reversed()
    val signed = (if (negative) "−" else "") + grouped
    return "${localizeDigits(signed, latinDigits)} ${context.getString(R.string.generic_toman)}"
}

fun formatClock(epochMillis: Long, latinDigits: Boolean = false, zone: ZoneId = ZoneId.systemDefault()): String {
    val time = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
        .format(Instant.ofEpochMilli(epochMillis).atZone(zone))
    return localizeDigits(time, latinDigits)
}

fun formatDuration(context: Context, minutes: Int, latinDigits: Boolean = false): String =
    "${localizeDigits(minutes.toString(), latinDigits)} ${context.getString(R.string.generic_minutes)}"

fun formatShortDate(context: Context, epochMillis: Long, latinDigits: Boolean = false): String =
    JalaliDate.fromGregorian(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate())
        .format(context, latinDigits)

fun jalaliDayRange(date: JalaliDate, zone: ZoneId = ZoneId.systemDefault()): LongRange {
    val start = date.toGregorianDate().atStartOfDay(zone).toInstant().toEpochMilli()
    val end = date.toGregorianDate().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return start until end
}

fun jalaliMonthRange(year: Int, month: Int, zone: ZoneId = ZoneId.systemDefault()): LongRange {
    val first = JalaliDate(year, month, 1).toGregorianDate()
    val next = if (month == 12) JalaliDate(year + 1, 1, 1).toGregorianDate() else JalaliDate(year, month + 1, 1).toGregorianDate()
    val start = first.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = next.atStartOfDay(zone).toInstant().toEpochMilli()
    return start until end
}

fun monthName(context: Context, month: Int): String =
    context.resources.getStringArray(R.array.jalali_months).getOrElse(month - 1) { "" }

fun normalizeDigits(input: String): String = buildString(input.length) {
    input.forEach { char ->
        when (char) {
            in '۰'..'۹' -> append(('0'.code + (char - '۰')).toChar())
            in '٠'..'٩' -> append(('0'.code + (char - '٠')).toChar())
            else -> append(char)
        }
    }
}

fun parseNonNegativeLong(input: String): Long = normalizeDigits(input).filter(Char::isDigit).toLongOrNull() ?: 0L

fun currentGregorianDate(): LocalDate = LocalDate.now()
