package com.aydanailroom.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class JalaliDateTest {
    @Test
    fun knownNowruzDatesConvertInBothDirections() {
        assertEquals(LocalDate.of(2024, 3, 20), JalaliDate(1403, 1, 1).toGregorianDate())
        assertEquals(JalaliDate(1403, 1, 1), JalaliDate.fromGregorian(LocalDate.of(2024, 3, 20)))
        assertEquals(JalaliDate(1400, 1, 1), JalaliDate.fromGregorian(LocalDate.of(2021, 3, 21)))
    }

    @Test
    fun leapYearsHaveEsfandThirtyAndNonLeapYearsDoNot() {
        assertTrue(JalaliDate.isLeapYear(1399))
        assertTrue(JalaliDate.isLeapYear(1403))
        assertFalse(JalaliDate.isLeapYear(1400))
        assertEquals(30, JalaliDate.monthLength(1399, 12))
        assertEquals(LocalDate.of(2021, 3, 20), JalaliDate(1399, 12, 30).toGregorianDate())
        assertEquals(29, JalaliDate.monthLength(1400, 12))
        assertEquals(LocalDate.of(2022, 3, 20), JalaliDate(1400, 12, 29).toGregorianDate())
    }

    @Test
    fun yearBoundaryAndAllDaysRoundTrip() {
        assertEquals(LocalDate.of(2024, 3, 19), JalaliDate(1402, 12, 29).toGregorianDate())
        assertEquals(JalaliDate(1403, 1, 1), JalaliDate(1402, 12, 29).plusDays(1))

        for (year in 1380..1420) {
            for (month in 1..12) {
                for (day in 1..JalaliDate.monthLength(year, month)) {
                    val expected = JalaliDate(year, month, day)
                    assertEquals(expected, JalaliDate.fromGregorian(expected.toGregorianDate()))
                }
            }
        }
    }
}
