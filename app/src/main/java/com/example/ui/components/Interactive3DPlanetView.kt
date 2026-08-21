package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CelestialBody
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun Interactive3DPlanetView(
    body: CelestialBody,
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
    initialCutaway: Boolean = false
) {
    var rotationX by remember { mutableFloatStateOf(15f) } // Pitch
    var rotationY by remember { mutableFloatStateOf(0f) }  // Yaw
    var scale by remember { mutableFloatStateOf(1.0f) }
    var autoRotate by remember { mutableStateOf(true) }
    var showCutaway by remember { mutableStateOf(initialCutaway) }
    var lightingMode by remember { mutableStateOf(true) } // Day/Night dynamic lighting vs studio light

    val infiniteTransition = rememberInfiniteTransition(label = "planetSpin")
    val animatedSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val currentYaw = if (autoRotate) (rotationY + animatedSpin) % 360f else rotationY

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.65f, 2.4f)
                    rotationY += pan.x * 0.5f
                    rotationX = (rotationX - pan.y * 0.5f).coerceIn(-80f, 80f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Main 3D Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("planet_3d_canvas")
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = min(size.width, size.height) * 0.33f * scale

            val pitchRad = Math.toRadians(rotationX.toDouble()).toFloat()
            val yawRad = Math.toRadians(currentYaw.toDouble()).toFloat()

            // Draw celestial body according to its texture type & properties
            drawCelestialBody3D(
                body = body,
                center = center,
                radius = baseRadius,
                pitchRad = pitchRad,
                yawRad = yawRad,
                lightingMode = lightingMode,
                showCutaway = showCutaway
            )
        }

        // Floating Control Bar
        if (showControls) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xDD0F172A),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Auto-spin toggle
                    FilterChip(
                        selected = autoRotate,
                        onClick = { autoRotate = !autoRotate },
                        label = { Text("Spin", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Toggle auto-spin",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color.Black,
                            selectedLeadingIconColor = Color.Black
                        )
                    )

                    // Cutaway layers toggle (if layers available)
                    if (body.structureLayers.isNotEmpty()) {
                        FilterChip(
                            selected = showCutaway,
                            onClick = { showCutaway = !showCutaway },
                            label = { Text("Core Layers", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Inspect core structure",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF59E0B),
                                selectedLabelColor = Color.Black,
                                selectedLeadingIconColor = Color.Black
                            )
                        )
                    }

                    // Lighting toggle
                    FilterChip(
                        selected = lightingMode,
                        onClick = { lightingMode = !lightingMode },
                        label = { Text(if (lightingMode) "Sun Shadow" else "Ambient", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Toggle lighting",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFA855F7),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )

                    // Reset button
                    IconButton(
                        onClick = {
                            rotationX = 15f
                            rotationY = 0f
                            scale = 1.0f
                            showCutaway = false
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset camera orientation",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCelestialBody3D(
    body: CelestialBody,
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float,
    lightingMode: Boolean,
    showCutaway: Boolean
) {
    val primaryColor = Color(body.primaryColorHex)
    val secondaryColor = Color(body.secondaryColorHex)

    // 1. Back rings (if planet has rings, draw the portion behind the planet first)
    if (body.hasRings) {
        drawSaturnRings(
            center = center,
            innerRadius = radius * body.ringInnerRatio,
            outerRadius = radius * body.ringOuterRatio,
            pitchRad = pitchRad,
            ringColor = Color(body.ringColorHex),
            isBackPortion = true
        )
    }

    // 2. Outer atmospheric glow / corona
    if (body.textureType == "sun") {
        drawSunGlow(center, radius)
    } else if (body.textureType == "black_hole") {
        drawBlackHoleAccretion(center, radius, pitchRad, yawRad)
    } else {
        // Subtle planetary atmospheric haze
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.25f
            ),
            radius = radius * 1.25f,
            center = center
        )
    }

    // 3. Render 3D planetary sphere with bands, craters, continents, and lighting
    if (!showCutaway) {
        drawSphericalPlanet(
            body = body,
            center = center,
            radius = radius,
            pitchRad = pitchRad,
            yawRad = yawRad,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            lightingMode = lightingMode
        )
    } else {
        // 3D Core Cutaway Cross Section
        drawPlanetCutaway(
            body = body,
            center = center,
            radius = radius,
            pitchRad = pitchRad,
            yawRad = yawRad
        )
    }

    // 4. Front rings (draw portion in front of the planet)
    if (body.hasRings) {
        drawSaturnRings(
            center = center,
            innerRadius = radius * body.ringInnerRatio,
            outerRadius = radius * body.ringOuterRatio,
            pitchRad = pitchRad,
            ringColor = Color(body.ringColorHex),
            isBackPortion = false
        )
    }
}

private fun DrawScope.drawSphericalPlanet(
    body: CelestialBody,
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float,
    primaryColor: Color,
    secondaryColor: Color,
    lightingMode: Boolean
) {
    // Base planetary sphere fill
    drawCircle(
        color = primaryColor,
        radius = radius,
        center = center
    )

    when (body.textureType) {
        "earth" -> drawEarthFeatures(center, radius, pitchRad, yawRad)
        "mars" -> drawMarsFeatures(center, radius, pitchRad, yawRad)
        "jupiter" -> drawJupiterBands(center, radius, pitchRad, yawRad)
        "saturn" -> drawSaturnBands(center, radius, pitchRad, yawRad)
        "sun" -> drawSunPlasma(center, radius, yawRad)
        "venus" -> drawVenusCloudBands(center, radius, pitchRad, yawRad)
        "mercury", "moon" -> drawCrateredTerrain(center, radius, pitchRad, yawRad, primaryColor, secondaryColor)
        "uranus", "neptune" -> drawIceGiantHaze(center, radius, pitchRad, primaryColor, secondaryColor)
        "pluto" -> drawPlutoHeart(center, radius, pitchRad, yawRad)
        "europa" -> drawEuropaCracks(center, radius, pitchRad, yawRad)
        "titan" -> drawTitanLakes(center, radius, pitchRad, yawRad)
        "black_hole" -> drawBlackHoleShadow(center, radius)
        else -> drawGenericFeatures(center, radius, primaryColor, secondaryColor, pitchRad, yawRad)
    }

    // Atmospheric Fresnel Rim Glow
    drawCircle(
        brush = Brush.radialGradient(
            0.75f to Color.Transparent,
            0.92f to primaryColor.copy(alpha = 0.45f),
            1.0f to Color.White.copy(alpha = 0.85f),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Dynamic 3D Sunlight & Shadow Terminator
    if (lightingMode && body.textureType != "sun" && body.textureType != "black_hole") {
        val lightOffset = Offset(center.x - radius * 0.45f, center.y - radius * 0.45f)
        // Specular reflection
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.40f),
                    Color.White.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = lightOffset,
                radius = radius * 0.65f
            ),
            radius = radius,
            center = center
        )

        // Day-Night Shadow Terminator gradient
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to Color.Transparent,
                0.55f to Color.Transparent,
                0.85f to Color.Black.copy(alpha = 0.65f),
                1.0f to Color.Black.copy(alpha = 0.88f),
                center = lightOffset,
                radius = radius * 1.45f
            ),
            radius = radius,
            center = center
        )
    }
}

// -------------------------------------------------------------
// PLANETARY TEXTURE RENDERERS (Earth, Mars, Jupiter, Saturn, etc.)
// -------------------------------------------------------------

private fun DrawScope.drawEarthFeatures(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float
) {
    // Ocean base
    drawCircle(color = Color(0xFF0284C7), radius = radius, center = center)

    // Continents & Landmasses rendered via spherical curves
    val landColor = Color(0xFF15803D)
    val aridLand = Color(0xFFCA8A04)

    for (i in 0..5) {
        val lonOffset = (i * 60f + Math.toDegrees(yawRad.toDouble()).toFloat()) % 360f
        val radOffset = Math.toRadians(lonOffset.toDouble()).toFloat()
        val xPos = center.x + radius * sin(radOffset) * cos(pitchRad)
        val isVisible = cos(radOffset) > -0.2f

        if (isVisible) {
            val landWidth = radius * 0.45f * abs(cos(radOffset))
            val landHeight = radius * 0.55f
            val yPos = center.y + radius * sin(pitchRad) * 0.3f + (i % 3 - 1) * radius * 0.25f

            drawOval(
                color = if (i % 2 == 0) landColor else aridLand,
                topLeft = Offset(xPos - landWidth / 2f, yPos - landHeight / 2f),
                size = Size(landWidth, landHeight)
            )
        }
    }

    // Swirling White Clouds
    val cloudColor = Color.White.copy(alpha = 0.65f)
    for (j in 0..4) {
        val cloudLon = (j * 75f + Math.toDegrees(yawRad.toDouble()).toFloat() * 1.3f) % 360f
        val cloudRad = Math.toRadians(cloudLon.toDouble()).toFloat()
        if (cos(cloudRad) > -0.1f) {
            val cx = center.x + radius * sin(cloudRad) * 0.9f
            val cy = center.y + (j - 2) * radius * 0.28f + sin(cloudRad * 2f) * 15f
            drawOval(
                color = cloudColor,
                topLeft = Offset(cx - radius * 0.25f, cy - 8.dp.toPx()),
                size = Size(radius * 0.5f, 16.dp.toPx())
            )
        }
    }

    // Polar Ice Caps
    drawOval(
        color = Color(0xFFF0FDF4),
        topLeft = Offset(center.x - radius * 0.45f, center.y - radius * 0.95f),
        size = Size(radius * 0.9f, radius * 0.25f)
    )
    drawOval(
        color = Color(0xFFF0FDF4),
        topLeft = Offset(center.x - radius * 0.4f, center.y + radius * 0.75f),
        size = Size(radius * 0.8f, radius * 0.25f)
    )
}

private fun DrawScope.drawMarsFeatures(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float
) {
    // Red planet rust base
    drawCircle(color = Color(0xFFDC2626), radius = radius, center = center)

    // Dark volcanic basalt regions (Syrtis Major, Acidalia Planitia)
    val darkBasalt = Color(0xFF7F1D1D)
    for (i in 0..3) {
        val lon = (i * 90f + Math.toDegrees(yawRad.toDouble()).toFloat()) % 360f
        val rad = Math.toRadians(lon.toDouble()).toFloat()
        if (cos(rad) > -0.2f) {
            val x = center.x + radius * sin(rad) * 0.8f
            val y = center.y + (i - 1.5f) * radius * 0.35f
            drawOval(
                color = darkBasalt,
                topLeft = Offset(x - radius * 0.3f, y - radius * 0.15f),
                size = Size(radius * 0.6f, radius * 0.3f)
            )
        }
    }

    // Olympus Mons caldera highlight
    val volcanoLon = (45f + Math.toDegrees(yawRad.toDouble()).toFloat()) % 360f
    val vRad = Math.toRadians(volcanoLon.toDouble()).toFloat()
    if (cos(vRad) > 0.1f) {
        val vx = center.x + radius * sin(vRad) * 0.7f
        val vy = center.y - radius * 0.1f
        drawCircle(
            color = Color(0xFF991B1B),
            radius = radius * 0.09f,
            center = Offset(vx, vy)
        )
        drawCircle(
            color = Color(0xFFFCA5A5).copy(alpha = 0.5f),
            radius = radius * 0.04f,
            center = Offset(vx, vy)
        )
    }

    // North & South Polar Ice Caps (CO2 & Water Ice)
    drawOval(
        color = Color(0xFFFEF2F2),
        topLeft = Offset(center.x - radius * 0.35f, center.y - radius * 0.96f),
        size = Size(radius * 0.7f, radius * 0.18f)
    )
    drawOval(
        color = Color(0xFFFEF2F2),
        topLeft = Offset(center.x - radius * 0.3f, center.y + radius * 0.8f),
        size = Size(radius * 0.6f, radius * 0.18f)
    )
}

private fun DrawScope.drawJupiterBands(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float
) {
    val colors = listOf(
        Color(0xFFFED7AA), // North Polar Hood
        Color(0xFFEA580C), // North Temperate Belt
        Color(0xFFFFFBEB), // North Tropical Zone
        Color(0xFF9A3412), // North Equatorial Belt
        Color(0xFFFEF08A), // Equatorial Zone
        Color(0xFF7C2D12), // South Equatorial Belt
        Color(0xFFFED7AA), // South Tropical Zone
        Color(0xFFC2410C), // South Temperate Belt
        Color(0xFFFDBA74)  // South Polar Region
    )

    val bandCount = colors.size
    val step = (radius * 2f) / bandCount

    for (i in 0 until bandCount) {
        val top = center.y - radius + i * step
        val yCenter = top + step / 2f
        val dy = yCenter - center.y
        if (abs(dy) < radius) {
            val halfWidth = sqrt(radius * radius - dy * dy)
            drawOval(
                color = colors[i],
                topLeft = Offset(center.x - halfWidth, top),
                size = Size(halfWidth * 2f, step * 1.1f)
            )
        }
    }

    // Great Red Spot
    val spotLon = (120f + Math.toDegrees(yawRad.toDouble()).toFloat()) % 360f
    val spotRad = Math.toRadians(spotLon.toDouble()).toFloat()
    if (cos(spotRad) > -0.2f) {
        val spotX = center.x + radius * sin(spotRad) * 0.75f
        val spotY = center.y + radius * 0.28f
        val spotWidth = radius * 0.28f * abs(cos(spotRad))
        val spotHeight = radius * 0.18f
        drawOval(
            color = Color(0xFFB91C1C),
            topLeft = Offset(spotX - spotWidth / 2f, spotY - spotHeight / 2f),
            size = Size(spotWidth, spotHeight)
        )
        drawOval(
            color = Color(0xFFF87171),
            topLeft = Offset(spotX - spotWidth / 4f, spotY - spotHeight / 4f),
            size = Size(spotWidth / 2f, spotHeight / 2f)
        )
    }
}

private fun DrawScope.drawSaturnBands(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float
) {
    val bands = listOf(
        Color(0xFFFDE68A),
        Color(0xFFD97706),
        Color(0xFFFEF3C7),
        Color(0xFFB45309),
        Color(0xFFFDE047),
        Color(0xFFCA8A04),
        Color(0xFFFEF08A)
    )
    val step = (radius * 2f) / bands.size
    for (i in bands.indices) {
        val top = center.y - radius + i * step
        val yCenter = top + step / 2f
        val dy = yCenter - center.y
        if (abs(dy) < radius) {
            val halfWidth = sqrt(radius * radius - dy * dy)
            drawOval(
                color = bands[i],
                topLeft = Offset(center.x - halfWidth, top),
                size = Size(halfWidth * 2f, step * 1.1f)
            )
        }
    }
}

private fun DrawScope.drawSaturnRings(
    center: Offset,
    innerRadius: Float,
    outerRadius: Float,
    pitchRad: Float,
    ringColor: Color,
    isBackPortion: Boolean
) {
    // Elliptical ring projection
    val tiltY = max(0.18f, abs(sin(pitchRad + 0.35f)))
    val numSubRings = 7

    for (r in 0..numSubRings) {
        val fraction = r.toFloat() / numSubRings
        val currentR = innerRadius + (outerRadius - innerRadius) * fraction
        val ringWidth = currentR * 2f
        val ringHeight = currentR * 2f * tiltY

        // Cassini Division gap
        val isCassiniGap = fraction in 0.52f..0.62f
        val subColor = if (isCassiniGap) Color.Transparent else ringColor.copy(
            alpha = (0.35f + (1f - fraction) * 0.45f).coerceIn(0f, 1f)
        )

        val topLeft = Offset(center.x - ringWidth / 2f, center.y - ringHeight / 2f)
        val sweepAngle = if (isBackPortion) 180f else 180f
        val startAngle = if (isBackPortion) 180f else 0f

        drawArc(
            color = subColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = Size(ringWidth, ringHeight),
            style = Stroke(width = (outerRadius - innerRadius) / numSubRings * 0.9f)
        )
    }
}

private fun DrawScope.drawSunPlasma(center: Offset, radius: Float, yawRad: Float) {
    // Dynamic solar convective granulation
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to Color(0xFFFFFBEB),
            0.4f to Color(0xFFFBBF24),
            0.8f to Color(0xFFF97316),
            1.0f to Color(0xFFEA580C),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Solar flares and sunspots
    val spotColor = Color(0xFF7C2D12)
    for (i in 0..4) {
        val lon = (i * 72f + Math.toDegrees(yawRad.toDouble()).toFloat()) % 360f
        val rad = Math.toRadians(lon.toDouble()).toFloat()
        if (cos(rad) > 0.0f) {
            val sx = center.x + radius * sin(rad) * 0.6f
            val sy = center.y + (i % 3 - 1) * radius * 0.35f
            drawCircle(
                color = spotColor,
                radius = radius * 0.05f,
                center = Offset(sx, sy)
            )
        }
    }
}

private fun DrawScope.drawSunGlow(center: Offset, radius: Float) {
    // Solar Corona & Flare Rays
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to Color(0xFFFBBF24).copy(alpha = 0.5f),
            0.6f to Color(0xFFF97316).copy(alpha = 0.25f),
            1.0f to Color.Transparent,
            center = center,
            radius = radius * 1.8f
        ),
        radius = radius * 1.8f,
        center = center
    )

    // Pulsing Flare Spokes
    for (deg in 0..360 step 30) {
        val rad = Math.toRadians(deg.toDouble()).toFloat()
        val flareLen = radius * 1.45f
        val x2 = center.x + flareLen * cos(rad)
        val y2 = center.y + flareLen * sin(rad)
        drawLine(
            color = Color(0xFFFBBF24).copy(alpha = 0.28f),
            start = center,
            end = Offset(x2, y2),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawVenusCloudBands(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to Color(0xFFFEF3C7),
            0.7f to Color(0xFFFBBF24),
            1.0f to Color(0xFFD97706),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Swirling Sulfuric Acid V-shaped clouds
    val cloudHaze = Color(0xFFB45309).copy(alpha = 0.35f)
    for (i in -3..3) {
        val y = center.y + i * radius * 0.22f
        drawOval(
            color = cloudHaze,
            topLeft = Offset(center.x - radius * 0.85f, y - 6.dp.toPx()),
            size = Size(radius * 1.7f, 14.dp.toPx())
        )
    }
}

private fun DrawScope.drawCrateredTerrain(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    // Craters and Maria
    for (i in 0..7) {
        val lon = (i * 45f + Math.toDegrees(yawRad.toDouble()).toFloat()) % 360f
        val rad = Math.toRadians(lon.toDouble()).toFloat()
        if (cos(rad) > -0.1f) {
            val cx = center.x + radius * sin(rad) * 0.75f
            val cy = center.y + (i % 5 - 2) * radius * 0.32f
            val cRadius = radius * (0.08f + (i % 3) * 0.05f)

            // Crater Basin
            drawCircle(
                color = secondaryColor.copy(alpha = 0.6f),
                radius = cRadius,
                center = Offset(cx, cy)
            )
            // Crater Rim Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = cRadius,
                center = Offset(cx - 2f, cy - 2f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawIceGiantHaze(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to primaryColor,
            0.75f to secondaryColor,
            1.0f to Color(0xFF0F172A),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawPlutoHeart(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float
) {
    drawCircle(color = Color(0xFFB45309), radius = radius, center = center)

    // Tombaugh Regio Heart-shaped Nitrogen Glacier
    val heartLon = (70f + Math.toDegrees(yawRad.toDouble()).toFloat()) % 360f
    val hRad = Math.toRadians(heartLon.toDouble()).toFloat()
    if (cos(hRad) > -0.2f) {
        val hx = center.x + radius * sin(hRad) * 0.7f
        val hy = center.y + radius * 0.1f
        val heartColor = Color(0xFFFEF3C7)

        // Left and Right lobes of the heart
        val lobeW = radius * 0.28f * abs(cos(hRad))
        val lobeH = radius * 0.38f
        drawOval(
            color = heartColor,
            topLeft = Offset(hx - lobeW * 0.8f, hy - lobeH * 0.4f),
            size = Size(lobeW, lobeH)
        )
        drawOval(
            color = heartColor,
            topLeft = Offset(hx - lobeW * 0.2f, hy - lobeH * 0.4f),
            size = Size(lobeW, lobeH)
        )
    }
}

private fun DrawScope.drawEuropaCracks(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float
) {
    drawCircle(color = Color(0xFFE0F2FE), radius = radius, center = center)

    // Reddish-brown Lineae (Tidal fractures)
    val crackColor = Color(0xFF9A3412).copy(alpha = 0.75f)
    for (i in 0..6) {
        val lon = (i * 50f + Math.toDegrees(yawRad.toDouble()).toFloat()) % 360f
        val rad = Math.toRadians(lon.toDouble()).toFloat()
        if (cos(rad) > -0.1f) {
            val sx = center.x + radius * sin(rad) * 0.7f
            val sy = center.y + (i % 4 - 1.5f) * radius * 0.4f
            val ex = sx + (i % 3 - 1) * radius * 0.5f
            val ey = sy + radius * 0.35f
            drawLine(
                color = crackColor,
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawTitanLakes(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float
) {
    // Dense orange photochemical haze
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to Color(0xFFFDE68A),
            0.65f to Color(0xFFF59E0B),
            1.0f to Color(0xFF92400E),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Dark liquid methane lakes at north pole (Kraken Mare)
    val lakeColor = Color(0xFF1E293B)
    drawOval(
        color = lakeColor,
        topLeft = Offset(center.x - radius * 0.3f, center.y - radius * 0.85f),
        size = Size(radius * 0.6f, radius * 0.2f)
    )
}

private fun DrawScope.drawBlackHoleAccretion(
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float
) {
    // Relativistic Glowing Accretion Disk (gravitationally bent)
    drawOval(
        brush = Brush.radialGradient(
            0.0f to Color.Transparent,
            0.45f to Color(0xFFF97316),
            0.7f to Color(0xFFFBBF24),
            1.0f to Color.Transparent,
            center = center,
            radius = radius * 2.2f
        ),
        topLeft = Offset(center.x - radius * 2.2f, center.y - radius * 0.6f),
        size = Size(radius * 4.4f, radius * 1.2f)
    )
    // Gravitational lensing upper halo
    drawArc(
        brush = Brush.radialGradient(
            0.0f to Color(0xFFF59E0B).copy(alpha = 0.85f),
            1.0f to Color.Transparent,
            center = center,
            radius = radius * 1.6f
        ),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 1.4f, center.y - radius * 1.4f),
        size = Size(radius * 2.8f, radius * 2.8f),
        style = Stroke(width = 16.dp.toPx())
    )
}

private fun DrawScope.drawBlackHoleShadow(center: Offset, radius: Float) {
    // Pure Black Event Horizon
    drawCircle(
        color = Color(0xFF000000),
        radius = radius * 0.95f,
        center = center
    )
    // Photon Sphere Ring
    drawCircle(
        color = Color(0xFFFDE68A).copy(alpha = 0.9f),
        radius = radius * 0.98f,
        center = center,
        style = Stroke(width = 3.dp.toPx())
    )
}

private fun DrawScope.drawGenericFeatures(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    secondaryColor: Color,
    pitchRad: Float,
    yawRad: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to primaryColor,
            0.8f to secondaryColor,
            1.0f to Color(0xFF0B0D17),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

// -------------------------------------------------------------
// 3D CORE CUTAWAY / LAYER INSPECTOR
// -------------------------------------------------------------

private fun DrawScope.drawPlanetCutaway(
    body: CelestialBody,
    center: Offset,
    radius: Float,
    pitchRad: Float,
    yawRad: Float
) {
    val layers = body.structureLayers
    if (layers.isEmpty()) {
        drawCircle(color = Color(body.primaryColorHex), radius = radius, center = center)
        return
    }

    val layerCount = layers.size
    val stepRadius = radius / layerCount

    // Draw back outer sphere
    drawCircle(
        color = Color(body.secondaryColorHex).copy(alpha = 0.35f),
        radius = radius,
        center = center
    )

    // Draw concentric 3D shell layers
    for (i in layerCount - 1 downTo 0) {
        val layer = layers[i]
        val layerR = stepRadius * (i + 1)
        val layerColor = Color(layer.colorHex)

        // Concentric disc cross-section
        drawCircle(
            color = layerColor,
            radius = layerR,
            center = center
        )

        // 3D Isometric cutaway quadrant slice
        val path = Path().apply {
            moveTo(center.x, center.y)
            lineTo(center.x + layerR, center.y)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    center.x - layerR,
                    center.y - layerR,
                    center.x + layerR,
                    center.y + layerR
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(
            path = path,
            color = layerColor.copy(alpha = 0.85f),
            style = Fill
        )
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.4f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }

    // Outer Crust Ring indicator
    drawCircle(
        color = Color.White.copy(alpha = 0.7f),
        radius = radius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
}
