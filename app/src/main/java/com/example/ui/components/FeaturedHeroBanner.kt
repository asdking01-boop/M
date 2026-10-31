package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.VodMovie
import com.example.localization.LocalKrasStrings
import com.example.ui.glass.GlassBadge
import com.example.ui.glass.glassBackground
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.GoldStar
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FeaturedHeroBanner(
    featuredMovie: VodMovie,
    onWatchClick: (VodMovie) -> Unit,
    onInfoClick: (VodMovie) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .glassBackground(
                shape = RoundedCornerShape(26.dp),
                backgroundColor = Color(0xD80D1017),
                borderColor = Color.White.copy(alpha = 0.35f),
                borderWidth = 1.2.dp,
                hasRedGlow = true,
                elevation = 14.dp,
                highlightIntensity = 0.30f
            )
    ) {
        // Hero Background Artwork
        if (featuredMovie.backdropUrl.isNotEmpty()) {
            AsyncImage(
                model = featuredMovie.backdropUrl,
                contentDescription = featuredMovie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.kras_hero_banner),
                contentDescription = featuredMovie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Multi-stage Cinematic Liquid Gradient Fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x550B0C0E),
                            Color(0xD90B0C0E),
                            Color(0xFA0B0C0E)
                        )
                    )
                )
        )

        // Banner Content Information inside frosted panel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Badges row: KRAS EXCLUSIVE, Genre, Year, Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassBadge(
                    text = "KRAS EXCLUSIVE",
                    backgroundColor = KrasRed.copy(alpha = 0.90f),
                    borderColor = KrasRedBright
                )

                GlassBadge(
                    text = featuredMovie.genre.split("/").firstOrNull()?.trim() ?: featuredMovie.genre,
                    backgroundColor = Color(0x66181D26)
                )

                GlassBadge(
                    text = featuredMovie.year,
                    backgroundColor = Color(0x66181D26)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .glassBackground(
                            shape = RoundedCornerShape(8.dp),
                            backgroundColor = Color(0x66181D26),
                            borderColor = Color.White.copy(alpha = 0.20f),
                            elevation = 2.dp
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(text = "★ ", color = GoldStar, fontSize = 11.sp)
                    Text(
                        text = featuredMovie.rating,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Movie Title
            Text(
                text = featuredMovie.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Short Description
            Text(
                text = featuredMovie.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Liquid Glass Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Liquid Glass Watch Now Button
                LiquidGlassButton(
                    text = strings.watchNow,
                    icon = Icons.Default.PlayArrow,
                    isPrimary = true,
                    testTag = "hero_watch_now_button",
                    onClick = { onWatchClick(featuredMovie) },
                    modifier = Modifier.weight(1f)
                )

                // Liquid Glass More Info Button
                LiquidGlassButton(
                    text = strings.moreInfo,
                    icon = Icons.Default.Info,
                    isPrimary = false,
                    testTag = "hero_more_info_button",
                    onClick = { onInfoClick(featuredMovie) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun LiquidGlassButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(),
        label = "glassButtonScale"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .height(48.dp)
            .glassBackground(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = if (isPrimary) KrasRed.copy(alpha = 0.90f) else Color(0x33202533),
                borderColor = if (isPrimary) KrasRedBright else Color.White.copy(alpha = 0.35f),
                borderWidth = 1.2.dp,
                hasRedGlow = isPrimary,
                elevation = if (isPrimary) 8.dp else 4.dp,
                highlightIntensity = if (isPrimary) 0.38f else 0.22f
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}
