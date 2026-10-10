package io.github.marshallhelenm.knimble.timer

import android.content.Context
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.exercises.ExercisePicker
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier
import io.github.marshallhelenm.knimble.model.TimerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import kotlin.random.Random

/** What to show in the break notification. [exercise] is null when everything in the timer's tiers is switched off. */
data class BreakAlert(val exercise: Exercise?)

class BreakTimer(
    db: KnimbleDatabase,
    private val alarms: AlarmScheduler,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val random: Random = Random.Default
) {
    private val states = db.timerStateDao()
    private val exercises = db.exerciseDao()

    fun observe(): Flow<TimerState> = states.observe().map { it ?: TimerState() }

    suspend fun current(): TimerState = states.get() ?: TimerState()

    /** Starting a timer also ends any break in progress. */
    suspend fun start(durationMinutes: Int, tiers: Set<Tier>) {
        val endsAt = clock.millis() + durationMinutes * 60_000L
        states.save(
            current().copy(
                endsAt = endsAt, durationMinutes = durationMinutes, tiers = tiers,
                onBreak = false, ringing = false, breakExerciseId = null
            )
        )
        alarms.schedule(endsAt)
    }

    suspend fun cancel() {
        states.save(current().copy(endsAt = null))
        alarms.cancel()
    }

    /**
     * Called when the alarm fires. When the timer has just ended, picks the break's exercise and starts ringing;
     * while ringing, repeats. Either way the next repeat is armed a minute out. Null means nothing to alert
     * (a stale alarm), and the right alarm is re-armed instead.
     */
    suspend fun onAlarm(): BreakAlert? {
        val state = current()
        val now = clock.millis()
        val ringing = when {
            state.ringing -> state
            state.endsAt != null && now >= state.endsAt -> {
                val picked = ExercisePicker.pick(exercises.getAll(), state.tiers, state.lastExerciseId, random)
                state.copy(
                    endsAt = null, onBreak = true, ringing = true, breakExerciseId = picked?.id,
                    lastExerciseId = picked?.id ?: state.lastExerciseId
                )
            }
            else -> {
                rearm()
                return null
            }
        }
        // Armed before saving: an open app reacts to the save by stopping the ringing, and its cancel must come last.
        alarms.schedule(now + REPEAT_MILLIS)
        states.save(ringing)
        return BreakAlert(ringing.breakExerciseId?.let { exercises.getById(it) })
    }

    /** The user has seen the break, so the repeats stop. The break and its exercise stay until it is finished. */
    suspend fun stopRinging() {
        val state = current()
        if (!state.ringing) return
        states.save(state.copy(ringing = false))
        alarms.cancel()
    }

    /** Swaps the break's exercise for another from the same tiers, if there is another. */
    suspend fun pickAnother() {
        val state = current()
        if (!state.onBreak) return
        val picked = ExercisePicker.pick(exercises.getAll(), state.tiers, state.breakExerciseId, random) ?: return
        states.save(state.copy(breakExerciseId = picked.id, lastExerciseId = picked.id))
    }

    /** Ends the break without starting another timer. */
    suspend fun finishBreak() {
        states.save(current().copy(onBreak = false, ringing = false, breakExerciseId = null))
        alarms.cancel()
    }

    /** Alarms don't survive a reboot or app update; the saved state does. A break that was ringing rings again right away. */
    suspend fun rearm() {
        val state = current()
        when {
            state.ringing -> alarms.schedule(clock.millis())
            state.endsAt != null -> alarms.schedule(maxOf(state.endsAt, clock.millis()))
            else -> alarms.cancel()
        }
    }

    companion object {
        const val REPEAT_MILLIS = 60_000L

        fun from(context: Context) = BreakTimer(
            KnimbleDatabase.getInstance(context),
            AndroidAlarmScheduler(context.applicationContext)
        )
    }
}
