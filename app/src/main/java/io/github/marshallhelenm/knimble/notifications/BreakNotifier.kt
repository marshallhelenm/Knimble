package io.github.marshallhelenm.knimble.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.github.marshallhelenm.knimble.MainActivity
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.timer.BreakAlert

/**
 * Posts the break alert. Since Android 8 a notification's sound belongs to its channel and can't be changed
 * afterwards, so there's one channel for the current sound, replaced whenever the sound changes. The system
 * plays it, honouring silent mode and Do Not Disturb.
 */
class BreakNotifier(private val context: Context) {
    private val manager = NotificationManagerCompat.from(context)

    /** Makes sure the channel for [sound] exists and removes the channels of sounds used before. */
    fun syncChannel(sound: Uri): String {
        val audio = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannelCompat.Builder(channelIdFor(sound), NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(context.getString(R.string.break_channel_name))
            .setSound(sound, audio)
            .build()
        manager.createNotificationChannel(channel)
        manager.deleteUnlistedNotificationChannels(listOf(channel.id))
        return channel.id
    }

    /** Re-posting under the same id replaces the previous alert, and plays the sound again. */
    fun show(alert: BreakAlert) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val sound = BreakSound.uri(context)
        val text = alert.exercise?.let { context.getString(R.string.break_exercise, it.name, it.tag) }
            ?: context.getString(R.string.break_no_exercise)
        val notification = NotificationCompat.Builder(context, syncChannel(sound))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.break_title))
            .setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            // Before Android 8 there are no channels: heads-up and sound are set per notification.
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(sound)
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun cancel() = manager.cancel(NOTIFICATION_ID)

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        const val NOTIFICATION_ID = 1

        // Android revives a deleted channel's old settings if its id is reused, so the id changes with the sound.
        fun channelIdFor(sound: Uri) = "break_${Integer.toHexString(sound.toString().hashCode())}"
    }
}
