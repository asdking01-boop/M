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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.data.model.SeriesItem
import com.example.data.model.WatchHistoryEntity
import com.example.localization.LocalKrasStrings
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.KrasDebouncedSearchBar
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.MediaFiltersBar
import com.example.ui.components.SeriesPosterCard
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun SeriesScreen(
    genres: List<String>,
    years: List<String>,
    recentlyAdded: List<SeriesItem>,
    latestReleases: List<SeriesItem>,
    topRated: List<SeriesItem>,
    continueWatching: List<WatchHistoryEntity>,
    getSeriesPager: (genre: String, year: String, minRating: Double, query: String, sortMode: String) -> Flow<PagingData<SeriesItem>>,
    onSeriesClick: (SeriesItem) -> Unit,
    onResumeHistory: (WatchHistoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    var selectedGenre by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    var selectedRating by remember { mutableDoubleStateOf(0.0) }
    var searchQuery by remember { mutableStateOf("") }
    var activeSortMode by remember { mutableStateOf("") } // "", "added", "year", "rating"

    val isFilterOrSearchActive = searchQuery.isNotBlank() || selectedGenre.isNotBlank() ||
            selectedYear.isNotBlank() || selectedRating > 0.0 || activeSortMode.isNotBlank()

    val pagedFlow = remember(selectedGenre, selectedYear, selectedRating, searchQuery, activeSortMode) {
        getSeriesPager(selectedGenre, selectedYear, selectedRating, searchQuery, activeSortMode)
    }
    val lazySeries = pagedFlow.collectAsLazyPagingItems()

    fun clearAllFilters() {
        searchQuery = ""
        selectedGenre = ""
        selectedYear = ""
        selectedRating = 0.0
        activeSortMode = ""
    }

    fun selectSection(sortMode: String) {
        activeSortMode = sortMode
        scope.launch {
            gridState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KrasBlack)
    ) {
        // Debounced Search Box (Requirement 1)
        KrasDebouncedSearchBar(
            query = searchQuery,
            onDebouncedQuery = { searchQuery = it },
            placeholder = strings.searchSeries
        )

        // Horizontal Scrollable Filter Chips (Genre, Year, Rating, Clear filters) (Requirement 1)
        MediaFiltersBar(
            genres = genres,
            selectedGenre = selectedGenre,
            onSelectGenre = {
                selectedGenre = it
                activeSortMode = ""
            },
            years = years,
            selectedYear = selectedYear,
            onSelectYear = {
                selectedYear = it
                activeSortMode = ""
            },
            minRating = selectedRating,
            onSelectRating = {
                selectedRating = it
                activeSortMode = ""
            },
            onClearFilters = { clearAllFilters() }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                lazySeries.loadState.refresh is LoadState.Loading && lazySeries.itemCount == 0 -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = KrasRedBright,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.loading,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                lazySeries.loadState.refresh is LoadState.Error && lazySeries.itemCount == 0 -> {
                    val error = (lazySeries.loadState.refresh as LoadState.Error).error
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error.localizedMessage ?: "Error loading series",
                            color = KrasRedBright,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LiquidGlassButton(
                            text = strings.retry,
                            icon = Icons.Default.Refresh,
                            isPrimary = true,
                            onClick = { lazySeries.retry() }
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 135.dp),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Curated Horizontal Rows shown when not in deep search mode (Requirement 2)
                        if (!isFilterOrSearchActive) {
                            // Row 1: Continue Watching
                            if (continueWatching.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Column(modifier = Modifier.padding(bottom = 14.dp)) {
                                        Text(
                                            text = strings.continueWatching,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        LazyRow(
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

                            // Row 2: New / Recently Added (LIMIT 30 items)
                            if (recentlyAdded.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Column(modifier = Modifier.padding(bottom = 14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = strings.recentlyAdded,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = strings.seeAll,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = KrasRedBright,
                                                modifier = Modifier.clickable { selectSection("added") }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(recentlyAdded, key = { it.id }) { item ->
                                                SeriesPosterCard(
                                                    series = item,
                                                    onClick = { onSeriesClick(item) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Row 3: Latest Releases (LIMIT 30 items)
                            if (latestReleases.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Column(modifier = Modifier.padding(bottom = 14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = strings.latestReleases,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = strings.seeAll,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = KrasRedBright,
                                                modifier = Modifier.clickable { selectSection("year") }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(latestReleases, key = { it.id }) { item ->
                                                SeriesPosterCard(
                                                    series = item,
                                                    onClick = { onSeriesClick(item) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Row 4: Top Rated (LIMIT 30 items)
                            if (topRated.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Column(modifier = Modifier.padding(bottom = 14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = strings.topRated,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = strings.seeAll,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = KrasRedBright,
                                                modifier = Modifier.clickable { selectSection("rating") }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(topRated, key = { it.id }) { item ->
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

                        // Header for Full Paged List
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val sectionTitle = when {
                                    activeSortMode == "added" -> strings.recentlyAdded
                                    activeSortMode == "year" -> strings.latestReleases
                                    activeSortMode == "rating" -> strings.topRated
                                    selectedGenre.isNotBlank() -> selectedGenre
                                    searchQuery.isNotBlank() -> strings.searchResults
                                    else -> strings.seriesLibrary
                                }

                                Text(
                                    text = sectionTitle,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                if (isFilterOrSearchActive) {
                                    Text(
                                        text = strings.clearFilters,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KrasRedBright,
                                        modifier = Modifier.clickable { clearAllFilters() }
                                    )
                                }
                            }
                        }

                        // Full Paged List Grid
                        if (lazySeries.itemCount == 0 && lazySeries.loadState.refresh is LoadState.NotLoading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = strings.noSeriesFound,
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(
                                count = lazySeries.itemCount,
                                key = { index -> lazySeries.peek(index)?.id ?: "ser_$index" }
                            ) { index ->
                                val item = lazySeries[index]
                                if (item != null) {
                                    SeriesPosterCard(
                                        series = item,
                                        onClick = { onSeriesClick(item) }
                                    )
                                }
                            }
                        }

                        if (lazySeries.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = KrasRedBright,
                                        modifier = Modifier.size(28.dp)
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
