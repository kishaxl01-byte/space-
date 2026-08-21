package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

data class Star(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Float,
    val color: Color
)

@Composable
fun StarfieldCanvas(
    modifier: Modifier = Modifier,
    starCount: Int = 110
) {
    val stars = remember {
        val random = Random(42)
        val starColors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFBAE6FD),
            Color(0xFFFDE68A),
            Color(0xFFE9D5FF),
            Color(0xFFFECDD3)
        )
        List(starCount) {
            Star(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                radius = random.nextFloat() * 1.8f + 0.6f,
                baseAlpha = random.nextFloat() * 0.6f + 0.3f,
                twinkleSpeed = random.nextFloat() * 2f + 1f,
                color = starColors[random.nextInt(starColors.size)]
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "starTwinkle")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Deep Space Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF070913),
                    Color(0xFF0B0F1F),
                    Color(0xFF0F172A),
                    Color(0xFF070913)
                )
            )
        )

        // Subtle Nebula Dust Clouds
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6B21A8).copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.8f, size.height * 0.2f),
                radius = size.width * 0.7f
            ),
            radius = size.width * 0.7f,
            center = Offset(size.width * 0.8f, size.height * 0.2f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0284C7).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.2f, size.height * 0.75f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(size.width * 0.2f, size.height * 0.75f)
        )

        // Draw Twinkling Stars
        stars.forEach { star ->
            val cx = star.xRatio * size.width
            val cy = star.yRatio * size.height
            val twinkle = (sin(time * star.twinkleSpeed + star.xRatio * 10f) + 1f) / 2f
            val currentAlpha = (star.baseAlpha * (0.4f + 0.6f * twinkle)).coerceIn(0.1f, 1.0f)

            drawCircle(
                color = star.color.copy(alpha = currentAlpha),
                radius = star.radius,
                center = Offset(cx, cy)
            )
        }
    }
}
