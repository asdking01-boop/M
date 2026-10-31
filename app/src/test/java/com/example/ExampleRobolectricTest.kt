package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.M3uParser
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("KRAS TV", appName)
    }

    @Test
    fun `parse valid m3u playlist with channels, movies, and series`() {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="kras1" tvg-name="KRAS Sports" tvg-logo="https://logo.png" group-title="Sports",KRAS Sports HD
            https://example.com/live/stream1.m3u8
            #EXTINF:-1 tvg-id="mov1" tvg-name="KRAS Action" group-title="Movies",Action Movie
            https://example.com/vod/movie1.mp4
            #EXTINF:-1 tvg-id="ser1" tvg-name="Dark Nexus S01 E01" group-title="Series",Dark Nexus S01 E01
            https://example.com/series/nexus_s1e1.mp4
            #EXTINF:-1 tvg-id="ser2" tvg-name="Dark Nexus S01 E02" group-title="Series",Dark Nexus S01 E02
            https://example.com/series/nexus_s1e2.mp4
        """.trimIndent()

        val parsed = M3uParser.parse(sampleM3u)
        assertEquals(1, parsed.channels.size)
        assertEquals("KRAS Sports", parsed.channels[0].name)
        assertEquals(1, parsed.movies.size)
        assertEquals("KRAS Action", parsed.movies[0].title)
        assertEquals(1, parsed.series.size)
        assertEquals("Dark Nexus", parsed.series[0].title)
        assertEquals(1, parsed.series[0].seasons.size)
        assertEquals(2, parsed.series[0].seasons[0].episodes.size)
    }
}
