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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.data.model.LiveChannelItem
import com.example.data.model.SeriesItem
import com.example.data.model.VodMovieItem
import com.example.localization.LocalKrasStrings
import com.example.ui.components.ChannelItemCard
import com.example.ui.components.MoviePosterCard
import com.example.ui.components.SeriesPosterCard
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.Flow

@Composable
fun SearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    getChannelsPager: (category: String, query: String) -> Flow<PagingData<LiveChannelItem>>,
    getMoviesPager: (genre: String, query: String) -> Flow<PagingData<VodMovieItem>>,
    getSeriesPager: (genre: String, query: String) -> Flow<PagingData<SeriesItem>>,
    onChannelClick: (LiveChannelItem) -> Unit,
    onMovieClick: (VodMovieItem) -> Unit,
    onSeriesClick: (SeriesItem) -> Unit,
    onToggleFavoriteChannel: (LiveChannelItem) -> Unit,
    onToggleFavoriteMovie: (VodMovieItem) -> Unit,
    isFavorite: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabTitles = listOf(strings.liveTvChannels, strings.movies, strings.series)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KrasBlack)
    ) {
        // Search Input Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = strings.searchPlaceholder,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = KrasRedBright,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0x3312151D),
                    unfocusedContainerColor = Color(0x2212151D),
                    focusedBorderColor = KrasRedBright,
                    unfocusedBorderColor = GlassWhiteStroke,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Section Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = KrasRedBright,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = KrasRedBright
                )
            },
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) KrasRedBright else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.typeToSearch,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            when (selectedTab) {
                0 -> {
                    // Paged Channels Search
                    val pagedChannels = remember(searchQuery) {
                        getChannelsPager("", searchQuery)
                    }.collectAsLazyPagingItems()

                    if (pagedChannels.loadState.refresh is LoadState.Loading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = KrasRedBright, modifier = Modifier.size(36.dp))
                        }
                    } else if (pagedChannels.itemCount == 0) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = strings.noChannelsFound, color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                count = pagedChannels.itemCount,
                                key = { i -> pagedChannels.peek(i)?.id ?: "search_ch_$i" }
                            ) { i ->
                                val channel = pagedChannels[i]
                                if (channel != null) {
                                    ChannelItemCard(
                                        channel = channel,
                                        onClick = { onChannelClick(channel) },
                                        onFavoriteClick = { onToggleFavoriteChannel(channel) },
                                        isFavorite = isFavorite(channel.id)
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Paged Movies Search
                    val pagedMovies = remember(searchQuery) {
                        getMoviesPager("", searchQuery)
                    }.collectAsLazyPagingItems()

                    if (pagedMovies.loadState.refresh is LoadState.Loading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = KrasRedBright, modifier = Modifier.size(36.dp))
                        }
                    } else if (pagedMovies.itemCount == 0) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = strings.noMoviesFound, color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 135.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                count = pagedMovies.itemCount,
                                key = { i -> pagedMovies.peek(i)?.id ?: "search_mov_$i" }
                            ) { i ->
                                val movie = pagedMovies[i]
                                if (movie != null) {
                                    MoviePosterCard(
                                        movie = movie,
                                        onClick = { onMovieClick(movie) },
                                        onFavoriteClick = { onToggleFavoriteMovie(movie) },
                                        isFavorite = isFavorite(movie.id)
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Paged Series Search
                    val pagedSeries = remember(searchQuery) {
                        getSeriesPager("", searchQuery)
                    }.collectAsLazyPagingItems()

                    if (pagedSeries.loadState.refresh is LoadState.Loading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = KrasRedBright, modifier = Modifier.size(36.dp))
                        }
                    } else if (pagedSeries.itemCount == 0) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = strings.noSeriesFound, color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 135.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                count = pagedSeries.itemCount,
                                key = { i -> pagedSeries.peek(i)?.id ?: "search_ser_$i" }
                            ) { i ->
                                val item = pagedSeries[i]
                                if (item != null) {
                                    SeriesPosterCard(
                                        series = item,
                                        onClick = { onSeriesClick(item) }
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
