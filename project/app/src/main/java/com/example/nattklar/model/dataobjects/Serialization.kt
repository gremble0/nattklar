package com.example.nattklar.model.dataobjects

import kotlinx.serialization.Serializable

/**
 * This data class is used for serializing the data inside the constellations.json asset.
 */
@Serializable
data class ConstellationData(
    val name: String,
    val stars: List<String>,
)

/**
 * This data class is used for serializing the data inside the stars.json asset.
 */
@Serializable
data class StarData(
    val name: String,
    val constellation: String,
    val rightAscension: RightAscension,
    val declination: Declination,
    val apparentMagnitude: Double, // may have value -1.0 to signal a missing value
    val absoluteMagnitude: Double, // may have value -1.0 to signal a missing value
    val distanceLightYear: Double, // may have value -1.0 to signal a missing value
    val spectralClass: String,
)

/**
 * This data class is used for serializing the the right ascension attribute in stars.json.
 */
@Serializable
data class RightAscension(
    val hours: Int,
    val minutes: Int,
    val seconds: Double,
)

/**
 * This data class is used for serializing the the declination attribute in stars.json.
 */
@Serializable
data class Declination(
    val degrees: Int,
    val minutes: Int,
    val seconds: Double,
)

/**
 * This data class is used for serializing the data inside the wiki-database.json asset.
 */
@Serializable
data class Article(
    val id: Int,
    val title: String?,
    val constellation: String?,
    val body: String?,
    val category: String?
)

/**
 * This data class is used for serializing the data inside the night-events.txt asset.
 */
data class NightEvent(
    val whenShown: String,
    val eventType: String,
    val title: String,
    val shortDescription : String,
    val description: String,
)