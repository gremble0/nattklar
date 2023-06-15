package com.example.nattklar.model.dataobjects

import com.example.nattklar.R

/**
 * This variable maps all article categories to hashmaps, which again maps all entries of that
 * category to a drawable image.
 *
 * An article image can be obtained with articleImages["myArticleCategory"]?.get("myArticleTitle").
 */
val articleImages: HashMap<String,HashMap<String, Int>> = hashMapOf(
    "Stjernebilder" to hashMapOf(
        "Dragen" to R.drawable.dragen,
        "Fiskene" to R.drawable.fiskene,
        "Karlsvogna" to R.drawable.karlsvogna,
        "Krepsen" to R.drawable.krepsen,
        "Skorpionen" to R.drawable.skorpionen,
        "Skytten" to R.drawable.skytten,
        "Svanen" to R.drawable.svanen,
        "Tvillingene" to R.drawable.tvillingene,
        "Væren" to R.drawable.vaeren,
        "Vannmannen" to R.drawable.vannmannen,
        "Storebjørn" to R.drawable.storebjoern,
        "Orion" to R.drawable.orion,
        "Lillebjørn" to R.drawable.lillebjoern,
        "Jomfruen" to R.drawable.jomfruen,
        "Løven" to R.drawable.loeven,
        "Steinbukken" to R.drawable.steinbukken,
        "Tyren" to R.drawable.tyren,
        "Vekten" to R.drawable.vekten
    ),
    "Planeter & måner" to hashMapOf(
        "Merkur" to R.drawable.merkur,
        "Venus" to R.drawable.venus,
        "Mars" to R.drawable.mars,
        "Jupiter" to R.drawable.jupiter,
        "Saturn" to R.drawable.saturn,
        "Uranus" to R.drawable.uranus,
        "Neptun" to R.drawable.neptun
    ),
    "Stjerner" to hashMapOf(
        "Cygnus3" to R.drawable.planet,
    ),
    "Kometer" to hashMapOf(
        "Cygnus3" to R.drawable.planet,
    )
)

/**
 * This variable maps all categories of night events to a drawable image.
 */
val nightEventsImages: HashMap<String, Int> = hashMapOf(
    "planetary" to R.drawable.planetary,
    "meteor shower" to R.drawable.meteor_shower,
    "polar light" to R.drawable.polar_light,
    "lunisolar" to R.drawable.lunisolar
)

// Icons are taken from https://tabler-icons.io/
fun getTopicIconResource(topic: String): Int {
    return when (topic) {
        "Stjernebilder" -> R.drawable.zodiac
        "Planeter & måner" -> R.drawable.planet
        "Stjerner" -> R.drawable.star
        "Kometer" -> R.drawable.comet
        else -> R.drawable.reload
    }
}
