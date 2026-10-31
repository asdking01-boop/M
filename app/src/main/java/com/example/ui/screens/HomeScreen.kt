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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountType
import com.example.data.model.IptvAccount
import com.example.data.model.LiveChannelItem
import com.example.data.model.SeriesItem
import com.example.data.model.VodMovieItem
import com.example.data.model.WatchHistoryEntity
import com.example.localization.LocalKrasStrings
import com.example.ui.components.ChannelItemCard
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.FeaturedHeroBanner
import com.example.ui.components.MoviePosterCard
import com.example.ui.components.SeriesPosterCard
import com.example.ui.glass.glassBackground
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    featuredMovie: VodMovieItem?,
    continueWatching: List<WatchHistoryEntity>,
    channels: List<LiveChannelItem>,
    movies: List<VodMovieItem>,
    series: List<SeriesItem>,
    activeAccount: IptvAccount?,
    loadingStatus: String? = null,
    isLoading: Boolean = false,
    onWatchMovie: (VodMovieItem) -> Unit,
    onInfoMovie: (VodMovieItem) -> Unit,
    onWatchChannel: (LiveChannelItem) -> Unit,
    onSelectSeries: (SeriesItem) -> Unit,
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
        // Sync Status Banner (Item 5: sequential loading indicator)
        if (isLoading || loadingStatus != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .glassBackground(
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = Color(0xD81E1528),
                            borderColor = KrasRedBright.copy(alpha = 0.6f),
                            hasRedGlow = true,
                            elevation = 4.dp
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = KrasRedBright,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = loadingStatus ?: strings.loading,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = KrasRedBright,
                            trackColor = Color(0x33FFFFFF)
                        )
                    }
                }
            }
        }

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

        // Continue Watching Section
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
                        items(continueWatching, key = { it.contentId }) { item ->
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
                        items(movies, key = { it.id }) { movie ->
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
                        items(series, key = { it.id }) { ser ->
                            SeriesPosterCard(
                                series = ser,
                                onClick = { onSelectSeries(ser) }
                            )
                        }
                    }
                }
            }
        }
    }
}
