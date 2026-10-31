package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassCardSurface
import com.example.ui.theme.GlassRedStroke
import com.example.ui.theme.GlassWhiteFill
import com.example.ui.theme.GlassWhiteReflection
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasRed

/**
 * Modifier for creating iOS 26 inspired Liquid Glass effect:
 * Translucent frosted gradient surface with top-left specular highlight border.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color(0xCC13161C),
    borderColor: Color = GlassWhiteStroke,
    borderWidth: Dp = 1.dp,
    hasRedGlow: Boolean = false,
    elevation: Dp = 8.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = if (hasRedGlow) KrasRed.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.5f),
        spotColor = if (hasRedGlow) KrasRed.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.6f)
    )
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.85f),
                backgroundColor.copy(alpha = 0.65f),
                backgroundColor.copy(alpha = 0.90f)
            )
        )
    )
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                GlassWhiteReflection.copy(alpha = 0.18f),
                Color.Transparent,
                GlassWhiteFill.copy(alpha = 0.05f)
            )
        )
    )
    .border(
        width = borderWidth,
        brush = Brush.verticalGradient(
            colors = listOf(
                if (hasRedGlow) GlassRedStroke else borderColor,
                borderColor.copy(alpha = 0.15f),
                if (hasRedGlow) GlassRedStroke.copy(alpha = 0.4f) else borderColor.copy(alpha = 0.05f)
            )
        ),
        shape = shape
    )

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color = Color(0xCC161922),
    borderColor: Color = GlassWhiteStroke,
    hasRedGlow: Boolean = false,
    elevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.liquidGlass(
            shape = shape,
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            hasRedGlow = hasRedGlow,
            elevation = elevation
        ),
        content = content
    )
}

@Composable
fun GlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0x99E50914),
    contentColor: Color = Color.White
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
