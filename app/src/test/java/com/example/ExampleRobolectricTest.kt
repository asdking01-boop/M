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
    fun `parse valid m3u playlist`() {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="kras1" tvg-name="KRAS Sports" tvg-logo="https://logo.png" group-title="Sports",KRAS Sports HD
            https://example.com/live/stream1.m3u8
            #EXTINF:-1 tvg-id="mov1" tvg-name="KRAS Action" group-title="Movies",Action Movie
            https://example.com/vod/movie1.mp4
        """.trimIndent()

        val parsed = M3uParser.parse(sampleM3u)
        assertEquals(1, parsed.channels.size)
        assertEquals("KRAS Sports", parsed.channels[0].name)
        assertEquals("https://example.com/live/stream1.m3u8", parsed.channels[0].streamUrl)
        assertEquals(1, parsed.movies.size)
        assertEquals("KRAS Action", parsed.movies[0].title)
    }
}
