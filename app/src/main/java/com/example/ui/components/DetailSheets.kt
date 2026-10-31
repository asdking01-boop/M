package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ContentType
import com.example.data.model.Episode
import com.example.data.model.Season
import com.example.data.model.Series
import com.example.data.model.VodMovie
import com.example.localization.LocalKrasStrings
import com.example.ui.glass.GlassBadge
import com.example.ui.glass.glassBackground
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.GoldStar
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import com.example.data.model.VodMovieItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailSheet(
    movie: VodMovieItem,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlayClick: (VodMovieItem) -> Unit,
    onToggleFavorite: (VodMovieItem) -> Unit
) {
    MovieDetailSheetInternal(
        title = movie.title,
        backdropUrl = movie.posterUrl,
        posterUrl = movie.posterUrl,
        year = movie.year,
        duration = "HD",
        rating = movie.rating,
        genre = movie.categoryName,
        description = "",
        isFavorite = isFavorite,
        onDismiss = onDismiss,
        onPlayClick = { onPlayClick(movie) },
        onToggleFavorite = { onToggleFavorite(movie) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailSheet(
    movie: VodMovie,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlayClick: (VodMovie) -> Unit,
    onToggleFavorite: (VodMovie) -> Unit
) {
    MovieDetailSheetInternal(
        title = movie.title,
        backdropUrl = movie.backdropUrl.ifEmpty { movie.posterUrl },
        posterUrl = movie.posterUrl,
        year = movie.year,
        duration = movie.duration,
        rating = movie.rating,
        genre = movie.genre,
        description = movie.description,
        isFavorite = isFavorite,
        onDismiss = onDismiss,
        onPlayClick = { onPlayClick(movie) },
        onToggleFavorite = { onToggleFavorite(movie) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailSheetInternal(
    title: String,
    backdropUrl: String,
    posterUrl: String,
    year: String,
    duration: String,
    rating: String,
    genre: String,
    description: String,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlayClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val strings = LocalKrasStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xF20B0C0E),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Header Backdrop
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        AsyncImage(
                            model = backdropUrl.ifEmpty { posterUrl },
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top and bottom gradients
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x88000000), Color.Transparent, Color(0xF20B0C0E))
                                    )
                                )
                        )

                        // Close button top-end
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Info Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Poster Card
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .liquidGlass(
                                        shape = RoundedCornerShape(14.dp),
                                        backgroundColor = Color(0xFF161922)
                                    )
                            ) {
                                AsyncImage(
                                    model = posterUrl,
                                    contentDescription = title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Title & Quick Specs
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (year.isNotBlank()) {
                                        GlassBadge(text = year, backgroundColor = Color(0x33FFFFFF))
                                    }
                                    if (duration.isNotBlank()) {
                                        GlassBadge(text = duration, backgroundColor = Color(0x33FFFFFF))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "★ ", color = GoldStar, fontSize = 13.sp)
                                        Text(
                                            text = rating.ifEmpty { "N/A" },
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = KrasRedBright
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Liquid Glass Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LiquidGlassButton(
                                text = strings.playMovie,
                                icon = Icons.Default.PlayArrow,
                                isPrimary = true,
                                testTag = "movie_play_button",
                                onClick = onPlayClick,
                                modifier = Modifier.weight(1f)
                            )

                            LiquidGlassButton(
                                text = if (isFavorite) strings.removeFromFavorites else strings.addToFavorites,
                                icon = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                isPrimary = false,
                                testTag = "movie_favorite_button",
                                onClick = onToggleFavorite,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(20.dp))

                            // Synopsis
                            Text(
                                text = strings.synopsis,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailSheet(
    series: Series,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlayEpisode: (Series, Season, Episode) -> Unit,
    onToggleFavorite: (Series) -> Unit
) {
    val strings = LocalKrasStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }

    val activeSeason = series.seasons.getOrNull(selectedSeasonIndex) ?: series.seasons.firstOrNull()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xF20B0C0E),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Header Backdrop
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        AsyncImage(
                            model = series.backdropUrl.ifEmpty { series.posterUrl },
                            contentDescription = series.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x88000000), Color.Transparent, Color(0xF20B0C0E))
                                    )
                                )
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Info
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = series.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassBadge(text = "${series.seasons.size} ${strings.seasons}", backgroundColor = Color(0x33FFFFFF))
                            GlassBadge(text = series.year, backgroundColor = Color(0x33FFFFFF))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "★ ", color = GoldStar, fontSize = 13.sp)
                                Text(
                                    text = series.rating,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = series.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Liquid Glass Favorite Button
                        LiquidGlassButton(
                            text = if (isFavorite) strings.removeFromFavorites else strings.addToFavorites,
                            icon = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            isPrimary = isFavorite,
                            testTag = "series_favorite_button",
                            onClick = { onToggleFavorite(series) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Seasons Tab Row
                        Text(
                            text = strings.seasons,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ScrollableTabRow(
                            selectedTabIndex = selectedSeasonIndex,
                            containerColor = Color.Transparent,
                            contentColor = KrasRedBright,
                            edgePadding = 0.dp,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedSeasonIndex]),
                                    color = KrasRedBright
                                )
                            }
                        ) {
                            series.seasons.forEachIndexed { index, season ->
                                Tab(
                                    selected = selectedSeasonIndex == index,
                                    onClick = { selectedSeasonIndex = index },
                                    text = {
                                        Text(
                                            text = "${strings.season} ${season.seasonNum}",
                                            fontWeight = if (selectedSeasonIndex == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedSeasonIndex == index) TextPrimary else TextSecondary
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Episodes List in Liquid Glass Cards
                if (activeSeason != null) {
                    items(activeSeason.episodes) { episode ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .glassBackground(
                                    shape = RoundedCornerShape(16.dp),
                                    backgroundColor = Color(0xD8131722),
                                    borderColor = Color.White.copy(alpha = 0.22f),
                                    elevation = 3.dp,
                                    highlightIntensity = 0.18f
                                )
                                .clickable { onPlayEpisode(series, activeSeason, episode) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Episode thumbnail with play overlay
                                Box(
                                    modifier = Modifier
                                        .size(76.dp, 54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF20242E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (episode.posterUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = episode.posterUrl,
                                            contentDescription = episode.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x99000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${episode.episodeNum}. ${episode.title}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = episode.description,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = episode.duration,
                                        fontSize = 11.sp,
                                        color = KrasRedBright,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
