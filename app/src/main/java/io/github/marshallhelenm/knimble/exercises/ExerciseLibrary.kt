package io.github.marshallhelenm.knimble.exercises

import android.content.Context
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource

/** Adding, editing, deleting and resetting exercises, keeping each one's [ExerciseSource] honest. */
class ExerciseLibrary(
    db: KnimbleDatabase,
    private val seeds: List<Exercise> = BuiltInExercises.asExercises()
) {
    private val exercises = db.exerciseDao()

    /** A new exercise of the user's own; returns its id. */
    suspend fun add(exercise: Exercise): Long =
        exercises.insert(exercise.copy(id = 0, source = ExerciseSource.USER, builtInKey = null))

    /** Saving changes to a built-in marks it edited, so the startup sync stops overwriting it. */
    suspend fun save(exercise: Exercise) {
        val source = if (exercise.source == ExerciseSource.BUILT_IN) ExerciseSource.EDITED else exercise.source
        exercises.update(exercise.copy(source = source))
    }

    /** Only the user's own exercises can be deleted; built-ins can be switched off or reset instead. */
    suspend fun delete(exercise: Exercise): Boolean {
        if (exercise.source != ExerciseSource.USER) return false
        exercises.deleteById(exercise.id)
        return true
    }

    /** Puts an edited built-in back to the shipped version, keeping its on/off switch. False if it has none. */
    suspend fun resetToOriginal(exercise: Exercise): Boolean {
        val seed = seeds.firstOrNull { it.builtInKey != null && it.builtInKey == exercise.builtInKey } ?: return false
        exercises.update(seed.copy(id = exercise.id, enabled = exercise.enabled))
        return true
    }

    companion object {
        fun from(context: Context) = ExerciseLibrary(KnimbleDatabase.getInstance(context))
    }
}
