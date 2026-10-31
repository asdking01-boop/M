package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.audio.SoundPlayer
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.AccountType
import com.example.data.model.Channel
import com.example.data.model.ContentType
import com.example.data.model.Episode
import com.example.data.model.FavoriteEntity
import com.example.data.model.IptvAccount
import com.example.data.model.PlayerAspectRatio
import com.example.data.model.Series
import com.example.data.model.VodMovie
import com.example.data.model.WatchHistoryEntity
import com.example.data.repository.IptvRepository
import com.example.data.repository.UserDataRepository
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Splash : Screen()
    data object Home : Screen()
    data object LiveTv : Screen()
    data object Movies : Screen()
    data object Series : Screen()
    data object Favorites : Screen()
    data object Settings : Screen()
    data object Search : Screen()
}

data class PlayerLaunchArgs(
    val id: String,
    val type: ContentType,
    val title: String,
    val streamUrl: String,
    val posterUrl: String = "",
    val seriesTitle: String = "",
    val seasonNum: Int = 0,
    val episodeNum: Int = 0,
    val startPositionMs: Long = 0L,
    val nextEpisode: Episode? = null,
    val prevEpisode: Episode? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "kras_tv_database"
    ).fallbackToDestructiveMigration().build()

    val iptvRepo = IptvRepository(db.accountDao(), viewModelScope)
    val userRepo = UserDataRepository(db.favoriteDao(), db.watchHistoryDao())
    val prefs = PreferencesManager(application)

    // Current Screen Navigation
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Active Player (if non-null, full screen player is displayed)
    private val _activePlayerArgs = MutableStateFlow<PlayerLaunchArgs?>(null)
    val activePlayerArgs: StateFlow<PlayerLaunchArgs?> = _activePlayerArgs.asStateFlow()

    // Modals & Details
    private val _selectedMovie = MutableStateFlow<VodMovie?>(null)
    val selectedMovie: StateFlow<VodMovie?> = _selectedMovie.asStateFlow()

    private val _selectedSeries = MutableStateFlow<Series?>(null)
    val selectedSeries: StateFlow<Series?> = _selectedSeries.asStateFlow()

    private val _showAccountDialog = MutableStateFlow(false)
    val showAccountDialog: StateFlow<Boolean> = _showAccountDialog.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Live Channels, Movies, Series from repository
    val channels: StateFlow<List<Channel>> = iptvRepo.channels
    val movies: StateFlow<List<VodMovie>> = iptvRepo.movies
    val series: StateFlow<List<Series>> = iptvRepo.series
    val isLoading: StateFlow<Boolean> = iptvRepo.isLoading
    val activeAccount: StateFlow<IptvAccount?> = iptvRepo.activeAccount
    val allAccounts: StateFlow<List<IptvAccount>> = iptvRepo.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites & History
    val favorites: StateFlow<List<FavoriteEntity>> = userRepo.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val continueWatching: StateFlow<List<WatchHistoryEntity>> = userRepo.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Preferences
    val currentLanguage: StateFlow<AppLanguage> = prefs.languageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.ARABIC)

    val startupSoundEnabled: StateFlow<Boolean> = prefs.startupSoundFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val defaultAspectRatio: StateFlow<String> = prefs.defaultAspectRatioFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "FIT")

    val autoPlayNext: StateFlow<Boolean> = prefs.autoPlayNextFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        // Play cinematic intro sound if enabled
        viewModelScope.launch {
            if (startupSoundEnabled.value) {
                SoundPlayer.playCinematicIntro(getApplication(), true)
            }
        }
    }

    fun onSplashFinished() {
        _currentScreen.value = Screen.Home
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openPlayer(args: PlayerLaunchArgs) {
        _activePlayerArgs.value = args
    }

    fun closePlayer() {
        _activePlayerArgs.value = null
    }

    fun playChannel(channel: Channel) {
        openPlayer(
            PlayerLaunchArgs(
                id = channel.id,
                type = ContentType.LIVE_CHANNEL,
                title = channel.name,
                streamUrl = channel.streamUrl,
                posterUrl = channel.logoUrl
            )
        )
    }

    fun playMovie(movie: VodMovie) {
        viewModelScope.launch {
            val history = userRepo.getWatchHistory(movie.id)
            openPlayer(
                PlayerLaunchArgs(
                    id = movie.id,
                    type = ContentType.MOVIE,
                    title = movie.title,
                    streamUrl = movie.streamUrl,
                    posterUrl = movie.posterUrl,
                    startPositionMs = history?.positionMs ?: 0L
                )
            )
        }
    }

    fun playEpisode(series: Series, season: com.example.data.model.Season, episode: Episode) {
        val allEpisodes = season.episodes
        val currentIndex = allEpisodes.indexOfFirst { it.id == episode.id }
        val nextEp = if (currentIndex != -1 && currentIndex < allEpisodes.size - 1) allEpisodes[currentIndex + 1] else null
        val prevEp = if (currentIndex > 0) allEpisodes[currentIndex - 1] else null

        viewModelScope.launch {
            val history = userRepo.getWatchHistory(episode.id)
            openPlayer(
                PlayerLaunchArgs(
                    id = episode.id,
                    type = ContentType.EPISODE,
                    title = episode.title,
                    streamUrl = episode.streamUrl,
                    posterUrl = episode.posterUrl.ifEmpty { series.posterUrl },
                    seriesTitle = series.title,
                    seasonNum = season.seasonNum,
                    episodeNum = episode.episodeNum,
                    startPositionMs = history?.positionMs ?: 0L,
                    nextEpisode = nextEp,
                    prevEpisode = prevEp
                )
            )
        }
    }

    fun resumeHistory(item: WatchHistoryEntity) {
        openPlayer(
            PlayerLaunchArgs(
                id = item.contentId,
                type = item.type,
                title = item.title,
                streamUrl = item.streamUrl,
                posterUrl = item.posterUrl,
                seriesTitle = item.seriesTitle,
                seasonNum = item.seasonNum,
                episodeNum = item.episodeNum,
                startPositionMs = item.positionMs
            )
        )
    }

    fun showMovieDetails(movie: VodMovie?) {
        _selectedMovie.value = movie
    }

    fun showSeriesDetails(series: Series?) {
        _selectedSeries.value = series
    }

    fun setAccountDialogVisible(visible: Boolean) {
        _showAccountDialog.value = visible
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(id: String, type: ContentType, title: String, poster: String, category: String, streamUrl: String) {
        viewModelScope.launch {
            userRepo.toggleFavorite(
                id = id,
                type = type,
                title = title,
                logoOrPosterUrl = poster,
                category = category,
                streamUrl = streamUrl
            )
        }
    }

    fun isFavorite(id: String): Boolean {
        return favorites.value.any { it.id == id }
    }

    fun recordProgress(contentId: String, type: ContentType, title: String, poster: String, streamUrl: String, pos: Long, dur: Long, season: Int = 0, ep: Int = 0, seriesTitle: String = "") {
        viewModelScope.launch {
            userRepo.recordProgress(
                contentId = contentId,
                type = type,
                title = title,
                posterUrl = poster,
                streamUrl = streamUrl,
                positionMs = pos,
                durationMs = dur,
                seasonNum = season,
                episodeNum = ep,
                seriesTitle = seriesTitle
            )
        }
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            prefs.setLanguage(lang)
        }
    }

    fun setStartupSound(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setStartupSound(enabled)
        }
    }

    fun setAutoPlayNext(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setAutoPlayNext(enabled)
        }
    }

    fun setDefaultAspectRatio(aspectRatio: PlayerAspectRatio) {
        viewModelScope.launch {
            prefs.setDefaultAspectRatio(aspectRatio.name)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            userRepo.clearHistory()
        }
    }

    fun refreshContent() {
        viewModelScope.launch {
            iptvRepo.refreshContent()
        }
    }

    fun addM3uAccount(name: String, url: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = iptvRepo.addM3uAccount(name, url)
            result.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { err -> onResult(false, err.localizedMessage) }
            )
        }
    }

    fun addXtreamAccount(name: String, server: String, user: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = iptvRepo.addXtreamAccount(name, server, user, pass)
            result.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { err -> onResult(false, err.localizedMessage) }
            )
        }
    }

    fun switchAccount(account: IptvAccount) {
        viewModelScope.launch {
            iptvRepo.switchAccount(account)
        }
    }

    fun deleteAccount(account: IptvAccount) {
        viewModelScope.launch {
            iptvRepo.deleteAccount(account)
        }
    }
}
