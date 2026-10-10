package io.github.marshallhelenm.knimble.ui.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExercisesViewModel(application: Application) : AndroidViewModel(application) {
    private val exercises = KnimbleDatabase.getInstance(application).exerciseDao()

    /** Alphabetical within each tier, tiers smallest first. */
    val byTier: StateFlow<Map<Tier, List<Exercise>>> = exercises.observeAll()
        .map { all -> Tier.entries.associateWith { tier -> all.filter { it.tier == tier } } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun observe(id: Long): Flow<Exercise?> = exercises.observeById(id)

    fun setEnabled(exercise: Exercise, enabled: Boolean) {
        viewModelScope.launch { exercises.setEnabled(exercise.id, enabled) }
    }
}
