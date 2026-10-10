package io.github.marshallhelenm.knimble.notifications

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri

/** The sound the break alert plays: the bundled marimba unless the user has chosen their own. */
object BreakSound {
    private const val PREFS = "break_sound"
    private const val KEY_URI = "uri"

    // Name-based rather than id-based: resource ids can change between builds, and the URI is baked into the channel.
    fun bundledUri(context: Context): Uri = "android.resource://${context.packageName}/raw/marimba_ringtone".toUri()

    fun uri(context: Context): Uri =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_URI, null)?.toUri()
            ?: bundledUri(context)

    /** Null goes back to the bundled sound. */
    fun set(context: Context, uri: Uri?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            if (uri == null) remove(KEY_URI) else putString(KEY_URI, uri.toString())
        }
    }
}
