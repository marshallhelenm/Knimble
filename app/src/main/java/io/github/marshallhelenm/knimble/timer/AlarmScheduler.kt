package io.github.marshallhelenm.knimble.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import io.github.marshallhelenm.knimble.MainActivity

// Only one alarm is ever armed: either the end of the running timer or the next repeat of a ringing break.
interface AlarmScheduler {
    fun schedule(atMillis: Long)
    fun cancel()
}

class AndroidAlarmScheduler(private val context: Context) : AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(atMillis: Long) {
        // Alarm-clock alarms are the only kind Doze never holds back, which matters with the phone lying idle
        // during a long crafting session. Without the exact-alarm grant they aren't allowed, so fall back to
        // one that may arrive a few minutes late.
        if (AlarmManagerCompat.canScheduleExactAlarms(alarmManager)) {
            AlarmManagerCompat.setAlarmClock(alarmManager, atMillis, openAppIntent(), alarmIntent())
        } else {
            AlarmManagerCompat.setAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, atMillis, alarmIntent())
        }
    }

    override fun cancel() = alarmManager.cancel(alarmIntent())

    private fun alarmIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, BreakAlarmReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Opened when the user taps the upcoming alarm in the system's clock UI.
    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
