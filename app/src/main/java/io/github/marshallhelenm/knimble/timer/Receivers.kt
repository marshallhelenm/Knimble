package io.github.marshallhelenm.knimble.timer

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.marshallhelenm.knimble.notifications.BreakNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Receivers must finish within ~10s; goAsync keeps the process alive while Room work runs off the main thread.
private fun BroadcastReceiver.runAsync(work: suspend () -> Unit) {
    val result = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            work()
        } finally {
            result.finish()
        }
    }
}

class BreakAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        runAsync {
            BreakTimer.from(app).onAlarm()?.let(BreakNotifier(app)::show)
        }
    }
}

class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val timer = BreakTimer.from(context.applicationContext)
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            // Switch the armed alarm between exact and inexact.
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> runAsync { timer.rearm() }
        }
    }
}
