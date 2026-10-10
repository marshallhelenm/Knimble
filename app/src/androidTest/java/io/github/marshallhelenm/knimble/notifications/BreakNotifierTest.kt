package io.github.marshallhelenm.knimble.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

// Uses the device's real notification service.
@RunWith(AndroidJUnit4::class)
class BreakNotifierTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val system = context.getSystemService(NotificationManager::class.java)
    private val notifier = BreakNotifier(context)

    @After
    fun tearDown() {
        notifier.syncChannel(BreakSound.bundledUri(context))
    }

    @Test
    fun syncChannel_bundledSound_isTheOnlyChannelAndPlaysIt() {
        val id = notifier.syncChannel(BreakSound.bundledUri(context))

        assertEquals(listOf(id), system.notificationChannels.map { it.id })
        assertEquals(BreakSound.bundledUri(context), system.getNotificationChannel(id).sound)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, system.getNotificationChannel(id).importance)
    }

    @Test
    fun syncChannel_newSound_replacesTheOldChannel() {
        val bundled = notifier.syncChannel(BreakSound.bundledUri(context))
        val other = "android.resource://${context.packageName}/raw/some_other_sound".toUri()

        val replaced = notifier.syncChannel(other)

        assertNotEquals(bundled, replaced)
        assertEquals(listOf(replaced), system.notificationChannels.map { it.id })
        assertEquals(other, system.getNotificationChannel(replaced).sound)
    }
}
