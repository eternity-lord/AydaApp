package com.aydanailroom.app.util

import com.aydanailroom.app.data.AppointmentEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppointmentRulesTest {
    private val minute = 60_000L

    @Test
    fun adjacentAppointmentsDoNotConflictButOverlappingOnesDo() {
        assertFalse(AppointmentRules.overlaps(9 * 60 * minute, 60, 10 * 60 * minute, 45))
        assertTrue(AppointmentRules.overlaps(9 * 60 * minute, 75, 10 * 60 * minute, 45))
    }

    @Test
    fun freeSlotFinderSkipsBookedTimeAndUsesQuarterHours() {
        val dayStart = 9 * 60 * minute
        val dayEnd = 12 * 60 * minute
        val booked = AppointmentEntity(
            id = 1,
            clientId = 1,
            startAt = 9 * 60 * minute,
            durationMin = 60,
        )
        assertEquals(10 * 60 * minute, AppointmentRules.findNextFreeSlot(dayStart, dayEnd, 45, listOf(booked)))
        assertNull(AppointmentRules.findNextFreeSlot(dayStart, 9 * 60 * minute + 30 * minute, 45, listOf(booked)))
    }

    @Test
    fun paymentMathNeverReturnsNegativeAmounts() {
        assertEquals(700L, PriceMath.payable(1_000L, 300L))
        assertEquals(0L, PriceMath.payable(100L, 200L))
        assertEquals(200L, PriceMath.remaining(1_000L, 100L, 700L))
        assertEquals(0L, PriceMath.remaining(100L, 0L, 200L))
    }
}
