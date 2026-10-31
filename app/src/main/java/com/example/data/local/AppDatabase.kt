package com.example.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.AccountType
import com.example.data.model.ContentType
import com.example.data.model.FavoriteEntity
import com.example.data.model.IptvAccount
import com.example.data.model.LiveChannelItem
import com.example.data.model.SeriesItem
import com.example.data.model.VodMovieItem
import com.example.data.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = try {
        AccountType.valueOf(value)
    } catch (_: Exception) {
        AccountType.M3U
    }

    @TypeConverter
    fun fromContentType(value: ContentType): String = value.name

    @TypeConverter
    fun toContentType(value: String): ContentType = try {
        ContentType.valueOf(value)
    } catch (_: Exception) {
        ContentType.MOVIE
    }
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM iptv_accounts ORDER BY id DESC")
    fun getAllAccountsFlow(): Flow<List<IptvAccount>>

    @Query("SELECT * FROM iptv_accounts WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAccount(): IptvAccount?

    @Query("SELECT * FROM iptv_accounts WHERE isActive = 1 LIMIT 1")
    fun getActiveAccountFlow(): Flow<IptvAccount?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: IptvAccount): Long

    @Query("UPDATE iptv_accounts SET isActive = CASE WHEN id = :accountId THEN 1 ELSE 0 END")
    suspend fun setActiveAccount(accountId: Long)

    @Query("DELETE FROM iptv_accounts WHERE id = :accountId")
    suspend fun deleteAccount(accountId: Long)
}

@Dao
interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(channels: List<LiveChannelItem>)

    @Query("DELETE FROM live_channels WHERE accountId = :accountId")
    suspend fun clearForAccount(accountId: Long)

    @Query("SELECT * FROM live_channels WHERE accountId = :accountId ORDER BY id ASC")
    fun getPagedChannels(accountId: Long): PagingSource<Int, LiveChannelItem>

    @Query("SELECT * FROM live_channels WHERE accountId = :accountId AND category = :category ORDER BY id ASC")
    fun getPagedChannelsByCategory(accountId: Long, category: String): PagingSource<Int, LiveChannelItem>

    @Query("SELECT * FROM live_channels WHERE accountId = :accountId AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY id ASC")
    fun searchPagedChannels(accountId: Long, query: String): PagingSource<Int, LiveChannelItem>

    @Query("SELECT DISTINCT category FROM live_channels WHERE accountId = :accountId AND category != '' ORDER BY category ASC")
    fun getCategories(accountId: Long): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM live_channels WHERE accountId = :accountId")
    suspend fun getCount(accountId: Long): Int

    @Query("SELECT * FROM live_channels WHERE accountId = :accountId LIMIT :limit")
    suspend fun getRecent(accountId: Long, limit: Int): List<LiveChannelItem>

    @Query("SELECT * FROM live_channels WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LiveChannelItem?
}

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(movies: List<VodMovieItem>)

    @Query("DELETE FROM vod_movies WHERE accountId = :accountId")
    suspend fun clearForAccount(accountId: Long)

    @Query("SELECT * FROM vod_movies WHERE accountId = :accountId ORDER BY id ASC")
    fun getPagedMovies(accountId: Long): PagingSource<Int, VodMovieItem>

    @Query("SELECT * FROM vod_movies WHERE accountId = :accountId AND genre LIKE '%' || :genre || '%' ORDER BY id ASC")
    fun getPagedMoviesByGenre(accountId: Long, genre: String): PagingSource<Int, VodMovieItem>

    @Query("SELECT * FROM vod_movies WHERE accountId = :accountId AND (title LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%') ORDER BY id ASC")
    fun searchPagedMovies(accountId: Long, query: String): PagingSource<Int, VodMovieItem>

    @Query("SELECT DISTINCT genre FROM vod_movies WHERE accountId = :accountId AND genre != '' ORDER BY genre ASC")
    fun getGenres(accountId: Long): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM vod_movies WHERE accountId = :accountId")
    suspend fun getCount(accountId: Long): Int

    @Query("SELECT * FROM vod_movies WHERE accountId = :accountId LIMIT :limit")
    suspend fun getRecent(accountId: Long, limit: Int): List<VodMovieItem>

    @Query("SELECT * FROM vod_movies WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VodMovieItem?
}

@Dao
interface SeriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(series: List<SeriesItem>)

    @Query("DELETE FROM series_items WHERE accountId = :accountId")
    suspend fun clearForAccount(accountId: Long)

    @Query("SELECT * FROM series_items WHERE accountId = :accountId ORDER BY id ASC")
    fun getPagedSeries(accountId: Long): PagingSource<Int, SeriesItem>

    @Query("SELECT * FROM series_items WHERE accountId = :accountId AND genre LIKE '%' || :genre || '%' ORDER BY id ASC")
    fun getPagedSeriesByGenre(accountId: Long, genre: String): PagingSource<Int, SeriesItem>

    @Query("SELECT * FROM series_items WHERE accountId = :accountId AND (title LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%') ORDER BY id ASC")
    fun searchPagedSeries(accountId: Long, query: String): PagingSource<Int, SeriesItem>

    @Query("SELECT DISTINCT genre FROM series_items WHERE accountId = :accountId AND genre != '' ORDER BY genre ASC")
    fun getGenres(accountId: Long): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM series_items WHERE accountId = :accountId")
    suspend fun getCount(accountId: Long): Int

    @Query("SELECT * FROM series_items WHERE accountId = :accountId LIMIT :limit")
    suspend fun getRecent(accountId: Long, limit: Int): List<SeriesItem>

    @Query("SELECT * FROM series_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SeriesItem?
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE type = :type ORDER BY addedAt DESC")
    fun getFavoritesByType(type: ContentType): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    fun isFavoriteFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: String)
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastPlayedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history ORDER BY lastPlayedAt DESC")
    fun getRecentHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE contentId = :contentId LIMIT 1")
    suspend fun getHistory(contentId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE contentId = :contentId")
    suspend fun deleteHistory(contentId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

@Database(
    entities = [
        IptvAccount::class,
        FavoriteEntity::class,
        WatchHistoryEntity::class,
        LiveChannelItem::class,
        VodMovieItem::class,
        SeriesItem::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun channelDao(): ChannelDao
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
}
