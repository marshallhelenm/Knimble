package io.github.marshallhelenm.knimble.notifications

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

// Uses the device's real MediaStore and notification service; imports need API 29+.
@RunWith(AndroidJUnit4::class)
class BreakSoundTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val system = context.getSystemService(NotificationManager::class.java)

    @Before
    fun setUp() = BreakSound.resetToBundled(context)

    @After
    fun tearDown() = BreakSound.resetToBundled(context)

    private fun bundledBytes(): ByteArray =
        context.contentResolver.openInputStream(BreakSound.bundledUri(context))!!.use { it.readBytes() }

    private fun sourceFile(name: String, bytes: ByteArray): Uri =
        File(context.cacheDir, name).apply { writeBytes(bytes) }.let(Uri::fromFile)

    private fun readable(uri: Uri): Boolean =
        try {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (_: Exception) {
            false
        }

    private fun channelSound(): Uri? = system.notificationChannels.single().sound

    @Test
    fun default_isTheBundledMarimba() {
        val sound = BreakSound.current(context)
        assertTrue(sound.isBundled)
        assertEquals(BreakSound.bundledUri(context), sound.uri)
    }

    @Test
    fun import_copiesTheFileAndUsesIt() = runBlocking {
        assertTrue(BreakSound.import(context, sourceFile("Wind chimes.mp3", bundledBytes())))

        val sound = BreakSound.current(context)
        assertFalse(sound.isBundled)
        assertEquals("Wind chimes", sound.name)
        assertArrayEquals(bundledBytes(), context.contentResolver.openInputStream(sound.uri)!!.use { it.readBytes() })
        assertEquals(sound.uri, channelSound())
    }

    @Test
    fun import_notAudio_isRefusedAndKeepsTheCurrentSound() = runBlocking {
        assertFalse(BreakSound.import(context, sourceFile("notes.txt", "hello".toByteArray())))

        assertTrue(BreakSound.current(context).isBundled)
    }

    @Test
    fun replacingAnImport_deletesItsFile() = runBlocking {
        BreakSound.import(context, sourceFile("First.mp3", bundledBytes()))
        val first = BreakSound.current(context).uri

        BreakSound.import(context, sourceFile("Second.mp3", bundledBytes()))

        assertFalse(readable(first))
        assertTrue(readable(BreakSound.current(context).uri))
    }

    @Test
    fun choosingAPhoneSound_usesItAndDeletesThePreviousImport() = runBlocking {
        BreakSound.import(context, sourceFile("Mine.mp3", bundledBytes()))
        val imported = BreakSound.current(context).uri
        val phoneSound = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION)
            ?: "content://settings/system/notification_sound".toUri()

        BreakSound.choosePhoneSound(context, phoneSound)

        val sound = BreakSound.current(context)
        assertEquals(phoneSound, sound.uri)
        assertNotNull(sound.name)
        assertEquals(phoneSound, channelSound())
        assertFalse(readable(imported))
    }

    @Test
    fun resetToBundled_goesBackToTheMarimbaAndDeletesTheImport() = runBlocking {
        BreakSound.import(context, sourceFile("Mine.mp3", bundledBytes()))
        val imported = BreakSound.current(context).uri

        BreakSound.resetToBundled(context)

        assertTrue(BreakSound.current(context).isBundled)
        assertEquals(BreakSound.bundledUri(context), channelSound())
        assertFalse(readable(imported))
    }
}
