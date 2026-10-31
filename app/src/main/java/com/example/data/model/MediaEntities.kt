package com.example.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "live_channels",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["category"]),
        Index(value = ["name"])
    ]
)
data class LiveChannelItem(
    @PrimaryKey val id: String,
    val accountId: Long = 0L,
    val streamId: String = "",
    val name: String,
    val streamUrl: String,
    val logoUrl: String = "",
    val category: String = "General",
    val channelNum: String = ""
) {
    @get:Ignore
    val categoryName: String get() = category
}

@Entity(
    tableName = "vod_movies",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["genre"]),
        Index(value = ["title"])
    ]
)
data class VodMovieItem(
    @PrimaryKey val id: String,
    val accountId: Long = 0L,
    val streamId: String = "",
    val title: String,
    val streamUrl: String,
    val posterUrl: String = "",
    val genre: String = "Movies",
    val year: String = "2024",
    val rating: String = "8.0"
) {
    @get:Ignore
    val categoryName: String get() = genre
}

@Entity(
    tableName = "series_items",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["genre"]),
        Index(value = ["title"])
    ]
)
data class SeriesItem(
    @PrimaryKey val id: String = "",
    val accountId: Long = 0L,
    val seriesId: String = "",
    val title: String,
    val posterUrl: String = "",
    val genre: String = "Series",
    val year: String = "2024",
    val rating: String = "8.5"
) {
    @get:Ignore
    val categoryName: String get() = genre

    @get:Ignore
    val episodeCount: Int get() = 0
}
