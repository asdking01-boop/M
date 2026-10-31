package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Series
import com.example.localization.LocalKrasStrings
import com.example.ui.components.SeriesPosterCard
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SeriesScreen(
    seriesList: List<Series>,
    onSeriesClick: (Series) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = remember(seriesList) {
        val uniqueGenres = seriesList.map { it.genre.split("/").first().trim() }.distinct().filter { it.isNotBlank() }
        listOf(strings.filterAll) + uniqueGenres
    }

    val selectedCategory = categories.getOrNull(selectedCategoryIndex) ?: strings.filterAll

    // Memoized filter for 10,000+ series
    val filteredSeries = remember(seriesList, selectedCategoryIndex, selectedCategory, searchQuery) {
        seriesList.filter { ser ->
            val matchesCategory = selectedCategoryIndex == 0 || ser.genre.contains(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    ser.title.contains(searchQuery, ignoreCase = true) ||
                    ser.genre.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // Incremental loading state
    var displayedCount by remember(selectedCategoryIndex, searchQuery) { mutableIntStateOf(60) }
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState, filteredSeries.size) {
        snapshotFlow {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible to totalItems
        }.distinctUntilChanged().collect { (lastVisible, totalItems) ->
            if (totalItems > 0 && lastVisible >= totalItems - 15 && displayedCount < filteredSeries.size) {
                displayedCount = (displayedCount + 60).coerceAtMost(filteredSeries.size)
            }
        }
    }

    val visibleSeries = remember(filteredSeries, displayedCount) {
        if (displayedCount >= filteredSeries.size) {
            filteredSeries
        } else {
            filteredSeries.subList(0, displayedCount)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KrasBlack)
    ) {
        // Search
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "${strings.searchHint} (${seriesList.size})",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0x331E222B),
                    unfocusedContainerColor = Color(0x221E222B),
                    focusedBorderColor = KrasRed,
                    unfocusedBorderColor = GlassWhiteStroke.copy(alpha = 0.2f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Category Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = Color.Transparent,
            contentColor = KrasRedBright,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (selectedCategoryIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                        color = KrasRedBright
                    )
                }
            }
        ) {
            categories.forEachIndexed { index, name ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = {
                        Text(
                            text = name,
                            fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategoryIndex == index) TextPrimary else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredSeries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = strings.noSearchResults, color = TextSecondary, fontSize = 15.sp)
            }
        } else {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 130.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleSeries, key = { it.id }) { series ->
                    SeriesPosterCard(
                        series = series,
                        onClick = { onSeriesClick(series) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (displayedCount < filteredSeries.size) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
