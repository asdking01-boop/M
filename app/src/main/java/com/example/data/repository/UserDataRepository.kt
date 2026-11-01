package com.example.data.repository

import com.example.data.local.FavoriteDao
import com.example.data.local.WatchHistoryDao
import com.example.data.model.ContentType
import com.example.data.model.FavoriteEntity
import com.example.data.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

class UserDataRepository(
    private val favoriteDao: FavoriteDao,
    private val watchHistoryDao: WatchHistoryDao
) {
    val allFavorites: Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()
    val watchHistory: Flow<List<WatchHistoryEntity>> = watchHistoryDao.getRecentHistory(15)

    fun getContinueWatching(type: ContentType, limit: Int = 30): Flow<List<WatchHistoryEntity>> =
        watchHistoryDao.getContinueWatching(type, limit)

    fun getAllContinueWatching(limit: Int = 30): Flow<List<WatchHistoryEntity>> =
        watchHistoryDao.getAllContinueWatching(limit)

    fun isFavoriteFlow(id: String): Flow<Boolean> = favoriteDao.isFavoriteFlow(id)

    suspend fun isFavorite(id: String): Boolean = favoriteDao.isFavorite(id)

    suspend fun toggleFavorite(
        id: String,
        type: ContentType,
        title: String,
        logoOrPosterUrl: String,
        category: String = "",
        streamUrl: String = "",
        rating: String = "",
        year: String = ""
    ) {
        if (favoriteDao.isFavorite(id)) {
            favoriteDao.deleteFavorite(id)
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    id = id,
                    originalId = id,
                    type = type,
                    title = title,
                    logoOrPosterUrl = logoOrPosterUrl,
                    category = category,
                    streamUrl = streamUrl,
                    rating = rating,
                    year = year
                )
            )
        }
    }

    suspend fun removeFavorite(id: String) {
        favoriteDao.deleteFavorite(id)
    }

    suspend fun recordProgress(
        contentId: String,
        type: ContentType,
        title: String,
        posterUrl: String,
        streamUrl: String,
        positionMs: Long,
        durationMs: Long,
        seasonNum: Int = 0,
        episodeNum: Int = 0,
        seriesTitle: String = ""
    ) {
        if (positionMs <= 1000L && durationMs <= 0L) return
        watchHistoryDao.insertOrUpdate(
            WatchHistoryEntity(
                contentId = contentId,
                type = type,
                title = title,
                posterUrl = posterUrl,
                streamUrl = streamUrl,
                positionMs = positionMs,
                durationMs = durationMs,
                seasonNum = seasonNum,
                episodeNum = episodeNum,
                seriesTitle = seriesTitle,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getWatchHistory(contentId: String): WatchHistoryEntity? =
        watchHistoryDao.getHistory(contentId)

    suspend fun clearHistory() {
        watchHistoryDao.clearAll()
    }
}
