package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.IptvAccount
import com.example.data.model.PlayerAspectRatio
import com.example.localization.AppLanguage
import com.example.localization.LocalKrasStrings
import com.example.ui.glass.GlassBadge
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage,
    startupSoundEnabled: Boolean,
    defaultAspectRatio: String,
    autoPlayNext: Boolean,
    activeAccount: IptvAccount?,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetStartupSound: (Boolean) -> Unit,
    onSetDefaultAspectRatio: (PlayerAspectRatio) -> Unit,
    onSetAutoPlayNext: (Boolean) -> Unit,
    onOpenAccounts: () -> Unit,
    onClearCache: () -> Unit,
    onRefreshContent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalKrasStrings.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(KrasBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header: Settings
        item {
            Text(
                text = strings.settingsTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        }

        // Active Account Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = Color(0xFF131722),
                        borderColor = KrasRed.copy(alpha = 0.5f),
                        hasRedGlow = true
                    )
                    .clickable { onOpenAccounts() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KrasRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeAccount?.name ?: strings.iptvAccounts,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = strings.manageAccounts,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    GlassBadge(
                        text = strings.switchAccount,
                        backgroundColor = Color(0x33E50914)
                    )
                }
            }
        }

        // Section: General Settings
        item {
            Text(
                text = strings.generalSettings,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = KrasRedBright,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = Color(0xDD12151D)
                    )
                    .padding(vertical = 4.dp)
            ) {
                Column {
                    // Language Switcher (Arabic / English)
                    SettingItemWithContent(
                        icon = Icons.Default.Language,
                        title = strings.language,
                        subtitle = currentLanguage.displayName
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LanguageChip(
                                text = "العربية",
                                isSelected = currentLanguage == AppLanguage.ARABIC,
                                onClick = { onSetLanguage(AppLanguage.ARABIC) }
                            )
                            LanguageChip(
                                text = "English",
                                isSelected = currentLanguage == AppLanguage.ENGLISH,
                                onClick = { onSetLanguage(AppLanguage.ENGLISH) }
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(horizontal = 16.dp))

                    // Theme
                    SettingItem(
                        icon = Icons.Default.Palette,
                        title = "Liquid Glass UI",
                        subtitle = "iOS 26 Frosted Glassmorphism Dark"
                    ) {
                        GlassBadge(text = "Active", backgroundColor = KrasRed)
                    }

                    HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(horizontal = 16.dp))

                    // Refresh Content
                    SettingItem(
                        icon = Icons.Default.Autorenew,
                        title = strings.refreshContent,
                        subtitle = strings.contentRefreshed,
                        onClick = {
                            onRefreshContent()
                            Toast.makeText(context, strings.contentRefreshed, Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(horizontal = 16.dp))

                    // Clear Cache
                    SettingItem(
                        icon = Icons.Default.CleanHands,
                        title = strings.clearCache,
                        subtitle = strings.cacheCleared,
                        onClick = {
                            onClearCache()
                            Toast.makeText(context, strings.cacheCleared, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Section: Player Settings
        item {
            Text(
                text = strings.playerSettings,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = KrasRedBright,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = Color(0xDD12151D)
                    )
                    .padding(vertical = 4.dp)
            ) {
                Column {
                    // Startup Chime Toggle
                    SettingItem(
                        icon = Icons.Default.MusicNote,
                        title = strings.startupSound,
                        subtitle = "Original KRAS TV luxury chime"
                    ) {
                        Switch(
                            checked = startupSoundEnabled,
                            onCheckedChange = { onSetStartupSound(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = KrasRed,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(horizontal = 16.dp))

                    // Auto-play Next Episode
                    SettingItem(
                        icon = Icons.Default.PlayCircle,
                        title = strings.autoPlayNext,
                        subtitle = strings.nextEpisode
                    ) {
                        Switch(
                            checked = autoPlayNext,
                            onCheckedChange = { onSetAutoPlayNext(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = KrasRed,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(horizontal = 16.dp))

                    // Default Aspect Ratio
                    SettingItemWithContent(
                        icon = Icons.Default.AspectRatio,
                        title = strings.aspectRatio,
                        subtitle = defaultAspectRatio
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PlayerAspectRatio.entries.forEach { ratio ->
                                val isSelected = defaultAspectRatio == ratio.name
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) KrasRed else Color(0x22FFFFFF))
                                        .clickable { onSetDefaultAspectRatio(ratio) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = ratio.labelEn,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: About & Privacy Information
        item {
            Text(
                text = strings.appInfo,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = KrasRedBright,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = Color(0xDD12151D)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KrasRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_kras_logo),
                                contentDescription = "Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "KRAS",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "TV",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = KrasRedBright
                                )
                            }
                            Text(
                                text = "${strings.appVersion} 1.0.0 (Build 2026.1)",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = strings.aboutDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = LiveGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.privacyDescription,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) KrasRed else Color(0x22FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = KrasRedBright,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }

        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
private fun SettingItemWithContent(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x1AFFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = KrasRedBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            content()
        }
    }
}
