package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AccountType
import com.example.data.model.IptvAccount
import com.example.localization.LocalKrasStrings
import com.example.ui.glass.glassBackground
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun KrasTopBar(
    activeAccount: IptvAccount?,
    onSearchClick: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .glassBackground(
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xE00C0F16),
                borderColor = Color.White.copy(alpha = 0.30f),
                borderWidth = 1.dp,
                elevation = 8.dp,
                highlightIntensity = 0.22f
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Branding Logo & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onAccountClick() }
                    .padding(4.dp)
            ) {
                // KRAS TV Liquid Glass emblem badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .glassBackground(
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color(0xFF141720),
                            borderColor = KrasRedBright,
                            borderWidth = 1.2.dp,
                            hasRedGlow = true,
                            elevation = 6.dp,
                            highlightIntensity = 0.40f
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_kras_logo),
                        contentDescription = "KRAS TV Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "KRAS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TV",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = KrasRedBright
                        )
                    }

                    // Status pill
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (activeAccount?.type == AccountType.DEMO) Color(0xFFFFB800) else LiveGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = activeAccount?.name ?: "KRAS IPTV",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Liquid Glass Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassIconButton(
                    icon = Icons.Default.Search,
                    contentDescription = strings.searchTitle,
                    testTag = "top_search_button",
                    onClick = onSearchClick
                )

                GlassIconButton(
                    icon = Icons.Default.Tune,
                    contentDescription = strings.manageAccounts,
                    testTag = "top_accounts_button",
                    tint = KrasRedBright,
                    onClick = onAccountClick
                )
            }
        }
    }
}

@Composable
fun GlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    tint: Color = TextPrimary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(),
        label = "glassIconScale"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .size(40.dp)
            .glassBackground(
                shape = CircleShape,
                backgroundColor = Color(0x33202533),
                borderColor = Color.White.copy(alpha = 0.28f),
                borderWidth = 1.dp,
                elevation = 4.dp,
                highlightIntensity = 0.25f
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}
