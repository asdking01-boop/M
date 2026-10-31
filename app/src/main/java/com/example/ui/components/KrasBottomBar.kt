package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalKrasStrings
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.KrasRedGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.Screen

private data class NavItemData(
    val screen: Screen,
    val title: (strings: com.example.localization.KrasStrings) -> String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun KrasBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current

    val items = remember {
        listOf(
            NavItemData(Screen.Home, { it.navHome }, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            NavItemData(Screen.LiveTv, { it.navLive }, Icons.Filled.LiveTv, Icons.Outlined.LiveTv, "nav_live"),
            NavItemData(Screen.Movies, { it.navMovies }, Icons.Filled.Movie, Icons.Outlined.Movie, "nav_movies"),
            NavItemData(Screen.Series, { it.navSeries }, Icons.Filled.Tv, Icons.Outlined.Tv, "nav_series"),
            NavItemData(Screen.Favorites, { it.navFavorites }, Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "nav_favorites"),
            NavItemData(Screen.Settings, { it.navSettings }, Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Floating Liquid Glass Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0xE610131A),
                    borderColor = GlassWhiteStroke.copy(alpha = 0.35f),
                    hasRedGlow = false,
                    elevation = 12.dp
                )
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) Color.White else TextSecondary,
                        animationSpec = spring(),
                        label = "iconTint"
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .testTag(item.testTag)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onNavigate(item.screen)
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        // Icon with active glowing pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) KrasRed else Color.Transparent
                                )
                                .padding(horizontal = 14.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title(strings),
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = item.title(strings),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
