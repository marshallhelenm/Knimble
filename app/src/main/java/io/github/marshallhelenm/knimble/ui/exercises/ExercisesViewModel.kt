package io.github.marshallhelenm.knimble.ui.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.exercises.ExerciseFilter
import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExercisesViewModel(application: Application) : AndroidViewModel(application) {
    private val exercises = KnimbleDatabase.getInstance(application).exerciseDao()

    private val _areas = MutableStateFlow<Set<BodyArea>>(emptySet())

    /** The body areas the list is narrowed to; empty shows everything. */
    val areas: StateFlow<Set<BodyArea>> = _areas.asStateFlow()

    val availableAreas: StateFlow<List<BodyArea>> = exercises.observeAll()
        .map(ExerciseFilter::areasIn)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Alphabetical within each tier, tiers smallest first. */
    val byTier: StateFlow<Map<Tier, List<Exercise>>> = combine(exercises.observeAll(), _areas, ExerciseFilter::byTier)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun toggleArea(area: BodyArea) {
        _areas.update { if (area in it) it - area else it + area }
    }

    fun observe(id: Long): Flow<Exercise?> = exercises.observeById(id)

    fun setEnabled(exercise: Exercise, enabled: Boolean) {
        viewModelScope.launch { exercises.setEnabled(exercise.id, enabled) }
    }
}
