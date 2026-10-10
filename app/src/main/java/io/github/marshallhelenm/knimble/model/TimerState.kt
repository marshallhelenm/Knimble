package io.github.marshallhelenm.knimble.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// A single row (id 1) holding the timer, so it survives the app being closed or the phone restarting.
@Entity(tableName = "timer_state")
data class TimerState(
    @PrimaryKey
    val id: Int = 1,
    // When the running timer ends; null when no timer is running.
    val endsAt: Long? = null,
    // Remembered so the next timer starts with the same settings.
    val durationMinutes: Int = 30,
    val tiers: Set<Tier> = Tier.entries.toSet(),
    // True from the moment the timer ends until the user opens the break; the alert repeats every minute meanwhile.
    val ringing: Boolean = false,
    // The exercise for the current break; null once the break is over, or if every exercise in its tiers is off.
    val breakExerciseId: Long? = null,
    // Kept after the break ends so the next break picks something different.
    val lastExerciseId: Long? = null
)
