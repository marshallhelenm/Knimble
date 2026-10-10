package io.github.marshallhelenm.knimble.timer

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.exercises.BuiltInExercises
import io.github.marshallhelenm.knimble.model.Tier
import io.github.marshallhelenm.knimble.notifications.BreakNotifier
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * Runs a real one-minute timer through AlarmManager, the receiver and the notification service, so it takes
 * about two and a half minutes. It uses the app's real database on the device.
 */
@RunWith(AndroidJUnit4::class)
class BreakAlarmEndToEndTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val notifications = context.getSystemService(NotificationManager::class.java)
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val timer = BreakTimer.from(context)

    @Before
    fun setUp() = runBlocking {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
        KnimbleDatabase.getInstance(context).exerciseDao().syncBuiltIn(BuiltInExercises.asExercises())
    }

    @After
    fun tearDown() = runBlocking {
        timer.stopRinging()
        timer.cancel()
        BreakNotifier(context).cancel()
    }

    private fun breakNotification(): StatusBarNotification? =
        notifications.activeNotifications.firstOrNull { it.id == BreakNotifier.NOTIFICATION_ID }

    private fun waitFor(timeoutMillis: Long, condition: () -> Boolean): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMillis
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return true
            SystemClock.sleep(500)
        }
        return false
    }

    @Test
    fun oneMinuteTimer_ringsThenRepeatsEveryMinuteUntilStopped() = runBlocking {
        timer.start(1, Tier.entries.toSet())
        assertNotNull("the timer's end should be armed as an alarm clock", alarmManager.nextAlarmClock)

        assertTrue("break alert never arrived", waitFor(90_000) { breakNotification() != null })
        val firstPost = breakNotification()!!.postTime
        assertTrue(timer.current().ringing)
        val nextRepeat = alarmManager.nextAlarmClock!!.triggerTime
        assertTrue("repeat should be armed about a minute out", abs(nextRepeat - (firstPost + 60_000)) < 5_000)

        assertTrue("alert didn't repeat", waitFor(90_000) { (breakNotification()?.postTime ?: 0) > firstPost })

        timer.stopRinging()
        assertFalse(timer.current().ringing)
        assertNull("no repeat should be armed after stopping", alarmManager.nextAlarmClock)
    }
}
