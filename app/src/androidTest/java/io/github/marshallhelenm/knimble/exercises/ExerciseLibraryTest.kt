package io.github.marshallhelenm.knimble.exercises

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Credit
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import io.github.marshallhelenm.knimble.model.Tier
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseLibraryTest {
    private lateinit var db: KnimbleDatabase
    private lateinit var library: ExerciseLibrary
    private val dao get() = db.exerciseDao()

    private val seed = Exercise(
        name = "Neck side tilt", tag = "Ear toward shoulder", description = "Tilt.", tier = Tier.SMALL,
        source = ExerciseSource.BUILT_IN, builtInKey = "neck_side_tilt",
        credits = listOf(Credit("Wellness52", "https://wellness52.com/x")), areas = setOf(BodyArea.NECK_SHOULDERS)
    )

    @Before
    fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            KnimbleDatabase::class.java
        ).build()
        library = ExerciseLibrary(db, listOf(seed))
        dao.syncBuiltIn(listOf(seed))
    }

    @After
    fun tearDown() = db.close()

    private suspend fun builtIn() = dao.getAll().single { it.builtInKey == "neck_side_tilt" }

    private fun mine(name: String) = Exercise(
        name = name, tag = "", description = "Do the thing.", tier = Tier.MEDIUM, source = ExerciseSource.BUILT_IN,
        builtInKey = "sneaky"
    )

    @Test
    fun add_isAlwaysTheUsersOwn() = runBlocking {
        val id = library.add(mine("Shoulder shrugs"))

        val row = dao.getById(id)!!
        assertEquals("Shoulder shrugs", row.name)
        assertEquals(ExerciseSource.USER, row.source)
        assertNull(row.builtInKey)
    }

    @Test
    fun save_builtIn_marksItEditedAndSurvivesTheNextSync() = runBlocking {
        library.save(builtIn().copy(name = "My neck tilt", description = "My way."))

        dao.syncBuiltIn(listOf(seed))

        val row = builtIn()
        assertEquals("My neck tilt", row.name)
        assertEquals("My way.", row.description)
        assertEquals(ExerciseSource.EDITED, row.source)
    }

    @Test
    fun save_usersOwn_staysTheirOwn() = runBlocking {
        val id = library.add(mine("Shrugs"))

        library.save(dao.getById(id)!!.copy(name = "Big shrugs"))

        val row = dao.getById(id)!!
        assertEquals("Big shrugs", row.name)
        assertEquals(ExerciseSource.USER, row.source)
    }

    @Test
    fun delete_onlyRemovesTheUsersOwn() = runBlocking {
        val id = library.add(mine("Shrugs"))

        assertTrue(library.delete(dao.getById(id)!!))
        assertNull(dao.getById(id))

        assertFalse(library.delete(builtIn()))
        library.save(builtIn().copy(name = "Edited"))
        assertFalse(library.delete(builtIn()))
        assertEquals(1, dao.getAll().size)
    }

    @Test
    fun resetToOriginal_restoresTheShippedVersionButKeepsTheSwitch() = runBlocking {
        library.save(builtIn().copy(name = "My neck tilt", tier = Tier.LARGE, credits = emptyList(), enabled = false))

        assertTrue(library.resetToOriginal(builtIn()))

        val row = builtIn()
        assertEquals(seed.copy(id = row.id, enabled = false), row)
        assertEquals(ExerciseSource.BUILT_IN, row.source)
    }

    @Test
    fun resetToOriginal_usersOwn_doesNothing() = runBlocking {
        val id = library.add(mine("Shrugs"))

        assertFalse(library.resetToOriginal(dao.getById(id)!!))
        assertEquals("Shrugs", dao.getById(id)!!.name)
    }
}
