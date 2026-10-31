package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.data.model.Series
import com.example.data.model.VodMovie
import com.example.localization.LocalKrasStrings
import com.example.ui.components.ChannelItemCard
import com.example.ui.components.MoviePosterCard
import com.example.ui.components.SeriesPosterCard
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    channels: List<Channel>,
    movies: List<VodMovie>,
    series: List<Series>,
    onChannelClick: (Channel) -> Unit,
    onMovieClick: (VodMovie) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onToggleFavoriteChannel: (Channel) -> Unit,
    isFavorite: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current

    // Memoize search matches for fast, stutter-free performance with 10,000+ items
    val matchingChannels = remember(searchQuery, channels) {
        if (searchQuery.isNotBlank()) {
            channels.filter { it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
        } else emptyList()
    }

    val matchingMovies = remember(searchQuery, movies) {
        if (searchQuery.isNotBlank()) {
            movies.filter { it.title.contains(searchQuery, ignoreCase = true) || it.genre.contains(searchQuery, ignoreCase = true) }
        } else emptyList()
    }

    val matchingSeries = remember(searchQuery, series) {
        if (searchQuery.isNotBlank()) {
            series.filter { it.title.contains(searchQuery, ignoreCase = true) || it.genre.contains(searchQuery, ignoreCase = true) }
        } else emptyList()
    }

    val totalMatches = matchingChannels.size + matchingMovies.size + matchingSeries.size

    // Incremental loading state
    var displayedLimit by remember(searchQuery) { mutableIntStateOf(50) }
    val listState = rememberLazyListState()

    LaunchedEffect(listState, totalMatches) {
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last to total
        }.distinctUntilChanged().collect { (last, total) ->
            if (total > 0 && last >= total - 10 && displayedLimit < totalMatches) {
                displayedLimit = (displayedLimit + 50).coerceAtMost(totalMatches)
            }
        }
    }

    val visibleChannels = remember(matchingChannels, displayedLimit) {
        if (displayedLimit >= matchingChannels.size) matchingChannels else matchingChannels.subList(0, displayedLimit)
    }

    val visibleMovies = remember(matchingMovies, displayedLimit) {
        if (displayedLimit >= matchingMovies.size) matchingMovies else matchingMovies.subList(0, displayedLimit)
    }

    val visibleSeries = remember(matchingSeries, displayedLimit) {
        if (displayedLimit >= matchingSeries.size) matchingSeries else matchingSeries.subList(0, displayedLimit)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KrasBlack)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text(strings.searchHint, color = TextSecondary, fontSize = 14.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0x331E222B),
                unfocusedContainerColor = Color(0x221E222B),
                focusedBorderColor = KrasRed,
                unfocusedBorderColor = GlassWhiteStroke.copy(alpha = 0.25f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.searchHint,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else if (totalMatches == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.noSearchResults,
                    color = TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Matching Channels
                if (matchingChannels.isNotEmpty()) {
                    item {
                        Text(
                            text = "${strings.navLive} (${matchingChannels.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    items(visibleChannels, key = { it.id }) { ch ->
                        ChannelItemCard(
                            channel = ch,
                            onClick = { onChannelClick(ch) },
                            onFavoriteClick = { onToggleFavoriteChannel(ch) },
                            isFavorite = isFavorite(ch.id)
                        )
                    }
                }

                // Matching Movies
                if (matchingMovies.isNotEmpty()) {
                    item {
                        Text(
                            text = "${strings.navMovies} (${matchingMovies.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    items(visibleMovies, key = { it.id }) { mov ->
                        MoviePosterCard(
                            movie = mov,
                            onClick = { onMovieClick(mov) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Matching Series
                if (matchingSeries.isNotEmpty()) {
                    item {
                        Text(
                            text = "${strings.navSeries} (${matchingSeries.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    items(visibleSeries, key = { it.id }) { ser ->
                        SeriesPosterCard(
                            series = ser,
                            onClick = { onSeriesClick(ser) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (displayedLimit < totalMatches) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = KrasRedBright,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
