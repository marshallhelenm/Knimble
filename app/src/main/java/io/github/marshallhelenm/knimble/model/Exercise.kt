package io.github.marshallhelenm.knimble.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Stored by name, so the labels shown to the user (strings.xml) can be renamed freely.
enum class Tier {
    // Can be done sitting at the craft: hands, wrists, neck, eyes.
    SMALL,
    // Needs standing up, but not the floor.
    MEDIUM,
    // Floor work or full-body movement.
    LARGE
}

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
    val enabled: Boolean = true
)
