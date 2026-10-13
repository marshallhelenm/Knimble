package io.github.marshallhelenm.knimble.exercises

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
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
import java.io.File

@RunWith(AndroidJUnit4::class)
class ExerciseLibraryTest {
    private lateinit var db: KnimbleDatabase
    private lateinit var library: ExerciseLibrary
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val photos = ExercisePhotos(context)
    private val dao get() = db.exerciseDao()

    private val seed = Exercise(
        name = "Neck side tilt", tag = "Ear toward shoulder", description = "Tilt.", tier = Tier.SMALL,
        source = ExerciseSource.BUILT_IN, builtInKey = "neck_side_tilt",
        credits = listOf(Credit("Wellness52", "https://wellness52.com/x")), areas = setOf(BodyArea.NECK_SHOULDERS)
    )

    @Before
    fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, KnimbleDatabase::class.java).build()
        library = ExerciseLibrary(db, photos, listOf(seed))
        dao.syncBuiltIn(listOf(seed))
    }

    @After
    fun tearDown() = runBlocking {
        dao.getAll().flatMap { it.photos }.forEach { photos.delete(it) }
        db.close()
    }

    private fun picked(name: String): Uri {
        val file = File(context.cacheDir, name)
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.MAGENTA) }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return Uri.fromFile(file)
    }

    private fun exists(photoUri: String?) = photos.fileFor(photoUri)?.exists() == true

    private suspend fun builtIn() = dao.getAll().single { it.builtInKey == "neck_side_tilt" }

    private fun mine(name: String) = Exercise(
        name = name, tag = "", description = "Do the thing.", tier = Tier.MEDIUM, source = ExerciseSource.BUILT_IN,
        builtInKey = "sneaky"
    )

    @Test
    fun add_isAlwaysTheUsersOwn() = runBlocking {
        val id = library.add(mine("Shoulder shrugs"))!!

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
        val id = library.add(mine("Shrugs"))!!

        library.save(dao.getById(id)!!.copy(name = "Big shrugs"))

        val row = dao.getById(id)!!
        assertEquals("Big shrugs", row.name)
        assertEquals(ExerciseSource.USER, row.source)
    }

    @Test
    fun delete_onlyRemovesTheUsersOwn() = runBlocking {
        val id = library.add(mine("Shrugs"))!!

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
        val id = library.add(mine("Shrugs"))!!

        assertFalse(library.resetToOriginal(dao.getById(id)!!))
        assertEquals("Shrugs", dao.getById(id)!!.name)
    }

    private fun picks(vararg names: String) = names.map { PhotoDraft.Picked(picked(it)) }

    private val notAnImage get() = PhotoDraft.Picked(Uri.fromFile(File(context.cacheDir, "notes.txt").apply { writeText("hello") }))

    private fun storedFiles() = photos.fileFor(Uri.fromFile(File(context.filesDir, "exercise_photos/x.jpg")).toString())!!
        .parentFile!!.listFiles().orEmpty().map { it.name }.toSet()

    @Test
    fun add_withPhotos_storesCopiesInOrder() = runBlocking {
        val id = library.add(mine("Shrugs"), picks("a.jpg", "b.jpg", "c.jpg"))!!

        val stored = dao.getById(id)!!.photos
        assertEquals(3, stored.size)
        assertTrue(stored.all(::exists))
    }

    @Test
    fun add_keepsAtMostFivePhotos() = runBlocking {
        val id = library.add(mine("Shrugs"), picks("1.jpg", "2.jpg", "3.jpg", "4.jpg", "5.jpg", "6.jpg"))!!

        assertEquals(5, dao.getById(id)!!.photos.size)
    }

    @Test
    fun add_oneUnreadablePhoto_addsNothingAndLeavesNoFiles() = runBlocking {
        val before = storedFiles()

        assertNull(library.add(mine("Shrugs"), picks("a.jpg") + notAnImage))

        assertEquals(1, dao.getAll().size)
        assertEquals(before, storedFiles())
    }

    @Test
    fun save_removingOnePhoto_deletesOnlyThatFile() = runBlocking {
        val id = library.add(mine("Shrugs"), picks("a.jpg", "b.jpg"))!!
        val (first, second) = dao.getById(id)!!.photos

        assertTrue(library.save(dao.getById(id)!!, listOf(PhotoDraft.Stored(second))))

        assertEquals(listOf(second), dao.getById(id)!!.photos)
        assertFalse(exists(first))
        assertTrue(exists(second))
    }

    @Test
    fun save_addingToExistingPhotos_keepsThemAndAppends() = runBlocking {
        val id = library.add(mine("Shrugs"), picks("a.jpg"))!!
        val first = dao.getById(id)!!.photos.single()

        library.save(dao.getById(id)!!, listOf(PhotoDraft.Stored(first)) + picks("b.jpg"))

        val stored = dao.getById(id)!!.photos
        assertEquals(2, stored.size)
        assertEquals(first, stored.first())
        assertTrue(stored.all(::exists))
    }

    @Test
    fun save_withoutAPhotoList_keepsThePhotos() = runBlocking {
        val id = library.add(mine("Shrugs"), picks("a.jpg"))!!
        val stored = dao.getById(id)!!.photos

        library.save(dao.getById(id)!!.copy(name = "Big shrugs"))

        assertEquals(stored, dao.getById(id)!!.photos)
    }

    @Test
    fun save_unreadablePhoto_savesNothing() = runBlocking {
        assertFalse(library.save(builtIn().copy(name = "Changed"), listOf(notAnImage)))
        assertEquals("Neck side tilt", builtIn().name)
        assertEquals(ExerciseSource.BUILT_IN, builtIn().source)
    }

    @Test
    fun delete_removesEveryPhoto() = runBlocking {
        val id = library.add(mine("Shrugs"), picks("a.jpg", "b.jpg"))!!
        val stored = dao.getById(id)!!.photos

        library.delete(dao.getById(id)!!)

        assertTrue(stored.none(::exists))
    }

    @Test
    fun resetToOriginal_removesTheUsersPhotos() = runBlocking {
        library.save(builtIn(), picks("a.jpg", "b.jpg"))
        val stored = builtIn().photos
        assertEquals(2, stored.size)

        library.resetToOriginal(builtIn())

        assertEquals(emptyList<String>(), builtIn().photos)
        assertTrue(stored.none(::exists))
    }
}
