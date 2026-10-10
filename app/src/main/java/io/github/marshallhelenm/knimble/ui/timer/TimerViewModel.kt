package io.github.marshallhelenm.knimble.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier
import io.github.marshallhelenm.knimble.model.TimerState
import io.github.marshallhelenm.knimble.notifications.BreakNotifier
import io.github.marshallhelenm.knimble.timer.BreakTimer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val timer = BreakTimer.from(application)
    private val exercises = KnimbleDatabase.getInstance(application).exerciseDao()
    private val notifier = BreakNotifier(application)

    /** Null until the saved state has loaded. */
    val state: StateFlow<TimerState?> = timer.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val breakExercise: StateFlow<Exercise?> = state
        .map { it?.breakExerciseId }
        .distinctUntilChanged()
        .flatMapLatest { id -> id?.let(exercises::observeById) ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun start(durationMinutes: Int, tiers: Set<Tier>) {
        viewModelScope.launch {
            timer.start(durationMinutes, tiers)
            notifier.cancel()
        }
    }

    fun cancel() {
        viewModelScope.launch { timer.cancel() }
    }

    // The notification is left up: cancelling it would cut its sound off if the break arrived while the app was open.
    fun stopRinging() {
        viewModelScope.launch { timer.stopRinging() }
    }

    fun pickAnother() {
        viewModelScope.launch { timer.pickAnother() }
    }

    fun finishBreak() {
        viewModelScope.launch {
            timer.finishBreak()
            notifier.cancel()
        }
    }
}
