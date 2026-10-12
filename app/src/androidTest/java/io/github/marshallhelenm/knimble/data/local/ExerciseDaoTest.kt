package io.github.marshallhelenm.knimble.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.marshallhelenm.knimble.exercises.BuiltInExercises
import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Credit
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import io.github.marshallhelenm.knimble.model.Tier
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {
    private lateinit var db: KnimbleDatabase
    private lateinit var dao: ExerciseDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            KnimbleDatabase::class.java
        ).build()
        dao = db.exerciseDao()
    }

    @After
    fun tearDown() = db.close()

    private fun seed(key: String, name: String = key, tier: Tier = Tier.SMALL) = Exercise(
        name = name, tag = "tag", description = "description", tier = tier,
        source = ExerciseSource.BUILT_IN, builtInKey = key
    )

    @Test
    fun syncBuiltIn_firstRun_insertsTheWholeShippedList() = runBlocking {
        dao.syncBuiltIn(BuiltInExercises.asExercises())

        val rows = dao.getAll()
        assertEquals(BuiltInExercises.all.map { it.key }.toSet(), rows.map { it.builtInKey }.toSet())
        assertTrue(rows.all { it.enabled })
    }

    @Test
    fun syncBuiltIn_twice_doesNotDuplicate() = runBlocking {
        dao.syncBuiltIn(BuiltInExercises.asExercises())
        dao.syncBuiltIn(BuiltInExercises.asExercises())

        assertEquals(BuiltInExercises.all.size, dao.getAll().size)
    }

    @Test
    fun syncBuiltIn_updatesTextButKeepsSwitchAndId() = runBlocking {
        dao.syncBuiltIn(listOf(seed("neck", name = "Neck tilt")))
        val before = dao.getAll().single()
        dao.setEnabled(before.id, false)

        dao.syncBuiltIn(listOf(seed("neck", name = "Neck side tilt", tier = Tier.MEDIUM)))

        val after = dao.getAll().single()
        assertEquals(before.id, after.id)
        assertEquals("Neck side tilt", after.name)
        assertEquals(Tier.MEDIUM, after.tier)
        assertFalse(after.enabled)
    }

    @Test
    fun syncBuiltIn_removesDroppedBuiltInsButNotUserExercises() = runBlocking {
        dao.syncBuiltIn(listOf(seed("keep"), seed("drop")))
        dao.insert(
            Exercise(name = "Mine", tag = "t", description = "d", tier = Tier.LARGE, source = ExerciseSource.USER)
        )

        dao.syncBuiltIn(listOf(seed("keep")))

        assertEquals(setOf("keep", "Mine"), dao.getAll().map { it.name }.toSet())
    }

    @Test
    fun syncBuiltIn_storesAndUpdatesCredits() = runBlocking {
        val webMd = Credit("WebMD", "https://www.webmd.com/x")
        val physio = Credit("Home Physiotherapy Exercises", "https://homephysiotherapyexercises.com/y/")
        dao.syncBuiltIn(listOf(seed("wrists").copy(credits = listOf(webMd))))
        assertEquals(listOf(webMd), dao.getAll().single().credits)

        dao.syncBuiltIn(listOf(seed("wrists").copy(credits = listOf(webMd, physio))))

        assertEquals(listOf(webMd, physio), dao.getAll().single().credits)
    }

    @Test
    fun syncBuiltIn_storesAndUpdatesAreas() = runBlocking {
        dao.syncBuiltIn(listOf(seed("twist").copy(areas = setOf(BodyArea.BACK))))
        assertEquals(setOf(BodyArea.BACK), dao.getAll().single().areas)

        dao.syncBuiltIn(listOf(seed("twist").copy(areas = setOf(BodyArea.BACK, BodyArea.NECK_SHOULDERS))))

        assertEquals(setOf(BodyArea.BACK, BodyArea.NECK_SHOULDERS), dao.getAll().single().areas)
    }

    @Test
    fun syncBuiltIn_leavesEditedRowsAlone() = runBlocking {
        dao.syncBuiltIn(listOf(seed("neck", name = "Neck tilt")))
        val row = dao.getAll().single()
        dao.update(row.copy(name = "My neck stretch", source = ExerciseSource.EDITED, enabled = false))

        dao.syncBuiltIn(listOf(seed("neck", name = "Neck side tilt", tier = Tier.MEDIUM)))

        val after = dao.getAll().single()
        assertEquals("My neck stretch", after.name)
        assertEquals(Tier.SMALL, after.tier)
        assertEquals(ExerciseSource.EDITED, after.source)
        assertFalse(after.enabled)
    }

    @Test
    fun syncBuiltIn_editedRowNoLongerShipped_becomesTheUsersOwn() = runBlocking {
        dao.syncBuiltIn(listOf(seed("keep"), seed("edited"), seed("untouched")))
        val edited = dao.getAll().single { it.builtInKey == "edited" }
        dao.update(edited.copy(name = "Mine now", source = ExerciseSource.EDITED))

        dao.syncBuiltIn(listOf(seed("keep")))

        val rows = dao.getAll().associateBy { it.name }
        assertEquals(setOf("keep", "Mine now"), rows.keys)
        assertEquals(ExerciseSource.USER, rows.getValue("Mine now").source)
        assertEquals(null, rows.getValue("Mine now").builtInKey)
    }
}
