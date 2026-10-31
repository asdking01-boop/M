package com.example.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.model.AccountType
import com.example.data.model.Channel
import com.example.data.model.Episode
import com.example.data.model.IptvAccount
import com.example.data.model.LiveChannelItem
import com.example.data.model.Season
import com.example.data.model.Series
import com.example.data.model.SeriesItem
import com.example.data.model.VodMovie
import com.example.data.model.VodMovieItem
import com.example.data.remote.SampleDataProvider
import com.example.data.remote.XtreamCodesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IptvRepository(
    private val db: AppDatabase,
    private val scope: CoroutineScope
) {
    private val accountDao = db.accountDao()
    private val channelDao = db.channelDao()
    private val movieDao = db.movieDao()
    private val seriesDao = db.seriesDao()
    private val xtreamService = XtreamCodesService()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingStatus = MutableStateFlow<String?>(null)
    val loadingStatus: StateFlow<String?> = _loadingStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _activeAccount = MutableStateFlow<IptvAccount?>(null)
    val activeAccount: StateFlow<IptvAccount?> = _activeAccount.asStateFlow()

    val allAccounts = accountDao.getAllAccountsFlow()

    companion object {
        const val DEMO_ACCOUNT_ID = -1L
    }

    init {
        scope.launch {
            loadInitialData()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private suspend fun loadInitialData() {
        val active = accountDao.getActiveAccount()
        if (active != null) {
            _activeAccount.value = active
            // Check if items are already in Room for this account, if not sync
            val count = channelDao.getCount(active.id)
            if (count == 0) {
                syncAccountContent(active)
            }
        } else {
            // Setup Demo Account and seed demo items into Room
            val demoAccount = IptvAccount(
                id = DEMO_ACCOUNT_ID,
                name = "KRAS TV Demo Mode",
                type = AccountType.DEMO,
                isActive = true,
                channelsCount = SampleDataProvider.sampleChannels.size,
                moviesCount = SampleDataProvider.sampleMovies.size,
                seriesCount = SampleDataProvider.sampleSeries.size
            )
            _activeAccount.value = demoAccount
            seedDemoData()
        }
    }

    private suspend fun seedDemoData() = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                channelDao.clearForAccount(DEMO_ACCOUNT_ID)
                movieDao.clearForAccount(DEMO_ACCOUNT_ID)
                seriesDao.clearForAccount(DEMO_ACCOUNT_ID)

                channelDao.insertBatch(
                    SampleDataProvider.sampleChannels.map { ch ->
                        LiveChannelItem(
                            id = ch.id,
                            accountId = DEMO_ACCOUNT_ID,
                            streamId = ch.streamId,
                            name = ch.name,
                            streamUrl = ch.streamUrl,
                            logoUrl = ch.logoUrl,
                            category = ch.category,
                            channelNum = ch.channelNum
                        )
                    }
                )

                movieDao.insertBatch(
                    SampleDataProvider.sampleMovies.map { m ->
                        VodMovieItem(
                            id = m.id,
                            accountId = DEMO_ACCOUNT_ID,
                            streamId = m.streamId,
                            title = m.title,
                            streamUrl = m.streamUrl,
                            posterUrl = m.posterUrl,
                            genre = m.genre,
                            year = m.year,
                            rating = m.rating
                        )
                    }
                )

                seriesDao.insertBatch(
                    SampleDataProvider.sampleSeries.map { s ->
                        SeriesItem(
                            id = s.id,
                            accountId = DEMO_ACCOUNT_ID,
                            seriesId = s.seriesId,
                            title = s.title,
                            posterUrl = s.posterUrl,
                            genre = s.genre,
                            year = s.year,
                            rating = s.rating
                        )
                    }
                )
            }
        } catch (_: Exception) {
        }
    }

    fun getPagedChannels(accountId: Long, category: String, query: String): Flow<PagingData<LiveChannelItem>> {
        return Pager(
            config = PagingConfig(pageSize = 50, prefetchDistance = 15, enablePlaceholders = false)
        ) {
            when {
                query.isNotBlank() -> channelDao.searchPagedChannels(accountId, query)
                category.isNotBlank() && category != "All" && category != "الكل" ->
                    channelDao.getPagedChannelsByCategory(accountId, category)
                else -> channelDao.getPagedChannels(accountId)
            }
        }.flow
    }

    fun getPagedMovies(accountId: Long, genre: String, query: String): Flow<PagingData<VodMovieItem>> {
        return Pager(
            config = PagingConfig(pageSize = 50, prefetchDistance = 15, enablePlaceholders = false)
        ) {
            when {
                query.isNotBlank() -> movieDao.searchPagedMovies(accountId, query)
                genre.isNotBlank() && genre != "All" && genre != "الكل" ->
                    movieDao.getPagedMoviesByGenre(accountId, genre)
                else -> movieDao.getPagedMovies(accountId)
            }
        }.flow
    }

    fun getPagedSeries(accountId: Long, genre: String, query: String): Flow<PagingData<SeriesItem>> {
        return Pager(
            config = PagingConfig(pageSize = 50, prefetchDistance = 15, enablePlaceholders = false)
        ) {
            when {
                query.isNotBlank() -> seriesDao.searchPagedSeries(accountId, query)
                genre.isNotBlank() && genre != "All" && genre != "الكل" ->
                    seriesDao.getPagedSeriesByGenre(accountId, genre)
                else -> seriesDao.getPagedSeries(accountId)
            }
        }.flow
    }

    fun getChannelCategories(accountId: Long): Flow<List<String>> = channelDao.getCategories(accountId)
    fun getMovieGenres(accountId: Long): Flow<List<String>> = movieDao.getGenres(accountId)
    fun getSeriesGenres(accountId: Long): Flow<List<String>> = seriesDao.getGenres(accountId)

    suspend fun getRecentChannels(accountId: Long, limit: Int = 10): List<LiveChannelItem> =
        withContext(Dispatchers.IO) { channelDao.getRecent(accountId, limit) }

    suspend fun getRecentMovies(accountId: Long, limit: Int = 10): List<VodMovieItem> =
        withContext(Dispatchers.IO) { movieDao.getRecent(accountId, limit) }

    suspend fun getRecentSeries(accountId: Long, limit: Int = 10): List<SeriesItem> =
        withContext(Dispatchers.IO) { seriesDao.getRecent(accountId, limit) }

    suspend fun addM3uAccount(name: String, url: String): Result<Unit> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val account = IptvAccount(
                name = name.ifEmpty { "M3U Playlist" },
                type = AccountType.M3U,
                url = url,
                isActive = true
            )
            val newId = accountDao.insertAccount(account)
            accountDao.setActiveAccount(newId)
            _activeAccount.value = account.copy(id = newId)

            _loadingStatus.value = "Downloading and indexing playlist..."

            // Stream M3U line by line and insert in batches of 500
            val result = xtreamService.streamM3u(
                url = url,
                accountId = newId,
                onChannelBatch = { batch ->
                    db.withTransaction { channelDao.insertBatch(batch) }
                },
                onMovieBatch = { batch ->
                    db.withTransaction { movieDao.insertBatch(batch) }
                },
                onSeriesBatch = { batch ->
                    db.withTransaction { seriesDao.insertBatch(batch) }
                }
            )

            result.fold(
                onSuccess = { (chanCount, movCount, serCount) ->
                    val updatedAccount = account.copy(
                        id = newId,
                        channelsCount = chanCount,
                        moviesCount = movCount,
                        seriesCount = serCount
                    )
                    accountDao.insertAccount(updatedAccount)
                    _activeAccount.value = updatedAccount
                    _loadingStatus.value = null
                    _isLoading.value = false
                    Result.success(Unit)
                },
                onFailure = { error ->
                    _loadingStatus.value = null
                    _isLoading.value = false
                    _errorMessage.value = error.localizedMessage ?: "Failed to load M3U playlist"
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _loadingStatus.value = null
            _isLoading.value = false
            _errorMessage.value = e.localizedMessage ?: "Unexpected error"
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
        _errorMessage.value = null
        try {
            // 1. Authenticate first
            _loadingStatus.value = "Authenticating with server..."
            val authResult = xtreamService.authenticate(server, user, pass)
            if (authResult.isFailure) {
                val err = authResult.exceptionOrNull() ?: Exception("Authentication failed")
                _errorMessage.value = err.localizedMessage ?: "Authentication failed"
                _isLoading.value = false
                _loadingStatus.value = null
                return@withContext Result.failure(err)
            }

            // Save account
            val account = IptvAccount(
                name = name.ifEmpty { "Xtream Account" },
                type = AccountType.XTREAM,
                serverUrl = server,
                username = user,
                password = pass,
                isActive = true
            )
            val newId = accountDao.insertAccount(account)
            accountDao.setActiveAccount(newId)
            val activeAcc = account.copy(id = newId)
            _activeAccount.value = activeAcc

            // Sequential streaming loading (channels, then movies, then series)
            val syncResult = syncXtreamSequentially(activeAcc)
            _isLoading.value = false
            _loadingStatus.value = null
            syncResult
        } catch (e: Exception) {
            _isLoading.value = false
            _loadingStatus.value = null
            _errorMessage.value = e.localizedMessage ?: "Connection error"
            Result.failure(e)
        }
    }

    suspend fun refreshContent() {
        val current = _activeAccount.value
        if (current != null) {
            syncAccountContent(current)
        }
    }

    suspend fun switchAccount(account: IptvAccount) {
        _errorMessage.value = null
        if (account.type == AccountType.DEMO) {
            _activeAccount.value = account
            seedDemoData()
            return
        }
        accountDao.setActiveAccount(account.id)
        _activeAccount.value = account
        val count = channelDao.getCount(account.id)
        if (count == 0) {
            syncAccountContent(account)
        }
    }

    suspend fun deleteAccount(account: IptvAccount) {
        withContext(Dispatchers.IO) {
            channelDao.clearForAccount(account.id)
            movieDao.clearForAccount(account.id)
            seriesDao.clearForAccount(account.id)
            accountDao.deleteAccount(account.id)
            if (_activeAccount.value?.id == account.id) {
                loadInitialData()
            }
        }
    }

    private suspend fun syncAccountContent(account: IptvAccount) {
        _isLoading.value = true
        _errorMessage.value = null
        withContext(Dispatchers.IO) {
            try {
                when (account.type) {
                    AccountType.M3U -> {
                        _loadingStatus.value = "Syncing M3U streams..."
                        channelDao.clearForAccount(account.id)
                        movieDao.clearForAccount(account.id)
                        seriesDao.clearForAccount(account.id)

                        xtreamService.streamM3u(
                            url = account.url,
                            accountId = account.id,
                            onChannelBatch = { batch -> db.withTransaction { channelDao.insertBatch(batch) } },
                            onMovieBatch = { batch -> db.withTransaction { movieDao.insertBatch(batch) } },
                            onSeriesBatch = { batch -> db.withTransaction { seriesDao.insertBatch(batch) } }
                        )
                    }
                    AccountType.XTREAM -> {
                        syncXtreamSequentially(account)
                    }
                    AccountType.DEMO -> {
                        seedDemoData()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Sync error"
            } finally {
                _isLoading.value = false
                _loadingStatus.value = null
            }
        }
    }

    /**
     * Requirement 5: Load live channels, movies, and series sequentially (one after the other, NOT all at once)
     * Requirement 3: Insert items into Room in batches of 500 inside a transaction.
     */
    private suspend fun syncXtreamSequentially(account: IptvAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Live Channels
            _loadingStatus.value = "Loading Live Channels (Streaming)..."
            channelDao.clearForAccount(account.id)
            val chanResult = xtreamService.streamLiveChannels(
                serverUrl = account.serverUrl,
                user = account.username,
                pass = account.password,
                accountId = account.id,
                onBatch = { batch ->
                    db.withTransaction { channelDao.insertBatch(batch) }
                }
            )

            val channelsCount = chanResult.getOrDefault(0)

            // Step 2: VOD Movies
            _loadingStatus.value = "Loading Movies (Streaming)..."
            movieDao.clearForAccount(account.id)
            val movResult = xtreamService.streamVodMovies(
                serverUrl = account.serverUrl,
                user = account.username,
                pass = account.password,
                accountId = account.id,
                onBatch = { batch ->
                    db.withTransaction { movieDao.insertBatch(batch) }
                }
            )

            val moviesCount = movResult.getOrDefault(0)

            // Step 3: TV Series
            _loadingStatus.value = "Loading Series (Streaming)..."
            seriesDao.clearForAccount(account.id)
            val serResult = xtreamService.streamSeries(
                serverUrl = account.serverUrl,
                user = account.username,
                pass = account.password,
                accountId = account.id,
                onBatch = { batch ->
                    db.withTransaction { seriesDao.insertBatch(batch) }
                }
            )

            val seriesCount = serResult.getOrDefault(0)

            val updatedAccount = account.copy(
                channelsCount = channelsCount,
                moviesCount = moviesCount,
                seriesCount = seriesCount
            )
            accountDao.insertAccount(updatedAccount)
            _activeAccount.value = updatedAccount

            Result.success(Unit)
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage ?: "Error syncing Xtream content"
            Result.failure(e)
        }
    }

    /**
     * Requirement 6: Lazy episode resolution when opening a series.
     */
    suspend fun getSeriesWithEpisodes(seriesId: String, title: String, poster: String, genre: String): Series =
        withContext(Dispatchers.IO) {
            val currentAcc = _activeAccount.value
            if (currentAcc?.type == AccountType.XTREAM && seriesId.isNotEmpty()) {
                val seasonsResult = xtreamService.fetchSeriesInfo(
                    currentAcc.serverUrl,
                    currentAcc.username,
                    currentAcc.password,
                    seriesId
                )
                val loadedSeasons = seasonsResult.getOrNull()
                if (!loadedSeasons.isNullOrEmpty()) {
                    return@withContext Series(
                        id = "xtream_ser_${currentAcc.id}_$seriesId",
                        seriesId = seriesId,
                        title = title,
                        posterUrl = poster,
                        backdropUrl = poster,
                        genre = genre,
                        seasons = loadedSeasons
                    )
                }
            }

            // Fallback default season
            Series(
                id = seriesId,
                seriesId = seriesId,
                title = title,
                posterUrl = poster,
                backdropUrl = poster,
                genre = genre,
                seasons = listOf(
                    Season(
                        seasonNum = 1,
                        title = "Season 1",
                        episodes = emptyList()
                    )
                )
            )
        }
}
