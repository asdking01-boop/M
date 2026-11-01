package com.example.data.remote

import android.util.JsonReader
import android.util.JsonToken
import com.example.data.model.Episode
import com.example.data.model.LiveChannelItem
import com.example.data.model.Season
import com.example.data.model.SeriesItem
import com.example.data.model.VodMovieItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class XtreamCodesService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun authenticate(
        serverUrl: String,
        user: String,
        pass: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val cleanServer = serverUrl.trimEnd('/')
            val authUrl = "$cleanServer/player_api.php?username=$user&password=$pass"

            val request = Request.Builder().url(authUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}: ${response.message}"))
            }

            // Stream user_info without holding entire response in memory
            response.body?.use { body ->
                val reader = JsonReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
                var authStatus = ""
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    if (name == "user_info" && reader.peek() == JsonToken.BEGIN_OBJECT) {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            if (reader.nextName() == "auth") {
                                authStatus = if (reader.peek() == JsonToken.STRING) reader.nextString() else reader.nextInt().toString()
                            } else {
                                reader.skipValue()
                            }
                        }
                        reader.endObject()
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endObject()

                if (authStatus == "0") {
                    return@withContext Result.failure(Exception("Invalid username or password"))
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Streams Live Channels using JsonReader without buffering the entire JSON array in memory.
     * Emits items in batches of 500 to onBatch.
     */
    suspend fun streamLiveChannels(
        serverUrl: String,
        user: String,
        pass: String,
        accountId: Long,
        onBatch: suspend (List<LiveChannelItem>) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        var totalCount = 0
        try {
            val cleanServer = serverUrl.trimEnd('/')
            val baseUrl = "$cleanServer/player_api.php?username=$user&password=$pass&action=get_live_streams"

            val request = Request.Builder().url(baseUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} fetching live streams"))
            }

            response.body?.use { body ->
                val reader = JsonReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
                if (reader.peek() != JsonToken.BEGIN_ARRAY) {
                    return@withContext Result.success(0)
                }

                reader.beginArray()
                val batch = ArrayList<LiveChannelItem>(500)

                while (reader.hasNext()) {
                    try {
                        reader.beginObject()
                        var streamId = ""
                        var name = ""
                        var icon = ""
                        var category = ""

                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "stream_id" -> streamId = nextSafeString(reader)
                                "name" -> name = nextSafeString(reader)
                                "stream_icon" -> icon = nextSafeString(reader)
                                "category_name" -> category = nextSafeString(reader)
                                "category_id" -> if (category.isEmpty()) category = "Category " + nextSafeString(reader) else reader.skipValue()
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()

                        if (streamId.isNotEmpty() || name.isNotEmpty()) {
                            val finalId = streamId.ifEmpty { (totalCount + 1).toString() }
                            val channel = LiveChannelItem(
                                id = "xtream_ch_${accountId}_$finalId",
                                accountId = accountId,
                                streamId = finalId,
                                name = name.ifEmpty { "Channel $finalId" },
                                streamUrl = "$cleanServer/live/$user/$pass/$finalId.m3u8",
                                logoUrl = icon,
                                category = category.ifEmpty { "General" },
                                channelNum = finalId
                            )
                            batch.add(channel)
                            totalCount++

                            if (batch.size >= 500) {
                                onBatch(ArrayList(batch))
                                batch.clear()
                            }
                        }
                    } catch (_: Exception) {
                        skipToNextObject(reader)
                    }
                }
                reader.endArray()

                if (batch.isNotEmpty()) {
                    onBatch(batch)
                    batch.clear()
                }
            }

            Result.success(totalCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Streams VOD Movies using JsonReader in batches of 500 without buffering the whole response into memory.
     */
    suspend fun streamVodMovies(
        serverUrl: String,
        user: String,
        pass: String,
        accountId: Long,
        onBatch: suspend (List<VodMovieItem>) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        var totalCount = 0
        try {
            val cleanServer = serverUrl.trimEnd('/')
            val baseUrl = "$cleanServer/player_api.php?username=$user&password=$pass&action=get_vod_streams"

            val request = Request.Builder().url(baseUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} fetching movies"))
            }

            response.body?.use { body ->
                val reader = JsonReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
                if (reader.peek() != JsonToken.BEGIN_ARRAY) {
                    return@withContext Result.success(0)
                }

                reader.beginArray()
                val batch = ArrayList<VodMovieItem>(500)

                while (reader.hasNext()) {
                    try {
                        reader.beginObject()
                        var streamId = ""
                        var name = ""
                        var icon = ""
                        var ext = "mp4"
                        var category = ""
                        var rating = "8.0"
                        var year = ""
                        var addedTimestamp = 0L

                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "stream_id" -> streamId = nextSafeString(reader)
                                "name" -> name = nextSafeString(reader)
                                "stream_icon" -> icon = nextSafeString(reader)
                                "container_extension" -> ext = nextSafeString(reader).ifEmpty { "mp4" }
                                "rating" -> rating = nextSafeString(reader).ifEmpty { "8.0" }
                                "category_name" -> category = nextSafeString(reader)
                                "category_id" -> if (category.isEmpty()) category = "VOD " + nextSafeString(reader) else reader.skipValue()
                                "year" -> year = nextSafeString(reader)
                                "added" -> addedTimestamp = parseTimestamp(nextSafeString(reader))
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()

                        if (streamId.isNotEmpty() || name.isNotEmpty()) {
                            val finalId = streamId.ifEmpty { (totalCount + 1).toString() }
                            val movieYear = if (year.length >= 4) year.take(4) else "2024"
                            val movie = VodMovieItem(
                                id = "xtream_mov_${accountId}_$finalId",
                                accountId = accountId,
                                streamId = finalId,
                                title = name.ifEmpty { "Movie $finalId" },
                                streamUrl = "$cleanServer/movie/$user/$pass/$finalId.$ext",
                                posterUrl = icon,
                                genre = category.ifEmpty { "Movies" },
                                year = movieYear,
                                rating = rating.take(3),
                                added = addedTimestamp
                            )
                            batch.add(movie)
                            totalCount++

                            if (batch.size >= 500) {
                                onBatch(ArrayList(batch))
                                batch.clear()
                            }
                        }
                    } catch (_: Exception) {
                        skipToNextObject(reader)
                    }
                }
                reader.endArray()

                if (batch.isNotEmpty()) {
                    onBatch(batch)
                    batch.clear()
                }
            }

            Result.success(totalCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Streams Series using JsonReader in batches of 500.
     */
    suspend fun streamSeries(
        serverUrl: String,
        user: String,
        pass: String,
        accountId: Long,
        onBatch: suspend (List<SeriesItem>) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        var totalCount = 0
        try {
            val cleanServer = serverUrl.trimEnd('/')
            val baseUrl = "$cleanServer/player_api.php?username=$user&password=$pass&action=get_series"

            val request = Request.Builder().url(baseUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} fetching series"))
            }

            response.body?.use { body ->
                val reader = JsonReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
                if (reader.peek() != JsonToken.BEGIN_ARRAY) {
                    return@withContext Result.success(0)
                }

                reader.beginArray()
                val batch = ArrayList<SeriesItem>(500)

                while (reader.hasNext()) {
                    try {
                        reader.beginObject()
                        var seriesId = ""
                        var name = ""
                        var cover = ""
                        var genre = ""
                        var releaseDate = ""
                        var year = ""
                        var rating = "8.5"
                        var addedTimestamp = 0L

                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "series_id" -> seriesId = nextSafeString(reader)
                                "name" -> name = nextSafeString(reader)
                                "cover" -> cover = nextSafeString(reader)
                                "genre" -> genre = nextSafeString(reader)
                                "releaseDate", "release_date" -> releaseDate = nextSafeString(reader)
                                "year" -> year = nextSafeString(reader)
                                "rating" -> rating = nextSafeString(reader).ifEmpty { "8.5" }
                                "category_name" -> if (genre.isEmpty()) genre = nextSafeString(reader) else reader.skipValue()
                                "added", "last_modified" -> {
                                    val ts = parseTimestamp(nextSafeString(reader))
                                    if (ts > 0L) addedTimestamp = ts
                                }
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()

                        if (seriesId.isNotEmpty() || name.isNotEmpty()) {
                            val finalId = seriesId.ifEmpty { (totalCount + 1).toString() }
                            val resolvedYear = when {
                                year.length >= 4 -> year.take(4)
                                releaseDate.length >= 4 -> releaseDate.take(4)
                                else -> "2024"
                            }

                            val series = SeriesItem(
                                id = "xtream_ser_${accountId}_$finalId",
                                accountId = accountId,
                                seriesId = finalId,
                                title = name.ifEmpty { "Series $finalId" },
                                posterUrl = cover,
                                genre = genre.ifEmpty { "Series" },
                                year = resolvedYear,
                                rating = rating.take(3),
                                added = addedTimestamp
                            )
                            batch.add(series)
                            totalCount++

                            if (batch.size >= 500) {
                                onBatch(ArrayList(batch))
                                batch.clear()
                            }
                        }
                    } catch (_: Exception) {
                        skipToNextObject(reader)
                    }
                }
                reader.endArray()

                if (batch.isNotEmpty()) {
                    onBatch(batch)
                    batch.clear()
                }
            }

            Result.success(totalCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Streams an M3U playlist file line by line without holding the entire file in RAM.
     * Inserts into Room in batches of 500.
     */
    suspend fun streamM3u(
        url: String,
        accountId: Long,
        onChannelBatch: suspend (List<LiveChannelItem>) -> Unit,
        onMovieBatch: suspend (List<VodMovieItem>) -> Unit,
        onSeriesBatch: suspend (List<SeriesItem>) -> Unit
    ): Result<Triple<Int, Int, Int>> = withContext(Dispatchers.IO) {
        var chanCount = 0
        var movCount = 0
        var serCount = 0

        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} downloading M3U"))
            }

            response.body?.use { body ->
                val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8), 32 * 1024)
                val chanBatch = ArrayList<LiveChannelItem>(500)
                val movBatch = ArrayList<VodMovieItem>(500)
                val serBatch = ArrayList<SeriesItem>(500)
                val knownSeriesTitles = HashSet<String>()

                var currentExtInf = ""
                var line = reader.readLine()
                var counter = 1

                val seasonEpisodeRegex = Regex(
                    """(?:[Ss](\d{1,2})[\s._-]*[Ee](\d{1,2})|[Ss]eason[\s._-]*(\d{1,2})[\s._-]*[Ee]pisode[\s._-]*(\d{1,2})|الموسم[\s._-]*(\d{1,2})[\s._-]*الحلقة[\s._-]*(\d{1,2}))""",
                    RegexOption.IGNORE_CASE
                )

                while (line != null) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                        currentExtInf = trimmed
                    } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && currentExtInf.isNotEmpty()) {
                        val streamUrl = trimmed
                        val name = extractAttribute(currentExtInf, "tvg-name").ifEmpty {
                            extractName(currentExtInf)
                        }.ifEmpty { "Item $counter" }
                        val logo = extractAttribute(currentExtInf, "tvg-logo")
                        val group = extractAttribute(currentExtInf, "group-title").ifEmpty { "General" }
                        val tvgId = extractAttribute(currentExtInf, "tvg-id")

                        val isSeriesGroup = group.contains("series", ignoreCase = true) ||
                                group.contains("serie", ignoreCase = true) ||
                                group.contains("مسلسل", ignoreCase = true) ||
                                streamUrl.contains("/series/", ignoreCase = true)

                        val seasonMatch = seasonEpisodeRegex.find(name) ?: seasonEpisodeRegex.find(streamUrl)

                        if (isSeriesGroup || seasonMatch != null) {
                            val cleanSeriesTitle = if (seasonMatch != null) {
                                name.substring(0, seasonMatch.range.first).trim().trimEnd('-', ':', '|', ' ')
                                    .ifEmpty { name }
                            } else name

                            if (knownSeriesTitles.add(cleanSeriesTitle)) {
                                val sId = "m3u_ser_${accountId}_${cleanSeriesTitle.hashCode().coerceAtLeast(0)}"
                                serBatch.add(
                                    SeriesItem(
                                        id = sId,
                                        accountId = accountId,
                                        seriesId = sId,
                                        title = cleanSeriesTitle,
                                        posterUrl = logo,
                                        genre = group,
                                        year = "2024",
                                        rating = "8.5"
                                    )
                                )
                                serCount++
                                if (serBatch.size >= 500) {
                                    onSeriesBatch(ArrayList(serBatch))
                                    serBatch.clear()
                                }
                            }
                        } else {
                            val isMovie = group.contains("movie", ignoreCase = true) ||
                                    group.contains("vod", ignoreCase = true) ||
                                    group.contains("فيلم", ignoreCase = true) ||
                                    group.contains("افلام", ignoreCase = true) ||
                                    streamUrl.contains("/movie/", ignoreCase = true) ||
                                    streamUrl.endsWith(".mp4", ignoreCase = true) ||
                                    streamUrl.endsWith(".mkv", ignoreCase = true)

                            if (isMovie) {
                                val mId = "m3u_mov_${accountId}_$counter"
                                movBatch.add(
                                    VodMovieItem(
                                        id = mId,
                                        accountId = accountId,
                                        streamId = counter.toString(),
                                        title = name,
                                        streamUrl = streamUrl,
                                        posterUrl = logo,
                                        genre = group,
                                        year = "2024",
                                        rating = "8.0"
                                    )
                                )
                                movCount++
                                if (movBatch.size >= 500) {
                                    onMovieBatch(ArrayList(movBatch))
                                    movBatch.clear()
                                }
                            } else {
                                val cId = "m3u_ch_${accountId}_$counter"
                                chanBatch.add(
                                    LiveChannelItem(
                                        id = cId,
                                        accountId = accountId,
                                        streamId = tvgId.ifEmpty { counter.toString() },
                                        name = name,
                                        streamUrl = streamUrl,
                                        logoUrl = logo,
                                        category = group,
                                        channelNum = counter.toString()
                                    )
                                )
                                chanCount++
                                if (chanBatch.size >= 500) {
                                    onChannelBatch(ArrayList(chanBatch))
                                    chanBatch.clear()
                                }
                            }
                        }

                        counter++
                        currentExtInf = ""
                    }
                    line = reader.readLine()
                }

                if (chanBatch.isNotEmpty()) {
                    onChannelBatch(chanBatch)
                    chanBatch.clear()
                }
                if (movBatch.isNotEmpty()) {
                    onMovieBatch(movBatch)
                    movBatch.clear()
                }
                if (serBatch.isNotEmpty()) {
                    onSeriesBatch(serBatch)
                    serBatch.clear()
                }
            }

            Result.success(Triple(chanCount, movCount, serCount))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches detailed seasons and episodes for a single selected series.
     */
    suspend fun fetchSeriesInfo(
        serverUrl: String,
        user: String,
        pass: String,
        seriesId: String
    ): Result<List<Season>> = withContext(Dispatchers.IO) {
        try {
            val cleanServer = serverUrl.trimEnd('/')
            val url = "$cleanServer/player_api.php?username=$user&password=$pass&action=get_series_info&series_id=$seriesId"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}"))
            }

            val body = response.body?.string() ?: "{}"
            val root = JSONObject(body)
            val episodesObj = root.optJSONObject("episodes") ?: JSONObject()

            val seasonsList = mutableListOf<Season>()
            val seasonKeys = episodesObj.keys()

            while (seasonKeys.hasNext()) {
                val sKey = seasonKeys.next()
                val seasonNum = sKey.toIntOrNull() ?: 1
                val epArray = episodesObj.optJSONArray(sKey) ?: JSONArray()
                val episodes = ArrayList<Episode>()

                for (i in 0 until epArray.length()) {
                    try {
                        val epObj = epArray.getJSONObject(i)
                        val epId = epObj.optString("id")
                        val epNum = epObj.optInt("episode_num", i + 1)
                        val title = epObj.optString("title").ifEmpty { "Episode $epNum" }
                        val ext = epObj.optString("container_extension").ifEmpty { "mp4" }
                        val streamUrl = "$cleanServer/series/$user/$pass/$epId.$ext"

                        val info = epObj.optJSONObject("info")
                        val plot = info?.optString("plot") ?: ""
                        val duration = info?.optString("duration") ?: "45m"
                        val movieImage = info?.optString("movie_image") ?: ""

                        episodes.add(
                            Episode(
                                id = "xtream_ep_${seriesId}_${epId}",
                                seriesId = seriesId,
                                seasonNum = seasonNum,
                                episodeNum = epNum,
                                title = title,
                                streamUrl = streamUrl,
                                posterUrl = movieImage,
                                duration = duration,
                                description = plot
                            )
                        )
                    } catch (_: Exception) {
                    }
                }

                seasonsList.add(
                    Season(
                        seasonNum = seasonNum,
                        title = "Season $seasonNum",
                        episodes = episodes.sortedBy { it.episodeNum }
                    )
                )
            }

            Result.success(seasonsList.sortedBy { it.seasonNum })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun nextSafeString(reader: JsonReader): String {
        return try {
            when (reader.peek()) {
                JsonToken.STRING -> reader.nextString()
                JsonToken.NUMBER -> reader.nextString()
                JsonToken.BOOLEAN -> reader.nextBoolean().toString()
                JsonToken.NULL -> {
                    reader.nextNull()
                    ""
                }
                else -> {
                    reader.skipValue()
                    ""
                }
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun skipToNextObject(reader: JsonReader) {
        try {
            while (reader.hasNext()) {
                reader.skipValue()
            }
            if (reader.peek() == JsonToken.END_OBJECT) {
                reader.endObject()
            }
        } catch (_: Exception) {
        }
    }

    private fun extractAttribute(extinf: String, attrName: String): String {
        return try {
            val regex = Regex("""$attrName="([^"]*)"""", RegexOption.IGNORE_CASE)
            val match = regex.find(extinf)
            match?.groupValues?.get(1)?.trim() ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun extractName(extinf: String): String {
        return try {
            val lastComma = extinf.lastIndexOf(',')
            if (lastComma != -1 && lastComma < extinf.length - 1) {
                extinf.substring(lastComma + 1).trim()
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseTimestamp(raw: String): Long {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return 0L
        val num = trimmed.toLongOrNull()
        if (num != null && num > 0L) {
            return if (num < 100_000_000_000L) num * 1000L else num
        }
        return try {
            if (trimmed.length >= 10 && trimmed[4] == '-' && trimmed[7] == '-') {
                val year = trimmed.substring(0, 4).toInt()
                val month = trimmed.substring(5, 7).toInt()
                val day = trimmed.substring(8, 10).toInt()
                val cal = java.util.Calendar.getInstance()
                cal.set(year, month - 1, day, 12, 0, 0)
                cal.timeInMillis
            } else {
                0L
            }
        } catch (_: Exception) {
            0L
        }
    }
}
