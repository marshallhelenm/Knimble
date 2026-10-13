package io.github.marshallhelenm.knimble.ui.exercises

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.exercises.ExerciseLibrary
import io.github.marshallhelenm.knimble.exercises.PhotoDraft
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import kotlinx.coroutines.launch

/** One editing session: a fresh instance per visit, so a new exercise never starts with the last one's leftovers. */
class EditExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private val library = ExerciseLibrary.from(application)
    private val exercises = KnimbleDatabase.getInstance(application).exerciseDao()

    /** Null while adding a new exercise. */
    var original by mutableStateOf<Exercise?>(null)
        private set
    var loaded by mutableStateOf(false)
        private set
    var form by mutableStateOf(ExerciseForm())
    /** In order; the first is the one shown in lists. */
    var photos by mutableStateOf<List<PhotoDraft>>(emptyList())
        private set

    /** Errors only show after a save attempt, so a blank new form doesn't open covered in red. */
    var showErrors by mutableStateOf(false)
        private set
    var photoFailed by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set

    private var startingForm = ExerciseForm()
    private var startingPhotos = emptyList<PhotoDraft>()

    fun load(id: Long?) {
        if (loaded) return
        if (id == null) {
            loaded = true
            return
        }
        viewModelScope.launch {
            exercises.getById(id)?.let {
                original = it
                form = ExerciseForm.from(it)
                startingForm = form
                photos = it.photos.map(PhotoDraft::Stored)
                startingPhotos = photos
            }
            loaded = true
        }
    }

    val canDelete get() = original?.source == ExerciseSource.USER
    val canReset get() = original?.source == ExerciseSource.EDITED

    val photoSlotsLeft get() = Exercise.MAX_PHOTOS - photos.size

    /** Adds picked images after the existing photos, ignoring any beyond the limit. */
    fun addPhotos(picked: List<Uri>) {
        photoFailed = false
        photos = (photos + picked.map(PhotoDraft::Picked)).take(Exercise.MAX_PHOTOS)
    }

    fun removePhoto(index: Int) {
        photoFailed = false
        photos = photos.filterIndexed { i, _ -> i != index }
    }

    fun hasUnsavedChanges() = form != startingForm || photos != startingPhotos

    /** Calls [onSaved] with the exercise's id once it's stored. */
    fun save(onSaved: (Long) -> Unit) {
        if (busy) return
        if (form.errors().isNotEmpty()) {
            showErrors = true
            return
        }
        // Saving an untouched built-in would mark it edited and stop it receiving fixes, so there's nothing to do.
        val unchanged = original?.takeIf { !hasUnsavedChanges() }
        if (unchanged != null) {
            onSaved(unchanged.id)
            return
        }
        busy = true
        viewModelScope.launch {
            val base = original
            val id = if (base == null) {
                library.add(form.toExercise(null), photos)
            } else {
                base.id.takeIf { library.save(form.toExercise(base), photos) }
            }
            busy = false
            if (id == null) photoFailed = true else onSaved(id)
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val exercise = original ?: return
        viewModelScope.launch { if (library.delete(exercise)) onDeleted() }
    }

    fun resetToOriginal(onReset: () -> Unit) {
        val exercise = original ?: return
        viewModelScope.launch { if (library.resetToOriginal(exercise)) onReset() }
    }
}
