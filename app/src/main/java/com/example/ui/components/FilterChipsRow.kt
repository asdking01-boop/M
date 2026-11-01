package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalKrasStrings
import com.example.ui.glass.glassBackground
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.GoldStar
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun KrasDebouncedSearchBar(
    query: String,
    onDebouncedQuery: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var textInput by remember(query) { mutableStateOf(query) }

    LaunchedEffect(textInput) {
        if (textInput != query) {
            delay(300L)
            onDebouncedQuery(textInput)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextSecondary,
                    fontSize = 13.5.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(19.dp)
                )
            },
            trailingIcon = {
                if (textInput.isNotEmpty()) {
                    IconButton(onClick = {
                        textInput = ""
                        onDebouncedQuery("")
                    }) {
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
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) {
                    Modifier.glassBackground(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = KrasRed.copy(alpha = 0.90f),
                        borderColor = KrasRedBright,
                        hasRedGlow = true,
                        elevation = 4.dp
                    )
                } else {
                    Modifier.glassBackground(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color(0x66141822),
                        borderColor = Color.White.copy(alpha = 0.18f),
                        elevation = 1.dp
                    )
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextPrimary
            )
        }
    }
}

@Composable
fun LiveTvFiltersBar(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    isFavoritesOnly: Boolean,
    onToggleFavoritesOnly: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current
    val isAnyFilterActive = selectedCategory.isNotBlank() || isFavoritesOnly

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Clear filters chip
        if (isAnyFilterActive) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .glassBackground(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color(0x4DE50914),
                        borderColor = KrasRedBright,
                        hasRedGlow = true,
                        elevation = 3.dp
                    )
                    .clickable { onClearFilters() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = strings.clearFilters,
                        tint = KrasRedBright,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = strings.clearFilters,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KrasRedBright
                    )
                }
            }
        }

        // Favorites filter chip
        FilterChipItem(
            label = strings.favoritesOnly,
            isSelected = isFavoritesOnly,
            onClick = { onToggleFavoritesOnly(!isFavoritesOnly) },
            leadingIcon = {
                Icon(
                    imageVector = if (isFavoritesOnly) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isFavoritesOnly) Color.White else KrasRedBright,
                    modifier = Modifier.size(14.dp)
                )
            }
        )

        // All Channels chip
        FilterChipItem(
            label = strings.allChannels,
            isSelected = selectedCategory.isEmpty(),
            onClick = { onSelectCategory("") }
        )

        // Category chips
        categories.filter { it.isNotBlank() && it != strings.allChannels }.forEach { cat ->
            FilterChipItem(
                label = cat,
                isSelected = selectedCategory == cat,
                onClick = { onSelectCategory(if (selectedCategory == cat) "" else cat) }
            )
        }
    }
}

@Composable
fun MediaFiltersBar(
    genres: List<String>,
    selectedGenre: String,
    onSelectGenre: (String) -> Unit,
    years: List<String>,
    selectedYear: String,
    onSelectYear: (String) -> Unit,
    minRating: Double,
    onSelectRating: (Double) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current
    val isAnyFilterActive = selectedGenre.isNotBlank() || selectedYear.isNotBlank() || minRating > 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clear Filters chip
            if (isAnyFilterActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .glassBackground(
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color(0x4DE50914),
                            borderColor = KrasRedBright,
                            hasRedGlow = true,
                            elevation = 3.dp
                        )
                        .clickable { onClearFilters() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterListOff,
                            contentDescription = strings.clearFilters,
                            tint = KrasRedBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = strings.clearFilters,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KrasRedBright
                        )
                    }
                }
            }

            // Rating Filters
            FilterChipItem(
                label = strings.ratingAbove8,
                isSelected = minRating >= 8.0,
                onClick = { onSelectRating(if (minRating >= 8.0) 0.0 else 8.0) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldStar,
                        modifier = Modifier.size(13.dp)
                    )
                }
            )

            FilterChipItem(
                label = strings.ratingAbove7,
                isSelected = minRating in 7.0..7.9,
                onClick = { onSelectRating(if (minRating in 7.0..7.9) 0.0 else 7.0) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldStar,
                        modifier = Modifier.size(13.dp)
                    )
                }
            )

            // Year chips
            years.take(6).forEach { yr ->
                FilterChipItem(
                    label = yr,
                    isSelected = selectedYear == yr,
                    onClick = { onSelectYear(if (selectedYear == yr) "" else yr) }
                )
            }

            // All Genres chip
            FilterChipItem(
                label = strings.filterAll,
                isSelected = selectedGenre.isEmpty(),
                onClick = { onSelectGenre("") }
            )

            // Genre chips
            genres.filter { it.isNotBlank() && it != strings.filterAll }.forEach { genre ->
                FilterChipItem(
                    label = genre,
                    isSelected = selectedGenre == genre,
                    onClick = { onSelectGenre(if (selectedGenre == genre) "" else genre) }
                )
            }
        }
    }
}
