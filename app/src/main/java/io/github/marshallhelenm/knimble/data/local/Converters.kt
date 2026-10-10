package io.github.marshallhelenm.knimble.data.local

import androidx.room.TypeConverter
import io.github.marshallhelenm.knimble.model.Credit
import io.github.marshallhelenm.knimble.model.Tier

class Converters {
    @TypeConverter
    fun tiersToString(tiers: Set<Tier>): String = tiers.joinToString(",") { it.name }

    @TypeConverter
    fun stringToTiers(value: String): Set<Tier> =
        if (value.isEmpty()) emptySet() else value.split(",").map(Tier::valueOf).toSet()

    // One credit per line as "name<TAB>url"; neither a name nor a URL contains a tab or newline.
    @TypeConverter
    fun creditsToString(credits: List<Credit>): String = credits.joinToString("\n") { "${it.name}\t${it.url}" }

    @TypeConverter
    fun stringToCredits(value: String): List<Credit> =
        if (value.isEmpty()) emptyList() else value.lines().map { line ->
            val (name, url) = line.split('\t', limit = 2)
            Credit(name, url)
        }
}
