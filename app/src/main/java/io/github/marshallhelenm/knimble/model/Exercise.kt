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

enum class ExerciseSource {
    BUILT_IN,
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
)
