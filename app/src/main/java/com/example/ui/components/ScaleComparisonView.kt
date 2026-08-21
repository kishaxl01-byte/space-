package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CelestialBody
import com.example.model.CosmosData
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaleComparisonView(
    modifier: Modifier = Modifier,
    initialBody1: CelestialBody = CosmosData.getById("earth") ?: CosmosData.celestialBodies[3],
    initialBody2: CelestialBody = CosmosData.getById("jupiter") ?: CosmosData.celestialBodies[5]
) {
    var body1 by remember { mutableStateOf(initialBody1) }
    var body2 by remember { mutableStateOf(initialBody2) }
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Celestial Scale Comparison",
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color(0xFF38BDF8),
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "Select any two cosmic bodies to compare their physical sizes & mass",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Body 1 Selector
            ExposedDropdownMenuBox(
                expanded = expanded1,
                onExpandedChange = { expanded1 = !expanded1 },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = body1.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Body 1", fontSize = 11.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("scale_select_body1"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded1,
                    onDismissRequest = { expanded1 = false },
                    modifier = Modifier.background(Color(0xFF0F172A))
                ) {
                    CosmosData.celestialBodies.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name, color = Color.White) },
                            onClick = {
                                body1 = item
                                expanded1 = false
                            }
                        )
                    }
                }
            }

            // Body 2 Selector
            ExposedDropdownMenuBox(
                expanded = expanded2,
                onExpandedChange = { expanded2 = !expanded2 },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = body2.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Body 2", fontSize = 11.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("scale_select_body2"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded2,
                    onDismissRequest = { expanded2 = false },
                    modifier = Modifier.background(Color(0xFF0F172A))
                ) {
                    CosmosData.celestialBodies.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name, color = Color.White) },
                            onClick = {
                                body2 = item
                                expanded2 = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Comparison Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxRatio = maxOf(body1.relativeSizeRatio, body2.relativeSizeRatio, 1.0f)
                    val baseScale = min(size.width * 0.22f, size.height * 0.42f) / maxRatio

                    val r1 = body1.relativeSizeRatio * baseScale
                    val r2 = body2.relativeSizeRatio * baseScale

                    val c1 = Offset(size.width * 0.28f, size.height * 0.5f)
                    val c2 = Offset(size.width * 0.72f, size.height * 0.5f)

                    // Draw body 1
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to Color(body1.primaryColorHex),
                            1.0f to Color(body1.secondaryColorHex),
                            center = c1,
                            radius = r1
                        ),
                        radius = r1,
                        center = c1
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f),
                        radius = r1,
                        center = c1,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Draw body 2
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to Color(body2.primaryColorHex),
                            1.0f to Color(body2.secondaryColorHex),
                            center = c2,
                            radius = r2
                        ),
                        radius = r2,
                        center = c2
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f),
                        radius = r2,
                        center = c2,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // Comparison stats overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = body1.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(body1.primaryColorHex),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8)
                    )
                    Text(
                        text = body2.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(body2.primaryColorHex),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Side-by-side comparison property cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ComparisonStatRow("Diameter", body1.diameter, body2.diameter)
                ComparisonStatRow("Mass", body1.mass, body2.mass)
                ComparisonStatRow("Distance", body1.distance, body2.distance)
                ComparisonStatRow("Day Length", body1.dayLength, body2.dayLength)
                ComparisonStatRow("Gravity", body1.surfaceGravity, body2.surfaceGravity)
                ComparisonStatRow("Surface Temp", body1.surfaceTemp, body2.surfaceTemp)
            }
        }
    }
}

@Composable
private fun ComparisonStatRow(label: String, val1: String, val2: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = val1,
                fontSize = 12.sp,
                color = Color(0xFFF1F5F9),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = val2,
                fontSize = 12.sp,
                color = Color(0xFFFDE68A),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color(0xFF334155))
        )
    }
}
