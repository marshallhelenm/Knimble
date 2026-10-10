package io.github.marshallhelenm.knimble.exercises

import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier

object ExerciseFilter {
    /**
     * Groups [exercises] by tier, smallest first, keeping only those that work any of [areas]. No areas means no
     * filter. Tiers left empty by the filter are dropped; without a filter every tier is kept.
     */
    fun byTier(exercises: List<Exercise>, areas: Set<BodyArea>): Map<Tier, List<Exercise>> {
        val shown = if (areas.isEmpty()) exercises else exercises.filter { it.areas.any(areas::contains) }
        return Tier.entries
            .associateWith { tier -> shown.filter { it.tier == tier } }
            .filter { (_, inTier) -> areas.isEmpty() || inTier.isNotEmpty() }
    }
}
