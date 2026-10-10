package io.github.marshallhelenm.knimble.ui.components

import android.content.res.Resources
import android.graphics.BitmapFactory
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier
import io.github.marshallhelenm.knimble.ui.theme.Cocoa
import io.github.marshallhelenm.knimble.ui.theme.color

// The stand-in picture is 2000px square; decoded at full size every row would hold ~16 MB. Each size is
// decoded once, scaled down, and shared by every exercise using it.
private object Placeholder {
    private val cache = mutableMapOf<Int, ImageBitmap>()

    fun get(resources: Resources, sampleSize: Int): ImageBitmap = synchronized(cache) {
        cache.getOrPut(sampleSize) {
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeResource(resources, R.drawable.stretch_placeholder, options).asImageBitmap()
        }
    }
}

/** [large] for the break and detail screens; otherwise a thumbnail-sized decode. */
@Composable
fun ExerciseImage(exercise: Exercise, large: Boolean, modifier: Modifier = Modifier) {
    val resources = LocalResources.current
    val bitmap = remember(large) { Placeholder.get(resources, if (large) 2 else 8) }
    Image(
        bitmap = bitmap,
        contentDescription = exercise.name,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(if (large) 24.dp else 12.dp))
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
