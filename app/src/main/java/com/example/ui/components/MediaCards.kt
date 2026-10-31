package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.data.model.Series
import com.example.data.model.VodMovie
import com.example.data.model.WatchHistoryEntity
import com.example.localization.LocalKrasStrings
import com.example.ui.glass.GlassBadge
import com.example.ui.glass.glassBackground
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.GoldStar
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasCardBg
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.LiveRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MoviePosterCard(
    movie: VodMovie,
    onClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(),
        label = "movieScale"
    )

    Column(
        modifier = modifier
            .width(135.dp)
            .scale(cardScale)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(135.dp)
                .height(195.dp)
                .glassBackground(
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color(0xD5121620),
                    borderColor = Color.White.copy(alpha = 0.28f),
                    borderWidth = 1.dp,
                    elevation = 6.dp,
                    highlightIntensity = 0.22f
                )
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Rating badge top-start with Liquid Glass surface
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .glassBackground(
                        shape = RoundedCornerShape(8.dp),
                        backgroundColor = Color(0xB30B0E14),
                        borderColor = Color.White.copy(alpha = 0.30f),
                        elevation = 2.dp
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "★", color = GoldStar, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = movie.rating,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Favorite toggle top-end with glass circle
            if (onFavoriteClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(30.dp)
                        .glassBackground(
                            shape = CircleShape,
                            backgroundColor = Color(0xB30B0E14),
                            borderColor = if (isFavorite) KrasRedBright else Color.White.copy(alpha = 0.30f),
                            hasRedGlow = isFavorite,
                            elevation = 3.dp
                        )
                        .clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) KrasRedBright else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Bottom specular fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xE60B0C0E))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = movie.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = movie.genre.split("/").firstOrNull()?.trim() ?: movie.genre,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1
            )
            Text(
                text = movie.year,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SeriesPosterCard(
    series: Series,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(),
        label = "seriesScale"
    )

    Column(
        modifier = modifier
            .width(135.dp)
            .scale(cardScale)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(135.dp)
                .height(195.dp)
                .glassBackground(
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color(0xD5121620),
                    borderColor = Color.White.copy(alpha = 0.28f),
                    borderWidth = 1.dp,
                    elevation = 6.dp,
                    highlightIntensity = 0.22f
                )
        ) {
            AsyncImage(
                model = series.posterUrl,
                contentDescription = series.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Series Badge in Liquid Glass
            GlassBadge(
                text = "${series.seasons.size} S",
                backgroundColor = Color(0xCC0B0E14),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            )

            // Rating badge top-end
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .glassBackground(
                        shape = RoundedCornerShape(8.dp),
                        backgroundColor = Color(0xB30B0E14),
                        borderColor = Color.White.copy(alpha = 0.30f),
                        elevation = 2.dp
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "★", color = GoldStar, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = series.rating,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = series.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = series.genre,
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
fun ChannelItemCard(
    channel: Channel,
    onClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(),
        label = "channelScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .glassBackground(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0xD812151D),
                borderColor = Color.White.copy(alpha = 0.25f),
                borderWidth = 1.dp,
                elevation = 5.dp,
                highlightIntensity = 0.20f
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Logo / Number Box in Liquid Glass
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .glassBackground(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color(0x661E2330),
                        borderColor = Color.White.copy(alpha = 0.25f),
                        elevation = 2.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = channel.channelNum.ifEmpty { "TV" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = KrasRedBright
                    )
                }

                // Live indicator badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .glassBackground(
                            shape = RoundedCornerShape(5.dp),
                            backgroundColor = LiveRed.copy(alpha = 0.90f),
                            borderColor = Color.White.copy(alpha = 0.4f),
                            hasRedGlow = true,
                            elevation = 2.dp
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Channel Details and EPG Information
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // EPG Current Show
                if (channel.currentProgram != null) {
                    Text(
                        text = "${strings.epgNow}: ${channel.currentProgram.title}",
                        fontSize = 12.sp,
                        color = KrasRedBright,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // EPG progress bar
                    LinearProgressIndicator(
                        progress = { channel.currentProgram.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth(0.90f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = KrasRed,
                        trackColor = Color(0x33FFFFFF)
                    )
                } else {
                    Text(
                        text = channel.category,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Actions: Favorite and Play Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onFavoriteClick != null) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) KrasRedBright else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Liquid Glass Play Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .glassBackground(
                            shape = CircleShape,
                            backgroundColor = KrasRed.copy(alpha = 0.90f),
                            borderColor = KrasRedBright,
                            hasRedGlow = true,
                            elevation = 6.dp,
                            highlightIntensity = 0.35f
                        )
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: WatchHistoryEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(),
        label = "continueScale"
    )

    Column(
        modifier = modifier
            .width(185.dp)
            .scale(cardScale)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(185.dp)
                .height(115.dp)
                .glassBackground(
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color(0xD5121620),
                    borderColor = Color.White.copy(alpha = 0.30f),
                    borderWidth = 1.dp,
                    elevation = 6.dp,
                    highlightIntensity = 0.25f
                )
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Play glass bubble in center
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .glassBackground(
                        shape = CircleShape,
                        backgroundColor = Color(0xB30B0E14),
                        borderColor = Color.White.copy(alpha = 0.40f),
                        elevation = 4.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Glowing Progress bar at bottom
            LinearProgressIndicator(
                progress = { item.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.BottomCenter),
                color = KrasRed,
                trackColor = Color(0x66000000)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = if (item.seriesTitle.isNotEmpty()) "${item.seriesTitle} - S${item.seasonNum}E${item.episodeNum}" else item.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
