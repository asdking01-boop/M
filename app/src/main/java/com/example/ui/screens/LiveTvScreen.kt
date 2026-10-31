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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.localization.LocalKrasStrings
import com.example.ui.components.ChannelItemCard
import com.example.ui.components.LiquidGlassButton
import com.example.ui.glass.glassBackground
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.Flow

@Composable
fun LiveTvScreen(
    categories: List<String>,
    getChannelsPager: (category: String, query: String) -> Flow<PagingData<LiveChannelItem>>,
    onChannelClick: (LiveChannelItem) -> Unit,
    onToggleFavorite: (LiveChannelItem) -> Unit,
    isFavorite: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val allCategoryLabel = strings.allChannels
    val fullCategories = remember(categories, allCategoryLabel) {
        listOf(allCategoryLabel) + categories.filter { it.isNotBlank() && it != allCategoryLabel }
    }

    val selectedCategory = fullCategories.getOrNull(selectedCategoryIndex) ?: allCategoryLabel
    val categoryQueryParam = if (selectedCategoryIndex == 0) "" else selectedCategory

    val pagedFlow = remember(categoryQueryParam, searchQuery) {
        getChannelsPager(categoryQueryParam, searchQuery)
    }
    val lazyChannels = pagedFlow.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KrasBlack)
    ) {
        // Search Bar in Live TV
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
                        text = strings.searchChannels,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
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
                shape = RoundedCornerShape(16.dp),
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

        // Categories Tab Row
        if (fullCategories.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex.coerceIn(0, fullCategories.size - 1),
                containerColor = Color.Transparent,
                contentColor = KrasRedBright,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    val validIndex = selectedCategoryIndex.coerceIn(0, tabPositions.size - 1)
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[validIndex]),
                        color = KrasRedBright
                    )
                },
                divider = {}
            ) {
                fullCategories.forEachIndexed { index, catName ->
                    Tab(
                        selected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index },
                        text = {
                            Text(
                                text = catName,
                                fontSize = 13.sp,
                                fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategoryIndex == index) KrasRedBright else TextSecondary
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

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
