package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountType
import com.example.data.model.Channel
import com.example.data.model.ContentType
import com.example.data.model.IptvAccount
import com.example.data.model.Series
import com.example.data.model.VodMovie
import com.example.data.model.WatchHistoryEntity
import com.example.localization.LocalKrasStrings
import com.example.ui.components.ChannelItemCard
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.FeaturedHeroBanner
import com.example.ui.components.MoviePosterCard
import com.example.ui.components.SeriesPosterCard
import com.example.ui.glass.GlassBadge
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    featuredMovie: VodMovie?,
    continueWatching: List<WatchHistoryEntity>,
    channels: List<Channel>,
    movies: List<VodMovie>,
    series: List<Series>,
    activeAccount: IptvAccount?,
    onWatchMovie: (VodMovie) -> Unit,
    onInfoMovie: (VodMovie) -> Unit,
    onWatchChannel: (Channel) -> Unit,
    onSelectSeries: (Series) -> Unit,
    onResumeHistory: (WatchHistoryEntity) -> Unit,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(KrasBlack),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Featured Hero Content Banner
        if (featuredMovie != null) {
            item {
                FeaturedHeroBanner(
                    featuredMovie = featuredMovie,
                    onWatchClick = onWatchMovie,
                    onInfoClick = onInfoMovie
                )
            }
        }

        // Demo Mode / Free Streams notice chip if in demo mode
        if (activeAccount?.type == AccountType.DEMO) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .liquidGlass(
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color(0x33E50914),
                            borderColor = KrasRed.copy(alpha = 0.5f)
                        )
                        .clickable { onNavigate(Screen.Settings) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.demoModeNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = strings.switchAccount,
                            tint = KrasRedBright,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Continue Watching Section (if user has watch progress)
        if (continueWatching.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = strings.continueWatching,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(continueWatching) { item ->
                            ContinueWatchingCard(
                                item = item,
                                onClick = { onResumeHistory(item) }
                            )
                        }
                    }
                }
            }
        }

        // Live TV Shortcut Banner / Carousel
        if (channels.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = KrasRedBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.liveTvChannels,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = strings.viewAll,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KrasRedBright,
                            modifier = Modifier.clickable { onNavigate(Screen.LiveTv) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(channels, key = { it.id }) { channel ->
                            Box(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable { onWatchChannel(channel) }
                            ) {
                                ChannelItemCard(
                                    channel = channel,
                                    onClick = { onWatchChannel(channel) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recently Added Movies Row
        if (movies.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.recentlyAddedMovies,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = strings.viewAll,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KrasRedBright,
                            modifier = Modifier.clickable { onNavigate(Screen.Movies) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movies) { movie ->
                            MoviePosterCard(
                                movie = movie,
                                onClick = { onInfoMovie(movie) }
                            )
                        }
                    }
                }
            }
        }

        // Recommended TV Series Row
        if (series.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.recommendedSeries,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = strings.viewAll,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KrasRedBright,
                            modifier = Modifier.clickable { onNavigate(Screen.Series) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(series) { ser ->
                            SeriesPosterCard(
                                series = ser,
                                onClick = { onSelectSeries(ser) }
                            )
                        }
                    }
                }
            }
        }

        // Popular Movies Row
        if (movies.size > 2) {
            item {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Text(
                        text = strings.popularMovies,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movies.asReversed()) { movie ->
                            MoviePosterCard(
                                movie = movie,
                                onClick = { onInfoMovie(movie) }
                            )
                        }
                    }
                }
            }
        }
    }
}
