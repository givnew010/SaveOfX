package com.example

import com.example.util.TweetFetcher
import com.example.util.TweetMediaItem
import com.example.util.TweetParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTweetUrlDetection() {
        val rawUrl = "https://x.com/AndroidDev/status/184512398471923011?s=20&a=pFkK6UMLVXjdEBnIdOc"
        assertTrue(TweetParser.isTweetUrl(rawUrl))

        val twitterUrl = "https://twitter.com/elonmusk/status/100000000"
        assertTrue(TweetParser.isTweetUrl(twitterUrl))
    }

    @Test
    fun testCleanUrlExtraction() {
        val textWithUrl = "Check this out! https://x.com/OpenAI/status/987654321?t=abc&s=21 Amazing model"
        val cleanUrl = TweetParser.extractCleanUrl(textWithUrl)
        assertEquals("https://x.com/OpenAI/status/987654321", cleanUrl)
    }

    @Test
    fun testQueryParamsDoNotLeakIntoContent() {
        val rawShared = "https://x.com/akshay_pachaar/status/184512398471923011?s=20&a=pFkK6UMLVXjdEBnIdOc"
        val parsed = TweetParser.parseSharedText(rawShared)

        assertEquals("https://x.com/akshay_pachaar/status/184512398471923011", parsed.tweetUrl)
        assertEquals("@akshay_pachaar", parsed.authorHandle)
        assertEquals("https://x.com/akshay_pachaar/status/184512398471923011", parsed.content)
        assertFalse(parsed.content.contains("s=20"))
        assertFalse(parsed.isContentFetched)
    }

    @Test
    fun testSharedTextWithSnippetParsing() {
        val rawShared = "Awesome Jetpack Compose guide! https://x.com/AndroidDev/status/184512398471923011?s=20"
        val parsed = TweetParser.parseSharedText(rawShared)

        assertEquals("https://x.com/AndroidDev/status/184512398471923011", parsed.tweetUrl)
        assertEquals("184512398471923011", parsed.tweetId)
        assertEquals("@AndroidDev", parsed.authorHandle)
        assertEquals("Android Developers", parsed.authorName)
        assertTrue(parsed.content.contains("Awesome Jetpack Compose guide!"))
        assertFalse(parsed.content.contains("?s=20"))
    }

    @Test
    fun testFxTwitterJsonParsingWithVideoAndPhotos() {
        val sampleJson = """
            {
              "code": 200,
              "tweet": {
                "id": "184512398471923011",
                "text": "Full tweet text with media!",
                "created_timestamp": 1730000000,
                "likes": 512,
                "retweets": 88,
                "author": {
                  "name": "Akshay Pachaar",
                  "screen_name": "akshay_pachaar",
                  "avatar_url": "https://pbs.twimg.com/profile_images/123/avatar.jpg"
                },
                "media": {
                  "all": [
                    {
                      "type": "video",
                      "url": "https://video.twimg.com/ext_tw_video/123/pu/vid/720x1280/sample.mp4",
                      "thumbnail_url": "https://pbs.twimg.com/ext_tw_video_thumb/123/img/thumb.jpg",
                      "duration": 35.5
                    },
                    {
                      "type": "photo",
                      "url": "https://pbs.twimg.com/media/sample_photo.jpg"
                    }
                  ]
                },
                "quote": {
                  "text": "Quoted post text",
                  "author": {
                    "name": "Quote Author",
                    "screen_name": "quote_dev"
                  }
                }
              }
            }
        """.trimIndent()

        val parsed = TweetFetcher.parseFxTwitterJson(
            sampleJson,
            "https://x.com/akshay_pachaar/status/184512398471923011"
        )

        assertNotNull(parsed)
        assertTrue(parsed!!.isContentFetched)
        assertEquals("Full tweet text with media!", parsed.content)
        assertEquals("Akshay Pachaar", parsed.authorName)
        assertEquals("@akshay_pachaar", parsed.authorHandle)
        assertEquals("https://pbs.twimg.com/profile_images/123/avatar.jpg", parsed.authorAvatarUrl)
        assertEquals(2, parsed.mediaItems.size)
        assertEquals("video", parsed.mediaItems[0].type)
        assertEquals("photo", parsed.mediaItems[1].type)
        assertEquals("Quoted post text", parsed.quoteText)
        assertEquals(512, parsed.likesCount)
    }

    @Test
    fun testMediaSerializationRoundTrip() {
        val items = listOf(
            TweetMediaItem(type = "photo", url = "https://example.com/1.jpg"),
            TweetMediaItem(
                type = "video",
                url = "https://example.com/v.mp4",
                thumbnailUrl = "https://example.com/t.jpg",
                durationSeconds = 12.0
            )
        )
        val json = TweetParser.serializeMediaItems(items)
        val restored = TweetParser.deserializeMediaItems(json)
        assertEquals(2, restored.size)
        assertEquals("video", restored[1].type)
        assertEquals("https://example.com/t.jpg", restored[1].thumbnailUrl)
    }
}
