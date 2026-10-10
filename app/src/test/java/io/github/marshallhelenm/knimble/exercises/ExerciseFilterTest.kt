package io.github.marshallhelenm.knimble.exercises

import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import io.github.marshallhelenm.knimble.model.Tier
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseFilterTest {
    private fun exercise(id: Long, tier: Tier, vararg areas: BodyArea) = Exercise(
        id = id, name = "Exercise $id", tag = "", description = "", tier = tier,
        source = ExerciseSource.BUILT_IN, areas = areas.toSet()
    )

    private val wrists = exercise(1, Tier.SMALL, BodyArea.HANDS_WRISTS)
    private val neck = exercise(2, Tier.SMALL, BodyArea.NECK_SHOULDERS)
    private val threadNeedle = exercise(3, Tier.LARGE, BodyArea.NECK_SHOULDERS, BodyArea.BACK)
    private val squats = exercise(4, Tier.LARGE, BodyArea.HIPS_LEGS)
    private val all = listOf(wrists, neck, threadNeedle, squats)

    private fun ids(byTier: Map<Tier, List<Exercise>>) = byTier.mapValues { (_, list) -> list.map { it.id } }

    @Test
    fun noAreas_showsEverythingInEveryTier() {
        assertEquals(
            mapOf(Tier.SMALL to listOf(1L, 2L), Tier.MEDIUM to emptyList(), Tier.LARGE to listOf(3L, 4L)),
            ids(ExerciseFilter.byTier(all, emptySet()))
        )
    }

    @Test
    fun oneArea_keepsMatchesAndDropsEmptyTiers() {
        assertEquals(
            mapOf(Tier.SMALL to listOf(2L), Tier.LARGE to listOf(3L)),
            ids(ExerciseFilter.byTier(all, setOf(BodyArea.NECK_SHOULDERS)))
        )
    }

    @Test
    fun severalAreas_matchAny() {
        assertEquals(
            mapOf(Tier.SMALL to listOf(1L), Tier.LARGE to listOf(3L, 4L)),
            ids(ExerciseFilter.byTier(all, setOf(BodyArea.HANDS_WRISTS, BodyArea.BACK, BodyArea.HIPS_LEGS)))
        )
    }

    @Test
    fun areaWithNoExercises_isEmpty() {
        assertEquals(emptyMap<Tier, List<Long>>(), ids(ExerciseFilter.byTier(all, setOf(BodyArea.EYES))))
    }
}
