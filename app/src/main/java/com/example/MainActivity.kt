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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountType
import com.example.data.model.ContentType
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
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
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
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val movies by viewModel.movies.collectAsStateWithLifecycle()
    val series by viewModel.series.collectAsStateWithLifecycle()
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
        PlayerScreen(
            args = playerArgs,
            liveChannels = channels,
            defaultAspectRatio = defaultAspectRatio,
            onClose = { viewModel.closePlayer() },
            onChannelChange = { newChannel -> viewModel.playChannel(newChannel) },
            onEpisodeChange = { newEp ->
                val parentSeries = series.find { it.id == newEp.seriesId }
                val season = parentSeries?.seasons?.find { it.seasonNum == newEp.seasonNum }
                if (parentSeries != null && season != null) {
                    viewModel.playEpisode(parentSeries, season, newEp)
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
                        featuredMovie = movies.firstOrNull(),
                        continueWatching = continueWatching,
                        channels = channels,
                        movies = movies,
                        series = series,
                        activeAccount = activeAccount,
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
                        channels = channels,
                        onChannelClick = { ch -> viewModel.playChannel(ch) },
                        onToggleFavorite = { ch ->
                            viewModel.toggleFavorite(
                                id = ch.id,
                                type = ContentType.LIVE_CHANNEL,
                                title = ch.name,
                                poster = ch.logoUrl,
                                category = ch.category,
                                streamUrl = ch.streamUrl
                            )
                        },
                        isFavorite = { id -> viewModel.isFavorite(id) }
                    )
                }

                Screen.Movies -> {
                    MoviesScreen(
                        movies = movies,
                        onMovieClick = { mov -> viewModel.showMovieDetails(mov) },
                        onToggleFavorite = { mov ->
                            viewModel.toggleFavorite(
                                id = mov.id,
                                type = ContentType.MOVIE,
                                title = mov.title,
                                poster = mov.posterUrl,
                                category = mov.genre,
                                streamUrl = mov.streamUrl
                            )
                        },
                        isFavorite = { id -> viewModel.isFavorite(id) }
                    )
                }

                Screen.Series -> {
                    SeriesScreen(
                        seriesList = series,
                        onSeriesClick = { ser -> viewModel.showSeriesDetails(ser) }
                    )
                }

                Screen.Favorites -> {
                    FavoritesScreen(
                        favorites = favorites,
                        onPlayFavorite = { fav ->
                            when (fav.type) {
                                ContentType.LIVE_CHANNEL -> {
                                    val ch = channels.find { it.id == fav.id }
                                    if (ch != null) viewModel.playChannel(ch)
                                    else {
                                        viewModel.openPlayer(
                                            com.example.ui.viewmodel.PlayerLaunchArgs(
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
                                    val mov = movies.find { it.id == fav.id }
                                    if (mov != null) viewModel.playMovie(mov)
                                    else {
                                        viewModel.openPlayer(
                                            com.example.ui.viewmodel.PlayerLaunchArgs(
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
                                    val ser = series.find { it.id == fav.id }
                                    if (ser != null) viewModel.showSeriesDetails(ser)
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
                        channels = channels,
                        movies = movies,
                        series = series,
                        onChannelClick = { ch -> viewModel.playChannel(ch) },
                        onMovieClick = { mov -> viewModel.showMovieDetails(mov) },
                        onSeriesClick = { ser -> viewModel.showSeriesDetails(ser) },
                        onToggleFavoriteChannel = { ch ->
                            viewModel.toggleFavorite(
                                id = ch.id,
                                type = ContentType.LIVE_CHANNEL,
                                title = ch.name,
                                poster = ch.logoUrl,
                                category = ch.category,
                                streamUrl = ch.streamUrl
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
                            category = mov.genre,
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
                    onDismiss = { viewModel.showSeriesDetails(null) },
                    onPlayEpisode = { ser, season, ep ->
                        viewModel.showSeriesDetails(null)
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
        }
    }
}
