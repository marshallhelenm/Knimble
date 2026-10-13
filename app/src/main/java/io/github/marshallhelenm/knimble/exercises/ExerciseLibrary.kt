package io.github.marshallhelenm.knimble.exercises

import android.content.Context
import android.net.Uri
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource

/** One photo in the editor's list: already stored, or newly picked and not yet copied in. */
sealed interface PhotoDraft {
    data class Stored(val uri: String) : PhotoDraft
    /** Copied in only when the exercise is saved, so a cancelled edit leaves nothing behind. */
    data class Picked(val source: Uri) : PhotoDraft
}

/**
 * Adding, editing, deleting and resetting exercises, keeping each one's [ExerciseSource] honest and its stored
 * photos tidied up.
 */
class ExerciseLibrary(
    db: KnimbleDatabase,
    private val photos: ExercisePhotos,
    private val seeds: List<Exercise> = BuiltInExercises.asExercises()
) {
    private val exercises = db.exerciseDao()

    /** A new exercise of the user's own; returns its id, or null if a picked photo couldn't be read. */
    suspend fun add(exercise: Exercise, photoList: List<PhotoDraft> = emptyList()): Long? {
        val stored = store(photoList) ?: return null
        return exercises.insert(
            exercise.copy(id = 0, source = ExerciseSource.USER, builtInKey = null).withPhotos(stored)
        )
    }

    /**
     * Saves changes, with [photoList] as the exercise's photos in order (null keeps them as they are). Saving a
     * built-in marks it edited, so the startup sync stops overwriting it. Returns false, saving nothing, if a picked
     * photo couldn't be read.
     */
    suspend fun save(exercise: Exercise, photoList: List<PhotoDraft>? = null): Boolean {
        val before = exercises.getById(exercise.id)?.photos.orEmpty()
        val after = if (photoList == null) before else store(photoList) ?: return false
        val source = if (exercise.source == ExerciseSource.BUILT_IN) ExerciseSource.EDITED else exercise.source
        exercises.update(exercise.copy(source = source).withPhotos(after))
        (before - after.toSet()).forEach { photos.delete(it) }
        return true
    }

    /** Only the user's own exercises can be deleted; built-ins can be switched off or reset instead. */
    suspend fun delete(exercise: Exercise): Boolean {
        if (exercise.source != ExerciseSource.USER) return false
        val before = exercises.getById(exercise.id)?.photos.orEmpty()
        exercises.deleteById(exercise.id)
        before.forEach { photos.delete(it) }
        return true
    }

    /** Puts an edited built-in back to the shipped version, keeping its on/off switch. False if it has none. */
    suspend fun resetToOriginal(exercise: Exercise): Boolean {
        val seed = seeds.firstOrNull { it.builtInKey != null && it.builtInKey == exercise.builtInKey } ?: return false
        val before = exercises.getById(exercise.id)?.photos.orEmpty()
        exercises.update(seed.copy(id = exercise.id, enabled = exercise.enabled))
        before.forEach { photos.delete(it) }
        return true
    }

    /**
     * Copies in the picked photos (keeping the list's order and at most [Exercise.MAX_PHOTOS]) and returns every URI.
     * If any can't be read, the ones copied so far are removed again and the result is null.
     */
    private suspend fun store(photoList: List<PhotoDraft>): List<String>? {
        val copied = mutableListOf<String>()
        val result = photoList.take(Exercise.MAX_PHOTOS).map { draft ->
            when (draft) {
                is PhotoDraft.Stored -> draft.uri
                is PhotoDraft.Picked -> photos.import(draft.source)?.also { copied += it } ?: run {
                    copied.forEach { photos.delete(it) }
                    return null
                }
            }
        }
        return result
    }

    companion object {
        fun from(context: Context) =
            ExerciseLibrary(KnimbleDatabase.getInstance(context), ExercisePhotos(context.applicationContext))
    }
}
