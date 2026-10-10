package io.github.marshallhelenm.knimble.exercises

import io.github.marshallhelenm.knimble.model.Tier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInExercisesTest {
    @Test
    fun keysAreUnique() {
        val keys = BuiltInExercises.all.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun everyTierHasExercises() {
        assertEquals(Tier.entries.toSet(), BuiltInExercises.all.map { it.tier }.toSet())
    }

    @Test
    fun everyEntryHasAllItsText() {
        BuiltInExercises.all.forEach {
            assertTrue(it.key, it.name.isNotBlank() && it.tag.isNotBlank() && it.description.isNotBlank())
        }
    }
}
