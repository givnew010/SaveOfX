package com.example

import com.example.util.AccountParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountParserTest {
    @Test
    fun parseAccountInput_handlesAtHandleAndBareUsername() {
        val fromAt = AccountParser.parseAccountInput("@AndroidDev")
        assertNotNull(fromAt)
        assertEquals("AndroidDev", fromAt!!.username)
        assertEquals("@AndroidDev", fromAt.handle)
        assertEquals("https://x.com/AndroidDev", fromAt.profileUrl)
        assertEquals("Android Developers", fromAt.displayName)

        val bare = AccountParser.parseAccountInput("karpathy")
        assertNotNull(bare)
        assertEquals("@karpathy", bare!!.handle)
        assertEquals("https://x.com/karpathy", bare.profileUrl)
        assertEquals("Karpathy", bare.displayName)
    }

    @Test
    fun parseAccountInput_handlesXAndTwitterUrlsAndRejectsReservedRoutes() {
        val fromUrl = AccountParser.parseAccountInput("https://x.com/OpenAI?s=20")
        assertNotNull(fromUrl)
        assertEquals("https://x.com/OpenAI", fromUrl!!.profileUrl)
        assertEquals("OpenAI", fromUrl.displayName)

        assertNull(AccountParser.parseAccountInput("https://x.com/home"))
        assertNull(AccountParser.parseAccountInput("@explore"))
        assertNull(AccountParser.parseAccountInput(""))
    }

    @Test
    fun isProfileUrl_distinguishesProfilesFromTweetStatusUrls() {
        assertTrue(AccountParser.isProfileUrl("https://x.com/naval"))
        assertTrue(AccountParser.isProfileUrl("https://twitter.com/OpenAI"))
        assertFalse(AccountParser.isProfileUrl("https://x.com/naval/status/1002103360646823936"))
        assertFalse(AccountParser.isProfileUrl("https://x.com/explore"))
    }
}
