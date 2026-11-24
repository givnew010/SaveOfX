package com.example

import com.example.util.TweetMediaItem
import com.example.util.TweetParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TweetParserTest {
    @Test
    fun extractCleanUrl_normalizesTrackingParams() {
        val raw = "Check this out https://twitter.com/AndroidDev/status/184512398471923011?s=20&t=abc"
        val clean = TweetParser.extractCleanUrl(raw)
        assertEquals("https://x.com/AndroidDev/status/184512398471923011", clean)
    }

    @Test
    fun isTweetUrl_validatesSupportedDomains() {
        assertTrue(TweetParser.isTweetUrl("https://x.com/elonmusk/status/1234567890"))
        assertTrue(TweetParser.isTweetUrl("https://mobile.twitter.com/user_1/status/987654321"))
        assertFalse(TweetParser.isTweetUrl("https://example.com/post/123"))
    }

    @Test
    fun parseSharedText_extractsHandleIdAndDisplayName() {
        val parsed = TweetParser.parseSharedText("https://x.com/AndroidDev/status/184512398471923011")
        assertEquals("@AndroidDev", parsed.authorHandle)
        assertEquals("Android Developers", parsed.authorName)
        assertEquals("184512398471923011", parsed.tweetId)
        assertFalse(parsed.isContentFetched)
    }

    @Test
    fun mediaSerialization_roundTripsCleanly() {
        val items = listOf(
            TweetMediaItem(type = "photo", url = "https://pbs.twimg.com/media/abc.jpg"),
            TweetMediaItem(type = "video", url = "https://video.twimg.com/v.mp4", durationSeconds = 42.0)
        )
        val json = TweetParser.serializeMediaItems(items)
        val restored = TweetParser.deserializeMediaItems(json)
        assertEquals(2, restored.size)
        assertEquals("video", restored[1].type)
        assertNull(TweetParser.serializeMediaItems(emptyList()))
    }
}
