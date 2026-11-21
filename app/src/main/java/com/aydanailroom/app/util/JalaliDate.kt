package com.aydanailroom.app.util

import android.content.Context
import com.aydanailroom.app.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** A validated date in the Solar Hijri (Jalali) calendar. */
data class JalaliDate(val year: Int, val month: Int, val day: Int) : Comparable<JalaliDate> {
    init {
        require(month in 1..12) { "Month must be between 1 and 12" }
        require(day in 1..monthLength(year, month)) { "Day is outside this Jalali month" }
    }

    fun toGregorianDate(): LocalDate {
        val calendar = jalaliCalendar(year)
        val offset = if (month <= 6) {
            (month - 1) * 31 + day - 1
        } else {
            186 + (month - 7) * 30 + day - 1
        }
        return LocalDate.of(calendar.gregorianYear, 3, calendar.farvardinDay).plusDays(offset.toLong())
    }

    fun plusDays(days: Long): JalaliDate = fromGregorian(toGregorianDate().plusDays(days))

    fun format(context: Context, latinDigits: Boolean = false): String {
        val months = context.resources.getStringArray(R.array.jalali_months)
        return localizeDigits("$day ${months[month - 1]} $year", latinDigits)
    }

    override fun compareTo(other: JalaliDate): Int = toGregorianDate().compareTo(other.toGregorianDate())

    companion object {
        private val breaks = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
            1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
        )

        fun today(): JalaliDate = fromGregorian(LocalDate.now())

        fun fromGregorian(date: LocalDate): JalaliDate {
            var jy = date.year - 621
            var first = farvardinFirst(jy)
            if (date.isBefore(first)) {
                jy -= 1
                first = farvardinFirst(jy)
            } else {
                val next = farvardinFirst(jy + 1)
                if (!date.isBefore(next)) {
                    jy += 1
                    first = next
                }
            }
            val dayOfYear = ChronoUnit.DAYS.between(first, date).toInt()
            return if (dayOfYear < 186) {
                JalaliDate(jy, dayOfYear / 31 + 1, dayOfYear % 31 + 1)
            } else {
                val remainder = dayOfYear - 186
                JalaliDate(jy, remainder / 30 + 7, remainder % 30 + 1)
            }
        }

        fun monthLength(year: Int, month: Int): Int {
            require(month in 1..12) { "Month must be between 1 and 12" }
            return when (month) {
                in 1..6 -> 31
                in 7..11 -> 30
                else -> if (isLeapYear(year)) 30 else 29
            }
        }

        fun isLeapYear(year: Int): Boolean = jalaliCalendar(year).leap == 0

        fun firstDaySaturdayIndex(year: Int, month: Int): Int {
            val weekday = JalaliDate(year, month, 1).toGregorianDate().dayOfWeek.value
            return (weekday + 1) % 7 // java.time: Monday=1; Solar Hijri weeks start on Saturday.
        }

        private fun farvardinFirst(year: Int): LocalDate {
            val calendar = jalaliCalendar(year)
            return LocalDate.of(calendar.gregorianYear, 3, calendar.farvardinDay)
        }

        private fun jalaliCalendar(jy: Int): JalaliCalendar {
            require(jy >= breaks.first() && jy < breaks.last()) { "Jalali year is outside the supported range" }
            val gy = jy + 621
            var leapJ = -14
            var jp = breaks.first()
            var jm = 0
            var jump = 0
            for (index in 1 until breaks.size) {
                jm = breaks[index]
                jump = jm - jp
                if (jy < jm) break
                leapJ += Math.floorDiv(jump, 33) * 8 + Math.floorDiv(Math.floorMod(jump, 33), 4)
                jp = jm
            }
            val n = jy - jp
            leapJ += Math.floorDiv(n, 33) * 8 + Math.floorDiv(Math.floorMod(n, 33) + 3, 4)
            if (Math.floorMod(jump, 33) == 4 && jump - n == 4) leapJ += 1

            val leapG = Math.floorDiv(gy, 4) - Math.floorDiv((Math.floorDiv(gy, 100) + 1) * 3, 4) - 150
            val march = 20 + leapJ - leapG
            var remainder = n
            if (jump - n < 6) {
                remainder = n - jump + Math.floorDiv(jump + 4, 33) * 33
            }
            var leap = ((remainder + 1) % 33 - 1) % 4
            if (leap == -1) leap = 4
            return JalaliCalendar(leap, gy, march)
        }

        private data class JalaliCalendar(val leap: Int, val gregorianYear: Int, val farvardinDay: Int)
    }
}

fun localizeDigits(value: String, latinDigits: Boolean): String {
    if (latinDigits) return value
    val persian = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString(value.length) {
        value.forEach { char ->
            append(if (char in '0'..'9') persian[char - '0'] else char)
        }
    }
}
