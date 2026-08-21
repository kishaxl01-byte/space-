package com.example.model

enum class CelestialCategory(val title: String, val iconName: String) {
    PLANETS("Planets & Sun", "Planet"),
    MOONS("Major Moons", "Moon"),
    GALAXIES("Galaxies & Deep Space", "Galaxy"),
    EXOPLANETS("Solar Systems & Exoplanets", "Exoplanet"),
    NASA_MISSIONS("NASA & Space Exploration", "NASA"),
    COSMIC_CONCEPTS("Cosmic Fundamentals", "Theory")
}

data class PlanetLayer(
    val name: String,
    val thickness: String,
    val composition: String,
    val colorHex: Long
)

data class CelestialBody(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: CelestialCategory,
    val overview: String,
    val keyFacts: List<String>,
    // Physical stats
    val diameter: String,
    val mass: String,
    val distance: String,
    val orbitalPeriod: String,
    val dayLength: String,
    val surfaceGravity: String,
    val surfaceTemp: String,
    val atmosphere: String,
    val moonsCount: Int = 0,
    // Deep info
    val discoveryYear: String = "Ancient",
    val historicalSignificance: String,
    val nasaMissions: List<String> = emptyList(),
    val structureLayers: List<PlanetLayer> = emptyList(),
    val textureType: String = "earth", // For 3D renderer
    val primaryColorHex: Long = 0xFF38BDF8,
    val secondaryColorHex: Long = 0xFF0284C7,
    val hasRings: Boolean = false,
    val ringColorHex: Long = 0xFFFDE68A,
    val ringInnerRatio: Float = 1.3f,
    val ringOuterRatio: Float = 2.2f,
    val relativeSizeRatio: Float = 1.0f // Scale relative to Earth = 1.0
)
