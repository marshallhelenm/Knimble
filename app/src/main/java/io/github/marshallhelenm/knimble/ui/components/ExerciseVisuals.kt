package io.github.marshallhelenm.knimble.ui.components

import android.content.ContentResolver
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier
import io.github.marshallhelenm.knimble.ui.theme.Cocoa
import io.github.marshallhelenm.knimble.ui.theme.color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

// The stand-in pictures are up to 2000px square; decoded at full size every row would hold ~16 MB. Each picture is
// decoded once per size, scaled down, and shared by every exercise using it.
private object Placeholder {
    private val cache = mutableMapOf<Pair<Int, Int>, ImageBitmap>()

    fun get(resources: Resources, @DrawableRes res: Int, sampleSize: Int): ImageBitmap = synchronized(cache) {
        cache.getOrPut(res to sampleSize) {
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeResource(resources, res, options).asImageBitmap()
        }
    }
}

// Exercises without a photo of their own get a stand-in chosen by the body area worked.
// Eyes comes first: eye exercises that also move the head are still mainly about the eyes.
@DrawableRes
fun placeholderFor(areas: Set<BodyArea>): Int = when {
    BodyArea.EYES in areas -> R.drawable.eyes_placeholder
    BodyArea.NECK_SHOULDERS in areas -> R.drawable.neck_placeholder
    BodyArea.WHOLE_BODY in areas -> R.drawable.whole_body_placeholder
    BodyArea.HIPS_LEGS in areas -> R.drawable.legs_placeholder
    BodyArea.BACK in areas -> R.drawable.back_placeholder
    else -> R.drawable.stretch_placeholder
}

// Users' photos, decoded at about the size they're shown and kept for reuse. 24 MB holds a list's worth of
// thumbnails plus a couple of full-size ones.
private object Photos {
    private val cache = object : LruCache<String, ImageBitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap) = value.width * value.height * 4
    }

    private fun key(uri: Uri, targetPx: Int) = "$targetPx@$uri"

    fun cached(uri: Uri, targetPx: Int): ImageBitmap? = cache.get(key(uri, targetPx))

    /** Null if the photo can't be read, e.g. it was deleted, or a picker grant has expired. */
    fun load(resolver: ContentResolver, uri: Uri, targetPx: Int): ImageBitmap? {
        cached(uri, targetPx)?.let { return it }
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val stream = resolver.openInputStream(uri) ?: return null
            stream.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (min(bounds.outWidth, bounds.outHeight) / (sample * 2) >= targetPx) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
            bitmap.asImageBitmap().also { cache.put(key(uri, targetPx), it) }
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * A photo if there is one, else the area placeholder. Photos load off the main thread; until then (or if the
 * photo can't be read) the placeholder shows. [large] pictures show whole, at their own shape, as big as the
 * [modifier] allows; give it a maximum size rather than a fixed width, or a tall photo can't shrink to fit.
 * Thumbnails are cropped square so list rows line up.
 */
@Composable
fun ExercisePicture(
    photo: Uri?,
    @DrawableRes placeholder: Int,
    large: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val resources = LocalResources.current
    val resolver = LocalContext.current.contentResolver
    val targetPx = if (large) 1000 else 200
    val standIn = remember(placeholder, large) { Placeholder.get(resources, placeholder, if (large) 2 else 8) }
    val loaded by produceState(photo?.let { Photos.cached(it, targetPx) }, photo, targetPx) {
        value = photo?.let { Photos.cached(it, targetPx) ?: withContext(Dispatchers.IO) { Photos.load(resolver, it, targetPx) } }
    }
    val bitmap = loaded ?: standIn
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        contentScale = if (large) ContentScale.Fit else ContentScale.Crop,
        modifier = (if (large) modifier.aspectRatio(bitmap.width.toFloat() / bitmap.height) else modifier)
            .clip(RoundedCornerShape(if (large) 24.dp else 12.dp))
    )
}

/** The first photo, or the area placeholder. [large] for full-size views; otherwise a thumbnail. */
@Composable
fun ExerciseImage(exercise: Exercise, large: Boolean, modifier: Modifier = Modifier) {
    ExercisePicture(exercise.photos.firstOrNull()?.toUri(), placeholderFor(exercise.areas), large, exercise.name, modifier)
}

/**
 * Every photo, full size, swiped through with dots showing which one is up. With one photo or none it's just
 * [ExerciseImage]. [maxHeight] bounds each picture, which keeps its own shape within it.
 */
@Composable
fun ExercisePhotoCarousel(exercise: Exercise, maxHeight: Dp, modifier: Modifier = Modifier) {
    val photos = exercise.photos
    if (photos.size <= 1) {
        ExerciseImage(exercise, large = true, modifier = modifier.heightIn(max = maxHeight))
        return
    }
    val pager = rememberPagerState(pageCount = { photos.size })
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(state = pager, pageSpacing = 16.dp, modifier = Modifier.fillMaxWidth()) { page ->
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ExercisePicture(
                    photo = photos[page].toUri(),
                    placeholder = placeholderFor(exercise.areas),
                    large = true,
                    contentDescription = stringResource(R.string.photo_of, page + 1, photos.size, exercise.name),
                    modifier = Modifier.heightIn(max = maxHeight)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(photos.size) { index ->
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pager.currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

fun BodyArea.label(): Int = when (this) {
    BodyArea.HANDS_WRISTS -> R.string.area_hands_wrists
    BodyArea.NECK_SHOULDERS -> R.string.area_neck_shoulders
    BodyArea.BACK -> R.string.area_back
    BodyArea.HIPS_LEGS -> R.string.area_hips_legs
    BodyArea.EYES -> R.string.area_eyes
    BodyArea.WHOLE_BODY -> R.string.area_whole_body
}

/** The tier and body areas, e.g. "Minor · Hands & wrists". */
@Composable
fun TierAndAreas(exercise: Exercise, modifier: Modifier = Modifier) {
    val parts = listOf(stringResource(exercise.tier.label())) +
        exercise.areas.sortedBy { it.ordinal }.map { stringResource(it.label()) }
    Text(
        parts.joinToString(" · "),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

fun Tier.label(): Int = when (this) {
    Tier.SMALL -> R.string.tier_small
    Tier.MEDIUM -> R.string.tier_medium
    Tier.LARGE -> R.string.tier_large
}

/**
 * The tag on a pill in its tier's colour. Tier colours are all light, so the text stays dark in both themes.
 * Nothing for an empty tag, which the user's own exercises may have.
 */
@Composable
fun TagPill(text: String, tier: Tier, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = Cocoa,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(tier.color())
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

/** "Adapted from …" with each source as a link where it has one. Nothing for exercises without credits. */
@Composable
fun ExerciseCredits(exercise: Exercise, modifier: Modifier = Modifier) {
    if (exercise.credits.isEmpty()) return
    val prefix = stringResource(R.string.credits_prefix)
    val linkStyle = TextLinkStyles(
        SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
    )
    val text = buildAnnotatedString {
        append(prefix)
        append(" ")
        exercise.credits.forEachIndexed { i, credit ->
            if (i > 0) append(" · ")
            val url = credit.url
            if (url == null) append(credit.name) else withLink(LinkAnnotation.Url(url, linkStyle)) { append(credit.name) }
        }
    }
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/** There's no physician behind these, so every exercise carries the sources' own advice to go gently. */
@Composable
fun SafetyNote(modifier: Modifier = Modifier) {
    Text(
        stringResource(R.string.safety_note),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}
