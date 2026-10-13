package io.github.marshallhelenm.knimble.ui.exercises

import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Credit
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import io.github.marshallhelenm.knimble.model.Tier

data class CreditDraft(val name: String = "", val url: String = "")

enum class FormError { NAME_MISSING, DESCRIPTION_MISSING, SOURCE_NAME_MISSING, SOURCE_LINK_INVALID }

/** The editor's fields, kept as typed so nothing is trimmed or dropped until save. */
data class ExerciseForm(
    val name: String = "",
    val tag: String = "",
    val description: String = "",
    val tier: Tier = Tier.SMALL,
    val areas: Set<BodyArea> = emptySet(),
    val credits: List<CreditDraft> = emptyList()
) {
    // Rows with neither a name nor a link are ignored rather than flagged; they're just unused space.
    private val filledCredits get() = credits.filter { it.name.isNotBlank() || it.url.isNotBlank() }

    fun errors(): Set<FormError> = buildSet {
        if (name.isBlank()) add(FormError.NAME_MISSING)
        if (description.isBlank()) add(FormError.DESCRIPTION_MISSING)
        if (filledCredits.any { it.name.isBlank() }) add(FormError.SOURCE_NAME_MISSING)
        if (filledCredits.any { it.url.isNotBlank() && !isWebLink(it.url.trim()) }) add(FormError.SOURCE_LINK_INVALID)
    }

    /** [base] supplies what the form doesn't edit (id, source, key, switch, photo); null for a new exercise. */
    fun toExercise(base: Exercise?): Exercise {
        val fields = base ?: Exercise(name = "", tag = "", description = "", tier = tier, source = ExerciseSource.USER)
        return fields.copy(
            name = name.trim(),
            tag = tag.trim(),
            description = description.trim(),
            tier = tier,
            areas = areas,
            credits = filledCredits.map { Credit(it.name.trim(), it.url.trim().ifEmpty { null }) }
        )
    }

    companion object {
        fun from(exercise: Exercise) = ExerciseForm(
            name = exercise.name,
            tag = exercise.tag,
            description = exercise.description,
            tier = exercise.tier,
            areas = exercise.areas,
            credits = exercise.credits.map { CreditDraft(it.name, it.url.orEmpty()) }
        )

        // Only web links: the credit line opens them in a browser.
        private fun isWebLink(url: String) =
            (url.startsWith("https://") || url.startsWith("http://")) && url.length > url.indexOf("//") + 2 && ' ' !in url
    }
}
