package com.example.data.local

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

    @Query("SELECT * FROM watch_history ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 10): Flow<List<WatchHistoryEntity>>

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
    entities = [IptvAccount::class, FavoriteEntity::class, WatchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
}
