package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType {
    M3U,
    XTREAM,
    DEMO
}

@Entity(tableName = "iptv_accounts")
data class IptvAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val url: String = "",
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isActive: Boolean = false,
    val channelsCount: Int = 0,
    val moviesCount: Int = 0,
    val seriesCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class ContentType {
    LIVE_CHANNEL,
    MOVIE,
    SERIES,
    EPISODE
}

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String, // e.g., "channel_101", "movie_202", "series_303"
    val originalId: String,
    val type: ContentType,
    val title: String,
    val logoOrPosterUrl: String,
    val category: String = "",
    val streamUrl: String = "",
    val rating: String = "",
    val year: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val contentId: String,
    val type: ContentType,
    val title: String,
    val posterUrl: String,
    val streamUrl: String,
    val positionMs: Long,
    val durationMs: Long,
    val seasonNum: Int = 0,
    val episodeNum: Int = 0,
    val seriesTitle: String = "",
    val lastPlayedAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

data class EpgProgram(
    val title: String,
    val description: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val progressPercent: Float = 0.5f,
    val isCurrent: Boolean = true
)

data class Channel(
    val id: String,
    val streamId: String = "",
    val name: String,
    val streamUrl: String,
    val logoUrl: String = "",
    val category: String = "General",
    val channelNum: String = "",
    val currentProgram: EpgProgram? = null,
    val upcomingProgram: EpgProgram? = null,
    val isFavorite: Boolean = false
)

data class VodMovie(
    val id: String,
    val streamId: String = "",
    val title: String,
    val streamUrl: String,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val genre: String = "Action",
    val year: String = "2024",
    val duration: String = "1h 54m",
    val durationSeconds: Long = 6840L,
    val rating: String = "8.6",
    val description: String = "",
    val isFavorite: Boolean = false
)

data class Episode(
    val id: String,
    val seriesId: String,
    val seasonNum: Int,
    val episodeNum: Int,
    val title: String,
    val streamUrl: String,
    val posterUrl: String = "",
    val duration: String = "45m",
    val durationSeconds: Long = 2700L,
    val description: String = "",
    val isWatched: Boolean = false,
    val lastPositionMs: Long = 0L
)

data class Season(
    val seasonNum: Int,
    val title: String,
    val episodes: List<Episode> = emptyList()
)

data class Series(
    val id: String,
    val seriesId: String = "",
    val title: String,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val genre: String = "Drama",
    val year: String = "2024",
    val rating: String = "8.9",
    val description: String = "",
    val seasons: List<Season> = emptyList(),
    val isFavorite: Boolean = false
)

enum class PlayerAspectRatio(val labelEn: String, val labelAr: String) {
    FIT("Fit", "ملاءمة"),
    CROP("Crop / Zoom", "قص وتكبير"),
    RATIO_16_9("16:9", "16:9"),
    STRETCH("Stretch", "تمديد")
}

data class VideoQualityOption(
    val id: String,
    val label: String,
    val height: Int,
    val bitrate: Int = 0,
    val trackIndex: Int = -1,
    val isAuto: Boolean = false
)
