package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.LocalKrasStrings
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalKrasStrings.current
    val scale = remember { Animatable(0.75f) }
    val alpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        glowAlpha.animateTo(0.6f, animationSpec = tween(900, easing = FastOutSlowInEasing))
        scale.animateTo(1.05f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
        scale.animateTo(1.0f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        delay(600L)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF20090D),
                        KrasBlack,
                        Color.Black
                    ),
                    radius = 900f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Red Ambient Glow behind logo
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(scale.value * 1.3f)
                .alpha(glowAlpha.value)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            KrasRed.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // KRAS TV Logo Emblem
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .liquidGlass(
                        shape = RoundedCornerShape(28.dp),
                        backgroundColor = Color(0xFF141720),
                        borderColor = KrasRedBright,
                        borderWidth = 1.5.dp,
                        hasRedGlow = true,
                        elevation = 16.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_kras_logo),
                    contentDescription = "KRAS TV",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // KRAS TV Lettering
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "KRAS",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TV",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = KrasRedBright
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.tagline,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }
    }
}
