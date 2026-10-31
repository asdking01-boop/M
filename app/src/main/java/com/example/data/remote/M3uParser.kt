package com.example.data.remote

import com.example.data.model.Channel
import com.example.data.model.Episode
import com.example.data.model.EpgProgram
import com.example.data.model.Season
import com.example.data.model.Series
import com.example.data.model.VodMovie
import java.io.BufferedReader
import java.io.StringReader

data class ParsedPlaylist(
    val channels: List<Channel>,
    val movies: List<VodMovie>,
    val series: List<Series>
)

object M3uParser {

    private val seasonEpisodeRegex = Regex(
        """(?:[Ss](\d{1,2})[\s._-]*[Ee](\d{1,2})|[Ss]eason[\s._-]*(\d{1,2})[\s._-]*[Ee]pisode[\s._-]*(\d{1,2})|الموسم[\s._-]*(\d{1,2})[\s._-]*الحلقة[\s._-]*(\d{1,2}))""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Parses an M3U / M3U8 string into structured Channels, VOD Movies, and Series.
     * Designed to handle 10,000+ items with resilient try-catch per item so no single error stops parsing.
     */
    fun parse(content: String): ParsedPlaylist {
        val channels = ArrayList<Channel>(1000)
        val movies = ArrayList<VodMovie>(1000)
        
        // Map to group series episodes by Series Title
        // seriesTitle -> (metadata, Map of seasonNum -> List of Episodes)
        val seriesEpisodesMap = LinkedHashMap<String, SeriesGroupData>()

        val reader = BufferedReader(StringReader(content))
        var currentExtInf = ""
        var line: String? = reader.readLine()

        var counter = 1

        while (line != null) {
            try {
                val trimmed = line.trim()
                if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                    currentExtInf = trimmed
                } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && currentExtInf.isNotEmpty()) {
                    val streamUrl = trimmed

                    // Resilient item processing
                    processM3uItem(
                        counter = counter,
                        extInf = currentExtInf,
                        streamUrl = streamUrl,
                        channels = channels,
                        movies = movies,
                        seriesMap = seriesEpisodesMap
                    )

                    counter++
                    currentExtInf = ""
                }
            } catch (_: Exception) {
                // If a single line causes an error, ignore it and continue parsing subsequent items!
                currentExtInf = ""
            }
            line = reader.readLine()
        }

        // Convert grouped series map into Series models
        val seriesList = ArrayList<Series>(seriesEpisodesMap.size)
        for ((seriesTitle, groupData) in seriesEpisodesMap) {
            try {
                val seasonsList = groupData.seasons.map { (seasonNum, episodes) ->
                    Season(
                        seasonNum = seasonNum,
                        title = "Season $seasonNum",
                        episodes = episodes.sortedBy { it.episodeNum }
                    )
                }.sortedBy { it.seasonNum }

                val seriesId = "m3u_series_${seriesTitle.hashCode().coerceAtLeast(0)}"
                seriesList.add(
                    Series(
                        id = seriesId,
                        seriesId = seriesId,
                        title = seriesTitle,
                        posterUrl = groupData.posterUrl,
                        backdropUrl = groupData.posterUrl,
                        genre = groupData.genre,
                        year = "2024",
                        rating = "8.5",
                        description = "Series from playlist: ${groupData.genre}",
                        seasons = seasonsList
                    )
                )
            } catch (_: Exception) {
                // Continue if a single series grouping fails
            }
        }

        return ParsedPlaylist(channels, movies, seriesList)
    }

    private class SeriesGroupData(
        val genre: String,
        val posterUrl: String,
        val seasons: LinkedHashMap<Int, ArrayList<Episode>> = LinkedHashMap()
    )

    private fun processM3uItem(
        counter: Int,
        extInf: String,
        streamUrl: String,
        channels: ArrayList<Channel>,
        movies: ArrayList<VodMovie>,
        seriesMap: LinkedHashMap<String, SeriesGroupData>
    ) {
        val name = extractAttribute(extInf, "tvg-name").ifEmpty {
            extractName(extInf)
        }.ifEmpty { "Item $counter" }

        val logo = extractAttribute(extInf, "tvg-logo")
        val group = extractAttribute(extInf, "group-title").ifEmpty { "General" }
        val tvgId = extractAttribute(extInf, "tvg-id")

        val isSeriesGroup = group.contains("series", ignoreCase = true) ||
                group.contains("serie", ignoreCase = true) ||
                group.contains("مسلسل", ignoreCase = true) ||
                streamUrl.contains("/series/", ignoreCase = true)

        val seasonEpisodeMatch = seasonEpisodeRegex.find(name) ?: seasonEpisodeRegex.find(streamUrl)

        if (isSeriesGroup || seasonEpisodeMatch != null) {
            // It's a Series Episode!
            var seasonNum = 1
            var episodeNum = 1

            if (seasonEpisodeMatch != null) {
                val sStr = seasonEpisodeMatch.groupValues[1].ifEmpty {
                    seasonEpisodeMatch.groupValues[3].ifEmpty { seasonEpisodeMatch.groupValues[5] }
                }
                val eStr = seasonEpisodeMatch.groupValues[2].ifEmpty {
                    seasonEpisodeMatch.groupValues[4].ifEmpty { seasonEpisodeMatch.groupValues[6] }
                }
                seasonNum = sStr.toIntOrNull() ?: 1
                episodeNum = eStr.toIntOrNull() ?: 1
            }

            // Clean title: remove S01E02 part to get base series title
            val cleanSeriesTitle = if (seasonEpisodeMatch != null) {
                name.substring(0, seasonEpisodeMatch.range.first).trim().trimEnd('-', ':', '|', ' ')
                    .ifEmpty { name }
            } else {
                name
            }

            val groupData = seriesMap.getOrPut(cleanSeriesTitle) {
                SeriesGroupData(genre = group, posterUrl = logo)
            }

            val episodesForSeason = groupData.seasons.getOrPut(seasonNum) { ArrayList() }
            val seriesId = "m3u_series_${cleanSeriesTitle.hashCode().coerceAtLeast(0)}"
            val epId = "ep_${seriesId}_s${seasonNum}_e${episodeNum}_$counter"

            episodesForSeason.add(
                Episode(
                    id = epId,
                    seriesId = seriesId,
                    seasonNum = seasonNum,
                    episodeNum = episodeNum,
                    title = name,
                    streamUrl = streamUrl,
                    posterUrl = logo,
                    duration = "45m",
                    durationSeconds = 2700L,
                    description = "Episode $episodeNum of Season $seasonNum"
                )
            )
            return
        }

        // Check if Movie
        val isMovie = group.contains("movie", ignoreCase = true) ||
                group.contains("vod", ignoreCase = true) ||
                group.contains("فيلم", ignoreCase = true) ||
                group.contains("افلام", ignoreCase = true) ||
                streamUrl.contains("/movie/", ignoreCase = true) ||
                streamUrl.endsWith(".mp4", ignoreCase = true) ||
                streamUrl.endsWith(".mkv", ignoreCase = true)

        if (isMovie) {
            movies.add(
                VodMovie(
                    id = "m3u_movie_$counter",
                    title = name,
                    streamUrl = streamUrl,
                    posterUrl = logo,
                    genre = group,
                    year = "2024",
                    duration = "1h 45m",
                    description = "Available via IPTV stream: $group"
                )
            )
        } else {
            // Live TV Channel
            channels.add(
                Channel(
                    id = "m3u_channel_$counter",
                    streamId = tvgId.ifEmpty { counter.toString() },
                    name = name,
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
}
