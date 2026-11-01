package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Channel
import com.example.data.model.ContentType
import com.example.data.model.SeriesItem
import com.example.localization.AppLanguage
import com.example.localization.ArabicStrings
import com.example.localization.EnglishStrings
import com.example.localization.LocalKrasLanguage
import com.example.localization.LocalKrasStrings
import com.example.ui.components.AccountManagerDialog
import com.example.ui.components.KrasBottomBar
import com.example.ui.components.KrasTopBar
import com.example.ui.components.MovieDetailSheet
import com.example.ui.components.SeriesDetailSheet
import com.example.ui.player.PlayerScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PlayerLaunchArgs
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val isInPipModeState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
            val krasStrings = if (currentLanguage == AppLanguage.ARABIC) ArabicStrings else EnglishStrings
            val layoutDirection = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection,
                LocalKrasStrings provides krasStrings,
                LocalKrasLanguage provides currentLanguage
            ) {
                MyApplicationTheme {
                    KrasTvApp(
                        viewModel = viewModel,
                        isInPipMode = isInPipModeState.value
                    )
                }
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipModeState.value = isInPictureInPictureMode
    }
}

@Composable
fun KrasTvApp(
    viewModel: MainViewModel,
    isInPipMode: Boolean
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activePlayerArgs by viewModel.activePlayerArgs.collectAsStateWithLifecycle()
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()

    // Lightweight preview lists for Home Screen (Requirement 3: keep memory small)
    val homeChannels by viewModel.homeChannels.collectAsStateWithLifecycle()
    val homeMovies by viewModel.homeMovies.collectAsStateWithLifecycle()
    val homeSeries by viewModel.homeSeries.collectAsStateWithLifecycle()

    val channelCategories by viewModel.getChannelCategories().collectAsStateWithLifecycle(emptyList())
    val movieGenres by viewModel.getMovieGenres().collectAsStateWithLifecycle(emptyList())
    val seriesGenres by viewModel.getSeriesGenres().collectAsStateWithLifecycle(emptyList())
    val movieYears by viewModel.getMovieYears().collectAsStateWithLifecycle(emptyList())
    val seriesYears by viewModel.getSeriesYears().collectAsStateWithLifecycle(emptyList())

    val recentlyAddedMovies by viewModel.getRecentlyAddedMovies().collectAsStateWithLifecycle(emptyList())
    val latestReleaseMovies by viewModel.getLatestReleaseMovies().collectAsStateWithLifecycle(emptyList())
    val topRatedMovies by viewModel.getTopRatedMovies().collectAsStateWithLifecycle(emptyList())
    val movieContinueWatching by viewModel.getContinueWatching(ContentType.MOVIE).collectAsStateWithLifecycle(emptyList())

    val recentlyAddedSeries by viewModel.getRecentlyAddedSeries().collectAsStateWithLifecycle(emptyList())
    val latestReleaseSeries by viewModel.getLatestReleaseSeries().collectAsStateWithLifecycle(emptyList())
    val topRatedSeries by viewModel.getTopRatedSeries().collectAsStateWithLifecycle(emptyList())
    val seriesContinueWatching by viewModel.getContinueWatching(ContentType.SERIES).collectAsStateWithLifecycle(emptyList())

    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val selectedMovie by viewModel.selectedMovie.collectAsStateWithLifecycle()
    val selectedSeries by viewModel.selectedSeries.collectAsStateWithLifecycle()
    val showAccountDialog by viewModel.showAccountDialog.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val startupSoundEnabled by viewModel.startupSoundEnabled.collectAsStateWithLifecycle()
    val defaultAspectRatio by viewModel.defaultAspectRatio.collectAsStateWithLifecycle()
    val autoPlayNext by viewModel.autoPlayNext.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadingStatus by viewModel.loadingStatus.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val savedCrashReport by viewModel.savedCrashReport.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

    // Back handling
    BackHandler(enabled = activePlayerArgs != null) {
        viewModel.closePlayer()
    }

    BackHandler(enabled = activePlayerArgs == null && currentScreen != Screen.Home && currentScreen != Screen.Splash) {
        viewModel.navigateTo(Screen.Home)
    }

    // Full-screen Player view
    if (activePlayerArgs != null) {
        val playerArgs = activePlayerArgs!!
        // Convert lightweight home channels to Channel items for player drawer
        val playerChannels = remember(homeChannels) {
            homeChannels.map {
                Channel(
                    id = it.id,
                    streamId = it.streamId,
                    name = it.name,
                    streamUrl = it.streamUrl,
                    logoUrl = it.logoUrl,
                    category = it.categoryName,
                    channelNum = it.channelNum
                )
            }
        }

        PlayerScreen(
            args = playerArgs,
            liveChannels = playerChannels,
            defaultAspectRatio = defaultAspectRatio,
            onClose = { viewModel.closePlayer() },
            onChannelChange = { newChannel ->
                val chItem = homeChannels.find { it.id == newChannel.id }
                if (chItem != null) {
                    viewModel.playChannel(chItem)
                } else {
                    viewModel.openPlayer(
                        PlayerLaunchArgs(
                            id = newChannel.id,
                            type = ContentType.LIVE_CHANNEL,
                            title = newChannel.name,
                            streamUrl = newChannel.streamUrl,
                            posterUrl = newChannel.logoUrl
                        )
                    )
                }
            },
            onEpisodeChange = { newEp ->
                selectedSeries?.let { ser ->
                    val season = ser.seasons.find { it.seasonNum == newEp.seasonNum }
                    if (season != null) {
                        viewModel.playEpisode(ser, season, newEp)
                    }
                }
            },
            onProgressUpdate = { pos, dur ->
                viewModel.recordProgress(
                    contentId = playerArgs.id,
                    type = playerArgs.type,
                    title = playerArgs.title,
                    poster = playerArgs.posterUrl,
                    streamUrl = playerArgs.streamUrl,
                    pos = pos,
                    dur = dur,
                    season = playerArgs.seasonNum,
                    ep = playerArgs.episodeNum,
                    seriesTitle = playerArgs.seriesTitle
                )
            }
        )
        return
    }

    if (currentScreen == Screen.Splash) {
        SplashScreen(
            onTimeout = { viewModel.onSplashFinished() }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = KrasBlack,
        topBar = {
            if (currentScreen != Screen.Search && !isInPipMode) {
                KrasTopBar(
                    activeAccount = activeAccount,
                    onSearchClick = { viewModel.navigateTo(Screen.Search) },
                    onAccountClick = { viewModel.setAccountDialogVisible(true) }
                )
            }
        },
        bottomBar = {
            if (!isInPipMode) {
                KrasBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(KrasBlack)
        ) {
            when (currentScreen) {
                Screen.Splash -> Unit
                Screen.Home -> {
                    HomeScreen(
                        featuredMovie = homeMovies.firstOrNull(),
                        continueWatching = continueWatching,
                        channels = homeChannels,
                        movies = homeMovies,
                        series = homeSeries,
                        activeAccount = activeAccount,
                        loadingStatus = loadingStatus,
                        isLoading = isLoading,
                        onWatchMovie = { mov -> viewModel.playMovie(mov) },
                        onInfoMovie = { mov -> viewModel.showMovieDetails(mov) },
                        onWatchChannel = { ch -> viewModel.playChannel(ch) },
                        onSelectSeries = { ser -> viewModel.showSeriesDetails(ser) },
                        onResumeHistory = { item -> viewModel.resumeHistory(item) },
                        onNavigate = { target -> viewModel.navigateTo(target) }
                    )
                }

                Screen.LiveTv -> {
                    LiveTvScreen(
                        categories = channelCategories,
                        getChannelsPager = { cat, fav, q -> viewModel.getChannelsPager(cat, fav, q) },
                        onChannelClick = { ch -> viewModel.playChannel(ch) },
                        onToggleFavorite = { ch ->
                            viewModel.toggleFavorite(
                                id = ch.id,
                                type = ContentType.LIVE_CHANNEL,
                                title = ch.name,
                                poster = ch.logoUrl,
                                category = ch.categoryName,
                                streamUrl = ch.streamUrl
                            )
                        },
                        isFavorite = { id -> viewModel.isFavorite(id) }
                    )
                }

                Screen.Movies -> {
                    MoviesScreen(
                        genres = movieGenres,
                        years = movieYears,
                        recentlyAdded = recentlyAddedMovies,
                        latestReleases = latestReleaseMovies,
                        topRated = topRatedMovies,
                        continueWatching = movieContinueWatching,
                        getMoviesPager = { genre, year, minRating, q, sortMode ->
                            viewModel.getMoviesPager(genre, year, minRating, q, sortMode)
                        },
                        onMovieClick = { mov -> viewModel.showMovieDetails(mov) },
                        onToggleFavorite = { mov ->
                            viewModel.toggleFavorite(
                                id = mov.id,
                                type = ContentType.MOVIE,
                                title = mov.title,
                                poster = mov.posterUrl,
                                category = mov.categoryName,
                                streamUrl = mov.streamUrl
                            )
                        },
                        onResumeHistory = { item -> viewModel.resumeHistory(item) },
                        isFavorite = { id -> viewModel.isFavorite(id) }
                    )
                }

                Screen.Series -> {
                    SeriesScreen(
                        genres = seriesGenres,
                        years = seriesYears,
                        recentlyAdded = recentlyAddedSeries,
                        latestReleases = latestReleaseSeries,
                        topRated = topRatedSeries,
                        continueWatching = seriesContinueWatching,
                        getSeriesPager = { genre, year, minRating, q, sortMode ->
                            viewModel.getSeriesPager(genre, year, minRating, q, sortMode)
                        },
                        onSeriesClick = { ser -> viewModel.showSeriesDetails(ser) },
                        onResumeHistory = { item -> viewModel.resumeHistory(item) }
                    )
                }

                Screen.Favorites -> {
                    FavoritesScreen(
                        favorites = favorites,
                        onPlayFavorite = { fav ->
                            when (fav.type) {
                                ContentType.LIVE_CHANNEL -> {
                                    val ch = homeChannels.find { it.id == fav.id }
                                    if (ch != null) viewModel.playChannel(ch)
                                    else {
                                        viewModel.openPlayer(
                                            PlayerLaunchArgs(
                                                id = fav.id,
                                                type = fav.type,
                                                title = fav.title,
                                                streamUrl = fav.streamUrl,
                                                posterUrl = fav.logoOrPosterUrl
                                            )
                                        )
                                    }
                                }
                                ContentType.MOVIE -> {
                                    val mov = homeMovies.find { it.id == fav.id }
                                    if (mov != null) viewModel.playMovie(mov)
                                    else {
                                        viewModel.openPlayer(
                                            PlayerLaunchArgs(
                                                id = fav.id,
                                                type = fav.type,
                                                title = fav.title,
                                                streamUrl = fav.streamUrl,
                                                posterUrl = fav.logoOrPosterUrl
                                            )
                                        )
                                    }
                                }
                                ContentType.SERIES -> {
                                    viewModel.showSeriesDetails(
                                        SeriesItem(
                                            id = fav.id,
                                            seriesId = fav.id,
                                            title = fav.title,
                                            posterUrl = fav.logoOrPosterUrl,
                                            genre = fav.category
                                        )
                                    )
                                }
                                ContentType.EPISODE -> Unit
                            }
                        },
                        onRemoveFavorite = { fav ->
                            viewModel.toggleFavorite(
                                id = fav.id,
                                type = fav.type,
                                title = fav.title,
                                poster = fav.logoOrPosterUrl,
                                category = fav.category,
                                streamUrl = fav.streamUrl
                            )
                        }
                    )
                }

                Screen.Search -> {
                    SearchScreen(
                        searchQuery = searchQuery,
                        onQueryChange = { q -> viewModel.onSearchQueryChange(q) },
                        getChannelsPager = { cat, q -> viewModel.getChannelsPager(cat, false, q) },
                        getMoviesPager = { genre, q -> viewModel.getMoviesPager(genre = genre, query = q) },
                        getSeriesPager = { genre, q -> viewModel.getSeriesPager(genre = genre, query = q) },
                        onChannelClick = { ch -> viewModel.playChannel(ch) },
                        onMovieClick = { mov -> viewModel.showMovieDetails(mov) },
                        onSeriesClick = { ser -> viewModel.showSeriesDetails(ser) },
                        onToggleFavoriteChannel = { ch ->
                            viewModel.toggleFavorite(
                                id = ch.id,
                                type = ContentType.LIVE_CHANNEL,
                                title = ch.name,
                                poster = ch.logoUrl,
                                category = ch.categoryName,
                                streamUrl = ch.streamUrl
                            )
                        },
                        onToggleFavoriteMovie = { mov ->
                            viewModel.toggleFavorite(
                                id = mov.id,
                                type = ContentType.MOVIE,
                                title = mov.title,
                                poster = mov.posterUrl,
                                category = mov.categoryName,
                                streamUrl = mov.streamUrl
                            )
                        },
                        isFavorite = { id -> viewModel.isFavorite(id) }
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        currentLanguage = currentLanguage,
                        startupSoundEnabled = startupSoundEnabled,
                        defaultAspectRatio = defaultAspectRatio,
                        autoPlayNext = autoPlayNext,
                        activeAccount = activeAccount,
                        onSetLanguage = { lang -> viewModel.setLanguage(lang) },
                        onSetStartupSound = { snd -> viewModel.setStartupSound(snd) },
                        onSetDefaultAspectRatio = { ratio -> viewModel.setDefaultAspectRatio(ratio) },
                        onSetAutoPlayNext = { next -> viewModel.setAutoPlayNext(next) },
                        onOpenAccounts = { viewModel.setAccountDialogVisible(true) },
                        onClearCache = { viewModel.clearCache() },
                        onRefreshContent = { viewModel.refreshContent() }
                    )
                }
            }

            // Movie Detail Modal Sheet
            if (selectedMovie != null) {
                MovieDetailSheet(
                    movie = selectedMovie!!,
                    isFavorite = viewModel.isFavorite(selectedMovie!!.id),
                    onDismiss = { viewModel.showMovieDetails(null) },
                    onPlayClick = { mov ->
                        viewModel.showMovieDetails(null)
                        viewModel.playMovie(mov)
                    },
                    onToggleFavorite = { mov ->
                        viewModel.toggleFavorite(
                            id = mov.id,
                            type = ContentType.MOVIE,
                            title = mov.title,
                            poster = mov.posterUrl,
                            category = mov.categoryName,
                            streamUrl = mov.streamUrl
                        )
                    }
                )
            }

            // Series Detail Modal Sheet
            if (selectedSeries != null) {
                SeriesDetailSheet(
                    series = selectedSeries!!,
                    isFavorite = viewModel.isFavorite(selectedSeries!!.id),
                    onDismiss = { viewModel.dismissSeriesDetails() },
                    onPlayEpisode = { ser, season, ep ->
                        viewModel.dismissSeriesDetails()
                        viewModel.playEpisode(ser, season, ep)
                    },
                    onToggleFavorite = { ser ->
                        viewModel.toggleFavorite(
                            id = ser.id,
                            type = ContentType.SERIES,
                            title = ser.title,
                            poster = ser.posterUrl,
                            category = ser.genre,
                            streamUrl = ""
                        )
                    }
                )
            }

            // Account Manager Dialog
            if (showAccountDialog) {
                AccountManagerDialog(
                    activeAccount = activeAccount,
                    allAccounts = allAccounts,
                    isLoading = isLoading,
                    onDismiss = { viewModel.setAccountDialogVisible(false) },
                    onSwitchAccount = { acc -> viewModel.switchAccount(acc) },
                    onDeleteAccount = { acc -> viewModel.deleteAccount(acc) },
                    onAddM3u = { name, url, onResult ->
                        viewModel.addM3uAccount(name, url, onResult)
                    },
                    onAddXtream = { name, server, user, pass, onResult ->
                        viewModel.addXtreamAccount(name, server, user, pass, onResult)
                    }
                )
            }

            // Crash Log Dialog (Requirement 9)
            if (savedCrashReport != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissCrashReport() },
                    containerColor = Color(0xF21C1622),
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = KrasRedBright,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Crash Diagnostic Log",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                    },
                    text = {
                        Column {
                            Text(
                                text = "A previous crash was captured and prevented from closing abruptly:",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFF0F1117), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = savedCrashReport!!,
                                    color = Color(0xFFFFB4AB),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissCrashReport() }) {
                            Text("Dismiss / تجاهل", color = KrasRedBright, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Real Error Alert Dialog (Requirement 8)
            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    containerColor = Color(0xF21D1519),
                    shape = RoundedCornerShape(18.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = KrasRedBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notice / تنبيه",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                    },
                    text = {
                        Text(
                            text = errorMessage!!,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK / حسناً", color = KrasRedBright, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}
