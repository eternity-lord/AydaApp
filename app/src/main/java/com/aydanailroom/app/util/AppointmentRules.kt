package com.aydanailroom.app.util

import com.aydanailroom.app.data.AppointmentEntity
import kotlin.math.max

object PriceMath {
    fun payable(totalPrice: Long, discount: Long): Long = (totalPrice - discount).coerceAtLeast(0L)

    fun remaining(totalPrice: Long, discount: Long, paidAmount: Long): Long =
        (payable(totalPrice, discount) - paidAmount).coerceAtLeast(0L)
}

object AppointmentRules {
    private const val MINUTE_MILLIS = 60_000L
    private const val SLOT_STEP_MINUTES = 15

    fun overlaps(startAt: Long, durationMinutes: Int, otherStartAt: Long, otherDurationMinutes: Int): Boolean {
        val endAt = startAt + durationMinutes.coerceAtLeast(0) * MINUTE_MILLIS
        val otherEndAt = otherStartAt + otherDurationMinutes.coerceAtLeast(0) * MINUTE_MILLIS
        return startAt < otherEndAt && otherStartAt < endAt
    }

    fun findConflict(
        startAt: Long,
        durationMinutes: Int,
        appointments: List<AppointmentEntity>,
        excludingAppointmentId: Long? = null,
    ): AppointmentEntity? = appointments.firstOrNull { appointment ->
        appointment.id != excludingAppointmentId && appointment.status == "booked" &&
            overlaps(startAt, durationMinutes, appointment.startAt, appointment.durationMin)
    }

    /** Returns the earliest 15-minute-aligned free start inside [dayStart, dayEnd]. */
    fun findNextFreeSlot(
        dayStart: Long,
        dayEnd: Long,
        durationMinutes: Int,
        appointments: List<AppointmentEntity>,
    ): Long? {
        if (durationMinutes <= 0) return null
        val duration = durationMinutes * MINUTE_MILLIS
        var candidate = roundUpToStep(dayStart)
        val sorted = appointments.filter { it.status == "booked" }.sortedBy { it.startAt }
        while (candidate + duration <= dayEnd) {
            val conflict = sorted.firstOrNull { overlaps(candidate, durationMinutes, it.startAt, it.durationMin) }
            if (conflict == null) return candidate
            val conflictEnd = conflict.startAt + conflict.durationMin.coerceAtLeast(0) * MINUTE_MILLIS
            candidate = roundUpToStep(max(candidate + SLOT_STEP_MINUTES * MINUTE_MILLIS, conflictEnd))
        }
        return null
    }

    private fun roundUpToStep(time: Long): Long {
        val stepMillis = SLOT_STEP_MINUTES * MINUTE_MILLIS
        val remainder = Math.floorMod(time, stepMillis)
        return if (remainder == 0L) time else time + stepMillis - remainder
    }
}
