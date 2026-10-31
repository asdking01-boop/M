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
import com.example.data.model.Channel
import com.example.localization.LocalKrasStrings
import com.example.ui.components.ChannelItemCard
import com.example.ui.glass.GlassBadge
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun LiveTvScreen(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    isFavorite: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Dynamic categories extraction
    val dynamicCategories = remember(channels) {
        val groups = channels.map { it.category }.distinct().filter { it.isNotBlank() }
        listOf(strings.allChannels) + groups
    }

    val selectedCategory = dynamicCategories.getOrNull(selectedCategoryIndex) ?: strings.allChannels

    // Efficient memoized filtering of channels for large datasets (10,000+ items)
    val filteredChannels = remember(channels, selectedCategoryIndex, selectedCategory, searchQuery) {
        channels.filter { ch ->
            val matchesCategory = selectedCategoryIndex == 0 || ch.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    ch.name.contains(searchQuery, ignoreCase = true) ||
                    ch.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // Incremental pagination: start with initial window and expand as user scrolls
    var displayedCount by remember(selectedCategoryIndex, searchQuery) { mutableIntStateOf(60) }
    val listState = rememberLazyListState()

    // Detect when user approaches the end of loaded items and load more incrementally
    LaunchedEffect(listState, filteredChannels.size) {
        snapshotFlow {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible to totalItems
        }.distinctUntilChanged().collect { (lastVisible, totalItems) ->
            if (totalItems > 0 && lastVisible >= totalItems - 15 && displayedCount < filteredChannels.size) {
                displayedCount = (displayedCount + 60).coerceAtMost(filteredChannels.size)
            }
        }
    }

    val visibleChannels = remember(filteredChannels, displayedCount) {
        if (displayedCount >= filteredChannels.size) {
            filteredChannels
        } else {
            filteredChannels.subList(0, displayedCount)
        }
    }

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
                        text = "${strings.searchChannels} (${channels.size})",
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

        // Category Tabs with channel counts
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
            dynamicCategories.forEachIndexed { index, categoryName ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = {
                        Text(
                            text = categoryName,
                            fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategoryIndex == index) TextPrimary else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Channels List with incremental loading
        if (filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.noSearchResults,
                    color = TextSecondary,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibleChannels, key = { it.id }) { channel ->
                    ChannelItemCard(
                        channel = channel,
                        onClick = { onChannelClick(channel) },
                        onFavoriteClick = { onToggleFavorite(channel) },
                        isFavorite = isFavorite(channel.id)
                    )
                }

                // Incremental loader footer when more items are being revealed
                if (displayedCount < filteredChannels.size) {
                    item(key = "loader_footer") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = KrasRedBright,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.size(10.dp))
                                Text(
                                    text = "${visibleChannels.size} / ${filteredChannels.size}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
