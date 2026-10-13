package io.github.marshallhelenm.knimble.ui.exercises

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.exercises.BuiltInExercises
import io.github.marshallhelenm.knimble.exercises.ExerciseLibrary
import io.github.marshallhelenm.knimble.model.ExerciseSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Uses the app's real database, as the screen does; each test puts the exercise it touches back afterwards.
@RunWith(AndroidJUnit4::class)
class EditExerciseViewModelTest {
    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private val dao = KnimbleDatabase.getInstance(app).exerciseDao()
    private var id = 0L

    @Before
    fun setUp() = runBlocking {
        dao.syncBuiltIn(BuiltInExercises.asExercises())
        val row = dao.getAll().single { it.builtInKey == "chin_tucks" }
        id = row.id
        // Start from the shipped version even if the device's copy was edited by hand.
        ExerciseLibrary.from(app).resetToOriginal(row)
        Unit
    }

    @After
    fun tearDown() = runBlocking {
        ExerciseLibrary.from(app).resetToOriginal(dao.getById(id)!!)
        Unit
    }

    private suspend fun editor(): EditExerciseViewModel {
        val viewModel = EditExerciseViewModel(app)
        viewModel.load(id)
        withTimeout(5_000) { while (!viewModel.loaded) delay(20) }
        return viewModel
    }

    private suspend fun EditExerciseViewModel.saveAndWait(): Long =
        withTimeout(5_000) {
            var saved: Long? = null
            save { saved = it }
            while (saved == null) delay(20)
            saved!!
        }

    @Test
    fun saveWithoutChanges_leavesABuiltInUntouched() = runBlocking {
        val savedId = editor().saveAndWait()

        assertEquals(id, savedId)
        assertEquals(ExerciseSource.BUILT_IN, dao.getById(id)!!.source)
    }

    @Test
    fun saveWithChanges_marksABuiltInEdited() = runBlocking {
        val viewModel = editor()
        viewModel.form = viewModel.form.copy(name = "Chin tucks, my way")

        viewModel.saveAndWait()

        val row = dao.getById(id)!!
        assertEquals("Chin tucks, my way", row.name)
        assertEquals(ExerciseSource.EDITED, row.source)
    }
}
