package io.github.marshallhelenm.knimble.ui.timer

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatCountdownTest {
    @Test
    fun underAnHour_isMinutesAndSeconds() {
        assertEquals("29:59", formatCountdown(29 * 60_000L + 59_000))
    }

    @Test
    fun anHourOrMore_includesHours() {
        assertEquals("1:05:00", formatCountdown(65 * 60_000L))
    }

    @Test
    fun partSeconds_roundUpSoZeroMeansDone() {
        assertEquals("0:01", formatCountdown(1))
        assertEquals("0:00", formatCountdown(0))
    }
}
