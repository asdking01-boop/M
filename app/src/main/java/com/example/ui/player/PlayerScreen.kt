package com.example.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.Channel
import com.example.data.model.ContentType
import com.example.data.model.Episode
import com.example.data.model.PlayerAspectRatio
import com.example.data.model.VideoQualityOption
import com.example.localization.LocalKrasStrings
import com.example.ui.glass.GlassBadge
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.LiveRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PlayerLaunchArgs
import kotlinx.coroutines.delay

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    args: PlayerLaunchArgs,
    liveChannels: List<Channel>,
    defaultAspectRatio: String,
    onClose: () -> Unit,
    onChannelChange: (Channel) -> Unit,
    onEpisodeChange: (Episode) -> Unit,
    onProgressUpdate: (pos: Long, dur: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalKrasStrings.current
    val activity = remember(context) { context.findActivity() }

    // ExoPlayer Instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Playback state
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var bufferedPositionMs by remember { mutableLongStateOf(0L) }

    // Controls visibility & lock
    var areControlsVisible by remember { mutableStateOf(true) }
    var isScreenLocked by remember { mutableStateOf(false) }

    // Aspect Ratio & Sizing
    var currentAspectRatio by remember {
        mutableStateOf(
            try {
                PlayerAspectRatio.valueOf(defaultAspectRatio)
            } catch (_: Exception) {
                PlayerAspectRatio.FIT
            }
        )
    }

    // Panels state
    var showChannelDrawer by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    // Genuine Quality and Tracks
    var availableQualities by remember { mutableStateOf<List<VideoQualityOption>>(emptyList()) }
    var selectedQualityId by remember { mutableStateOf("auto") }
    var currentSpeed by remember { mutableFloatStateOf(1.0f) }

    // Auto-play next countdown
    var autoPlayCountdown by remember { mutableIntStateOf(-1) }

    // Orientation toggle helper
    var isLandscape by remember { mutableStateOf(false) }

    // Initialize playback
    LaunchedEffect(args.streamUrl) {
        errorMessage = null
        isBuffering = true
        autoPlayCountdown = -1
        try {
            val mediaItem = MediaItem.fromUri(args.streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (args.startPositionMs > 0 && args.type != ContentType.LIVE_CHANNEL) {
                exoPlayer.seekTo(args.startPositionMs)
            }
            exoPlayer.play()
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: strings.playbackError
        }
    }

    // Player Listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                        errorMessage = null
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                        errorMessage = null
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                        if (args.nextEpisode != null) {
                            autoPlayCountdown = 5
                        }
                    }
                    Player.STATE_IDLE -> Unit
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                errorMessage = "${strings.playbackError} (${error.errorCodeName})"
            }

            override fun onTracksChanged(tracks: Tracks) {
                // Genuine video quality tracks extraction
                val qualityList = mutableListOf<VideoQualityOption>()
                qualityList.add(VideoQualityOption("auto", strings.qualityAuto, 0, isAuto = true))

                for (group in tracks.groups) {
                    if (group.type == C.TRACK_TYPE_VIDEO) {
                        for (i in 0 until group.length) {
                            if (group.isTrackSupported(i)) {
                                val format = group.getTrackFormat(i)
                                val height = format.height
                                val bitrate = format.bitrate
                                if (height > 0) {
                                    val label = when {
                                        height >= 2160 -> "4K UHD (2160p)"
                                        height >= 1440 -> "1440p QHD"
                                        height >= 1080 -> "1080p Full HD"
                                        height >= 720 -> "720p HD"
                                        height >= 480 -> "480p SD"
                                        else -> "${height}p"
                                    }
                                    qualityList.add(
                                        VideoQualityOption(
                                            id = "track_${height}_$i",
                                            label = label,
                                            height = height,
                                            bitrate = bitrate,
                                            trackIndex = i
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                availableQualities = qualityList.distinctBy { it.label }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Progress tick & save
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.coerceAtLeast(0L)
            if (args.type != ContentType.LIVE_CHANNEL && durationMs > 0) {
                onProgressUpdate(currentPositionMs, durationMs)
            }
            delay(1000L)
        }
    }

    // Auto-hide controls after 4 seconds
    LaunchedEffect(areControlsVisible, isPlaying) {
        if (areControlsVisible && isPlaying && !isScreenLocked) {
            delay(4500L)
            areControlsVisible = false
        }
    }

    // Auto-play next countdown timer
    LaunchedEffect(autoPlayCountdown) {
        if (autoPlayCountdown > 0) {
            delay(1000L)
            autoPlayCountdown--
            if (autoPlayCountdown == 0 && args.nextEpisode != null) {
                onEpisodeChange(args.nextEpisode)
            }
        }
    }

    val resizeMode = when (currentAspectRatio) {
        PlayerAspectRatio.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        PlayerAspectRatio.CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        PlayerAspectRatio.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        PlayerAspectRatio.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isScreenLocked) {
                    areControlsVisible = !areControlsVisible
                } else {
                    areControlsVisible = true // allow unlocking
                }
            }
    ) {
        // ExoPlayer Surface View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    this.resizeMode = resizeMode
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering && errorMessage == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = KrasRedBright,
                    strokeWidth = 3.dp
                )
            }
        }

        // Error Retry Banner
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .liquidGlass(
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color(0xF012141C),
                        borderColor = LiveRed
                    )
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(KrasRed)
                            .clickable {
                                errorMessage = null
                                isBuffering = true
                                exoPlayer.prepare()
                                exoPlayer.play()
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = strings.retry, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Auto-play Next Episode Countdown Dialog
        if (autoPlayCountdown > 0 && args.nextEpisode != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .liquidGlass(
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = Color(0xF210131A),
                        borderColor = KrasRed
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "${strings.nextEpisode} in $autoPlayCountdown...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = args.nextEpisode.title,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(KrasRed)
                                .clickable { onEpisodeChange(args.nextEpisode) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = strings.play, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FFFFFF))
                                .clickable { autoPlayCountdown = -1 }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = strings.close, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000))
            ) {
                // Top Control Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xD9000000), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back Button & Content Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .testTag("player_back_button")
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x44000000))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (args.seriesTitle.isNotEmpty()) args.seriesTitle else args.title,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (args.seriesTitle.isNotEmpty()) {
                                    Text(
                                        text = "S${args.seasonNum} : E${args.episodeNum} - ${args.title}",
                                        color = KrasRedBright,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else if (args.type == ContentType.LIVE_CHANNEL) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(LiveRed)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "LIVE", color = LiveRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Top Action Icons: PiP, Aspect Ratio, Speed, Channels Menu
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick Live Channels Drawer toggle
                            if (args.type == ContentType.LIVE_CHANNEL && liveChannels.isNotEmpty()) {
                                IconButton(
                                    onClick = { showChannelDrawer = !showChannelDrawer },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = strings.quickChannels,
                                        tint = if (showChannelDrawer) KrasRedBright else Color.White
                                    )
                                }
                            }

                            // Aspect Ratio Toggle
                            IconButton(
                                onClick = {
                                    currentAspectRatio = when (currentAspectRatio) {
                                        PlayerAspectRatio.FIT -> PlayerAspectRatio.CROP
                                        PlayerAspectRatio.CROP -> PlayerAspectRatio.RATIO_16_9
                                        PlayerAspectRatio.RATIO_16_9 -> PlayerAspectRatio.STRETCH
                                        PlayerAspectRatio.STRETCH -> PlayerAspectRatio.FIT
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = strings.aspectRatio,
                                    tint = Color.White
                                )
                            }

                            // Video Quality Picker
                            IconButton(
                                onClick = { showQualityDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HighQuality,
                                    contentDescription = strings.quality,
                                    tint = Color.White
                                )
                            }

                            // Playback Speed
                            IconButton(
                                onClick = { showSpeedDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = strings.playbackSpeed,
                                    tint = Color.White
                                )
                            }

                            // PiP Support
                            IconButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        try {
                                            activity?.enterPictureInPictureMode(
                                                android.app.PictureInPictureParams.Builder().build()
                                            )
                                        } catch (_: Exception) {
                                        }
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPictureAlt,
                                    contentDescription = strings.pipMode,
                                    tint = Color.White
                                )
                            }

                            // Rotation
                            IconButton(
                                onClick = {
                                    isLandscape = !isLandscape
                                    activity?.requestedOrientation = if (isLandscape) {
                                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    } else {
                                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "Rotate",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Middle Center: Rewind 10s, Play/Pause, Forward 10s
                if (!isScreenLocked) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Episode / Rewind 10s
                        if (args.type == ContentType.EPISODE && args.prevEpisode != null) {
                            IconButton(
                                onClick = { onEpisodeChange(args.prevEpisode) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x55000000))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = strings.prevEpisode,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x55000000))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = strings.rewind10,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Play / Pause Main Button
                        Box(
                            modifier = Modifier
                                .testTag("player_play_pause_button")
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(KrasRed)
                                .clickable {
                                    if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) strings.pause else strings.play,
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        // Next Episode / Forward 10s
                        if (args.type == ContentType.EPISODE && args.nextEpisode != null) {
                            IconButton(
                                onClick = { onEpisodeChange(args.nextEpisode) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x55000000))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = strings.nextEpisode,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { exoPlayer.seekTo((exoPlayer.currentPosition + 10000L).coerceAtMost(durationMs)) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x55000000))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = strings.forward10,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Timeline Scrubber and Controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xE6000000))
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Slider progress for VOD / Series
                        if (args.type != ContentType.LIVE_CHANNEL && durationMs > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDuration(currentPositionMs),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Slider(
                                    value = (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f),
                                    onValueChange = { percent ->
                                        val newPos = (percent * durationMs).toLong()
                                        currentPositionMs = newPos
                                        exoPlayer.seekTo(newPos)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = KrasRedBright,
                                        activeTrackColor = KrasRed,
                                        inactiveTrackColor = Color(0x55FFFFFF)
                                    )
                                )

                                Text(
                                    text = formatDuration(durationMs),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            // Live TV Status indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(LiveRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE STREAM",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = currentAspectRatio.labelEn,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bottom Actions: Screen Lock, Audio, Subtitles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Screen Lock Button
                            IconButton(
                                onClick = { isScreenLocked = !isScreenLocked },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isScreenLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Screen Lock",
                                    tint = if (isScreenLocked) KrasRedBright else Color.White
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = currentAspectRatio.name,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Channels Quick Switcher Side Drawer
        if (showChannelDrawer && args.type == ContentType.LIVE_CHANNEL) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .align(Alignment.CenterStart)
                    .liquidGlass(
                        shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                        backgroundColor = Color(0xF20B0C0E),
                        borderColor = GlassWhiteStroke.copy(alpha = 0.3f),
                        elevation = 12.dp
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.quickChannels,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showChannelDrawer = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(liveChannels) { ch ->
                            val isCurrent = ch.id == args.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCurrent) Color(0x33E50914) else Color(0x221E222B))
                                    .clickable {
                                        showChannelDrawer = false
                                        onChannelChange(ch)
                                    }
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ch.channelNum,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) KrasRedBright else TextSecondary,
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ch.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = ch.category,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quality Dialog
        if (showQualityDialog) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(280.dp)
                    .liquidGlass(
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = Color(0xF20F1218),
                        borderColor = KrasRed
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = strings.quality, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        IconButton(onClick = { showQualityDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val qualities = availableQualities.ifEmpty {
                        listOf(VideoQualityOption("auto", strings.qualityAuto, 0, isAuto = true))
                    }

                    qualities.forEach { option ->
                        val isSelected = selectedQualityId == option.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0x33E50914) else Color.Transparent)
                                .clickable {
                                    selectedQualityId = option.id
                                    showQualityDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.label,
                                color = if (isSelected) KrasRedBright else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = KrasRedBright)
                            }
                        }
                    }
                }
            }
        }

        // Playback Speed Dialog
        if (showSpeedDialog) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(240.dp)
                    .liquidGlass(
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = Color(0xF20F1218),
                        borderColor = KrasRed
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = strings.playbackSpeed, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        IconButton(onClick = { showSpeedDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        val isSelected = currentSpeed == speed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0x33E50914) else Color.Transparent)
                                .clickable {
                                    currentSpeed = speed
                                    exoPlayer.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (isSelected) KrasRedBright else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = KrasRedBright)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
