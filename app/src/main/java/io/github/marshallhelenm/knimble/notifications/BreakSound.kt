package io.github.marshallhelenm.knimble.notifications

import android.content.ContentValues
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException

/** The sound the break alert plays. [name] is null for the bundled marimba, or if the sound has no title. */
data class AlertSound(val uri: Uri, val name: String?, val isBundled: Boolean)

/**
 * The sound the break alert plays: the bundled marimba unless the user has chosen one of the phone's sounds
 * or imported a file. Only one choice is kept, so an imported file is deleted once it's replaced.
 */
object BreakSound {
    private const val PREFS = "break_sound"
    private const val KEY_URI = "uri"
    private const val KEY_NAME = "name"
    private const val KEY_IMPORTED = "imported"

    private val changes = MutableStateFlow(0)

    /** Ticks whenever the choice changes, so screens can re-read [current]. */
    val changed: StateFlow<Int> = changes.asStateFlow()

    // Imports go into shared storage so the system can play them; that needs MediaStore's permission-free
    // writes, which arrived in Android 10.
    val canImport: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    // Name-based rather than id-based: resource ids can change between builds, and the URI is baked into the channel.
    fun bundledUri(context: Context): Uri = "android.resource://${context.packageName}/raw/marimba_ringtone".toUri()

    fun uri(context: Context): Uri = current(context).uri

    fun current(context: Context): AlertSound {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val uri = prefs.getString(KEY_URI, null)?.toUri() ?: return AlertSound(bundledUri(context), null, isBundled = true)
        return AlertSound(uri, prefs.getString(KEY_NAME, null), isBundled = false)
    }

    /** One of the phone's own sounds, from the system sound picker. */
    suspend fun choosePhoneSound(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        val name = RingtoneManager.getRingtone(context, uri)?.getTitle(context)
        save(context, uri, name, imported = false)
    }

    fun resetToBundled(context: Context) = save(context, null, null, imported = false)

    /**
     * Copies [source] into the shared Notifications/Knimble folder and makes it the alert sound. Returns false if
     * it isn't audio or can't be copied. The system only plays a channel sound the app itself can read, and an
     * app can always read media entries it created.
     */
    suspend fun import(context: Context, source: Uri): Boolean = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext false
        val fileName = displayNameOf(context, source)
        val target = copyIntoMediaStore(context, source, fileName ?: "sound") ?: return@withContext false
        save(context, target, fileName?.substringBeforeLast('.')?.ifBlank { null }, imported = true)
        true
    }

    private fun save(context: Context, uri: Uri?, name: String?, imported: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previousImport = prefs.getString(KEY_URI, null)?.takeIf { prefs.getBoolean(KEY_IMPORTED, false) }
        prefs.edit {
            if (uri == null) {
                clear()
            } else {
                putString(KEY_URI, uri.toString())
                putString(KEY_NAME, name)
                putBoolean(KEY_IMPORTED, imported)
            }
        }
        if (previousImport != null && previousImport != uri?.toString()) deleteImport(context, previousImport.toUri())
        BreakNotifier(context).syncChannel(current(context).uri)
        changes.value++
    }

    private fun deleteImport(context: Context, uri: Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (_: SecurityException) {
            // The file no longer belongs to us (e.g. restored from a backup); leave it where it is.
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun copyIntoMediaStore(context: Context, source: Uri, fileName: String): Uri? {
        val resolver = context.contentResolver
        // Some sources (file:// URIs, some document providers) report no type, so fall back to the extension.
        val mimeType = (resolver.getType(source) ?: mimeTypeFromName(fileName))?.takeIf { it.startsWith("audio/") }
            ?: return null
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
            put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_NOTIFICATIONS}/Knimble")
            put(MediaStore.Audio.Media.IS_NOTIFICATION, 1)
            // Hidden from other apps until the copy finishes.
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val target = resolver.insert(collection, values) ?: return null
        return try {
            resolver.openInputStream(source)?.use { input ->
                resolver.openOutputStream(target)?.use { output -> input.copyTo(output) }
            } ?: throw IOException("Can't copy $source")
            resolver.update(target, ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) }, null, null)
            target
        } catch (_: Exception) {
            resolver.delete(target, null, null)
            null
        }
    }

    private fun mimeTypeFromName(fileName: String): String? =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substringAfterLast('.', "").lowercase())

    private fun displayNameOf(context: Context, source: Uri): String? {
        if (source.scheme == "file") return source.lastPathSegment
        return context.contentResolver.query(source, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }
    }
}
