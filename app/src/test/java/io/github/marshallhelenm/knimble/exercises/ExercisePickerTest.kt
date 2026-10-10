package io.github.marshallhelenm.knimble.exercises

import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import io.github.marshallhelenm.knimble.model.Tier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class ExercisePickerTest {
    private fun exercise(id: Long, tier: Tier, enabled: Boolean = true) = Exercise(
        id = id, name = "Exercise $id", tag = "", description = "", tier = tier,
        source = ExerciseSource.BUILT_IN, enabled = enabled
    )

    private val pool = listOf(
        exercise(1, Tier.SMALL),
        exercise(2, Tier.SMALL),
        exercise(3, Tier.MEDIUM),
        exercise(4, Tier.LARGE),
        exercise(5, Tier.LARGE, enabled = false)
    )

    private val allTiers = Tier.entries.toSet()

    private fun pickedIds(tiers: Set<Tier>, previousId: Long? = null) =
        (0 until 300).mapNotNull { ExercisePicker.pick(pool, tiers, previousId, Random(it))?.id }.toSet()

    @Test
    fun allTiers_reachesEveryEnabledExercise() {
        assertEquals(setOf(1L, 2L, 3L, 4L), pickedIds(allTiers))
    }

    @Test
    fun onlyPicksFromChosenTiers() {
        assertEquals(setOf(1L, 2L, 3L), pickedIds(setOf(Tier.SMALL, Tier.MEDIUM)))
    }

    @Test
    fun neverPicksSwitchedOff() {
        assertEquals(setOf(4L), pickedIds(setOf(Tier.LARGE)))
    }

    @Test
    fun avoidsRepeatWhenThereIsAnAlternative() {
        repeat(200) { seed ->
            assertNotEquals(1L, ExercisePicker.pick(pool, setOf(Tier.SMALL), 1L, Random(seed))?.id)
        }
    }

    @Test
    fun onlyOneChoice_repeatsIt() {
        assertEquals(4L, ExercisePicker.pick(pool, setOf(Tier.LARGE), 4L, Random(1))?.id)
    }

    @Test
    fun everythingInTiersSwitchedOff_isNull() {
        val off = pool.map { it.copy(enabled = false) }
        assertNull(ExercisePicker.pick(off, allTiers, null, Random(1)))
    }

    @Test
    fun noTiersChosen_isNull() {
        assertNull(ExercisePicker.pick(pool, emptySet(), null, Random(1)))
    }
}
