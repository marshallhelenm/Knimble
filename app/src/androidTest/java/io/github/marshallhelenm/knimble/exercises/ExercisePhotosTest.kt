package io.github.marshallhelenm.knimble.exercises

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ExercisePhotosTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val photos = ExercisePhotos(context)
    private val stored = mutableListOf<String>()

    @After
    fun tearDown() = runBlocking { stored.forEach { photos.delete(it) } }

    private fun jpeg(name: String, width: Int, height: Int, orientation: Int? = null): Uri {
        val file = File(context.cacheDir, name)
        // Left half red, right half blue, so a rotation shows up as the colours moving.
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) for (y in 0 until height) bitmap.setPixel(x, y, if (x < width / 2) Color.RED else Color.BLUE)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        if (orientation != null) {
            ExifInterface(file.path).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                saveAttributes()
            }
        }
        return Uri.fromFile(file)
    }

    private suspend fun import(source: Uri): Bitmap {
        val uri = photos.import(source)
        assertNotNull(uri)
        stored += uri!!
        return BitmapFactory.decodeFile(photos.fileFor(uri)!!.path)
    }

    @Test
    fun import_largePhoto_isScaledToTheMaximumSide() = runBlocking {
        val photo = import(jpeg("big.jpg", 3000, 2000))

        assertEquals(ExercisePhotos.MAX_SIDE, photo.width)
        assertEquals(1067, photo.height)
    }

    @Test
    fun import_smallPhoto_keepsItsSize() = runBlocking {
        val photo = import(jpeg("small.jpg", 400, 300))

        assertEquals(400, photo.width)
        assertEquals(300, photo.height)
    }

    @Test
    fun import_sidewaysPhoto_comesOutUpright() = runBlocking {
        val photo = import(jpeg("sideways.jpg", 400, 200, ExifInterface.ORIENTATION_ROTATE_90))

        assertEquals(200, photo.width)
        assertEquals(400, photo.height)
        // Turned a quarter clockwise, the red left half ends up on top.
        assertEquals(Color.RED, nearest(photo.getPixel(100, 50)))
        assertEquals(Color.BLUE, nearest(photo.getPixel(100, 350)))
    }

    @Test
    fun import_notAnImage_isNull() = runBlocking {
        val notes = File(context.cacheDir, "notes.txt").apply { writeText("hello") }

        assertNull(photos.import(Uri.fromFile(notes)))
    }

    @Test
    fun delete_removesStoredPhotosOnly() = runBlocking {
        val uri = photos.import(jpeg("a.jpg", 100, 100))!!
        val outsider = File(context.filesDir, "keep_me.txt").apply { writeText("not a photo") }

        photos.delete(Uri.fromFile(outsider).toString())
        photos.delete("android.resource://${context.packageName}/drawable/stretch_placeholder")
        photos.delete(uri)

        assertTrue(outsider.exists())
        assertFalse(photos.fileFor(uri)!!.exists())
        outsider.delete()
        Unit
    }

    // JPEG blurs colours slightly; snap to whichever of red or blue is closer.
    private fun nearest(pixel: Int): Int =
        if (Color.red(pixel) > Color.blue(pixel)) Color.RED else Color.BLUE
}
