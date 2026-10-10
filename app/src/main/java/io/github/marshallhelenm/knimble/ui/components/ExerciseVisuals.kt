package io.github.marshallhelenm.knimble.ui.components

import android.content.res.Resources
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier
import io.github.marshallhelenm.knimble.ui.theme.Cocoa
import io.github.marshallhelenm.knimble.ui.theme.color

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

// Until exercises have their own pictures, the stand-in is chosen by the body area worked.
@DrawableRes
// Eyes comes first: eye exercises that also move the head are still mainly about the eyes.
private fun placeholderFor(exercise: Exercise): Int = when {
    BodyArea.EYES in exercise.areas -> R.drawable.eyes_placeholder
    BodyArea.NECK_SHOULDERS in exercise.areas -> R.drawable.neck_placeholder
    BodyArea.WHOLE_BODY in exercise.areas -> R.drawable.whole_body_placeholder
    BodyArea.HIPS_LEGS in exercise.areas -> R.drawable.legs_placeholder
    BodyArea.BACK in exercise.areas -> R.drawable.back_placeholder
    else -> R.drawable.stretch_placeholder
}

/** [large] for the break and detail screens; otherwise a thumbnail-sized decode. */
@Composable
fun ExerciseImage(exercise: Exercise, large: Boolean, modifier: Modifier = Modifier) {
    val resources = LocalResources.current
    val res = placeholderFor(exercise)
    val bitmap = remember(res, large) { Placeholder.get(resources, res, if (large) 2 else 8) }
    Image(
        bitmap = bitmap,
        contentDescription = exercise.name,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(if (large) 24.dp else 12.dp))
    )
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

/** The tag on a pill in its tier's colour. Tier colours are all light, so the text stays dark in both themes. */
@Composable
fun TagPill(text: String, tier: Tier, modifier: Modifier = Modifier) {
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
