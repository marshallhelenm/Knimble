package io.github.marshallhelenm.knimble.exercises

import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier
import kotlin.random.Random

object ExercisePicker {
    /**
     * A random switched-on exercise from [tiers], skipping [previousId] when there's anything else to choose.
     * Null when nothing in those tiers is switched on.
     */
    fun pick(exercises: List<Exercise>, tiers: Set<Tier>, previousId: Long?, random: Random): Exercise? {
        val pool = exercises.filter { it.enabled && it.tier in tiers }
        if (pool.isEmpty()) return null
        return pool.filter { it.id != previousId }.ifEmpty { pool }.random(random)
    }
}
