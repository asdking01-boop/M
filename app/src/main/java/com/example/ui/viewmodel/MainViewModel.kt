package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.room.Room
import com.example.KrasTvApplication
import com.example.audio.SoundPlayer
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.ContentType
import com.example.data.model.Episode
import com.example.data.model.FavoriteEntity
import com.example.data.model.IptvAccount
import com.example.data.model.LiveChannelItem
import com.example.data.model.PlayerAspectRatio
import com.example.data.model.Season
import com.example.data.model.Series
import com.example.data.model.SeriesItem
import com.example.data.model.VodMovie
import com.example.data.model.VodMovieItem
import com.example.data.model.WatchHistoryEntity
import com.example.data.repository.IptvRepository
import com.example.data.repository.UserDataRepository
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.Flow
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

    val iptvRepo = IptvRepository(db, viewModelScope)
    val userRepo = UserDataRepository(db.favoriteDao(), db.watchHistoryDao())
    val prefs = PreferencesManager(application)

    // Current Screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Active Player state
    private val _activePlayerArgs = MutableStateFlow<PlayerLaunchArgs?>(null)
    val activePlayerArgs: StateFlow<PlayerLaunchArgs?> = _activePlayerArgs.asStateFlow()

    // Details & Modals
    private val _selectedMovie = MutableStateFlow<VodMovieItem?>(null)
    val selectedMovie: StateFlow<VodMovieItem?> = _selectedMovie.asStateFlow()

    private val _selectedSeries = MutableStateFlow<Series?>(null)
    val selectedSeries: StateFlow<Series?> = _selectedSeries.asStateFlow()

    private val _showAccountDialog = MutableStateFlow(false)
    val showAccountDialog: StateFlow<Boolean> = _showAccountDialog.asStateFlow()

    // Crash Log Dialog (Requirement 9)
    private val _savedCrashReport = MutableStateFlow<String?>(null)
    val savedCrashReport: StateFlow<String?> = _savedCrashReport.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // State flows from repo
    val activeAccount: StateFlow<IptvAccount?> = iptvRepo.activeAccount
    val allAccounts: StateFlow<List<IptvAccount>> = iptvRepo.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isLoading: StateFlow<Boolean> = iptvRepo.isLoading
    val loadingStatus: StateFlow<String?> = iptvRepo.loadingStatus
    val errorMessage: StateFlow<String?> = iptvRepo.errorMessage

    // Recent preview items for Home Screen (lightweight, under 20 items each)
    private val _homeChannels = MutableStateFlow<List<LiveChannelItem>>(emptyList())
    val homeChannels: StateFlow<List<LiveChannelItem>> = _homeChannels.asStateFlow()

    private val _homeMovies = MutableStateFlow<List<VodMovieItem>>(emptyList())
    val homeMovies: StateFlow<List<VodMovieItem>> = _homeMovies.asStateFlow()

    private val _homeSeries = MutableStateFlow<List<SeriesItem>>(emptyList())
    val homeSeries: StateFlow<List<SeriesItem>> = _homeSeries.asStateFlow()

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
        // Check for saved crash on startup (Requirement 9)
        _savedCrashReport.value = KrasTvApplication.getSavedCrash(application)

        // Play startup sound if enabled
        viewModelScope.launch {
            if (startupSoundEnabled.value) {
                SoundPlayer.playCinematicIntro(getApplication(), true)
            }
        }

        // Observe active account to refresh home previews
        viewModelScope.launch {
            activeAccount.collect { acc ->
                if (acc != null) {
                    refreshHomePreviews(acc.id)
                }
            }
        }
    }

    fun dismissCrashReport() {
        KrasTvApplication.clearSavedCrash(getApplication())
        _savedCrashReport.value = null
    }

    fun clearError() {
        iptvRepo.clearError()
    }

    fun refreshHomePreviews(accountId: Long) {
        viewModelScope.launch {
            _homeChannels.value = iptvRepo.getRecentChannels(accountId, 20)
            _homeMovies.value = iptvRepo.getRecentMovies(accountId, 20)
            _homeSeries.value = iptvRepo.getRecentSeries(accountId, 20)
        }
    }

    // Paging 3 Streams with comprehensive filters and sort modes
    fun getChannelsPager(category: String = "", favoritesOnly: Boolean = false, query: String = ""): Flow<PagingData<LiveChannelItem>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getPagedChannels(accId, category, favoritesOnly, query).cachedIn(viewModelScope)
    }

    fun getMoviesPager(
        genre: String = "",
        year: String = "",
        minRating: Double = 0.0,
        query: String = "",
        sortMode: String = ""
    ): Flow<PagingData<VodMovieItem>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getPagedMovies(accId, genre, year, minRating, query, sortMode).cachedIn(viewModelScope)
    }

    fun getSeriesPager(
        genre: String = "",
        year: String = "",
        minRating: Double = 0.0,
        query: String = "",
        sortMode: String = ""
    ): Flow<PagingData<SeriesItem>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getPagedSeries(accId, genre, year, minRating, query, sortMode).cachedIn(viewModelScope)
    }

    // Curated Section Rows (LIMIT 30) for Movies
    fun getRecentlyAddedMovies(): Flow<List<VodMovieItem>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getRecentlyAddedMovies(accId, 30)
    }

    fun getLatestReleaseMovies(): Flow<List<VodMovieItem>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getLatestReleaseMovies(accId, 30)
    }

    fun getTopRatedMovies(): Flow<List<VodMovieItem>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getTopRatedMovies(accId, 30)
    }

    // Curated Section Rows (LIMIT 30) for Series
    fun getRecentlyAddedSeries(): Flow<List<SeriesItem>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getRecentlyAddedSeries(accId, 30)
    }

    fun getLatestReleaseSeries(): Flow<List<SeriesItem>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getLatestReleaseSeries(accId, 30)
    }

    fun getTopRatedSeries(): Flow<List<SeriesItem>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getTopRatedSeries(accId, 30)
    }

    fun getContinueWatching(type: ContentType): Flow<List<WatchHistoryEntity>> {
        return userRepo.getContinueWatching(type, 30)
    }

    fun getChannelCategories(): Flow<List<String>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getChannelCategories(accId)
    }

    fun getMovieGenres(): Flow<List<String>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getMovieGenres(accId)
    }

    fun getMovieYears(): Flow<List<String>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getMovieYears(accId)
    }

    fun getSeriesGenres(): Flow<List<String>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getSeriesGenres(accId)
    }

    fun getSeriesYears(): Flow<List<String>> {
        val accId = activeAccount.value?.id ?: IptvRepository.DEMO_ACCOUNT_ID
        return iptvRepo.getSeriesYears(accId)
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

    fun playChannel(item: LiveChannelItem) {
        openPlayer(
            PlayerLaunchArgs(
                id = item.id,
                type = ContentType.LIVE_CHANNEL,
                title = item.name,
                streamUrl = item.streamUrl,
                posterUrl = item.logoUrl
            )
        )
    }

    fun playMovie(item: VodMovieItem) {
        viewModelScope.launch {
            val history = userRepo.getWatchHistory(item.id)
            openPlayer(
                PlayerLaunchArgs(
                    id = item.id,
                    type = ContentType.MOVIE,
                    title = item.title,
                    streamUrl = item.streamUrl,
                    posterUrl = item.posterUrl,
                    startPositionMs = history?.positionMs ?: 0L
                )
            )
        }
    }

    fun playEpisode(series: Series, season: Season, episode: Episode) {
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

    fun showMovieDetails(movie: VodMovieItem?) {
        _selectedMovie.value = movie
    }

    fun showSeriesDetails(item: SeriesItem?) {
        if (item == null) {
            _selectedSeries.value = null
            return
        }
        viewModelScope.launch {
            val fullSeries = iptvRepo.getSeriesWithEpisodes(item.seriesId, item.title, item.posterUrl, item.genre)
            _selectedSeries.value = fullSeries
        }
    }

    fun dismissSeriesDetails() {
        _selectedSeries.value = null
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
            activeAccount.value?.let { refreshHomePreviews(it.id) }
        }
    }

    fun addM3uAccount(name: String, url: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = iptvRepo.addM3uAccount(name, url)
            result.fold(
                onSuccess = {
                    activeAccount.value?.let { refreshHomePreviews(it.id) }
                    onResult(true, null)
                },
                onFailure = { err -> onResult(false, err.localizedMessage) }
            )
        }
    }

    fun addXtreamAccount(name: String, server: String, user: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = iptvRepo.addXtreamAccount(name, server, user, pass)
            result.fold(
                onSuccess = {
                    activeAccount.value?.let { refreshHomePreviews(it.id) }
                    onResult(true, null)
                },
                onFailure = { err -> onResult(false, err.localizedMessage) }
            )
        }
    }

    fun switchAccount(account: IptvAccount) {
        viewModelScope.launch {
            iptvRepo.switchAccount(account)
            refreshHomePreviews(account.id)
        }
    }

    fun deleteAccount(account: IptvAccount) {
        viewModelScope.launch {
            iptvRepo.deleteAccount(account)
        }
    }
}
