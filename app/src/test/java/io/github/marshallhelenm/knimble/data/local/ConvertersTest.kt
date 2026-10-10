package io.github.marshallhelenm.knimble.data.local

import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Credit
import io.github.marshallhelenm.knimble.model.Tier
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun credits_roundTrip() {
        val credits = listOf(
            Credit("WebMD", "https://www.webmd.com/a?b=c&d=e"),
            Credit("Home Physiotherapy Exercises", "https://homephysiotherapyexercises.com/x/")
        )
        assertEquals(credits, converters.stringToCredits(converters.creditsToString(credits)))
    }

    @Test
    fun noCredits_roundTrip() {
        assertEquals(emptyList<Credit>(), converters.stringToCredits(converters.creditsToString(emptyList())))
    }

    @Test
    fun tiers_roundTrip() {
        val tiers = setOf(Tier.SMALL, Tier.LARGE)
        assertEquals(tiers, converters.stringToTiers(converters.tiersToString(tiers)))
    }

    @Test
    fun areas_roundTrip() {
        val areas = setOf(BodyArea.NECK_SHOULDERS, BodyArea.BACK)
        assertEquals(areas, converters.stringToAreas(converters.areasToString(areas)))
        assertEquals(emptySet<BodyArea>(), converters.stringToAreas(converters.areasToString(emptySet())))
    }
}
