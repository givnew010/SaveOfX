package com.example

import com.example.util.PersonWorkCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PersonWorkCodecTest {
    @Test
    fun normalizeUrl_addsHttpsWhenMissingScheme() {
        assertEquals("https://imdb.com/name/nm00001", PersonWorkCodec.normalizeUrl("imdb.com/name/nm00001"))
        assertEquals("content://media/external/images/1", PersonWorkCodec.normalizeUrl("content://media/external/images/1"))
        assertEquals("", PersonWorkCodec.normalizeUrl("   "))
    }

    @Test
    fun detectWorkTypeFromUrl_identifiesPhotosAndVideos() {
        assertEquals("photo", PersonWorkCodec.detectWorkTypeFromUrl("https://cdn.site/actor.jpg"))
        assertEquals("video", PersonWorkCodec.detectWorkTypeFromUrl("https://www.youtube.com/watch?v=abc123"))
        assertEquals("video", PersonWorkCodec.detectWorkTypeFromUrl("https://cdn.site/trailer.mp4"))
        assertEquals("link", PersonWorkCodec.detectWorkTypeFromUrl("https://en.wikipedia.org/wiki/Actor"))
    }

    @Test
    fun serializeAndParseWorks_roundTripsCorrectly() {
        val work = PersonWorkCodec.createWorkItem(
            type = "video",
            title = "مقابلة خاصة",
            url = "youtu.be/xyz999",
            note = "لقاء نادر",
            now = 1700000000000L
        )
        assertNotNull(work)
        val json = PersonWorkCodec.serializeWorks(listOf(work!!))
        val parsed = PersonWorkCodec.parseWorks(json)

        assertEquals(1, parsed.size)
        assertEquals("video", parsed[0].type)
        assertEquals("https://youtu.be/xyz999", parsed[0].url)
        assertEquals("مقابلة خاصة", parsed[0].title)
        assertEquals("لقاء نادر", parsed[0].note)

        val summary = PersonWorkCodec.summarizeWorks(parsed)
        assertEquals(1, summary.total)
        assertEquals(1, summary.videos)
        assertEquals(0, summary.links)
    }

    @Test
    fun createWorkItem_returnsNullWhenUrlBlank() {
        assertNull(PersonWorkCodec.createWorkItem("link", "Title", "   "))
    }
}
