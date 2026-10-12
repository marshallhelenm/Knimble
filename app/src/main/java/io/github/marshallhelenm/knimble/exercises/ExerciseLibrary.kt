package io.github.marshallhelenm.knimble.exercises

import android.content.Context
import android.net.Uri
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource

/** What the editor wants done with an exercise's photo. */
sealed interface PhotoChange {
    data object Keep : PhotoChange
    data object Remove : PhotoChange
    /** A newly picked image, copied in only when the exercise is saved so a cancelled edit leaves nothing behind. */
    data class Replace(val source: Uri) : PhotoChange
}

/**
 * Adding, editing, deleting and resetting exercises, keeping each one's [ExerciseSource] honest and its stored
 * photo tidied up.
 */
class ExerciseLibrary(
    db: KnimbleDatabase,
    private val photos: ExercisePhotos,
    private val seeds: List<Exercise> = BuiltInExercises.asExercises()
) {
    private val exercises = db.exerciseDao()

    /** A new exercise of the user's own; returns its id, or null if the picked photo couldn't be read. */
    suspend fun add(exercise: Exercise, photo: PhotoChange = PhotoChange.Keep): Long? {
        val imageUri = when (photo) {
            is PhotoChange.Replace -> photos.import(photo.source) ?: return null
            else -> null
        }
        return exercises.insert(
            exercise.copy(id = 0, source = ExerciseSource.USER, builtInKey = null, imageUri = imageUri)
        )
    }

    /**
     * Saving changes to a built-in marks it edited, so the startup sync stops overwriting it. Returns false, saving
     * nothing, if the picked photo couldn't be read.
     */
    suspend fun save(exercise: Exercise, photo: PhotoChange = PhotoChange.Keep): Boolean {
        val stored = exercises.getById(exercise.id)?.imageUri
        val imageUri = when (photo) {
            PhotoChange.Keep -> stored
            PhotoChange.Remove -> null
            is PhotoChange.Replace -> photos.import(photo.source) ?: return false
        }
        val source = if (exercise.source == ExerciseSource.BUILT_IN) ExerciseSource.EDITED else exercise.source
        exercises.update(exercise.copy(source = source, imageUri = imageUri))
        if (stored != imageUri) photos.delete(stored)
        return true
    }

    /** Only the user's own exercises can be deleted; built-ins can be switched off or reset instead. */
    suspend fun delete(exercise: Exercise): Boolean {
        if (exercise.source != ExerciseSource.USER) return false
        val stored = exercises.getById(exercise.id)?.imageUri
        exercises.deleteById(exercise.id)
        photos.delete(stored)
        return true
    }

    /** Puts an edited built-in back to the shipped version, keeping its on/off switch. False if it has none. */
    suspend fun resetToOriginal(exercise: Exercise): Boolean {
        val seed = seeds.firstOrNull { it.builtInKey != null && it.builtInKey == exercise.builtInKey } ?: return false
        val stored = exercises.getById(exercise.id)?.imageUri
        exercises.update(seed.copy(id = exercise.id, enabled = exercise.enabled))
        photos.delete(stored)
        return true
    }

    companion object {
        fun from(context: Context) =
            ExerciseLibrary(KnimbleDatabase.getInstance(context), ExercisePhotos(context.applicationContext))
    }
}
