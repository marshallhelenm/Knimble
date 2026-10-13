package io.github.marshallhelenm.knimble.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Stored by name, so the labels shown to the user (strings.xml) can be renamed freely.
enum class Tier {
    // "Minor": small stretches that don't require moving much, like wrist bends or head tilts.
    SMALL,
    // "Balanced": stretches with slightly more movement, like seated twists.
    MEDIUM,
    // "Involved": exercises that need getting out of your seat and moving around.
    LARGE
}

// Stored by name, so the labels shown to the user (strings.xml) can be renamed freely.
enum class BodyArea {
    HANDS_WRISTS,
    NECK_SHOULDERS,
    BACK,
    HIPS_LEGS,
    EYES,
    WHOLE_BODY
}

/** Where an exercise's instructions were adapted from. [url] is null for sources with no public page. */
data class Credit(val name: String, val url: String? = null)

// Who wrote the content you see. Stored by name, so adding a value needs no schema change.
enum class ExerciseSource {
    BUILT_IN,
    // A built-in the user has changed: the startup sync leaves its content alone.
    EDITED,
    USER
}

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["builtInKey"], unique = true)]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val tag: String,
    val description: String,
    val tier: Tier,
    // The stored photos, one URI per line; read and write them through [photos] and [withPhotos]. Kept in this
    // single-photo column (a lone URI is a one-photo list) so adding more photos needed no migration.
    // Null shows the stand-in picture.
    val imageUri: String? = null,
    val source: ExerciseSource,
    // Links a row to its entry in BuiltInExercises so text edits there reach existing installs
    // without resetting [enabled]. Null for the user's own exercises.
    val builtInKey: String? = null,
    // Off means it's never picked for a break.
    val enabled: Boolean = true,
    val credits: List<Credit> = emptyList(),
    val areas: Set<BodyArea> = emptySet()
) {
    /** In the order they were added; the first is the one shown in lists. */
    val photos: List<String>
        get() = imageUri?.lines()?.filter { it.isNotBlank() }.orEmpty()

    fun withPhotos(photos: List<String>): Exercise = copy(imageUri = photos.joinToString("\n").ifEmpty { null })

    companion object {
        const val MAX_PHOTOS = 5
    }
}
