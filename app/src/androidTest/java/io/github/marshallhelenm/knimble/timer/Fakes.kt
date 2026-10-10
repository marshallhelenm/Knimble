package io.github.marshallhelenm.knimble.timer

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

// Records the one armed alarm instead of touching AlarmManager.
class FakeAlarms : AlarmScheduler {
    var armedAt: Long? = null

    override fun schedule(atMillis: Long) {
        armedAt = atMillis
    }

    override fun cancel() {
        armedAt = null
    }
}

class MutableClock(var millis: Long) : Clock() {
    override fun instant(): Instant = Instant.ofEpochMilli(millis)
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId?): Clock = this

    fun advanceMinutes(minutes: Long) {
        millis += minutes * 60_000
    }
}
