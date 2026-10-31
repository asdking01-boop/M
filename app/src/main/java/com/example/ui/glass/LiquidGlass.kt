package com.example.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassRedStroke
import com.example.ui.theme.GlassWhiteFill
import com.example.ui.theme.GlassWhiteReflection
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Premium iOS 26 Inspired Liquid Glass (Glassmorphism) Modifier:
 * Simulates ultra-modern frosted glass with realistic refraction,
 * soft ambient lighting, edge rim reflections, and depth.
 */
fun Modifier.glassBackground(
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color = Color(0xD011141C),
    borderColor: Color = GlassWhiteStroke,
    borderWidth: Dp = 1.dp,
    hasRedGlow: Boolean = false,
    elevation: Dp = 10.dp,
    highlightIntensity: Float = 0.24f
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = if (hasRedGlow) KrasRed.copy(alpha = 0.40f) else Color(0x66000000),
        spotColor = if (hasRedGlow) KrasRed.copy(alpha = 0.55f) else Color(0x88000000)
    )
    .clip(shape)
    // Layer 1: Translucent Frosted Glass Base Gradient
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.88f),
                backgroundColor.copy(alpha = 0.65f),
                backgroundColor.copy(alpha = 0.92f)
            )
        )
    )
    // Layer 2: Ambient Diffuse Glow & Inner Glass Refraction
    .background(
        brush = Brush.radialGradient(
            colors = listOf(
                if (hasRedGlow) KrasRed.copy(alpha = 0.15f) else Color(0x18FFFFFF),
                Color.Transparent
            ),
            center = Offset(100f, 60f),
            radius = 450f
        )
    )
    // Layer 3: Specular Top-Left Diagonal Sheen (iOS 26 Liquid Glass characteristic)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = highlightIntensity),
                Color.White.copy(alpha = highlightIntensity * 0.35f),
                Color.Transparent,
                if (hasRedGlow) KrasRed.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f)
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 600f)
        )
    )
    // Layer 4: Micro-Prismatic Border Highlight with Top Rim Reflection
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                if (hasRedGlow) KrasRedBright.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.55f),
                if (hasRedGlow) GlassRedStroke else borderColor.copy(alpha = 0.25f),
                Color.White.copy(alpha = 0.10f),
                if (hasRedGlow) KrasRed.copy(alpha = 0.50f) else borderColor.copy(alpha = 0.15f)
            ),
            start = Offset(0f, 0f),
            end = Offset.Infinite
        ),
        shape = shape
    )

/**
 * Backward compatibility alias for liquidGlass.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color = Color(0xD011141C),
    borderColor: Color = GlassWhiteStroke,
    borderWidth: Dp = 1.dp,
    hasRedGlow: Boolean = false,
    elevation: Dp = 10.dp
): Modifier = glassBackground(
    shape = shape,
    backgroundColor = backgroundColor,
    borderColor = borderColor,
    borderWidth = borderWidth,
    hasRedGlow = hasRedGlow,
    elevation = elevation
)

/**
 * Reusable GlassCard container.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color(0xD0131720),
    borderColor: Color = GlassWhiteStroke,
    hasRedGlow: Boolean = false,
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glassBackground(
            shape = shape,
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            hasRedGlow = hasRedGlow,
            elevation = elevation
        ),
        content = content
    )
}

/**
 * Liquid Glass Pill Button for categories, tabs, and action controls.
 */
@Composable
fun GlassCategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(),
        label = "chipScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .glassBackground(
                shape = RoundedCornerShape(24.dp),
                backgroundColor = if (isSelected) KrasRed.copy(alpha = 0.85f) else Color(0x331E2330),
                borderColor = if (isSelected) KrasRedBright else Color.White.copy(alpha = 0.25f),
                hasRedGlow = isSelected,
                elevation = if (isSelected) 8.dp else 2.dp,
                highlightIntensity = if (isSelected) 0.35f else 0.15f
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(end = 6.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}

/**
 * GlassBadge for badges, ratings, and tags.
 */
@Composable
fun GlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xAA0D1017),
    borderColor: Color = Color.White.copy(alpha = 0.35f),
    contentColor: Color = Color.White
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            ),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
