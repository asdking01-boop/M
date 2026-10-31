package com.example.data.remote

import com.example.data.model.Channel
import com.example.data.model.EpgProgram
import com.example.data.model.IptvAccount
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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun authenticateAndLoad(
        serverUrl: String,
        user: String,
        pass: String
    ): Result<Triple<List<Channel>, List<VodMovie>, Int>> = withContext(Dispatchers.IO) {
        try {
            val cleanServer = serverUrl.trimEnd('/')
            val authUrl = "$cleanServer/player_api.php?username=$user&password=$pass"

            val authRequest = Request.Builder().url(authUrl).build()
            val authResponse = client.newCall(authRequest).execute()
            if (!authResponse.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${authResponse.code}"))
            }

            val authJsonStr = authResponse.body?.string() ?: ""
            val authJson = JSONObject(authJsonStr)
            val userInfo = authJson.optJSONObject("user_info")
            val authStatus = userInfo?.optString("auth")
            if (authStatus == "0") {
                return@withContext Result.failure(Exception("Invalid username or password"))
            }

            // Fetch live streams
            val liveUrl = "$cleanServer/player_api.php?username=$user&password=$pass&action=get_live_streams"
            val liveRequest = Request.Builder().url(liveUrl).build()
            val liveResponse = client.newCall(liveRequest).execute()
            val channels = mutableListOf<Channel>()

            if (liveResponse.isSuccessful) {
                val liveJsonStr = liveResponse.body?.string() ?: "[]"
                val liveArray = JSONArray(liveJsonStr)
                for (i in 0 until minOf(liveArray.length(), 200)) {
                    val obj = liveArray.getJSONObject(i)
                    val streamId = obj.optString("stream_id")
                    val name = obj.optString("name")
                    val streamIcon = obj.optString("stream_icon")
                    val categoryId = obj.optString("category_id", "General")
                    val streamUrl = "$cleanServer/live/$user/$pass/$streamId.m3u8"

                    channels.add(
                        Channel(
                            id = "xtream_ch_$streamId",
                            streamId = streamId,
                            name = name,
                            streamUrl = streamUrl,
                            logoUrl = streamIcon,
                            category = "Category $categoryId",
                            channelNum = streamId,
                            currentProgram = EpgProgram(
                                title = "Live on $name",
                                description = "Live transmission stream",
                                startTime = "Current",
                                endTime = "Live"
                            )
                        )
                    )
                }
            }

            // Fetch VOD streams
            val vodUrl = "$cleanServer/player_api.php?username=$user&password=$pass&action=get_vod_streams"
            val vodRequest = Request.Builder().url(vodUrl).build()
            val vodResponse = client.newCall(vodRequest).execute()
            val movies = mutableListOf<VodMovie>()

            if (vodResponse.isSuccessful) {
                val vodJsonStr = vodResponse.body?.string() ?: "[]"
                val vodArray = JSONArray(vodJsonStr)
                for (i in 0 until minOf(vodArray.length(), 100)) {
                    val obj = vodArray.getJSONObject(i)
                    val streamId = obj.optString("stream_id")
                    val name = obj.optString("name")
                    val icon = obj.optString("stream_icon")
                    val ext = obj.optString("container_extension", "mp4")
                    val rating = obj.optString("rating", "8.0")
                    val streamUrl = "$cleanServer/movie/$user/$pass/$streamId.$ext"

                    movies.add(
                        VodMovie(
                            id = "xtream_mov_$streamId",
                            streamId = streamId,
                            title = name,
                            streamUrl = streamUrl,
                            posterUrl = icon,
                            genre = "VOD",
                            year = "2024",
                            rating = rating,
                            description = "Streamed from Xtream server"
                        )
                    )
                }
            }

            Result.success(Triple(channels, movies, channels.size + movies.size))
        } catch (e: Exception) {
            Result.failure(e)
        }
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
