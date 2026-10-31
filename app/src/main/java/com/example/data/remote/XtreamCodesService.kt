package com.example.data.remote

import com.example.data.model.Channel
import com.example.data.model.Episode
import com.example.data.model.EpgProgram
import com.example.data.model.Season
import com.example.data.model.Series
import com.example.data.model.VodMovie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class XtreamCodesService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
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
                return@withContext Result.failure(Exception("HTTP error ${response.code}"))
            }

            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            val userInfo = json.optJSONObject("user_info")
            val authStatus = userInfo?.optString("auth")
            if (authStatus == "0") {
                return@withContext Result.failure(Exception("Invalid username or password"))
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches ALL live streams with separate request.
     * Supports pagination if enabled by server, and removes all hardcoded limits.
     * Fault-tolerant: errors in individual items will never halt parsing of the remaining items.
     */
    suspend fun fetchLiveStreams(
        serverUrl: String,
        user: String,
        pass: String
    ): Result<List<Channel>> = withContext(Dispatchers.IO) {
        try {
            val cleanServer = serverUrl.trimEnd('/')
            val baseUrl = "$cleanServer/player_api.php?username=$user&password=$pass&action=get_live_streams"
            val channels = ArrayList<Channel>()

            val allItems = fetchWithPaginationSupport(baseUrl)
            for (i in 0 until allItems.length()) {
                try {
                    val obj = allItems.getJSONObject(i)
                    val streamId = obj.optString("stream_id").ifEmpty {
                        obj.optInt("stream_id", i + 1).toString()
                    }
                    val name = obj.optString("name").ifEmpty { "Channel $streamId" }
                    val streamIcon = obj.optString("stream_icon")
                    val categoryName = obj.optString("category_name").ifEmpty {
                        val categoryId = obj.optString("category_id")
                        if (categoryId.isNotEmpty()) "Category $categoryId" else "General"
                    }
                    val streamUrl = "$cleanServer/live/$user/$pass/$streamId.m3u8"

                    channels.add(
                        Channel(
                            id = "xtream_ch_$streamId",
                            streamId = streamId,
                            name = name,
                            streamUrl = streamUrl,
                            logoUrl = streamIcon,
                            category = categoryName,
                            channelNum = streamId,
                            currentProgram = EpgProgram(
                                title = "Live: $name",
                                description = "Live transmission stream",
                                startTime = "Current",
                                endTime = "Live"
                            )
                        )
                    )
                } catch (_: Exception) {
                    // Skip corrupt item and keep parsing all other channels
                }
            }

            Result.success(channels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches ALL VOD movies with separate request.
     * Supports pagination if present, and removes all hardcoded limits.
     */
    suspend fun fetchVodStreams(
        serverUrl: String,
        user: String,
        pass: String
    ): Result<List<VodMovie>> = withContext(Dispatchers.IO) {
        try {
            val cleanServer = serverUrl.trimEnd('/')
            val baseUrl = "$cleanServer/player_api.php?username=$user&password=$pass&action=get_vod_streams"
            val movies = ArrayList<VodMovie>()

            val allItems = fetchWithPaginationSupport(baseUrl)
            for (i in 0 until allItems.length()) {
                try {
                    val obj = allItems.getJSONObject(i)
                    val streamId = obj.optString("stream_id").ifEmpty {
                        obj.optInt("stream_id", i + 1).toString()
                    }
                    val name = obj.optString("name").ifEmpty { "Movie $streamId" }
                    val icon = obj.optString("stream_icon")
                    val ext = obj.optString("container_extension").ifEmpty { "mp4" }
                    val rating = obj.optString("rating").ifEmpty {
                        obj.optDouble("rating_5based", 8.0).times(2.0).toString()
                    }
                    val categoryName = obj.optString("category_name").ifEmpty {
                        val categoryId = obj.optString("category_id")
                        if (categoryId.isNotEmpty()) "VOD $categoryId" else "Movies"
                    }
                    val streamUrl = "$cleanServer/movie/$user/$pass/$streamId.$ext"

                    movies.add(
                        VodMovie(
                            id = "xtream_mov_$streamId",
                            streamId = streamId,
                            title = name,
                            streamUrl = streamUrl,
                            posterUrl = icon,
                            backdropUrl = icon,
                            genre = categoryName,
                            year = "2024",
                            rating = rating.take(3),
                            description = "Available on Xtream VOD ($categoryName)"
                        )
                    )
                } catch (_: Exception) {
                    // Skip corrupt item and continue
                }
            }

            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches ALL series with separate request (action=get_series).
     * Supports pagination if enabled, and removes all limits so 10,000+ series can be loaded.
     */
    suspend fun fetchSeries(
        serverUrl: String,
        user: String,
        pass: String
    ): Result<List<Series>> = withContext(Dispatchers.IO) {
        try {
            val cleanServer = serverUrl.trimEnd('/')
            val baseUrl = "$cleanServer/player_api.php?username=$user&password=$pass&action=get_series"
            val seriesList = ArrayList<Series>()

            val allItems = fetchWithPaginationSupport(baseUrl)
            for (i in 0 until allItems.length()) {
                try {
                    val obj = allItems.getJSONObject(i)
                    val seriesId = obj.optString("series_id").ifEmpty {
                        obj.optInt("series_id", i + 1).toString()
                    }
                    val name = obj.optString("name").ifEmpty { "Series $seriesId" }
                    val cover = obj.optString("cover")
                    val plot = obj.optString("plot")
                    val genre = obj.optString("genre").ifEmpty {
                        val cat = obj.optString("category_name")
                        if (cat.isNotEmpty()) cat else "Series"
                    }
                    val releaseDate = obj.optString("releaseDate")
                    val year = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else "2024"
                    val rating = obj.optString("rating").ifEmpty { "8.5" }

                    // Initial default season for series until user clicks to fetch detailed episode breakdown
                    val defaultSeason = Season(
                        seasonNum = 1,
                        title = "Season 1",
                        episodes = emptyList()
                    )

                    seriesList.add(
                        Series(
                            id = "xtream_ser_$seriesId",
                            seriesId = seriesId,
                            title = name,
                            posterUrl = cover,
                            backdropUrl = cover,
                            genre = genre,
                            year = year,
                            rating = rating.take(3),
                            description = plot.ifEmpty { "Series streamed via Xtream Codes" },
                            seasons = listOf(defaultSeason)
                        )
                    )
                } catch (_: Exception) {
                    // Skip corrupt item
                }
            }

            Result.success(seriesList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches detailed seasons and episodes for a specific series from Xtream Codes.
     * action=get_series_info&series_id=...
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
                                seriesId = "xtream_ser_$seriesId",
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
                        // Skip corrupted episode
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

    /**
     * Executes requests with pagination detection.
     * If server returns paginated data (e.g. responds to &page=1, &page=2), it iterates until all pages are retrieved.
     * If server returns all items in a single response (the standard Xtream Codes behavior), returns all items directly.
     */
    private fun fetchWithPaginationSupport(baseUrl: String): JSONArray {
        val firstRequest = Request.Builder().url(baseUrl).build()
        val firstResponse = client.newCall(firstRequest).execute()
        if (!firstResponse.isSuccessful) {
            return JSONArray()
        }

        val body = firstResponse.body?.string()?.trim() ?: "[]"
        if (!body.startsWith("[")) {
            return JSONArray()
        }

        val firstArray = JSONArray(body)

        // Check if server is paginating (typically 100 or 500 items per page when pagination is enforced)
        // If it returned 0 items or a small batch, we already have everything or nothing
        if (firstArray.length() == 0) {
            return firstArray
        }

        // Test if the server supports &page=2 by probing page 2
        // Only attempt pagination if the URL didn't already specify a page
        if (!baseUrl.contains("&page=")) {
            try {
                val page2Url = "$baseUrl&page=2"
                val page2Request = Request.Builder().url(page2Url).build()
                val page2Response = client.newCall(page2Request).execute()

                if (page2Response.isSuccessful) {
                    val p2Body = page2Response.body?.string()?.trim() ?: "[]"
                    if (p2Body.startsWith("[")) {
                        val p2Array = JSONArray(p2Body)
                        if (p2Array.length() > 0) {
                            // Server supports pagination! Collect all pages
                            val combinedArray = JSONArray()
                            // Add page 1
                            for (i in 0 until firstArray.length()) combinedArray.put(firstArray.get(i))
                            // Add page 2
                            for (i in 0 until p2Array.length()) combinedArray.put(p2Array.get(i))

                            var currentPage = 3
                            var hasMore = true
                            while (hasMore && currentPage <= 500) { // Safety ceiling of 500 pages (e.g. 50,000+ items)
                                try {
                                    val pageUrl = "$baseUrl&page=$currentPage"
                                    val pReq = Request.Builder().url(pageUrl).build()
                                    val pRes = client.newCall(pReq).execute()
                                    if (pRes.isSuccessful) {
                                        val pBody = pRes.body?.string()?.trim() ?: "[]"
                                        if (pBody.startsWith("[")) {
                                            val pArr = JSONArray(pBody)
                                            if (pArr.length() > 0) {
                                                for (i in 0 until pArr.length()) combinedArray.put(pArr.get(i))
                                                currentPage++
                                            } else {
                                                hasMore = false
                                            }
                                        } else {
                                            hasMore = false
                                        }
                                    } else {
                                        hasMore = false
                                    }
                                } catch (_: Exception) {
                                    hasMore = false
                                }
                            }
                            return combinedArray
                        }
                    }
                }
            } catch (_: Exception) {
                // If pagination probe fails, fallback to firstArray
            }
        }

        return firstArray
    }

    suspend fun fetchM3u(url: String): Result<ParsedPlaylist> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}"))
            }
            val body = response.body?.string() ?: ""
            Result.success(M3uParser.parse(body))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
