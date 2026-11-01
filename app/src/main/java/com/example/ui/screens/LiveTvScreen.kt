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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.data.model.LiveChannelItem
import com.example.localization.LocalKrasStrings
import com.example.ui.components.ChannelItemCard
import com.example.ui.components.KrasDebouncedSearchBar
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiveTvFiltersBar
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.Flow

@Composable
fun LiveTvScreen(
    categories: List<String>,
    getChannelsPager: (category: String, favoritesOnly: Boolean, query: String) -> Flow<PagingData<LiveChannelItem>>,
    onChannelClick: (LiveChannelItem) -> Unit,
    onToggleFavorite: (LiveChannelItem) -> Unit,
    isFavorite: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current
    var selectedCategory by remember { mutableStateOf("") }
    var isFavoritesOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val pagedFlow = remember(selectedCategory, isFavoritesOnly, searchQuery) {
        getChannelsPager(selectedCategory, isFavoritesOnly, searchQuery)
    }
    val lazyChannels = pagedFlow.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    fun clearAllFilters() {
        selectedCategory = ""
        isFavoritesOnly = false
        searchQuery = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KrasBlack)
    ) {
        // Debounced Search Bar (Requirement 1)
        KrasDebouncedSearchBar(
            query = searchQuery,
            onDebouncedQuery = { searchQuery = it },
            placeholder = strings.searchChannels
        )

        // Horizontal Scrollable Category + Favorites + Clear Filters Bar (Requirement 1)
        LiveTvFiltersBar(
            categories = categories,
            selectedCategory = selectedCategory,
            onSelectCategory = { selectedCategory = it },
            isFavoritesOnly = isFavoritesOnly,
            onToggleFavoritesOnly = { isFavoritesOnly = it },
            onClearFilters = { clearAllFilters() }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Paged channels list
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                lazyChannels.loadState.refresh is LoadState.Loading && lazyChannels.itemCount == 0 -> {
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

                lazyChannels.loadState.refresh is LoadState.Error && lazyChannels.itemCount == 0 -> {
                    val error = (lazyChannels.loadState.refresh as LoadState.Error).error
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error.localizedMessage ?: "Error loading channels",
                            color = KrasRedBright,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LiquidGlassButton(
                            text = strings.retry,
                            icon = Icons.Default.Refresh,
                            isPrimary = true,
                            onClick = { lazyChannels.retry() }
                        )
                    }
                }

                lazyChannels.itemCount == 0 && lazyChannels.loadState.refresh is LoadState.NotLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.noChannelsFound,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            count = lazyChannels.itemCount,
                            key = { index -> lazyChannels.peek(index)?.id ?: "ch_$index" }
                        ) { index ->
                            val channel = lazyChannels[index]
                            if (channel != null) {
                                ChannelItemCard(
                                    channel = channel,
                                    onClick = { onChannelClick(channel) },
                                    onFavoriteClick = { onToggleFavorite(channel) },
                                    isFavorite = isFavorite(channel.id)
                                )
                            }
                        }

                        if (lazyChannels.loadState.append is LoadState.Loading) {
                            item {
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
