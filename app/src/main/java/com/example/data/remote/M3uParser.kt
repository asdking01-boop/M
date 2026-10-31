package com.example.data.remote

import com.example.data.model.Channel
import com.example.data.model.EpgProgram
import com.example.data.model.VodMovie
import java.io.BufferedReader
import java.io.StringReader

data class ParsedPlaylist(
    val channels: List<Channel>,
    val movies: List<VodMovie>
)

object M3uParser {

    /**
     * Parses an M3U / M3U8 string into structured Channel and VOD Movie models.
     */
    fun parse(content: String): ParsedPlaylist {
        val channels = mutableListOf<Channel>()
        val movies = mutableListOf<VodMovie>()

        val reader = BufferedReader(StringReader(content))
        var currentExtInf = ""
        var line: String? = reader.readLine()

        var counter = 1
        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:")) {
                currentExtInf = trimmed
            } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && currentExtInf.isNotEmpty()) {
                val streamUrl = trimmed
                val name = extractAttribute(currentExtInf, "tvg-name").ifEmpty {
                    extractName(currentExtInf)
                }
                val logo = extractAttribute(currentExtInf, "tvg-logo")
                val group = extractAttribute(currentExtInf, "group-title").ifEmpty { "General" }
                val tvgId = extractAttribute(currentExtInf, "tvg-id")

                val isMovie = group.contains("movie", ignoreCase = true) ||
                        group.contains("vod", ignoreCase = true) ||
                        streamUrl.endsWith(".mp4", ignoreCase = true) ||
                        streamUrl.endsWith(".mkv", ignoreCase = true)

                if (isMovie) {
                    movies.add(
                        VodMovie(
                            id = "m3u_movie_$counter",
                            title = name.ifEmpty { "Movie $counter" },
                            streamUrl = streamUrl,
                            posterUrl = logo,
                            genre = group,
                            year = "2024",
                            duration = "1h 45m",
                            description = "Available via IPTV stream: $group"
                        )
                    )
                } else {
                    channels.add(
                        Channel(
                            id = "m3u_channel_$counter",
                            streamId = tvgId.ifEmpty { counter.toString() },
                            name = name.ifEmpty { "Channel $counter" },
                            streamUrl = streamUrl,
                            logoUrl = logo,
                            category = group,
                            channelNum = counter.toString(),
                            currentProgram = EpgProgram(
                                title = "Live: $name",
                                description = "Live transmission on $group",
                                startTime = "Current",
                                endTime = "Live"
                            )
                        )
                    )
                }
                counter++
                currentExtInf = ""
            }
            line = reader.readLine()
        }

        return ParsedPlaylist(channels, movies)
    }

    private fun extractAttribute(extinf: String, attrName: String): String {
        val regex = Regex("""$attrName="([^"]*)"""", RegexOption.IGNORE_CASE)
        val match = regex.find(extinf)
        return match?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun extractName(extinf: String): String {
        val lastComma = extinf.lastIndexOf(',')
        return if (lastComma != -1 && lastComma < extinf.length - 1) {
            extinf.substring(lastComma + 1).trim()
        } else {
            ""
        }
    }
}
