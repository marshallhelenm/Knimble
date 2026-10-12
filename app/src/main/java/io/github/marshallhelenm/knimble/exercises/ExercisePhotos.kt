package io.github.marshallhelenm.knimble.exercises

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.core.graphics.scale
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Exercise photos live in the app's own storage, so they survive the original being deleted and need no
 * permission to show. Each import is a fresh, upright, scaled-down JPEG.
 */
class ExercisePhotos(context: Context) {
    private val dir = File(context.filesDir, "exercise_photos")
    private val resolver = context.contentResolver

    /** Copies the picked image in; returns the stored photo's URI string, or null if it isn't a readable image. */
    suspend fun import(source: Uri): String? = withContext(Dispatchers.IO) {
        try {
            // A bounds-only decode always returns null; the size arrives in the options instead.
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val stream = resolver.openInputStream(source) ?: return@withContext null
            stream.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

            // Decode at the smallest power-of-two size that's still at least MAX_SIDE, then scale exactly.
            var sample = 1
            while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
            val decoded = resolver.openInputStream(source)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@withContext null

            val upright = rotate(decoded, orientationOf(source))
            val scaled = scaleToFit(upright)
            dir.mkdirs()
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            file.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            Uri.fromFile(file).toString()
        } catch (_: Exception) {
            null
        }
    }

    /** Removes a stored photo. Anything not in the app's photo folder (e.g. a placeholder) is left alone. */
    suspend fun delete(photoUri: String?) = withContext(Dispatchers.IO) {
        fileFor(photoUri)?.delete()
        Unit
    }

    /** The stored file behind [photoUri], or null if it isn't one of ours. */
    fun fileFor(photoUri: String?): File? {
        val path = photoUri?.toUri()?.takeIf { it.scheme == "file" }?.path ?: return null
        val file = File(path).canonicalFile
        return file.takeIf { it.parentFile == dir.canonicalFile }
    }

    // Phone cameras often save sideways pixels plus a "rotate me" tag, which BitmapFactory ignores.
    private fun orientationOf(source: Uri): Int =
        try {
            resolver.openInputStream(source)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

    private fun rotate(bitmap: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaleToFit(bitmap: Bitmap): Bitmap {
        val longest = max(bitmap.width, bitmap.height)
        if (longest <= MAX_SIDE) return bitmap
        val ratio = MAX_SIDE.toFloat() / longest
        return bitmap.scale((bitmap.width * ratio).roundToInt(), (bitmap.height * ratio).roundToInt())
    }

    companion object {
        const val MAX_SIDE = 1600
    }
}
