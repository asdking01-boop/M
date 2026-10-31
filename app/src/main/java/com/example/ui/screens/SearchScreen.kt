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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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

    val matchingChannels = if (searchQuery.isNotBlank()) {
        channels.filter { it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
    } else emptyList()

    val matchingMovies = if (searchQuery.isNotBlank()) {
        movies.filter { it.title.contains(searchQuery, ignoreCase = true) || it.genre.contains(searchQuery, ignoreCase = true) }
    } else emptyList()

    val matchingSeries = if (searchQuery.isNotBlank()) {
        series.filter { it.title.contains(searchQuery, ignoreCase = true) || it.genre.contains(searchQuery, ignoreCase = true) }
    } else emptyList()

    val totalMatches = matchingChannels.size + matchingMovies.size + matchingSeries.size

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
                    items(matchingChannels) { ch ->
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
                    items(matchingMovies) { mov ->
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
                    items(matchingSeries) { ser ->
                        SeriesPosterCard(
                            series = ser,
                            onClick = { onSeriesClick(ser) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
