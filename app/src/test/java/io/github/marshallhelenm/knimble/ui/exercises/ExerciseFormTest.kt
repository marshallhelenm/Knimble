package io.github.marshallhelenm.knimble.ui.exercises

import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Credit
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import io.github.marshallhelenm.knimble.model.Tier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseFormTest {
    private val valid = ExerciseForm(name = "Shrugs", description = "Lift and drop.")

    @Test
    fun validForm_hasNoErrors() {
        assertEquals(emptySet<FormError>(), valid.errors())
    }

    @Test
    fun blankNameAndDescription_areErrors() {
        assertEquals(
            setOf(FormError.NAME_MISSING, FormError.DESCRIPTION_MISSING),
            ExerciseForm(name = "  ", description = "\n").errors()
        )
    }

    @Test
    fun emptySourceRows_areIgnored() {
        assertEquals(emptySet<FormError>(), valid.copy(credits = listOf(CreditDraft(" ", ""))).errors())
        assertEquals(emptyList<Credit>(), valid.copy(credits = listOf(CreditDraft())).toExercise(null).credits)
    }

    @Test
    fun sourceWithLinkButNoName_isAnError() {
        assertEquals(
            setOf(FormError.SOURCE_NAME_MISSING),
            valid.copy(credits = listOf(CreditDraft(url = "https://example.com"))).errors()
        )
    }

    @Test
    fun sourceLink_mustBeAWebLink() {
        listOf("example.com", "ftp://example.com", "https://", "https://exa mple.com").forEach { url ->
            assertTrue(url, FormError.SOURCE_LINK_INVALID in valid.copy(credits = listOf(CreditDraft("Site", url))).errors())
        }
        listOf("https://example.com/page", "http://example.com", "  https://example.com  ").forEach { url ->
            assertEquals(url, emptySet<FormError>(), valid.copy(credits = listOf(CreditDraft("Site", url))).errors())
        }
    }

    @Test
    fun toExercise_new_trimsAndIsTheUsersOwn() {
        val form = ExerciseForm(
            name = " Shrugs ", tag = " Up, down ", description = " Lift. ", tier = Tier.MEDIUM,
            areas = setOf(BodyArea.NECK_SHOULDERS),
            credits = listOf(CreditDraft(" My physio ", " "), CreditDraft("Site", " https://example.com "))
        )

        val exercise = form.toExercise(null)

        assertEquals("Shrugs", exercise.name)
        assertEquals("Up, down", exercise.tag)
        assertEquals("Lift.", exercise.description)
        assertEquals(Tier.MEDIUM, exercise.tier)
        assertEquals(setOf(BodyArea.NECK_SHOULDERS), exercise.areas)
        assertEquals(listOf(Credit("My physio", null), Credit("Site", "https://example.com")), exercise.credits)
        assertEquals(ExerciseSource.USER, exercise.source)
    }

    @Test
    fun toExercise_existing_keepsWhatTheFormDoesntEdit() {
        val base = Exercise(
            id = 7, name = "Old", tag = "t", description = "d", tier = Tier.SMALL, source = ExerciseSource.BUILT_IN,
            builtInKey = "neck", enabled = false, imageUri = "file:///photo.jpg"
        )

        val exercise = ExerciseForm.from(base).copy(name = "New").toExercise(base)

        assertEquals(base.copy(name = "New"), exercise)
    }

    @Test
    fun from_roundTripsAnExercise() {
        val base = Exercise(
            name = "Neck tilt", tag = "Ear down", description = "Tilt.", tier = Tier.SMALL,
            source = ExerciseSource.BUILT_IN, areas = setOf(BodyArea.NECK_SHOULDERS),
            credits = listOf(Credit("a doctor's exercise sheet"), Credit("Site", "https://example.com"))
        )

        assertEquals(base, ExerciseForm.from(base).toExercise(base))
    }
}
