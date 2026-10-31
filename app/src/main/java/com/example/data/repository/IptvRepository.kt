package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.model.AccountType
import com.example.data.model.Channel
import com.example.data.model.IptvAccount
import com.example.data.model.Series
import com.example.data.model.VodMovie
import com.example.data.remote.SampleDataProvider
import com.example.data.remote.XtreamCodesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IptvRepository(
    private val accountDao: AccountDao,
    private val scope: CoroutineScope
) {
    private val xtreamService = XtreamCodesService()

    private val _channels = MutableStateFlow<List<Channel>>(SampleDataProvider.sampleChannels)
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _movies = MutableStateFlow<List<VodMovie>>(SampleDataProvider.sampleMovies)
    val movies: StateFlow<List<VodMovie>> = _movies.asStateFlow()

    private val _series = MutableStateFlow<List<Series>>(SampleDataProvider.sampleSeries)
    val series: StateFlow<List<Series>> = _series.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeAccount = MutableStateFlow<IptvAccount?>(null)
    val activeAccount: StateFlow<IptvAccount?> = _activeAccount.asStateFlow()

    val allAccounts = accountDao.getAllAccountsFlow()

    init {
        scope.launch {
            loadInitialData()
        }
    }

    private suspend fun loadInitialData() {
        val active = accountDao.getActiveAccount()
        if (active != null) {
            _activeAccount.value = active
            loadAccountContent(active)
        } else {
            // Default to Demo Account
            _activeAccount.value = IptvAccount(
                id = -1,
                name = "KRAS TV Demo Mode",
                type = AccountType.DEMO,
                isActive = true,
                channelsCount = SampleDataProvider.sampleChannels.size,
                moviesCount = SampleDataProvider.sampleMovies.size,
                seriesCount = SampleDataProvider.sampleSeries.size
            )
            _channels.value = SampleDataProvider.sampleChannels
            _movies.value = SampleDataProvider.sampleMovies
            _series.value = SampleDataProvider.sampleSeries
        }
    }

    suspend fun refreshContent() {
        val current = _activeAccount.value
        if (current != null && current.type != AccountType.DEMO) {
            loadAccountContent(current)
        } else {
            _channels.value = SampleDataProvider.sampleChannels
            _movies.value = SampleDataProvider.sampleMovies
            _series.value = SampleDataProvider.sampleSeries
        }
    }

    suspend fun addM3uAccount(name: String, url: String): Result<Unit> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val result = xtreamService.fetchM3u(url)
            result.fold(
                onSuccess = { parsed ->
                    val account = IptvAccount(
                        name = name.ifEmpty { "M3U Playlist" },
                        type = AccountType.M3U,
                        url = url,
                        isActive = true,
                        channelsCount = parsed.channels.size,
                        moviesCount = parsed.movies.size,
                        seriesCount = parsed.series.size
                    )
                    val newId = accountDao.insertAccount(account)
                    accountDao.setActiveAccount(newId)
                    _activeAccount.value = account.copy(id = newId)

                    _channels.value = parsed.channels
                    _movies.value = parsed.movies
                    _series.value = parsed.series

                    _isLoading.value = false
                    Result.success(Unit)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(e)
        }
    }

    suspend fun addXtreamAccount(
        name: String,
        server: String,
        user: String,
        pass: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            // 1. Authenticate first
            val authResult = xtreamService.authenticate(server, user, pass)
            if (authResult.isFailure) {
                _isLoading.value = false
                return@withContext Result.failure(authResult.exceptionOrNull() ?: Exception("Authentication failed"))
            }

            // 2. Separate requests for Live, VOD, and Series
            val liveDeferred = async { xtreamService.fetchLiveStreams(server, user, pass) }
            val vodDeferred = async { xtreamService.fetchVodStreams(server, user, pass) }
            val seriesDeferred = async { xtreamService.fetchSeries(server, user, pass) }

            val liveResult = liveDeferred.await()
            val vodResult = vodDeferred.await()
            val seriesResult = seriesDeferred.await()

            val loadedChannels = liveResult.getOrDefault(emptyList())
            val loadedMovies = vodResult.getOrDefault(emptyList())
            val loadedSeries = seriesResult.getOrDefault(emptyList())

            val account = IptvAccount(
                name = name.ifEmpty { "Xtream Account" },
                type = AccountType.XTREAM,
                serverUrl = server,
                username = user,
                password = pass,
                isActive = true,
                channelsCount = loadedChannels.size,
                moviesCount = loadedMovies.size,
                seriesCount = loadedSeries.size
            )
            val newId = accountDao.insertAccount(account)
            accountDao.setActiveAccount(newId)
            _activeAccount.value = account.copy(id = newId)

            _channels.value = loadedChannels
            _movies.value = loadedMovies
            _series.value = loadedSeries

            _isLoading.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(e)
        }
    }

    suspend fun switchAccount(account: IptvAccount) {
        if (account.type == AccountType.DEMO) {
            _activeAccount.value = account
            _channels.value = SampleDataProvider.sampleChannels
            _movies.value = SampleDataProvider.sampleMovies
            _series.value = SampleDataProvider.sampleSeries
            return
        }
        accountDao.setActiveAccount(account.id)
        _activeAccount.value = account
        loadAccountContent(account)
    }

    suspend fun deleteAccount(account: IptvAccount) {
        accountDao.deleteAccount(account.id)
        if (_activeAccount.value?.id == account.id) {
            loadInitialData()
        }
    }

    private suspend fun loadAccountContent(account: IptvAccount) {
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                when (account.type) {
                    AccountType.M3U -> {
                        val result = xtreamService.fetchM3u(account.url)
                        result.onSuccess { parsed ->
                            _channels.value = parsed.channels
                            _movies.value = parsed.movies
                            _series.value = parsed.series
                        }
                    }
                    AccountType.XTREAM -> {
                        // Separate concurrent requests for live streams, movies, and series
                        val liveDeferred = async {
                            xtreamService.fetchLiveStreams(account.serverUrl, account.username, account.password)
                        }
                        val vodDeferred = async {
                            xtreamService.fetchVodStreams(account.serverUrl, account.username, account.password)
                        }
                        val seriesDeferred = async {
                            xtreamService.fetchSeries(account.serverUrl, account.username, account.password)
                        }

                        val live = liveDeferred.await()
                        live.onSuccess { if (it.isNotEmpty()) _channels.value = it }

                        val vod = vodDeferred.await()
                        vod.onSuccess { if (it.isNotEmpty()) _movies.value = it }

                        val ser = seriesDeferred.await()
                        ser.onSuccess { if (it.isNotEmpty()) _series.value = it }
                    }
                    AccountType.DEMO -> {
                        _channels.value = SampleDataProvider.sampleChannels
                        _movies.value = SampleDataProvider.sampleMovies
                        _series.value = SampleDataProvider.sampleSeries
                    }
                }
            } catch (_: Exception) {
                // Keep current items if network glitch
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Resolves complete seasons and episodes when a series is selected.
     * If episodes haven't been fetched yet and source is Xtream, queries action=get_series_info.
     */
    suspend fun getSeriesWithEpisodes(series: Series): Series = withContext(Dispatchers.IO) {
        val currentAcc = _activeAccount.value
        if (currentAcc?.type == AccountType.XTREAM && series.seriesId.isNotEmpty()) {
            val hasEpisodes = series.seasons.any { it.episodes.isNotEmpty() }
            if (!hasEpisodes) {
                val seasonsResult = xtreamService.fetchSeriesInfo(
                    currentAcc.serverUrl,
                    currentAcc.username,
                    currentAcc.password,
                    series.seriesId
                )
                val loadedSeasons = seasonsResult.getOrNull()
                if (!loadedSeasons.isNullOrEmpty()) {
                    val updatedSeries = series.copy(seasons = loadedSeasons)
                    // Update in-memory list
                    _series.value = _series.value.map {
                        if (it.id == series.id) updatedSeries else it
                    }
                    return@withContext updatedSeries
                }
            }
        }
        series
    }
}
